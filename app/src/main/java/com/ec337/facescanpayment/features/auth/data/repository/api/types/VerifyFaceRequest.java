package com.ec337.facescanpayment.features.auth.data.repository.api.types;

import java.util.List;

public class VerifyFaceRequest {
    private String userId;
    private String userEmail;
    private List<Float> embeddings;

    public VerifyFaceRequest(String userId, String userEmail,List<Float> embeddings) {
        this.embeddings = embeddings;
        this.userId = userId;
        this.userEmail = userEmail;
    }

    public List<Float> getEmbeddings() {
        return embeddings;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setEmbeddings(List<Float> embeddings) {
        this.embeddings = embeddings;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userEmail) {
        this.userEmail = userEmail;
    }
}
