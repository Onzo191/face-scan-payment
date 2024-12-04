package com.ec337.facescanpayment.core.face_detection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import com.ec337.facescanpayment.core.errors.ErrorCode;
import com.ec337.facescanpayment.core.errors.Failure;
import com.ec337.facescanpayment.core.utils.Either;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.core.Delegate;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult;

import java.util.List;

public class MediapipeFaceOrientationDetector {
    private static final String TAG = MediapipeFaceOrientationDetector.class.getSimpleName();
//    private static final String MODEL_PATH = "face_landmark.tflite";
    private static final String MODEL_PATH = "face_landmark.task";

    public static final float DEFAULT_FACE_DETECTION_CONFIDENCE = 0.5f;
    public static final float DEFAULT_FACE_TRACKING_CONFIDENCE = 0.5f;
    public static final float DEFAULT_FACE_PRESENCE_CONFIDENCE = 0.5f;
    public static final int DEFAULT_NUM_FACES = 1;

    private final Context context;
    private FaceLandmarker faceLandmarker;

    public MediapipeFaceOrientationDetector(Context context) {
        this.context = context;

        setupFaceLandmarker();
    }

    public void setupFaceLandmarker() {
        BaseOptions.Builder baseOptionBuilder = BaseOptions.builder().setModelAssetPath(MODEL_PATH);

        try {
            BaseOptions baseOptions = baseOptionBuilder.build();
            FaceLandmarker.FaceLandmarkerOptions.Builder optionsBuilder = FaceLandmarker.FaceLandmarkerOptions.builder()
                    .setBaseOptions(baseOptions)
                    .setMinFaceDetectionConfidence(DEFAULT_FACE_DETECTION_CONFIDENCE)
                    .setMinTrackingConfidence(DEFAULT_FACE_TRACKING_CONFIDENCE)
                    .setMinFacePresenceConfidence(DEFAULT_FACE_PRESENCE_CONFIDENCE)
                    .setNumFaces(DEFAULT_NUM_FACES)
                    .setOutputFaceBlendshapes(false)
                    .setOutputFacialTransformationMatrixes(false)
                    .setRunningMode(RunningMode.IMAGE);

            faceLandmarker = FaceLandmarker.createFromOptions(context, optionsBuilder.build());
        } catch (Exception e) {
            Log.e(TAG, "Error initializing FaceLandmarker: " + e.getMessage());
            this.faceLandmarker = null;
        }
    }


    // Method to detect the orientation of the face from the image
    public Either<Failure, String> detectOrientation(Bitmap imageBitmap) {
        try {
            if (imageBitmap == null) {
                return Either.left(new Failure(ErrorCode.FACE_ORIENTATION_DETECTION_FAILURE));
            }

            if (imageBitmap == null || faceLandmarker == null) {
                return Either.left(new Failure("Boom"));
            }

            // Detect face landmarks
            FaceLandmarkerResult result = faceLandmarker.detect(new BitmapImageBuilder(imageBitmap).build());
            List<List<NormalizedLandmark>> faceLandmarks = result.faceLandmarks();

            if (faceLandmarks.isEmpty()) {
                return Either.left(new Failure(ErrorCode.NO_FACE_LANDMARKS));
            }

            // Use the landmarks to compute the face orientation
            List<NormalizedLandmark> landmarks = faceLandmarks.get(0); // Assuming a single face
            String orientation = computeOrientationFromLandmarks(landmarks);

            return Either.right(orientation);
        } catch (Exception e) {
            Log.e(TAG, "Error detecting face orientation", e);
            return Either.left(new Failure(ErrorCode.FACE_ORIENTATION_DETECTION_FAILURE));
        }
    }


    private String computeOrientationFromLandmarks(List<NormalizedLandmark> landmarks) {
        // Get essential landmarks (e.g., nose, eyes, and chin)
        NormalizedLandmark leftEye = landmarks.get(36);  // Left eye
        NormalizedLandmark rightEye = landmarks.get(45);  // Right eye
        NormalizedLandmark nose = landmarks.get(30);  // Nose
        NormalizedLandmark chin = landmarks.get(8);  // Chin (bottom)

        // Calculate the relative positions of landmarks to determine orientation
        float eyeDistanceX = rightEye.x() - leftEye.x();
        float eyeDistanceY = rightEye.y() - leftEye.y();
        float noseToChinDistance = chin.y() - nose.y();

        // For simplicity, let's classify the face orientation based on relative distances
        // Example: Calculate pitch (up-down), yaw (left-right), and roll (tilt)

        if (Math.abs(eyeDistanceX) < 0.1f && Math.abs(eyeDistanceY) < 0.1f) {
            return "Face is looking directly at camera";
        } else if (eyeDistanceX > 0.1f) {
            return "Face turned right";
        } else if (eyeDistanceX < -0.1f) {
            return "Face turned left";
        } else if (noseToChinDistance > 0.05f) {
            return "Face looking downward";
        } else {
            return "Face looking upward";
        }
    }

    // Helper method to handle image rotation (similar to the one in MediapipeFaceDetector)
    private Bitmap rotateBitmapIfRequired(Bitmap source, ExifInterface exifInterface) {
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateBitmap(source, 90f);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateBitmap(source, 180f);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateBitmap(source, 270f);
            default:
                return source;
        }
    }

    private Bitmap rotateBitmap(Bitmap source, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, false);
    }
}
