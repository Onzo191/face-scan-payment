package com.ec337.facescanpayment.features.auth.data.model;

import android.util.Log;

import com.ec337.facescanpayment.features.auth.data.entity.FaceEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FaceModel extends FaceEntity {
    private final String TAG = this.getClass().getSimpleName();

    public FaceModel(String userId, String userName, String userEmail, Map<String, List<Float>> faceEmbeddings) {
        super(userId, userName, userEmail, faceEmbeddings);
    }

    public static List<Float> convertToList(float[] array) {
        List<Float> list = new ArrayList<>();
        for (float value : array) {
            list.add(value);
        }
        return list;
    }

    // log embeddings
    public void logFaceEmbeddings() {
        if (getFaceEmbeddings() != null) {
            StringBuilder embeddingString = new StringBuilder();
            for (Map.Entry<String, List<Float>> entry : getFaceEmbeddings().entrySet()) {
                String key = entry.getKey();
                List<Float> embedding = entry.getValue();

                embeddingString.append(key).append(": ");
                for (float value : embedding) {
                    embeddingString.append(value).append(", ");
                }
                embeddingString.append("\n");
            }

            if (embeddingString.length() > 0) {
                embeddingString.setLength(embeddingString.length() - 2);
            }

            Log.d(TAG, "Face Embeddings: " + embeddingString.toString());
        } else {
            Log.d(TAG, "Face Embeddings are null.");
        }
    }
}
