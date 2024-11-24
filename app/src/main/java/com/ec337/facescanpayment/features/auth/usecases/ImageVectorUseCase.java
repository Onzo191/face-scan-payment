package com.ec337.facescanpayment.features.auth.usecases;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.ec337.facescanpayment.core.embeddings.FaceNet;
import com.ec337.facescanpayment.core.errors.Failure;
import com.ec337.facescanpayment.core.face_detection.MediapipeFaceDetector;
import com.ec337.facescanpayment.core.utils.Either;
import com.ec337.facescanpayment.features.auth.data.model.FaceModel;
import com.ec337.facescanpayment.features.auth.data.repository.FaceRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageVectorUseCase {
    private final MediapipeFaceDetector faceDetector;
    private final FaceNet faceNet;
    private final FaceRepository faceRepository;

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
        faceRepository.registerFace(faceModel);
    }
}
