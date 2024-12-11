package com.ec337.facescanpayment.features.cart.presentation;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ec337.facescanpayment.R;
import com.ec337.facescanpayment.core.utils.NavigationUtils;
import com.ec337.facescanpayment.features.cart.data.repository.CartRepository;
import com.ec337.facescanpayment.features.cart.presentation.adapter.CartAdapter;
import com.ec337.facescanpayment.features.checkout.CheckoutPage;
import com.ec337.facescanpayment.features.store.data.entity.ProductEntity;
import com.ec337.facescanpayment.features.store.data.repository.StoreRepository;
import com.ec337.facescanpayment.features.store.presentation.StorePage;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;

public class CartPage extends AppCompatActivity {
    private RecyclerView recyclerView;
    private LinearLayout llEmptyCart;
    private Button btnStore, btnPayment;
    private ImageButton btnBack;
    private CartRepository cartRepository;
    private CartAdapter cartAdapter;
    private StoreRepository storeRepository;
    private ShimmerFrameLayout shimmerFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        initInsets();

        //cart
        cartRepository = new CartRepository(this);
        storeRepository = new StoreRepository(this);

        initViews();
        fetchCart();
    }

    private void initInsets() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.app__cart_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        shimmerFrameLayout = findViewById(R.id.shimmerContainer);
        recyclerView = findViewById(R.id.rvCartList);
        llEmptyCart = findViewById(R.id.llEmptyCart);
        btnBack = findViewById(R.id.btnBack);
        btnStore = findViewById(R.id.btnStore);
        btnPayment = findViewById(R.id.btnPayment);
        setupClickListeners();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this);
        recyclerView.setAdapter(cartAdapter);

        //visibility
        btnPayment.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        llEmptyCart.setVisibility(View.GONE);
        btnStore.setVisibility(View.VISIBLE);
        shimmerFrameLayout.setVisibility(View.VISIBLE);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> NavigationUtils.navigateTo(this, StorePage.class, true));
        btnStore.setOnClickListener(v -> NavigationUtils.navigateTo(this, StorePage.class, true));
        btnPayment.setOnClickListener(v -> NavigationUtils.navigateTo(this, CheckoutPage.class, true));
    }

    private void fetchCart() {
        cartRepository.getUserCart().thenAccept(item -> {
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);

            if (item != null) {
                cartAdapter.setProducts(item.getProducts());
                Log.d("Tuandeptrai: ", item.toString());
                recyclerView.setVisibility(View.VISIBLE);
                btnPayment.setVisibility(View.VISIBLE);
                btnStore.setVisibility(View.GONE);
            } else {
                llEmptyCart.setVisibility(View.VISIBLE);
                btnPayment.setVisibility(View.GONE);
                btnStore.setVisibility(View.VISIBLE);
            }
        }).exceptionally(ex -> {
            Toast.makeText(this, "Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        });
    }
}