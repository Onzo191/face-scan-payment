package com.ec337.facescanpayment.features.auth.usecases;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.ec337.facescanpayment.core.embeddings.FaceNet;
import com.ec337.facescanpayment.core.errors.Failure;
import com.ec337.facescanpayment.core.face_detection.MediapipeFaceDetector;
import com.ec337.facescanpayment.core.face_detection.OrientationDetect;
import com.ec337.facescanpayment.core.utils.Either;
import com.ec337.facescanpayment.features.auth.data.model.FaceModel;
import com.ec337.facescanpayment.features.auth.data.repository.AuthRepository;
import com.ec337.facescanpayment.features.auth.data.repository.FaceRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ImageVectorUseCase {
    private static final String TAG = "ImageVectorUseCase";
    private static final int MAX_VALID_IMAGES = 5;
    private static final float ORIENTATION_CONFIDENCE_THRESHOLD = 0.6f;
    private final MediapipeFaceDetector faceDetector;
    private final FaceNet faceNet;
    private final FaceRepository faceRepository;
    private AuthRepository authRepository = new AuthRepository();
    private OrientationDetect orientationDetect;
    public ImageVectorUseCase(
            Context context,
            FaceRepository faceRepository
    ) {
        this.faceDetector = new MediapipeFaceDetector(context);
        this.faceNet = new FaceNet(context, true, true);
        this.faceRepository = faceRepository;
        this.orientationDetect = new OrientationDetect(context);
    }

    // Add the user's embedding to the firestore database
    public void processAndAddImage(Context ctx, String userId, String userName, String userEmail, List<Uri> imageUriList) {
        Map<String, List<Float>> validEmbeddings  = new HashMap<>();
        int validImageCount = 0;

        for (Uri imageUri : imageUriList) {
            Either<Failure, Bitmap> faceDetectionResult = faceDetector.getCroppedFace(imageUri);

            if (faceDetectionResult.isRight()) {
                Bitmap croppedFace = faceDetectionResult.getRight();

                float[] embedding = faceNet.getFaceEmbedding(croppedFace);
                List<Float> converted = FaceModel.convertToList(embedding);

                String key = "image" + (validImageCount + 1);
                validEmbeddings.put(key, converted);
                validImageCount++;
            }

            if (validImageCount >= MAX_VALID_IMAGES) {
                break;
            }
        }

        if (!validEmbeddings.isEmpty()) {
            FaceModel faceModel = new FaceModel(userId, userName, userEmail, validEmbeddings);
            faceModel.logFaceEmbeddings();
            authRepository.registerFace(ctx, faceModel);
            authRepository.getCurrentUser(ctx,userId);
        } else {

            Toast.makeText(ctx, "Không tìm thấy hình ảnh khuôn mặt phù hợp", Toast.LENGTH_SHORT).show();
        }
    }

    public float[] processImage(Bitmap bitmap) {
        Either<Failure, Bitmap> faceDetectionResult = faceDetector.getCroppedFaceFromBitmap(bitmap);
        if (faceDetectionResult.isRight()) {
            Bitmap croppedFace = faceDetectionResult.getRight();
            if (croppedFace != null) {
                return faceNet.getFaceEmbedding(croppedFace);
            }
        }
        return new float[0];
    }
}
