package com.ec337.facescanpayment.features.cart.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ec337.facescanpayment.R;
import com.ec337.facescanpayment.core.notify.NotifyPage;
import com.ec337.facescanpayment.core.utils.NavigationUtils;
import com.ec337.facescanpayment.features.auth.presentation.FaceDetectionPage;
import com.ec337.facescanpayment.features.cart.data.entity.CartEntity;
import com.ec337.facescanpayment.features.cart.data.entity.CheckoutRequestEntity;
import com.ec337.facescanpayment.features.cart.data.repository.CartRepository;
import com.ec337.facescanpayment.features.cart.presentation.adapter.CartAdapter;
import com.ec337.facescanpayment.features.store.presentation.StorePage;

import java.util.ArrayList;
import java.util.List;

public class CheckoutPage extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvTotal;
    private ImageButton btnBack;
    private Button btnFaceVerify, btnPayment;
    private CartRepository cartRepository;
    private CartAdapter cartAdapter;
    Integer total;
    List<CartEntity.CartProduct> cartProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String productsJson = getIntent().getStringExtra("cart_products");
        total = getIntent().getIntExtra("total_price", 0);

        EdgeToEdge.enable(this);
        setContentView(R.layout.app__checkout_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.rvCartList);
        tvTotal = findViewById(R.id.tvTotalPrice);
        btnBack = findViewById(R.id.btnBack);
        btnFaceVerify = findViewById(R.id.btnFaceVerify);
        btnPayment = findViewById(R.id.btnPayment);
        setupClickListeners();

        cartRepository = new CartRepository(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this);
        recyclerView.setAdapter(cartAdapter);
        if (productsJson != null) {
            cartProducts = CartEntity.productsFromJson(productsJson);
            cartAdapter.setProducts(cartProducts);
        }
        cartAdapter.isCheckout();

        updateTotal(total);
    }

    private final ActivityResultLauncher<Intent> faceVerificationLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    boolean isVerified = result.getData().getBooleanExtra("isVerified", false);
//                    boolean isVerified = true;
                    if (isVerified) {
                        btnFaceVerify.setVisibility(View.GONE);
                        btnPayment.setVisibility(View.VISIBLE);
                        btnPayment.setEnabled(true);
                        showToast("Face verified successfully.");
                    } else {
                        btnFaceVerify.setVisibility(View.VISIBLE);
                        btnPayment.setVisibility(View.GONE);
                        btnPayment.setEnabled(false);
                        showToast("Face verification failed.");
                    }
                }
            });

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> NavigationUtils.navigateTo(this, CartPage.class, true));
        btnFaceVerify.setOnClickListener(v -> {
            Intent intent = new Intent(CheckoutPage.this, FaceDetectionPage.class);
            faceVerificationLauncher.launch(intent);
        });
        btnPayment.setOnClickListener(v -> proceedToCheckout());
    }

    private void proceedToCheckout() {
        CheckoutRequestEntity checkoutRequest = new CheckoutRequestEntity();
        checkoutRequest.setUserId(cartRepository.getUserId());
        checkoutRequest.setTotal(total);

        List<CheckoutRequestEntity.CartProduct> checkoutProducts = prepareCheckoutProducts(cartProducts);
        checkoutRequest.setProducts(checkoutProducts);

        cartRepository.addNewOrder(checkoutRequest)
                .thenAccept(cartEntity -> {
                    showToast("Checkout successful!");
                    Intent notifyIntent = new Intent(CheckoutPage.this, NotifyPage.class);
                    notifyIntent.putExtra("text", "Thanh toán thành công");
                    notifyIntent.putExtra("imageSrc", R.drawable.logo_checkout_completed);
                    startActivity(notifyIntent);

                    finish();
                })
                .exceptionally(throwable -> {
                    showToast("Checkout failed: " + throwable.getMessage());
                    return null;
                });
    }

    private List<CheckoutRequestEntity.CartProduct> prepareCheckoutProducts(List<CartEntity.CartProduct> cartProducts) {
        List<CheckoutRequestEntity.CartProduct> checkoutProducts = new ArrayList<>();
        for (CartEntity.CartProduct cartProduct : cartProducts) {
            CheckoutRequestEntity.CartProduct checkoutProduct = new CheckoutRequestEntity.CartProduct();
            checkoutProduct.setProduct_id(cartProduct.getProduct_id());
            checkoutProduct.setQuantity(cartProduct.getQuantity());
            checkoutProducts.add(checkoutProduct);
        }
        Log.d("TAG", checkoutProducts.toString());
        return checkoutProducts;
    }

    private void updateTotal(Integer total) {
        String formattedTotal = String.format("%,d", total);
        tvTotal.setText(formattedTotal);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
