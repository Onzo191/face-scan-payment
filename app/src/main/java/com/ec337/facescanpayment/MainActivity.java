package com.ec337.facescanpayment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ec337.facescanpayment.core.utils.JwtToken;
import com.ec337.facescanpayment.core.utils.NavigationUtils;
import com.ec337.facescanpayment.features.auth.data.repository.AuthRepository;
import com.ec337.facescanpayment.features.auth.presentation.FaceDetectionPage;
import com.ec337.facescanpayment.features.auth.presentation.FaceRegisterPage;
import com.ec337.facescanpayment.features.auth.presentation.LoginPage;
import com.ec337.facescanpayment.features.store.presentation.StorePage;

public class MainActivity extends AppCompatActivity {

    // jwtToken
    private JwtToken jwtToken;

    // UI Components
    private TextView txtWelcome, txtEmail;
    private Button btnLogout;
    private LinearLayout btnShop, btnRegisterFace, btnVerifyFace;

    // Repository
    private final AuthRepository authRepository = new AuthRepository();

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app__main_page);

        // Initialize JwtToken
        jwtToken = new JwtToken(this);

        // Initialize UI components
        initUI();

        // Handle user authentication
        handleAuthentication();

        // Set click listeners
        setClickListeners();
    }

    private void initUI() {
        txtWelcome = findViewById(R.id.txtWelcome);
        txtEmail = findViewById(R.id.txtEmail);
        btnLogout = findViewById(R.id.btnLogout);
        btnShop = findViewById(R.id.btnShop);
        btnRegisterFace = findViewById(R.id.btnRegisterFace);
        btnVerifyFace = findViewById(R.id.btnVerifyFace);
        btnVerifyFace.setVisibility(View.GONE);
    }

    private void handleAuthentication() {
        String token = jwtToken.getToken();
        String userId = jwtToken.getUserId();

        if (token != null && userId != null){
            // Set email from JWT token
            authRepository.getCurrentUser(this, userId);
            txtEmail.setText(jwtToken.getUserEmail());
        } else {
            // Redirect to login if no token
            NavigationUtils.navigateTo(this, LoginPage.class, true);
        }
    }

    private void setClickListeners() {
        btnShop.setOnClickListener(v -> NavigationUtils.navigateTo(this, StorePage.class, false));
        btnRegisterFace.setOnClickListener(v -> NavigationUtils.navigateTo(this, FaceRegisterPage.class, false));
        btnVerifyFace.setOnClickListener(v -> NavigationUtils.navigateTo(this, FaceDetectionPage.class, false));
        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        jwtToken.clearToken();
        jwtToken.deleteUser();
        NavigationUtils.navigateTo(this, LoginPage.class, true);
    }
}
