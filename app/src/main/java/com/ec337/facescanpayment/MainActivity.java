package com.ec337.facescanpayment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ec337.facescanpayment.features.auth.presentation.LoginPage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth m_auth;
    private TextView txt_welcome, txt_email;
    private Button btn_logout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.app__main_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        m_auth = FirebaseAuth.getInstance();

        // Initialize UI elements
        txt_welcome = findViewById(R.id.txt_welcome);
        txt_email = findViewById(R.id.txt_email);
        btn_logout = findViewById(R.id.btn_logout);

        // Get current user
        FirebaseUser current_user = m_auth.getCurrentUser();
        if (current_user != null) {
            // Display user's email on the screen
            txt_email.setText(current_user.getEmail());
        } else {
            // If user is not logged in, navigate to login screen
            startActivity(new Intent(MainActivity.this, LoginPage.class));
            finish();
        }

        // Set up the logout button to sign out the user
        btn_logout.setOnClickListener(v -> {
            m_auth.signOut();
            startActivity(new Intent(MainActivity.this, LoginPage.class));
            finish(); // Close the main activity after logout
        });
    }
}