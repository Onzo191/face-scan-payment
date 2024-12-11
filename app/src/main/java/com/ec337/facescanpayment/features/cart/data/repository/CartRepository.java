package com.ec337.facescanpayment.features.cart.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ec337.facescanpayment.core.api.ApiClient;
import com.ec337.facescanpayment.core.utils.JwtToken;
import com.ec337.facescanpayment.features.cart.data.entity.CartEntity;
import com.ec337.facescanpayment.features.cart.data.repository.api.CartApiService;
import com.ec337.facescanpayment.features.store.data.repository.api.StoreApiService;

import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartRepository {
    private final String TAG = this.getClass().getSimpleName();
    private JwtToken jwtToken;
    private String token;
    private String userId;

    public CartRepository(Context ctx) {
        jwtToken = new JwtToken(ctx);
        token = jwtToken.getToken();
        userId = jwtToken.getUserId();
    }

    public CompletableFuture<CartEntity> getUserCart() {
        CompletableFuture<CartEntity> future = new CompletableFuture<>();

        ApiClient.getRetrofitClient(token).create(CartApiService.class)
                .getUserCart(userId).enqueue(new Callback<CartEntity>() {
                    @Override
                    public void onResponse(@NonNull Call<CartEntity> call, @NonNull Response<CartEntity> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            future.complete(response.body());
                        } else {
                            String error = response.errorBody() != null ? response.errorBody().toString() : "Unknown error";
                            future.completeExceptionally(new Exception(error));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CartEntity> call, @NonNull Throwable t) {
                        Log.e(TAG, "Failed to fetch user cart: " + t.getMessage());
                        future.completeExceptionally(t);
                    }
                });

        return future;
    }

    public CompletableFuture<CartEntity> addProductToCart(String productId, int quantity) {
        CompletableFuture<CartEntity> future = new CompletableFuture<>();

        CartApiService.AddProductRequestBody body = new CartApiService.AddProductRequestBody(userId, productId, quantity);
        ApiClient.getRetrofitClient(token).create(CartApiService.class)
                .addProductToCart(body).enqueue(new Callback<CartEntity>() {
                    @Override
                    public void onResponse(@NonNull Call<CartEntity> call, @NonNull Response<CartEntity> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            future.complete(response.body());
                        } else {
                            String error = response.errorBody() != null ? response.errorBody().toString() : "Unknown error";
                            future.completeExceptionally(new Exception(error));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CartEntity> call, @NonNull Throwable t) {
                        Log.e(TAG, "Failed to add product to cart: " + t.getMessage());
                        future.completeExceptionally(t);
                    }
                });

        return future;
    }

    public CompletableFuture<CartEntity> removeProductFromCart(String productId, int quantity) {
        CompletableFuture<CartEntity> future = new CompletableFuture<>();
        CartApiService.AddProductRequestBody body = new CartApiService.AddProductRequestBody(userId, productId, quantity);
        ApiClient.getRetrofitClient(token).create(CartApiService.class)
                .removeProductFromCart(body).enqueue(new Callback<CartEntity>() {
                    @Override
                    public void onResponse(@NonNull Call<CartEntity> call, @NonNull Response<CartEntity> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            future.complete(response.body());
                        } else {
                            String error = response.errorBody() != null ? response.errorBody().toString() : "Unknown error";
                            future.completeExceptionally(new Exception(error));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CartEntity> call, @NonNull Throwable t) {
                        Log.e(TAG, "Failed to add product to cart: " + t.getMessage());
                        future.completeExceptionally(t);
                    }
                });

        return future;
    }
}
