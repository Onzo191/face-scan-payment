package com.ec337.facescanpayment.features.store.presentation;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ec337.facescanpayment.MainActivity;
import com.ec337.facescanpayment.R;
import com.ec337.facescanpayment.core.utils.NavigationUtils;
import com.ec337.facescanpayment.features.cart.CartPage;
import com.ec337.facescanpayment.features.store.data.repository.StoreRepository;
import com.ec337.facescanpayment.features.store.presentation.adapter.StoreAdapter;
import com.facebook.shimmer.ShimmerFrameLayout;

public class StorePage extends AppCompatActivity {

    private RecyclerView recyclerView;
    ImageButton btnBack, btnCart;
    private TextView badge;
    private StoreAdapter storeAdapter;
    private StoreRepository storeRepository;
    private ShimmerFrameLayout shimmerFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
    }

    private void init() {
        initInsets();

        //store
        storeRepository = new StoreRepository(this);

        initViews();
        fetchProducts();
    }

    private void initInsets() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.app__store_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        //map components
        shimmerFrameLayout = findViewById(R.id.shimmerContainer);
        recyclerView = findViewById(R.id.rvProductList);
        btnBack = findViewById(R.id.btnBack);
        btnCart = findViewById(R.id.btnCart);
        badge = findViewById(R.id.badge);
        setupClickListener();

        //adapter - recycler views
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        storeAdapter = new StoreAdapter();
        storeAdapter.setCartUpdateListener(this::updateCartBadege);
        recyclerView.setAdapter(storeAdapter);

        //visibility
        recyclerView.setVisibility(View.GONE);
        shimmerFrameLayout.setVisibility(View.VISIBLE);
    }

    private void setupClickListener() {
        btnBack.setOnClickListener(v -> NavigationUtils.navigateTo(this, MainActivity.class, true));
        btnCart.setOnClickListener(v -> NavigationUtils.navigateTo(this, CartPage.class, true));
    }

    private void updateCartBadege() {
        int cartSize = 1; //gonna fetch

        if (cartSize > 0) {
            badge.setVisibility(View.VISIBLE);
            badge.setText(String.valueOf(cartSize));
        } else {
            badge.setVisibility(View.GONE);
        }
    }

    private void fetchProducts() {
        shimmerFrameLayout.startShimmer();
        storeRepository.getAllProducts().thenAccept(products -> {
            if (products != null) {
                storeAdapter.setProducts(products);
                Log.d("Tuandeptrai: ", products.toString());
            }

            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }).exceptionally(ex -> {
            Toast.makeText(StorePage.this, "Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            return null;
        });
    }
}