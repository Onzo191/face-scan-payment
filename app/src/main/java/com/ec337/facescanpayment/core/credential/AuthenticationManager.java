package com.ec337.facescanpayment.core.credential;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthenticationManager {

    private final FirebaseAuth mAuth;
    private final SharedPreferences sharedPreferences;

    public AuthenticationManager(Context context) {
        mAuth = FirebaseAuth.getInstance();
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    "credentials",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to create encrypted shared preferences", e);
        }
    }

    public void signInWithEmailPassword(String email, String password, OnAuthCompleteListener listener) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        storeCredentials(email, password);
                        listener.onSuccess(mAuth.getCurrentUser());
                    } else {
                        listener.onFailure(task.getException());
                    }
                });
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void signOut() {
        mAuth.signOut();
    }

    public void storeCredentials(String email, String password) {
        sharedPreferences.edit()
                .putString("email", email)
                .putString("password", password)
                .apply();
    }

    public String[] retrieveCredentials() {
        String email = sharedPreferences.getString("email", null);
        String password = sharedPreferences.getString("password", null);
        return (email != null && password != null) ? new String[]{email, password} : null;
    }

    public interface OnAuthCompleteListener {
        void onSuccess(FirebaseUser user);
        void onFailure(Exception e);
    }
}
