package com.ec337.facescanpayment.features.auth.data.entity;

import java.util.List;

public class FaceEntity {
    String userId;
    String userName;
    String userEmail;
    List<Float> faceEmbedding;

    public FaceEntity(String userId, String userName, String userEmail, List<Float> faceEmbedding) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.faceEmbedding = faceEmbedding;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public List<Float> getFaceEmbedding() {
        return faceEmbedding;
    }

    public void setFaceEmbedding(List<Float> faceEmbedding) {
        this.faceEmbedding = faceEmbedding;
    }
}
