package com.ec337.facescanpayment.features.auth.data.repository.api.types;

public class RegisterFaceResponse {
    private String message;

    public RegisterFaceResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
