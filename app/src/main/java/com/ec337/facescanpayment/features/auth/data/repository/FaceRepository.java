package com.ec337.facescanpayment.features.auth.data.repository;

import android.content.Context;
import android.util.Log;

import com.ec337.facescanpayment.features.auth.data.model.FaceModel;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

public class FaceRepository {
    private final String TAG = this.getClass().getSimpleName();

    private final FirebaseFirestore firestore;
    private final Context context;


    public FaceRepository(Context context) {
        this.firestore = FirebaseFirestore.getInstance();;
        this.context = context;
    }

    public void registerFace(FaceModel faceModel) {
        DocumentReference faceRef = firestore.collection("faces").document(faceModel.getUserId());

        faceRef.set(faceModel, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Face registered successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error registering face: ", e));
    }
}
