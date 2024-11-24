package com.ec337.facescanpayment.features.auth.data.entity;

import java.util.List;
import java.util.Map;

public class FaceEntity {
    String userId;
    String userName;
    String userEmail;
    Map<String, List<Float>> faceEmbeddings;

    public FaceEntity(String userId, String userName, String userEmail, Map<String, List<Float>> faceEmbeddings) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.faceEmbeddings = faceEmbeddings;
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

    public Map<String, List<Float>> getFaceEmbeddings() {
        return faceEmbeddings;
    }

    public void setFaceEmbeddings(Map<String, List<Float>> faceEmbeddings) {
        this.faceEmbeddings = faceEmbeddings;
    }
}
