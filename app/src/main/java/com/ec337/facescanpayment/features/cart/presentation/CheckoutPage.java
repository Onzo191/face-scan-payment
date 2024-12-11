package com.ec337.facescanpayment.features.cart.presentation;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ec337.facescanpayment.R;
import com.ec337.facescanpayment.features.cart.data.entity.CartEntity;

import java.util.List;

public class CheckoutPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.app__checkout_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String productsJson = getIntent().getStringExtra("cart_products");
        if (productsJson != null) {
            List<CartEntity.CartProduct> cartProducts = CartEntity.productsFromJson(productsJson);
            for (CartEntity.CartProduct product : cartProducts) {
                Log.d("Checkout", "Product: " + product.getName() + ", Quantity: " + product.getQuantity());
            }
        } else {
            Log.e("Checkout", "No products received.");
        }
    }
}