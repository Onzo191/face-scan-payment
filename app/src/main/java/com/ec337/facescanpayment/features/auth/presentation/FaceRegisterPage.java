package com.ec337.facescanpayment.features.auth.presentation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ec337.facescanpayment.R;
import com.ec337.facescanpayment.core.notify.NotifyPage;
import com.ec337.facescanpayment.core.utils.JwtToken;
import com.ec337.facescanpayment.features.auth.data.repository.FaceRepository;
import com.ec337.facescanpayment.features.auth.presentation.adapters.ImageAdapter;
import com.ec337.facescanpayment.features.auth.usecases.ImageVectorUseCase;
import com.ec337.facescanpayment.features.cart.presentation.CheckoutPage;

import java.util.ArrayList;
import java.util.List;


public class FaceRegisterPage extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    private final List<Uri> selectedImages = new ArrayList<>();
    private ImageAdapter imageAdapter;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMediaLauncher;
    FaceRepository faceRepository;

    private EditText etPersonName;
    private Button btnChoosePhoto, btnAddToDatabase;

    private ImageVectorUseCase imageVectorUseCase;

    private JwtToken jwtToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        faceRepository = new FaceRepository(getApplication());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth__face_register_page);

        imageVectorUseCase = new ImageVectorUseCase(this, new FaceRepository(this));

        initViews();
        initRecyclerView();
        initPickMediaLauncher();
    }

    private void initViews() {
        etPersonName = findViewById(R.id.et_person_name);
        btnChoosePhoto = findViewById(R.id.btn_choose_photos);
        btnAddToDatabase = findViewById(R.id.btn_add_to_database);

        btnChoosePhoto.setOnClickListener(v -> pickMultipleMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        btnAddToDatabase.setOnClickListener(v -> handleAddToDatabase());
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.rv_images);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        imageAdapter = new ImageAdapter(selectedImages);
        recyclerView.setAdapter(imageAdapter);
    }

    private void initPickMediaLauncher() {
        pickMultipleMediaLauncher = registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(),
                this::handleSelectedMedia
        );
    }

    private void handleAddToDatabase() {
        String userName = etPersonName.getText().toString().trim();

        if (userName.isEmpty() || selectedImages.isEmpty()) {
            Toast.makeText(this, "Please fill name and select images", Toast.LENGTH_SHORT).show();
            return;
        }

        jwtToken = new JwtToken(this);
        String userId = jwtToken.getUserId();
        String userEmail = jwtToken.getUserEmail();
        imageVectorUseCase.processAndAddImage(this ,userId, userName, userEmail, selectedImages);

        Toast.makeText(this, "Face register successful!", Toast.LENGTH_SHORT).show();
        Intent notifyIntent = new Intent(this, NotifyPage.class);
        notifyIntent.putExtra("text", "Đăng ký thành công");
        notifyIntent.putExtra("imageSrc", R.drawable.logo_face_register_completed);
        startActivity(notifyIntent);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void handleSelectedMedia(List<Uri> uris) {
        if (uris.isEmpty()) {
            Log.d(TAG, "No media selected");
            return;
        }

        Log.d(TAG, "Number of items selected: " + uris.size());
        selectedImages.clear();
        selectedImages.addAll(uris);
        imageAdapter.notifyDataSetChanged();
    }
}