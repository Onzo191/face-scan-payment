package com.ec337.facescanpayment.features.auth.presentation;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ec337.facescanpayment.R;
import com.ec337.facescanpayment.core.face_detection.OrientationDetect;
import com.ec337.facescanpayment.core.utils.JwtToken;
import com.ec337.facescanpayment.core.utils.UriHelper;
import com.ec337.facescanpayment.features.auth.data.repository.FaceRepository;
import com.ec337.facescanpayment.features.auth.presentation.adapters.ImageAdapter;
import com.ec337.facescanpayment.features.auth.usecases.ImageVectorUseCase;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class FaceRegisterPage extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    private final List<Bitmap> validBitmaps = new ArrayList<>(); // Lưu các ảnh hợp lệ
    private final List<Uri> selectedImages = new ArrayList<>();
    private ImageAdapter imageAdapter;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMediaLauncher;
    FaceRepository faceRepository;

    private EditText etPersonName;
    private Button btnChoosePhoto, btnAddToDatabase , btnStartCapture;

    private ImageVectorUseCase imageVectorUseCase;

    private JwtToken jwtToken;
    private ActivityResultLauncher<Void> takePictureLauncher;
    private OrientationDetect orientationDetect;
    private int currentOrientationIndex = 0;

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        faceRepository = new FaceRepository(getApplication());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth__face_register_page);

        orientationDetect = new OrientationDetect(this);
        imageVectorUseCase = new ImageVectorUseCase(this, new FaceRepository(this));

        initViews();
//        previewView = findViewById(R.id.previewView);
        cameraExecutor = Executors.newSingleThreadExecutor();
        startCamera();
    }

    private void initViews() {
        etPersonName = findViewById(R.id.et_person_name);
        btnChoosePhoto = findViewById(R.id.btn_choose_photos);
        btnAddToDatabase = findViewById(R.id.btn_add_to_database);

        btnStartCapture = findViewById(R.id.btn_start_capture);
        btnStartCapture.setOnClickListener(v -> checkCameraPermission());

        btnAddToDatabase.setOnClickListener(v -> handleAddToDatabase());
    }

    private void handleAddToDatabase() {
        String userName = etPersonName.getText().toString().trim();

        if (userName.isEmpty() || validBitmaps.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên và chụp ít nhất một ảnh khuôn mặt!", Toast.LENGTH_SHORT).show();
            return;
        }


        jwtToken = new JwtToken(this);
        String userId = jwtToken.getUserId();
        String userEmail = jwtToken.getUserEmail();

        List<Uri> imageUriList = UriHelper.saveBitmapListAsUris(this, validBitmaps);

        imageVectorUseCase.processAndAddImage(this, userId, userName, userEmail, imageUriList);

        Toast.makeText(this, "Thêm khuôn mặt vào cơ sở dữ liệu thành công!", Toast.LENGTH_SHORT).show();
        validBitmaps.clear();
    }

    private final List<OrientationDetect.TargetOrientation> requiredOrientations = List.of(
            OrientationDetect.TargetOrientation.FRONT,
            OrientationDetect.TargetOrientation.LEFT,
            OrientationDetect.TargetOrientation.RIGHT,
            OrientationDetect.TargetOrientation.UP,
            OrientationDetect.TargetOrientation.DOWN
    );

    private void initCameraLauncher() {
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(),
                bitmap -> { // This code is now redundant, remove it.
                });
    }

    private void startCaptureLoop() {
        if (validBitmaps.size() >= requiredOrientations.size()) {
            Toast.makeText(this, "Bạn đã đạt đủ số lượng ảnh!", Toast.LENGTH_SHORT).show();
            return;
        }

        OrientationDetect.TargetOrientation target = requiredOrientations.get(currentOrientationIndex);
        Toast.makeText(this, "Vui lòng nhìn theo hướng: " + target.name(), Toast.LENGTH_LONG).show();

        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(
                        new File(UriHelper.createImageDirectory(this), UriHelper.createImageFileName(currentOrientationIndex))
                ).build(),
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = outputFileResults.getSavedUri();

                        if (savedUri != null) {
                            Bitmap bitmap = UriHelper.getBitmapFromUri(FaceRegisterPage.this, savedUri);
                            if (bitmap != null && isValidFaceOrientation(bitmap, target)) {
                                validBitmaps.add(bitmap);
                                currentOrientationIndex++;
                                Toast.makeText(FaceRegisterPage.this, "Ảnh hợp lệ đã được thêm! (" + validBitmaps.size() + ")", Toast.LENGTH_SHORT).show();
                                startCaptureLoop(); // Call recursively for the next orientation
                            } else {
                                Toast.makeText(FaceRegisterPage.this, "Ảnh không hợp lệ cho hướng " + target.name() + ", vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Error capturing image: " + exception.getMessage(), exception);
                        Toast.makeText(FaceRegisterPage.this, "Lỗi chụp ảnh: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void checkCameraPermission() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCaptureLoop();
        } else {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 1001);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCaptureLoop();
        } else {
            Toast.makeText(this, "Quyền camera bị từ chối!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidFaceOrientation(Bitmap bitmap, OrientationDetect.TargetOrientation target) {
        try {
            OrientationDetect.OrientationResult orientation = orientationDetect.detectOrientation(bitmap);
            return orientationDetect.isTargetOrientation(orientation, target);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi kiểm tra hướng khuôn mặt", e);
            return false;
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());


                imageCapture = new ImageCapture.Builder().build();


                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }
}