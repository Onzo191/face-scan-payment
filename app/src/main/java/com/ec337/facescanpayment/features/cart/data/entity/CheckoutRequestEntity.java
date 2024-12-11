package com.ec337.facescanpayment.features.cart.data.entity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class CheckoutRequestEntity {
    private String user_id;
    private int total;
    private List<CartProduct> products;

    public static class CartProduct {
        private String product_id;
        private int quantity;

        public String getProduct_id() {
            return product_id;
        }

        public void setProduct_id(String product_id) {
            this.product_id = product_id;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    public static String productsToJson(List<CartProduct> products) {
        Gson gson = new Gson();
        return gson.toJson(products);
    }

    public static List<CartProduct> productsFromJson(String json) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<CartProduct>>() {}.getType();
        return gson.fromJson(json, listType);
    }

    public String getUserId() {
        return user_id;
    }

    public void setUserId(String user_id) {
        this.user_id = user_id;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<CartProduct> getProducts() {
        return products;
    }

    public void setProducts(List<CartProduct> products) {
        this.products = products;
    }

    public int getTotalQuantity() {
        int totalQuantity = 0;
        if (products != null) {
            for (CartProduct product : products) {
                totalQuantity += product.getQuantity();
            }
        }
        return totalQuantity;
    }
}
