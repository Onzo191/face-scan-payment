package com.ec337.facescanpayment.features.auth.data.repository;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.ec337.facescanpayment.MainActivity;
import com.ec337.facescanpayment.core.utils.JwtToken;
import com.ec337.facescanpayment.core.utils.NavigationUtils;
import com.ec337.facescanpayment.features.auth.data.entity.UserEntity;
import com.ec337.facescanpayment.features.auth.data.model.FaceModel;
import com.ec337.facescanpayment.features.auth.data.repository.api.AuthApiClient;
import com.ec337.facescanpayment.features.auth.data.repository.api.types.LoginRequest;
import com.ec337.facescanpayment.features.auth.data.repository.api.types.LoginResponse;
import com.ec337.facescanpayment.features.auth.data.repository.api.types.RegisterFaceResponse;
import com.ec337.facescanpayment.features.auth.data.repository.api.types.RegisterRequest;
import com.ec337.facescanpayment.features.auth.data.repository.api.types.RegisterResponse;
import com.ec337.facescanpayment.features.auth.data.repository.api.types.UserRequest;
import com.ec337.facescanpayment.features.auth.data.repository.api.types.UserResponse;
import com.ec337.facescanpayment.features.auth.data.repository.api.types.VerifyFaceRequest;
import com.ec337.facescanpayment.features.auth.data.repository.api.types.VerifyFaceResponse;
import com.ec337.facescanpayment.features.auth.presentation.RegisterPage;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private String token;
    private JwtToken jwtToken;
    public void login(Context ctx ,String email, String password) {
        if (jwtToken == null) {
            jwtToken = new JwtToken(ctx);
        }
        LoginRequest request = new LoginRequest(email, password);
        AuthApiClient.getApiService(null)
                .loginUser(request)
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            token = response.body().getToken();
                            String id = response.body().getUser().getId();
                            String email = response.body().getUser().getEmail();
                            Log.d("AuthRepoitory", "id: " + id);
                            jwtToken.saveToken(token);
                            jwtToken.saveUserId(ctx, id);
                            jwtToken.saveUserEmail(ctx, email);
                            Toast.makeText(ctx, "Login successful", Toast.LENGTH_SHORT).show();
                            NavigationUtils.navigateTo((Activity) ctx, MainActivity.class);
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        Log.e("AuthRepoitory", "Failed to login: " + t.getMessage());
                    }
                });
    }

    public void register(Context ctx, String firstName, String lastName, String email, String phone, String gender, String password) {
        RegisterRequest request = new RegisterRequest(firstName,lastName,email,phone,gender,password);
        AuthApiClient.getApiService(null)
                .registerUser(request)
                .enqueue(new Callback<RegisterResponse>() {
                    @Override
                    public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String mess = response.body().getMessage();
                            Log.d("AuthRepoitory", "Registration message: " + mess);
                            Toast.makeText(ctx, "Registration successful", Toast.LENGTH_SHORT).show();
                            NavigationUtils.navigateTo((Activity) ctx, MainActivity.class);
                        }
                    }

                    @Override
                    public void onFailure(Call<RegisterResponse> call, Throwable t) {
                        Log.e("AuthRepoitory", "Failed to register: " + t.getMessage());
                    }
                });
    }

    public void getCurrentUser(Context ctx, String id) {
        if (jwtToken == null) {
            jwtToken = new JwtToken(ctx);
            token = jwtToken.getToken();
        }
        UserRequest request = new UserRequest(id);
        AuthApiClient.getApiService(token)
                .getCurrentUser(id)
                .enqueue(new Callback<UserResponse>() {
                    @Override
                    public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            UserResponse user = response.body();
//                            jwtToken.saveUserEmail(ctx, user.getUser().getEmail());
                        }
                    }

                    @Override
                    public void onFailure(Call<UserResponse> call, Throwable t) {
                        Log.e("AuthRepoitory", "Failed to get current user: " + t.getMessage());
                    }
                });
    }

    public void registerFace(Context ctx, FaceModel faceModel) {
        if (jwtToken == null) {
            jwtToken = new JwtToken(ctx);
            token = jwtToken.getToken();
        }
        AuthApiClient.getApiService(token)
                .registerFace(faceModel)
                .enqueue(new Callback<RegisterFaceResponse>() {
                    @Override
                    public void onResponse(Call<RegisterFaceResponse> call, Response<RegisterFaceResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d("AuthRepoitory", "Face registered successfully");
                        }
                    }

                    @Override
                    public void onFailure(Call<RegisterFaceResponse> call, Throwable t) {

                    }
                });
    }

    public void verifyFace(Context ctx, float[] embeddings,String userId, String userEmail,OnVerifyFaceListener listener) {
        if (jwtToken == null) {
            jwtToken = new JwtToken(ctx);
            token = jwtToken.getToken();
        }

        List<Float> embeddingList = new ArrayList<>();
        for (float value : embeddings) {
            embeddingList.add(value);
        }
        VerifyFaceRequest request = new VerifyFaceRequest(userId, userEmail, embeddingList);
        AuthApiClient.getApiService(token)
                .verifyFace(request)
                .enqueue(new Callback<VerifyFaceResponse>() {
                    @Override
                    public void onResponse(Call<VerifyFaceResponse> call, Response<VerifyFaceResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d("AuthRepoitory", "Face verified successfully");
                            listener.onSuccess(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<VerifyFaceResponse> call, Throwable t) {
                        Log.e("AuthRepoitory", "Failed to verify face: " + t.getMessage());
                        listener.onFailure(t.getMessage());
                    }
                });
    }

    public interface OnVerifyFaceListener {
        void onSuccess(VerifyFaceResponse response);
        void onFailure(String message);
    }
}
