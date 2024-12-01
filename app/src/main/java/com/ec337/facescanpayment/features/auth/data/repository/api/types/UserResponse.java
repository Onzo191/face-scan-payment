package com.ec337.facescanpayment.features.auth.data.repository.api.types;

import com.ec337.facescanpayment.features.auth.data.entity.UserEntity;

public class UserResponse {
    private UserEntity user;

    public UserResponse(UserEntity user) {
        this.user = user;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }
}
