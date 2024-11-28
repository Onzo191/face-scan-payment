package com.ec337.facescanpayment.features.auth.data.repository.api.types;

import com.ec337.facescanpayment.features.auth.data.entity.UserEntity;

public class VerifyFaceResponse {
    private boolean verified;
    private UserEntity user;
    private Float similarity;

    public VerifyFaceResponse(boolean verified, UserEntity user, Float similarity) {
        this.verified = verified;
        this.user = user;
        this.similarity = similarity;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public Float getSimilarity() {
        return similarity;
    }

    public void setSimilarity(Float similarity) {
        this.similarity = similarity;
    }
}
