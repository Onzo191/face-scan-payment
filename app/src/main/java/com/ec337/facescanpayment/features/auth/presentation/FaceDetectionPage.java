package com.ec337.facescanpayment.features.auth.presentation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ec337.facescanpayment.R;
import com.ec337.facescanpayment.core.errors.Failure;
import com.ec337.facescanpayment.core.face_detection.MediapipeFaceDetector;
import com.ec337.facescanpayment.core.utils.Either;
import com.ec337.facescanpayment.features.auth.data.repository.FaceRepository;
import com.ec337.facescanpayment.features.auth.usecases.ImageVectorUseCase;
import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FaceDetectionPage extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    private ImageVectorUseCase imageVectorUseCase;
    private MediapipeFaceDetector faceDetector;
    private boolean isDetecting = false;
    private Handler handler = new Handler(Looper.getMainLooper());

    private int cameraFacing = CameraSelector.LENS_FACING_FRONT;
    private Camera camera;
    private ProcessCameraProvider cameraProvider;

    private ExecutorService cameraExecutor;
    private PreviewView previewView;
    private ImageView capturedImageView;
    private ImageCapture imageCapture;
    private Button switchCameraButton, capturePhotoButton, retryButton;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private static final int FACE_DETECTION_INTERVAL_MS = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth__face_detection_page);

        imageVectorUseCase = new ImageVectorUseCase(this, new FaceRepository(this));
        faceDetector = new MediapipeFaceDetector(this);

        previewView = findViewById(R.id.previewView);
        cameraExecutor = Executors.newSingleThreadExecutor();

        switchCameraButton = findViewById(R.id.switchCameraButton);
        capturePhotoButton = findViewById(R.id.capturePhotoButton);
        retryButton = findViewById(R.id.retryButton);
        capturedImageView = findViewById(R.id.capturedImageView);

        switchCameraButton.setOnClickListener(v -> switchCamera());
        capturePhotoButton.setOnClickListener(v -> capturePhoto());
        retryButton.setOnClickListener(v -> restartCamera());

        if (hasCameraPermissions()) {
            startCamera();
        } else {
            requestCameraPermissions();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdownNow(); // Clean up executor
        handler.removeCallbacksAndMessages(null);
    }

    private void restartCamera() {
        startCamera();
    }

    private void startCamera() {
        isDetecting = true;
        hideCapturedImageView();

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (Exception e) {
                Log.e(TAG, "Camera initialization failed: " + e.getMessage());
                showToast("Camera initialization failed.");
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases() {
        previewView.post(() -> {
            int previewWidth = previewView.getWidth();
            int previewHeight = previewView.getHeight();

            Preview preview = new Preview.Builder().build();
            preview.setSurfaceProvider(previewView.getSurfaceProvider());

            imageCapture = new ImageCapture.Builder()
                    .setTargetResolution(new android.util.Size(previewWidth, previewHeight))
                    .setTargetRotation(previewView.getDisplay().getRotation())
                    .build();

            CameraSelector cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(cameraFacing)
                    .build();

            cameraProvider.unbindAll();
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            // Start periodic face detection
            startFaceDetection();
        });
    }

    private void startFaceDetection() {
        Log.d(TAG, "Starting face detection...");
        if (isDetecting) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isDetecting) {
                        Log.d(TAG, "Detecting faces...");
                        Bitmap currentFrame = previewView.getBitmap();
                        if (currentFrame != null) {
                            detectFaceInFrame(currentFrame);
                        }

                        handler.postDelayed(this, FACE_DETECTION_INTERVAL_MS);
                    }
                }
            }, FACE_DETECTION_INTERVAL_MS);
        } else {
            Log.d(TAG, "Face detection is paused or not started.");
        }
    }

    private void detectFaceInFrame(Bitmap currentFrame) {
        Log.d(TAG, "Running face detection on frame...");
        Either<Failure, Bitmap> faceDetectionResult = faceDetector.getCroppedFaceFromBitmap(currentFrame);

        if (faceDetectionResult.isRight()) {
            Log.d(TAG, "Face detected!");
            isDetecting = false;
            handler.postDelayed(() -> {
                runOnUiThread(this::capturePhoto);
            }, 200);
        } else {
            Log.d(TAG, "No face detected.");
        }
    }

    private void capturePhoto() {
        if (imageCapture == null) {
            showToast("ImageCapture is not initialized.");
            return;
        }

        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                super.onCaptureSuccess(image);
                Log.d(TAG, "Photo capture succeeded.");

                Bitmap bitmap = imageProxyToBitmap(image);
                if (bitmap != null) {
                    capturedImageView.setImageBitmap(bitmap);
                    showCapturedImageView();
                    stopCamera();

                    // Testing
                    reviewCapturedPhoto();
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

    private Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        ByteBuffer buffer = imageProxy.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
        bitmap = rotateBitmap(bitmap, rotationDegrees);

        if (cameraFacing == CameraSelector.LENS_FACING_FRONT) {
            bitmap = flipBitmap(bitmap);
        }

        return bitmap;
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int rotationDegrees) {
        android.graphics.Matrix matrix = new android.graphics.Matrix();
        matrix.postRotate(rotationDegrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private Bitmap flipBitmap(Bitmap bitmap) {
        android.graphics.Matrix matrix = new android.graphics.Matrix();
        matrix.preScale(-1, 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void reviewCapturedPhoto() {
        Bitmap capturedBitmap = ((BitmapDrawable) capturedImageView.getDrawable()).getBitmap();

        float[] embedding = imageVectorUseCase.processImage(capturedBitmap);
        if (embedding.length > 0) {
            Log.d(TAG, "Face embedding: " + Arrays.toString(embedding));
            showToast("Face embedding logged.");
        } else {
            showToast("No face detected.");
        }
    }

    private void stopCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    private void switchCamera() {
        cameraFacing = (cameraFacing == CameraSelector.LENS_FACING_BACK)
                ? CameraSelector.LENS_FACING_FRONT
                : CameraSelector.LENS_FACING_BACK;

        startCamera();
        showToast("Camera switched!");
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
