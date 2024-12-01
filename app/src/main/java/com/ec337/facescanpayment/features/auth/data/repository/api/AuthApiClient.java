package com.ec337.facescanpayment.features.auth.data.repository.api;

import com.ec337.facescanpayment.features.auth.data.repository.api.types.RegisterRequest;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AuthApiClient {
    private static final String BASE_URL = "https://flask-api-weld-ten.vercel.app";
    private static AuthApiService apiService;

    public static AuthApiService getApiService(String token) {
        if (apiService == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + token)
                            .build()))
                    .addInterceptor(logging)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();

            apiService = retrofit.create(AuthApiService.class);
        }

        return apiService;
    }
}
