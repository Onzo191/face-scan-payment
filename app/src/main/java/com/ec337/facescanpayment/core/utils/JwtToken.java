package com.ec337.facescanpayment.core.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class JwtToken {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public JwtToken(Context ctx) {
        this.sharedPreferences = ctx.getSharedPreferences("AppPrefs", ctx.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
    }

    public void saveToken(String token) {
        editor.putString("token", token);
        editor.apply();
    }

    public String getToken() {
        return sharedPreferences.getString("token", null);
    }

    public void clearToken() {
        editor.remove("token");
        editor.apply();
    }

    public void saveUserId(Context context, String userId) {
        editor.putString("userId", userId);
        editor.apply();
    }

    public String getUserId(Context context) {
        return sharedPreferences.getString("userId", null);
    }

    public void saveUserEmail(Context context, String email) {
        editor.putString("email", email);
        editor.apply();
    }

    public String getUserEmail(Context context) {
        return sharedPreferences.getString("email", null);
    }

    public void deleteUser(Context context) {
        editor.remove("userId");
        editor.remove("email");
        editor.apply();
    }
}
