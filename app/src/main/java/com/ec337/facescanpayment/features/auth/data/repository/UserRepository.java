package com.ec337.facescanpayment.features.auth.data.repository;

import android.app.Application;
import android.widget.Toast;

import com.ec337.facescanpayment.features.auth.data.entity.UserEntity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserRepository {
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final Application application;

    public UserRepository(Application application) {
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
        this.application = application;
    }

    public void registerUser(String email, String password, UserEntity userEntity, OnCompleteListener<Void> onCompleteListener) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = auth.getCurrentUser();
                if (firebaseUser != null) {
                    firestore.collection("users")
                            .document(firebaseUser.getUid())
                            .set(userEntity)
                            .addOnCompleteListener(onCompleteListener);
                }
            } else {
                Toast.makeText(application, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
