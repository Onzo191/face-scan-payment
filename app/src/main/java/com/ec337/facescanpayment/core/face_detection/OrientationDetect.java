package com.ec337.facescanpayment.core.face_detection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.Matrix;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import com.google.mediapipe.framework.MediaPipeException;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.components.containers.NormalizedKeypoint;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector;
import com.google.mediapipe.tasks.components.containers.Detection;
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrientationDetect {
    private static final String TAG = "OrientationDetect";
    private static final float DEFAULT_HORIZONTAL_THRESHOLD = 15f;
    private static final float DEFAULT_VERTICAL_THRESHOLD = 15f;;
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

    public boolean isTargetOrientation(OrientationResult result, TargetOrientation target) {
        switch (target) {
            case FRONT:
                return result.isFrontFacing();
            case LEFT:
                return result.isLeftTurned();
            case RIGHT:
                return result.isRightTurned();
            case UP:
                return result.isUpTurned();
            case DOWN:
                return result.isDownTurned();
            default:
                return false;
        }
    }

    public OrientationResult detectOrientation(Bitmap bitmap) {
        return detectOrientation(bitmap, DEFAULT_HORIZONTAL_THRESHOLD, DEFAULT_VERTICAL_THRESHOLD);
    }

    public OrientationResult detectOrientation(Bitmap bitmap, float horizontalThreshold, float verticalThreshold) {
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

        return calculateFaceOrientation(keypoints, bitmap.getWidth(), bitmap.getHeight(),
                horizontalThreshold, verticalThreshold);
    }

    private OrientationResult calculateFaceOrientation(
            List<NormalizedKeypoint> landmarks,
            int width,
            int height,
            float horizontalThreshold,
            float verticalThreshold
    ) {
        OrientationResult orientation = new OrientationResult();

        if (landmarks.size() < 478) {
            Log.e(TAG, "Not enough landmarks detected for orientation calculation.");
            return orientation;
        }

        NormalizedKeypoint noseTip = landmarks.get(1);
        NormalizedKeypoint leftCheek = landmarks.get(93);
        NormalizedKeypoint rightCheek = landmarks.get(323);

        float horizontalAngle = calculateHorizontalAngle(noseTip, leftCheek, rightCheek);
        float verticalAngle = calculateVerticalAngle(noseTip, leftCheek, rightCheek);

        orientation.setLeftTurned(horizontalAngle < -horizontalThreshold);
        orientation.setRightTurned(horizontalAngle > horizontalThreshold);
        orientation.setUpTurned(verticalAngle < -verticalThreshold);
        orientation.setDownTurned(verticalAngle > verticalThreshold);
        orientation.setFrontFacing(
                Math.abs(horizontalAngle) <= horizontalThreshold &&
                        Math.abs(verticalAngle) <= verticalThreshold
        );

        orientation.setHorizontalAngle(horizontalAngle);
        orientation.setVerticalAngle(verticalAngle);

        return orientation;
    }

    private float calculateHorizontalAngle(
            NormalizedKeypoint noseTip,
            NormalizedKeypoint leftCheek,
            NormalizedKeypoint rightCheek
    ) {
        float midlineX = (leftCheek.x() + rightCheek.x()) / 2;
        float angleDiff = noseTip.x() - midlineX;

        return (float) Math.toDegrees(Math.atan2(angleDiff, 1));
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
        if (result.isUpTurned()) orientationCount++;
        if (result.isDownTurned()) orientationCount++;
        if (result.isFrontFacing()) orientationCount++;

        return (float) orientationCount / 5.0f;
    }

    public static class OrientationResult {
        private boolean leftTurned = false;
        private boolean rightTurned = false;
        private boolean upTurned = false;
        private boolean downTurned = false;
        private boolean frontFacing = false;
        private float horizontalAngle = 0f;
        private float verticalAngle = 0f;

        // Getters and setters
        public boolean isLeftTurned() { return leftTurned; }
        public void setLeftTurned(boolean leftTurned) { this.leftTurned = leftTurned; }

        public boolean isRightTurned() { return rightTurned; }
        public void setRightTurned(boolean rightTurned) { this.rightTurned = rightTurned; }

        public boolean isUpTurned() { return upTurned; }
        public void setUpTurned(boolean upTurned) { this.upTurned = upTurned; }

        public boolean isDownTurned() { return downTurned; }
        public void setDownTurned(boolean downTurned) { this.downTurned = downTurned; }

        public boolean isFrontFacing() { return frontFacing; }
        public void setFrontFacing(boolean frontFacing) { this.frontFacing = frontFacing; }

        public float getHorizontalAngle() { return horizontalAngle; }
        public void setHorizontalAngle(float horizontalAngle) { this.horizontalAngle = horizontalAngle; }

        public float getVerticalAngle() { return verticalAngle; }
        public void setVerticalAngle(float verticalAngle) { this.verticalAngle = verticalAngle; }
    }
}
