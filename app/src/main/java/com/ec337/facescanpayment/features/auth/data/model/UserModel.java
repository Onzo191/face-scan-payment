package com.ec337.facescanpayment.features.auth.data.model;

import com.ec337.facescanpayment.features.auth.data.entity.UserEntity;

import java.util.HashMap;
import java.util.Map;

public class UserModel extends UserEntity {
    public UserModel(String firstName, String lastName, String gender, String email, String phone,Boolean hasFaceRegister) {
        super(firstName, lastName, gender, email, phone, hasFaceRegister);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("firstName", getFirstName());
        userMap.put("lastName", getLastName());
        userMap.put("gender", getGender());
        userMap.put("email", getEmail());
        userMap.put("phone", getPhone());
        userMap.put("hasFaceRegister", getHasFaceRegister());
        return userMap;
    }
}
