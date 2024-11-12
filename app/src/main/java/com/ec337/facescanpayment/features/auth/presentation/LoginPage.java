package com.ec337.facescanpayment.features.auth.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import com.ec337.facescanpayment.MainActivity;
import com.ec337.facescanpayment.R;
import com.ec337.facescanpayment.core.credential.AuthenticationManager;
import com.ec337.facescanpayment.core.utils.BiometricHelper;
import com.ec337.facescanpayment.core.utils.UIManager;
import com.google.firebase.auth.FirebaseUser;

public class LoginPage extends AppCompatActivity {

    private AuthenticationManager authManager;
    private BiometricHelper biometricHelper;
    private UIManager uiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.auth__login_page);

        authManager = new AuthenticationManager(this);
        uiManager = new UIManager(findViewById(R.id.loadingIndicator));

        biometricHelper = new BiometricHelper(this, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                biometricLogin();
            }

            @Override
            public void onAuthenticationFailed() {
                Toast.makeText(LoginPage.this, "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_fingerprint_login).setOnClickListener(v -> biometricHelper.authenticate());

        Button loginButton = findViewById(R.id.btn_login);
        loginButton.setOnClickListener(v -> handleLogin());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = authManager.getCurrentUser();
        if (currentUser != null) {
            navigateToMainActivity();
        }

        if (authManager.retrieveCredentials() != null) {
            findViewById(R.id.btn_fingerprint_login).setVisibility(View.VISIBLE);
        }
        uiManager.hideLoading();
    }

    private void handleLogin() {
        String email = ((EditText) findViewById(R.id.et_email)).getText().toString();
        String password = ((EditText) findViewById(R.id.et_password)).getText().toString();

        if (!email.isEmpty() && !password.isEmpty()) {
            uiManager.showLoading();
            authManager.signInWithEmailPassword(email, password, new AuthenticationManager.OnAuthCompleteListener() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    Toast.makeText(LoginPage.this, "Login succeeded!", Toast.LENGTH_SHORT).show();
                    navigateToMainActivity();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(LoginPage.this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    uiManager.hideLoading();
                }
            });
        } else {
            Toast.makeText(LoginPage.this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
        }
    }

    private void biometricLogin() {
        String[] credentials = authManager.retrieveCredentials();
        if (credentials != null) {
            authManager.signInWithEmailPassword(credentials[0], credentials[1], new AuthenticationManager.OnAuthCompleteListener() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    Toast.makeText(LoginPage.this, "Biometric login succeeded!", Toast.LENGTH_SHORT).show();
                    navigateToMainActivity();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(LoginPage.this, "Biometric login failed", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No saved credentials found", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToMainActivity() {
        startActivity(new Intent(LoginPage.this, MainActivity.class));
        finish();
    }
}
