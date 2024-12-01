package com.ec337.facescanpayment.features.auth.presentation;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ec337.facescanpayment.MainActivity;
import com.ec337.facescanpayment.R;
import com.ec337.facescanpayment.core.utils.NavigationUtils;
import com.ec337.facescanpayment.features.auth.data.entity.UserEntity;
import com.ec337.facescanpayment.features.auth.data.repository.AuthRepository;
import com.ec337.facescanpayment.features.auth.data.repository.UserRepository;

public class RegisterPage extends AppCompatActivity {
    private EditText etFirstName, etLastName, etEmail, etPhone, etPassword, etConfirmPassword;
    private RadioGroup rgGender;
    private UserRepository userRepository;
    private AuthRepository authRepository = new AuthRepository();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.auth__register_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userRepository = new UserRepository(getApplication());

        etFirstName = findViewById(R.id.et_name);
        etLastName = findViewById(R.id.et_last_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_verified_password);
        rgGender = findViewById(R.id.rg_gender);

        Button btnRegister = findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(v -> handleRegister());

        TextView toRegister = findViewById(R.id.btn_to_login);
        toRegister.setOnClickListener(v -> NavigationUtils.navigateTo(RegisterPage.this, LoginPage.class));
    }

    private void handleRegister() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String gender = getSelectedGender();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (validateInput(firstName, lastName, email, phone, password, confirmPassword, gender)) {
            UserEntity userEntity = new UserEntity(firstName, lastName, gender, email, phone);
            authRepository.register(this ,firstName, lastName, email, phone, gender, password);
        }
    }

    private boolean validateInput(String firstName, String lastName, String email, String phone, String password, String confirmPassword, String gender) {
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || gender == null) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private String getSelectedGender() {
        int selectedId = rgGender.getCheckedRadioButtonId();
        if (selectedId == R.id.rb_male) return "Male";
        if (selectedId == R.id.rb_female) return "Female";
        if (selectedId == R.id.rb_other) return "Other";
        return null;
    }
}