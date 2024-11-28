package com.ec337.facescanpayment.features.auth.data.repository.api.types;

import com.ec337.facescanpayment.features.auth.data.entity.UserEntity;

public class LoginResponse {
    private String status;
    private String token;
    private UserEntity user;

    public LoginResponse(String status, String token, UserEntity user) {
        this.status = status;
        this.token = token;
        this.user = user;
    }

    public String getStatus() {
        return status;
    }

    public String getToken() {
        return token;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }
}
