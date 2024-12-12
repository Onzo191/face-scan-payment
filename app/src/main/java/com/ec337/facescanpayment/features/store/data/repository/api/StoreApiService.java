package com.ec337.facescanpayment.features.store.data.repository.api;

import com.ec337.facescanpayment.features.store.data.entity.ProductEntity;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface StoreApiService {
    @GET("/get-all-products")
    Call<List<ProductEntity>> getAllProducts();
}
