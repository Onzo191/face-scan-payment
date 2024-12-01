package com.ec337.facescanpayment.features.auth.data.entity;

public class UserEntity {
    private String firstName;
    private String lastName;
    private String gender;
    private String email;
    private String phone;
    private Boolean hasFaceRegister;

    public UserEntity() {
    }

    public UserEntity(String firstName, String lastName, String gender, String email, String phone, Boolean hasFaceRegister) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.email = email;
        this.phone = phone;
        this.hasFaceRegister = hasFaceRegister;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getHasFaceRegister() {
        return hasFaceRegister;
    }

    public void setHasFaceRegister(Boolean hasFaceRegister) {
        this.hasFaceRegister = hasFaceRegister;
    }
}
