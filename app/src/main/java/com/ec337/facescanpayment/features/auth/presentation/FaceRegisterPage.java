package com.ec337.facescanpayment.features.auth.presentation;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
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
import com.ec337.facescanpayment.core.embeddings.FaceNet;
import com.ec337.facescanpayment.core.errors.Failure;
import com.ec337.facescanpayment.core.face_detection.MediapipeFaceDetector;
import com.ec337.facescanpayment.core.utils.Either;
import com.ec337.facescanpayment.features.auth.data.model.FaceModel;
import com.ec337.facescanpayment.features.auth.data.repository.FaceRepository;
import com.ec337.facescanpayment.features.auth.presentation.adapter.ImageAdapter;

import java.util.ArrayList;
import java.util.List;


public class FaceRegisterPage extends AppCompatActivity {
    private static final String TAG = FaceRegisterPage.class.getSimpleName();

    private List<Uri> selectedImages = new ArrayList<>();
    private ImageAdapter imageAdapter;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMediaLauncher;
    FaceRepository faceRepository;

    private MediapipeFaceDetector mediapipeFaceDetector;
    private FaceNet faceNet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        faceRepository = new FaceRepository(getApplication());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth__face_register_page);

        mediapipeFaceDetector = new MediapipeFaceDetector(this);
        faceNet = new FaceNet(this, true, true);

        RecyclerView recyclerView = findViewById(R.id.rv_images);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        imageAdapter = new ImageAdapter(selectedImages);
        recyclerView.setAdapter(imageAdapter);

        pickMultipleMediaLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.PickMultipleVisualMedia(),
                        this::handleSelectedMedia
                );
        initViews();
    }

    private void initViews() {
        EditText etPersonName = findViewById(R.id.et_person_name);
        Button btnChoosePhoto = findViewById(R.id.btn_choose_photos);
        Button btnAddToDatabase = findViewById(R.id.btn_add_to_database);

        btnChoosePhoto.setOnClickListener(v -> pickMultipleMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        btnAddToDatabase.setOnClickListener(v -> {
            String personName = etPersonName.getText().toString();

            if (!personName.isEmpty() && !selectedImages.isEmpty()) {
                for (Uri selectedImage : selectedImages) {
                    addImage("user_" + personName, personName, personName + "@example.com", selectedImage);
                }
                Toast.makeText(this, "Added to database", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please fill name and select images", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addImage(String userId, String userName, String userEmail, Uri imageUri) {
        Either<Failure, Bitmap> faceDetectionResult = mediapipeFaceDetector.getCroppedFace(imageUri);
        if (faceDetectionResult.isRight()) {
            float[] embedding = faceNet.getFaceEmbedding(faceDetectionResult.getRight());
            List<Float> converted = FaceModel.convertToList(embedding);
            FaceModel record = new FaceModel(userId, userName, userEmail, converted);
            record.logFaceEmbedding();
        faceRepository.registerFace(record);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void handleSelectedMedia(List<Uri> uris) {
        if (!uris.isEmpty()) {
            Log.d(TAG, "Number of items selected: " + uris.size());
            selectedImages.clear();
            selectedImages.addAll(uris);
            imageAdapter.notifyDataSetChanged();
        } else {
            Log.d(TAG, "No media selected");
        }
    }
}