package com.ec337.facescanpayment.core.face_detection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import androidx.exifinterface.media.ExifInterface;

import com.ec337.facescanpayment.core.errors.ErrorCode;
import com.ec337.facescanpayment.core.errors.Failure;
import com.ec337.facescanpayment.core.utils.Either;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.tasks.components.containers.Detection;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector;
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector.FaceDetectorOptions;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MediapipeFaceDetector {
    private static final String TAG = MediapipeFaceDetector.class.getSimpleName();
    private final Context context;
    BaseOptions baseOptions;
    private final FaceDetector faceDetector;
    FaceDetectorOptions faceDetectorOptions;


    public MediapipeFaceDetector(Context context) {
        this.context = context;
        final String modelName = "blaze_face_short_range.tflite";
        baseOptions = BaseOptions.builder().setModelAssetPath(modelName).build();
        faceDetectorOptions = FaceDetectorOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)
                .build();
        this.faceDetector = FaceDetector.createFromOptions(context, faceDetectorOptions);
    }

    public Either<Failure, Bitmap> getCroppedFace(Uri imageUri) {
        try {
            InputStream imageInputStream = context.getContentResolver().openInputStream(imageUri);
            if (imageInputStream == null) {
                return Either.left(new Failure(ErrorCode.FACE_DETECTOR_FAILURE));
            }

            Bitmap imageBitmap = BitmapFactory.decodeStream(imageInputStream);
            imageInputStream.close();

            // Re-create an input-stream to reset its position
            // InputStream returns false with markSupported(), hence we cannot
            // reset its position
            // Without recreating the inputStream, no exif-data is read
            imageInputStream = context.getContentResolver().openInputStream(imageUri);
            if (imageInputStream == null) {
                return Either.left(new Failure(ErrorCode.FACE_DETECTOR_FAILURE));
            }

            ExifInterface exifInterface = new ExifInterface(imageInputStream);
            imageBitmap = rotateBitmapIfRequired(imageBitmap, exifInterface);
            imageInputStream.close();

            List<Detection> faces = faceDetector.detect(new BitmapImageBuilder(imageBitmap).build()).detections();
            if (faces.size() > 1) {
                return Either.left(new Failure(ErrorCode.MULTIPLE_FACES));
            } else if (faces.isEmpty()) {
                return Either.left(new Failure(ErrorCode.NO_FACE));
            } else {
                RectF rectF = faces.get(0).boundingBox();
                Rect rect = convertRectFToRect(rectF);

                if (validateRect(imageBitmap, rect)) {
                    return Either.right(cropBitmap(imageBitmap, rect));
                } else {
                    return Either.left(new Failure(ErrorCode.FACE_DETECTOR_FAILURE));
                }
            }
        } catch (Exception e) {
            return Either.left(new Failure(ErrorCode.FACE_DETECTOR_FAILURE));
        }
    }

    public Either<Failure, Bitmap> getCroppedFaceFromBitmap(Bitmap imageBitmap) {
        try {
            if (imageBitmap == null) {
                return Either.left(new Failure(ErrorCode.FACE_DETECTOR_FAILURE));
            }

            List<Detection> faces = faceDetector.detect(new BitmapImageBuilder(imageBitmap).build()).detections();
            if (faces.size() > 1) {
                return Either.left(new Failure(ErrorCode.MULTIPLE_FACES));
            } else if (faces.isEmpty()) {
                return Either.left(new Failure(ErrorCode.NO_FACE));
            } else {
                RectF rectF = faces.get(0).boundingBox();
                Rect rect = convertRectFToRect(rectF);

                if (validateRect(imageBitmap, rect)) {
                    return Either.right(cropBitmap(imageBitmap, rect));
                } else {
                    return Either.left(new Failure(ErrorCode.FACE_DETECTOR_FAILURE));
                }
            }
        } catch (Exception e) {
            return Either.left(new Failure(ErrorCode.FACE_DETECTOR_FAILURE));
        }
    }

    public List<Pair<Bitmap, Rect>> getAllCroppedFaces(Bitmap frameBitmap) {
        List<Pair<Bitmap, Rect>> croppedFaces  = new ArrayList<>();
        List<Detection> faces = faceDetector.detect(new BitmapImageBuilder(frameBitmap).build()).detections();
        faces.forEach(detection -> {
            RectF rectF = detection.boundingBox();
            Rect rect = convertRectFToRect(rectF);

            if (validateRect(frameBitmap, rect)) {
                Bitmap croppedBitmap = cropBitmap(frameBitmap, rect);
                croppedFaces .add(new Pair<>(croppedBitmap, rect));
            }
        });
        return croppedFaces ;
    }

    public void saveBitmap(Context context, Bitmap image, String name) {
        try {
            String filePath = context.getFilesDir().getAbsolutePath() + "/" + name + ".png";
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            image.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "Error saving bitmap: " + name, e);
        }
    }

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

    private Bitmap cropBitmap(Bitmap imageBitmap, Rect rect) {
        return Bitmap.createBitmap(imageBitmap, rect.left, rect.top, rect.width(), rect.height());
    }

    private Rect convertRectFToRect(RectF rectF) {
        return new Rect((int) rectF.left, (int) rectF.top,
                (int) rectF.right, (int) rectF.bottom);
    }

    private boolean validateRect(Bitmap cameraFrameBitmap, Rect boundingBox) {
        return boundingBox.left >= 0 &&
                boundingBox.top >= 0 &&
                (boundingBox.left + boundingBox.width()) < cameraFrameBitmap.getWidth() &&
                (boundingBox.top + boundingBox.height()) < cameraFrameBitmap.getHeight();
    }
}
