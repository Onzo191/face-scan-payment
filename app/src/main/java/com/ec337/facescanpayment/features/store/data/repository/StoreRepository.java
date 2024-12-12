package com.ec337.facescanpayment.features.store.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ec337.facescanpayment.core.api.ApiClient;
import com.ec337.facescanpayment.core.utils.JwtToken;
import com.ec337.facescanpayment.features.store.data.entity.ProductEntity;
import com.ec337.facescanpayment.features.store.data.repository.api.StoreApiService;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StoreRepository {
    private final String TAG = this.getClass().getSimpleName();
    private JwtToken jwtToken;
    private String token;

    public StoreRepository(Context ctx) {
        jwtToken = new JwtToken(ctx);
        token = jwtToken.getToken();
    }

    public CompletableFuture<List<ProductEntity>> getAllProducts() {
        CompletableFuture<List<ProductEntity>> future = new CompletableFuture<>();

        if (token == null) {
            future.complete(Collections.emptyList());
            return future;
        }

        ApiClient.getRetrofitClient(token).create(StoreApiService.class)
                .getAllProducts()
                .enqueue(new Callback<List<ProductEntity>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<ProductEntity>> call, @NonNull Response<List<ProductEntity>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            future.complete(response.body());
                        } else {
                            future.completeExceptionally(new Exception("Failed to load products"));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ProductEntity>> call, Throwable t) {
                        Log.e(TAG, "Failed to load products: " + t.getMessage());
                        future.completeExceptionally(t);
                    }
                });

        return future;
    }
}
