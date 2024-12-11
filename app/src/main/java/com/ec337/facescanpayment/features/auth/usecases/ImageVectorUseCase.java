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
import com.ec337.facescanpayment.core.utils.JwtToken;
import com.ec337.facescanpayment.features.auth.data.model.FaceModel;
import com.ec337.facescanpayment.features.auth.data.repository.AuthRepository;
import com.ec337.facescanpayment.features.auth.data.repository.FaceRepository;
import com.ec337.facescanpayment.features.auth.data.repository.api.AuthApiClient;
import com.ec337.facescanpayment.features.auth.data.repository.api.types.RegisterFaceResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

                if(isValidFaceOrientation(croppedFace)) {
                    float[] embedding = faceNet.getFaceEmbedding(croppedFace);
                    List<Float> converted = FaceModel.convertToList(embedding);

                    String key = "image" + (validImageCount + 1);
                    validEmbeddings.put(key, converted);
                    validImageCount++;
                }
            }

            if (validImageCount >= MAX_VALID_IMAGES) {
                break;
            }
        }

        if (!validEmbeddings.isEmpty()) {
            FaceModel faceModel = new FaceModel(userId, userName, userEmail, validEmbeddings);
            faceModel.logFaceEmbeddings();
            authRepository.registerFace(ctx, faceModel);
        } else {
            // Thông báo không có hình ảnh hợp lệ
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

    private boolean isValidFaceOrientation(Bitmap faceBitmap) {
        try {
            OrientationDetect.OrientationResult orientation =
                    orientationDetect.detectOrientation(faceBitmap);

            float confidence = orientationDetect.getOrientationConfidence(orientation);

            // Validate face orientation with confidence check
            boolean isValid = orientation.isFrontFacing() ||
                    (orientation.isLeftTurned() ||
                            orientation.isRightTurned() ||
                            orientation.isUpTurned() ||
                            orientation.isDownTurned()) &&
                            confidence >= ORIENTATION_CONFIDENCE_THRESHOLD;

            if (!isValid) {
                Log.w(TAG, "Invalid face orientation. Confidence: " + confidence);
            }

            return isValid;
        } catch (Exception e) {
            Log.e(TAG, "Error checking face orientation", e);
            return false;
        }
    }
}
