package com.ec337.facescanpayment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ec337.facescanpayment.features.auth.presentation.FaceDetectionPage;
import com.ec337.facescanpayment.features.auth.presentation.FaceRegisterPage;
import com.ec337.facescanpayment.features.auth.presentation.LoginPage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth m_auth;
    private TextView txt_welcome, txt_email;
    private Button btn_logout, btnRegisterFace, btnVerifyFace;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app__main_page);

        m_auth = FirebaseAuth.getInstance();

        txt_welcome = findViewById(R.id.txt_welcome);
        txt_email = findViewById(R.id.txt_email);
        btn_logout = findViewById(R.id.btn_logout);

        FirebaseUser current_user = m_auth.getCurrentUser();
        if (current_user != null) {
            txt_email.setText(current_user.getEmail());
        } else {
            startActivity(new Intent(MainActivity.this, LoginPage.class));
            finish();
        }

        btnRegisterFace = findViewById(R.id.btnRegisterFace);
        btnVerifyFace = findViewById(R.id.btnVerifyFace);

        btnRegisterFace.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FaceRegisterPage.class);
            startActivity(intent);
        });

        btnVerifyFace.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FaceDetectionPage.class);
            startActivity(intent);
        });

        btn_logout.setOnClickListener(v -> {
            m_auth.signOut();
            startActivity(new Intent(MainActivity.this, LoginPage.class));
            finish();
        });
    }
}