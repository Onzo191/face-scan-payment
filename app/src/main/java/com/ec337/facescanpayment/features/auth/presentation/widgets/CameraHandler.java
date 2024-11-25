package com.ec337.facescanpayment.features.auth.presentation.widgets;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import java.nio.ByteBuffer;

public class CameraHandler {
    private final String TAG = this.getClass().getSimpleName();
    private Camera camera;
    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private Handler handler;
    private PreviewView previewView;
    private int cameraFacing = CameraSelector.LENS_FACING_FRONT;

    public CameraHandler(PreviewView previewView, Handler handler) {
        this.previewView = previewView;
        this.handler = handler;
    }

    public void startCamera(ProcessCameraProvider cameraProvider) {
        this.cameraProvider = cameraProvider;
        bindCameraUseCases();
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
            camera = cameraProvider.bindToLifecycle((androidx.lifecycle.LifecycleOwner) previewView.getContext(), cameraSelector, preview, imageCapture);
        });
    }

    public void capturePhoto(ImageCapture.OnImageCapturedCallback callback) {
        if (imageCapture != null) {
            imageCapture.takePicture(ContextCompat.getMainExecutor(previewView.getContext()), callback);
        }
    }

    public void switchCamera() {
        cameraFacing = (cameraFacing == CameraSelector.LENS_FACING_BACK)
                ? CameraSelector.LENS_FACING_FRONT
                : CameraSelector.LENS_FACING_BACK;

        // Reinitialize the camera with the new camera facing
        startCamera(cameraProvider);
    }

    public void stopCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    public Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
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
}
