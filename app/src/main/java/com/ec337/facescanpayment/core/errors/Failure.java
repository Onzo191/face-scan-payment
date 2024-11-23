package com.ec337.facescanpayment.core.errors;

import java.util.Objects;

public class Failure {
    private static final String DEFAULT_ERROR_MESSAGE = "An unexpected error occurred.";
    private final String message;

    public Failure() {
        this.message = DEFAULT_ERROR_MESSAGE;
    }

    public Failure(String customMessage) {
        this.message = customMessage != null && !customMessage.isEmpty() ? customMessage : DEFAULT_ERROR_MESSAGE;
    }

    public Failure(ErrorCode errorCode) {
        this.message = Objects.requireNonNullElse(errorCode.getMessage(), DEFAULT_ERROR_MESSAGE);
    }

    public String getErrorMessage() {
        return message;
    }
}
