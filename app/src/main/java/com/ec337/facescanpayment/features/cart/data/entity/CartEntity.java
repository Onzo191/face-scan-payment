package com.ec337.facescanpayment.features.cart.data.entity;

import java.util.List;

public class CartEntity {
    private String id;
    private String user_id;
    private int total;
    private List<CartProduct> products;

    public static class CartProduct {
        private String id;
        private String image;
        private String name;
        private Long price;
        private String product_id;
        private int quantity;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getPrice() {
            return price;
        }

        public void setPrice(Long price) {
            this.price = price;
        }

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
