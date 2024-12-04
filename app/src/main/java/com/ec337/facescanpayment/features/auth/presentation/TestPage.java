package com.ec337.facescanpayment.features.auth.presentation;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ec337.facescanpayment.R;
import com.ec337.facescanpayment.core.errors.Failure;
import com.ec337.facescanpayment.core.face_detection.MediapipeFaceDetector;
import com.ec337.facescanpayment.core.face_detection.MediapipeFaceOrientationDetector;
import com.ec337.facescanpayment.core.utils.Either;
import com.ec337.facescanpayment.features.auth.presentation.widgets.CameraHandler;
import com.google.common.util.concurrent.ListenableFuture;

public class TestPage extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    private MediapipeFaceOrientationDetector faceOrientationDetector;
    private MediapipeFaceDetector faceDetector;

    private CameraHandler cameraHandler;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private ProcessCameraProvider cameraProvider;
    private PreviewView previewView;
    private ImageView capturedImageView;
    private Button switchCameraButton, capturePhotoButton, retryButton;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private static final int FACE_DETECTION_INTERVAL_MS = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app__test_page);

        faceOrientationDetector = new MediapipeFaceOrientationDetector(this);
        faceDetector = new MediapipeFaceDetector(this);

        initViews();

        if (hasCameraPermissions()) {
            startCamera();
        } else {
            requestCameraPermissions();
        }
    }

    private void initViews() {
        previewView = findViewById(R.id.previewView);

        cameraHandler = new CameraHandler(previewView, handler);

        switchCameraButton = findViewById(R.id.switchCameraButton);
        capturePhotoButton = findViewById(R.id.capturePhotoButton);
        retryButton = findViewById(R.id.retryButton);
        capturedImageView = findViewById(R.id.capturedImageView);

        switchCameraButton.setOnClickListener(v -> cameraHandler.switchCamera());
        capturePhotoButton.setOnClickListener(v -> capturePhoto());
        retryButton.setOnClickListener(v -> restartCamera());
    }

    private void detectFaceOrientationInFrame(Bitmap currentFrame) {
        Log.d(TAG, "Running face detection on frame...");
        Either<Failure, String> result = faceOrientationDetector.detectOrientation(currentFrame);
        Either<Failure, Bitmap> result2 = faceDetector.getCroppedFaceFromBitmap(currentFrame);

        if (result2.isRight()) Log.d(TAG, "Face size: " + result2.getRight().getWidth() + "x" + result2.getRight().getHeight());
        if (result.isRight()) {
            Log.d(TAG, "Landmark: " + result.getRight());
        } else {
            Log.d(TAG, "Landmark error: " + result.getLeft().getErrorMessage());
        }
    }

    private void capturePhoto() {
        cameraHandler.capturePhoto(new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                super.onCaptureSuccess(image);
                Log.d(TAG, "Photo capture succeeded.");

                Bitmap bitmap = cameraHandler.imageProxyToBitmap(image);
                if (bitmap != null) {
                    capturedImageView.setImageBitmap(bitmap);
                    showCapturedImageView();
                    stopCamera();
                    detectFaceOrientationInFrame(bitmap);
                }
                image.close();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                super.onError(exception);
                Log.e(TAG, "Photo capture failed: " + exception.getMessage());
                showToast("Photo capture failed.");
            }
        });
    }

    private void startCamera() {
        hideCapturedImageView();

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                cameraHandler.startCamera(cameraProvider);
            } catch (Exception e) {
                Log.e(TAG, "Camera initialization failed: " + e.getMessage());
                showToast("Camera initialization failed.");
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void restartCamera() {
        startCamera();
    }

    private void stopCamera() {
        cameraHandler.stopCamera();
    }

    private void showCapturedImageView() {
        retryButton.setVisibility(View.VISIBLE);
        capturedImageView.setVisibility(View.VISIBLE);
        capturePhotoButton.setVisibility(View.GONE);
        switchCameraButton.setVisibility(View.GONE);
        previewView.setVisibility(View.GONE);
    }

    private void hideCapturedImageView() {
        retryButton.setVisibility(View.GONE);
        capturedImageView.setVisibility(View.GONE);
        capturePhotoButton.setVisibility(View.VISIBLE);
        switchCameraButton.setVisibility(View.VISIBLE);
        previewView.setVisibility(View.VISIBLE);
    }

    private boolean hasCameraPermissions() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            showToast("Camera permission is required to use the camera.");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}