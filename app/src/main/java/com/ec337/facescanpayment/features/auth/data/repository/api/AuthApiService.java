package com.ec337.facescanpayment.features.auth.data.repository.api;

import com.ec337.facescanpayment.features.auth.data.model.FaceModel;
import com.ec337.facescanpayment.features.auth.data.repository.api.types.LoginRequest;
import com.ec337.facescanpayment.features.auth.data.repository.api.types.LoginResponse;
import com.ec337.facescanpayment.features.auth.data.repository.api.types.RegisterFaceResponse;
import com.ec337.facescanpayment.features.auth.data.repository.api.types.RegisterRequest;
import com.ec337.facescanpayment.features.auth.data.repository.api.types.RegisterResponse;
import com.ec337.facescanpayment.features.auth.data.repository.api.types.UserRequest;
import com.ec337.facescanpayment.features.auth.data.repository.api.types.UserResponse;
import com.ec337.facescanpayment.features.auth.data.repository.api.types.VerifyFaceRequest;
import com.ec337.facescanpayment.features.auth.data.repository.api.types.VerifyFaceResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AuthApiService {
    @POST("/create")
    Call<RegisterResponse> registerUser(@Body RegisterRequest registerRequest);

    @POST("/login")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

    @GET("/get-current-user")
    Call<UserResponse> getCurrentUser(@Query("id") String userID);

    @POST("/register-face-v2")
    Call<RegisterFaceResponse> registerFace(@Body FaceModel faceModel);

    @POST("/verify-face")
    Call<VerifyFaceResponse> verifyFace(@Body VerifyFaceRequest verifyFaceRequest);
}
