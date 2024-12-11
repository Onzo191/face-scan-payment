package com.ec337.facescanpayment.features.cart.data.repository.api;

import com.ec337.facescanpayment.features.cart.data.entity.CartEntity;
import com.ec337.facescanpayment.features.cart.data.entity.CheckoutRequestEntity;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CartApiService {
    @GET("/get-cart")
    Call<CartEntity> getUserCart(@Query("user_id") String userId);

    @POST("/add-product-to-cart")
    Call<CartEntity> addProductToCart(@Body AddProductRequestBody body);

    @POST("/remove-product-from-cart")
    Call<CartEntity> removeProductFromCart(@Body AddProductRequestBody body);

    @POST("/add-new-order")
    Call<CartEntity> addNewOrder(@Body CheckoutRequestEntity body);

    class AddProductRequestBody {
        private String user_id;
        private String product_id;
        private int quantity;

        public AddProductRequestBody(String user_id, String product_id, int quantity) {
            this.user_id = user_id;
            this.product_id = product_id;
            this.quantity = quantity;
        }

        public String getUserId() {
            return user_id;
        }

        public void setUserId(String user_id) {
            this.user_id = user_id;
        }

        public String getProductId() {
            return product_id;
        }

        public void setProductId(String product_id) {
            this.product_id = product_id;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}
