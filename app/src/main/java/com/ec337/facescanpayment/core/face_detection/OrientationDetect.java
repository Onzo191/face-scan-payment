package com.ec337.facescanpayment.core.face_detection;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.components.containers.NormalizedKeypoint;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector;
import com.google.mediapipe.tasks.components.containers.Detection;
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult;

import java.util.List;


public class OrientationDetect {
    private static final String TAG = "OrientationDetect";
    private static final float DEFAULT_HORIZONTAL_THRESHOLD = 10f;
    private static final float DEFAULT_VERTICAL_THRESHOLD = 10f;;
    private FaceDetector faceDetector;

    public OrientationDetect(Context context) {
        FaceDetector.FaceDetectorOptions options = FaceDetector.FaceDetectorOptions.builder()
                .setBaseOptions(BaseOptions.builder().setModelAssetPath("blaze_face_short_range.tflite").build())
                .setRunningMode(RunningMode.IMAGE)
                .build();

        try {
            faceDetector = FaceDetector.createFromOptions(context, options);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing face detector", e);
        }
    }

    public boolean detectFace(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e(TAG, "Bitmap is null");
            return false;
        }

        try {
            MPImage mpImage = new BitmapImageBuilder(bitmap).build();
            FaceDetectorResult faceDetectorResult = faceDetector.detect(mpImage);
            List<Detection> results = faceDetectorResult.detections();
            return !results.isEmpty(); // Return true if a face is detected
        } catch (Exception e) {
            Log.e(TAG, "Error detecting face", e);
            return false;
        }
    }

    public enum TargetOrientation {
        FRONT, LEFT, RIGHT
    }

    public boolean isTargetOrientation(OrientationResult result, TargetOrientation target, float dynamicThreshold) {
        switch (target) {
            case FRONT:
                return Math.abs(result.getHorizontalAngle()) <= dynamicThreshold * 0.5f;
            case LEFT:
                return result.getHorizontalAngle() < -dynamicThreshold;
            case RIGHT:
                return result.getHorizontalAngle() > dynamicThreshold;
            // Handle UP/DOWN if needed
            default:
                return false;
        }
    }



    public static class OrientationResult {
        private boolean leftTurned = false;
        private boolean rightTurned = false;
        private boolean frontFacing = false;
        private float horizontalAngle = 0f;
        private float verticalAngle = 0f;

        // Getters and setters
        public boolean isLeftTurned() { return leftTurned; }
        public void setLeftTurned(boolean leftTurned) { this.leftTurned = leftTurned; }

        public boolean isRightTurned() { return rightTurned; }
        public void setRightTurned(boolean rightTurned) { this.rightTurned = rightTurned; }

        public boolean isFrontFacing() { return frontFacing; }
        public void setFrontFacing(boolean frontFacing) { this.frontFacing = frontFacing; }

        public float getHorizontalAngle() { return horizontalAngle; }
        public void setHorizontalAngle(float horizontalAngle) { this.horizontalAngle = horizontalAngle; }

        public float getVerticalAngle() { return verticalAngle; }
        public void setVerticalAngle(float verticalAngle) { this.verticalAngle = verticalAngle; }
    }
}
