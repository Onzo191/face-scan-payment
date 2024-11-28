package com.ec337.facescanpayment.features.auth.usecases;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.ec337.facescanpayment.core.embeddings.FaceNet;
import com.ec337.facescanpayment.core.errors.Failure;
import com.ec337.facescanpayment.core.face_detection.MediapipeFaceDetector;
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
    private final MediapipeFaceDetector faceDetector;
    private final FaceNet faceNet;
    private final FaceRepository faceRepository;
    private AuthRepository authRepository = new AuthRepository();
    public ImageVectorUseCase(
            Context context,
            FaceRepository faceRepository
    ) {
        this.faceDetector = new MediapipeFaceDetector(context);
        this.faceNet = new FaceNet(context, true, true);
        this.faceRepository = faceRepository;
    }

    // Add the user's embedding to the firestore database
    public void processAndAddImage(String userId, String userName, String userEmail, List<Uri> imageUriList) {
        Map<String, List<Float>> allEmbeddings = new HashMap<>();

        int index = 1;
        for (Uri imageUri : imageUriList) {
            Either<Failure, Bitmap> faceDetectionResult = faceDetector.getCroppedFace(imageUri);

            if (faceDetectionResult.isRight()) {
                float[] embedding = faceNet.getFaceEmbedding(faceDetectionResult.getRight());
                List<Float> converted = FaceModel.convertToList(embedding);

                String key = "image" + index++;
                allEmbeddings.put(key, converted);
            }
        }

        FaceModel faceModel = new FaceModel(userId, userName, userEmail, allEmbeddings);
        faceModel.logFaceEmbeddings();
//        faceRepository.registerFace(faceModel);
        authRepository.registerFace(faceModel);
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
