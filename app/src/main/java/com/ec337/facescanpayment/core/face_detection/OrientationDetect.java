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
                .setMinSuppressionThreshold(0.3f)
                .setRunningMode(RunningMode.IMAGE)
                .setMinDetectionConfidence(0.5f)
                .build();

        try {
            faceDetector = FaceDetector.createFromOptions(context, options);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing face detector", e);
        }
    }


    public enum TargetOrientation {
        FRONT, LEFT, RIGHT, UP, DOWN
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

    public OrientationResult detectOrientation(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e(TAG, "Bitmap is null");
            return new OrientationResult();
        }

        MPImage mpImage = new BitmapImageBuilder(bitmap).build();
        FaceDetectorResult faceDetectorResult = faceDetector.detect(mpImage);
        List<Detection> results = faceDetectorResult.detections();

        if (results.isEmpty() || !results.get(0).keypoints().isPresent()) {
            Log.d(TAG, "No face detected or no keypoints found.");
            return new OrientationResult();
        }

        List<NormalizedKeypoint> keypoints = results.get(0).keypoints().get();

        return calculateFaceOrientation(keypoints);
    }

    private OrientationResult calculateFaceOrientation(List<NormalizedKeypoint> landmarks) {

        OrientationResult orientation = new OrientationResult();
        if (landmarks.size() < 6) { // Number of landmarks depends on the model
            Log.e(TAG, "Not enough landmarks detected for orientation calculation.");
            return orientation;
        }

        int leftEyeOuterIndex = 3;    // Example index - VERIFY!
        int rightEyeOuterIndex = 3; // Example index - VERIFY!
        int noseTipIndex = 0;       // Example index - VERIFY!

        NormalizedKeypoint leftEyeOuter = landmarks.get(leftEyeOuterIndex);
        NormalizedKeypoint rightEyeOuter = landmarks.get(rightEyeOuterIndex);
        NormalizedKeypoint noseTip = landmarks.get(noseTipIndex);


        float eyeDistance = rightEyeOuter.x() - leftEyeOuter.x();
        float dynamicHorizontalThreshold = eyeDistance * 0.2f; // Example: 20% of eye distance


        float horizontalAngle = calculateHorizontalAngle(noseTip, leftEyeOuter, rightEyeOuter);

        orientation.setHorizontalAngle(horizontalAngle);

        orientation.setLeftTurned(horizontalAngle < -dynamicHorizontalThreshold);
        orientation.setRightTurned(horizontalAngle > dynamicHorizontalThreshold);
        orientation.setFrontFacing(Math.abs(horizontalAngle) <= dynamicHorizontalThreshold * 0.5f);


        Log.d(TAG, "Horizontal Angle: " + horizontalAngle + ", Threshold: " + dynamicHorizontalThreshold);
        Log.d(TAG, "Orientation: Front=" + orientation.isFrontFacing() +
                ", Left=" + orientation.isLeftTurned() +
                ", Right=" + orientation.isRightTurned());

        return orientation;
    }

    private float calculateHorizontalAngle(
            NormalizedKeypoint noseTip,
            NormalizedKeypoint leftEyeOuter,
            NormalizedKeypoint rightEyeOuter
    ) {
        float eyeDistance = rightEyeOuter.x() - leftEyeOuter.x();
        float midlineX = (leftEyeOuter.x() + rightEyeOuter.x()) / 2;
        float noseMidlineDeviation = noseTip.x() - midlineX;

        // Use atan2 for correct quadrant handling
        float angle = (float) Math.toDegrees(Math.atan2(noseMidlineDeviation, eyeDistance / 2));
        return angle;
    }

    private float calculateVerticalAngle(
            NormalizedKeypoint noseTip,
            NormalizedKeypoint leftCheek,
            NormalizedKeypoint rightCheek
    ) {
        float midlineY = (leftCheek.y() + rightCheek.y()) / 2;
        float angleDiff = noseTip.y() - midlineY;

        return (float) Math.toDegrees(Math.atan2(angleDiff, 1));
    }

    public float getOrientationConfidence(OrientationResult result) {
        int orientationCount = 0;
        if (result.isLeftTurned()) orientationCount++;
        if (result.isRightTurned()) orientationCount++;
        if (result.isFrontFacing()) orientationCount++;

        return (float) orientationCount / 5.0f;
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
