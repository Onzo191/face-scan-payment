package com.ec337.facescanpayment.core.errors;

public enum ErrorCode {
    MULTIPLE_FACES("Multiple faces found in the image."),
    NO_FACE("No faces were in the image."),
    FACE_DETECTOR_FAILURE("Face detection failed."),
    FACE_ORIENTATION_DETECTION_FAILURE("Face orientation detection failed."),
    NO_FACE_LANDMARKS("No face landmarks in the image.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
