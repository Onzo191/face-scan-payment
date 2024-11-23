package com.ec337.facescanpayment.features.auth.usecase;

import android.graphics.Bitmap;
import android.net.Uri;

import com.ec337.facescanpayment.core.embeddings.FaceNet;
import com.ec337.facescanpayment.core.errors.Failure;
import com.ec337.facescanpayment.core.face_detection.MediapipeFaceDetector;
import com.ec337.facescanpayment.core.utils.Either;
import com.ec337.facescanpayment.features.auth.data.entity.FaceImageRecord;
import com.ec337.facescanpayment.features.auth.data.model.ImagesVectorDB;
import com.ec337.facescanpayment.features.auth.data.model.PersonDB;

public class ImageVectorUseCase {
    private final MediapipeFaceDetector mediapipeFaceDetector;
    private final FaceNet faceNet;
    private final ImagesVectorDB imagesVectorDB;
    private final PersonDB personDB;

    public ImageVectorUseCase(
            MediapipeFaceDetector mediapipeFaceDetector,
            FaceNet faceNet,
            ImagesVectorDB imagesVectorDB,
            PersonDB personDB
    ) {
        this.mediapipeFaceDetector = mediapipeFaceDetector;
        this.faceNet = faceNet;
        this.imagesVectorDB = imagesVectorDB;
        this.personDB = personDB;
    }

    // Add the person's image to the Firestore database
    public void addImage(long personID, String personName, Uri imageUri) {
        Either<Failure, Bitmap> faceDetectionResult = mediapipeFaceDetector.getCroppedFace(imageUri);
        if (faceDetectionResult.isRight()) {
            float[] embedding = faceNet.getFaceEmbedding(faceDetectionResult.getRight());
            FaceImageRecord record = new FaceImageRecord(System.currentTimeMillis(), personID, personName, embedding);
            imagesVectorDB.addFaceImageRecord(record);
        }
    }

    // Query Firestore for the nearest person
    public void getNearestPersonName(Bitmap frameBitmap) {
    }
}
