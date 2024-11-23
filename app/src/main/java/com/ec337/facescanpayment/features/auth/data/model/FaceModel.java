package com.ec337.facescanpayment.features.auth.data.model;

import android.util.Log;

import com.ec337.facescanpayment.features.auth.data.entity.FaceEntity;

import java.util.ArrayList;
import java.util.List;

public class FaceModel extends FaceEntity {
    private final String TAG = this.getClass().getSimpleName();

    public FaceModel(String userId, String userName, String userEmail, List<Float> faceEmbedding) {
        super(userId, userName, userEmail, faceEmbedding);
    }

    public static List<Float> convertToList(float[] array) {
        List<Float> list = new ArrayList<>();
        for (float value : array) {
            list.add(value);
        }
        return list;
    }

    public void logFaceEmbedding() {
        if (getFaceEmbedding() != null) {
            StringBuilder embeddingString = new StringBuilder();
            for (float value : getFaceEmbedding()) {
                embeddingString.append(value).append(", ");
            }

            if (embeddingString.length() > 0) {
                embeddingString.setLength(embeddingString.length() - 2);
            }

            Log.d(TAG, "Face Embedding: " + embeddingString.toString());
        } else {
            Log.d(TAG, "Face Embedding is null.");
        }
    }
}
