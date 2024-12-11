package com.ec337.facescanpayment.features.cart.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.ec337.facescanpayment.features.cart.data.entity.CartEntity;
import com.ec337.facescanpayment.features.cart.data.repository.CartRepository;
import com.ec337.facescanpayment.features.cart.presentation.adapter.CartAdapter;
import com.ec337.facescanpayment.features.store.data.repository.StoreRepository;
import com.ec337.facescanpayment.features.store.presentation.StorePage;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.List;

public class CartPage extends AppCompatActivity {
    private RecyclerView recyclerView;
    private LinearLayout llEmptyCart, llTotal;
    private Button btnStore, btnPayment;
    private ImageButton btnBack;
    private CartRepository cartRepository;
    private CartAdapter cartAdapter;
    private TextView tvTotal;
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
        llTotal = findViewById(R.id.llTotal);
        tvTotal = findViewById(R.id.tvTotalPrice);
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
        llTotal.setVisibility(View.GONE);
        btnStore.setVisibility(View.VISIBLE);
        shimmerFrameLayout.setVisibility(View.VISIBLE);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> NavigationUtils.navigateTo(this, StorePage.class, true));
        btnStore.setOnClickListener(v -> NavigationUtils.navigateTo(this, StorePage.class, true));
        btnPayment.setOnClickListener(v -> {
            List<CartEntity.CartProduct> cartProducts = cartAdapter.getProducts();
            String productsJson = CartEntity.productsToJson(cartProducts);
            int total = cartAdapter.getCartTotal();

            Intent intent = new Intent(this, CheckoutPage.class);
            intent.putExtra("cart_products", productsJson);
            intent.putExtra("total_price", total);
            startActivity(intent);
        });
    }

    private void updateTotal() {
        int total = cartAdapter.getCartTotal();
        String formattedTotal = String.format("%,d", total);
        tvTotal.setText(formattedTotal);
    }

    private void fetchCart() {
        cartRepository.getUserCart().thenAccept(item -> {
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);

            if (item != null && item.getProducts() != null && !item.getProducts().isEmpty()) {
                cartAdapter.setProducts(item.getProducts());
                cartAdapter.setCartUpdateListener(new CartAdapter.CartUpdateListener() {
                    @Override
                    public void onCartUpdated(boolean increment) {
                        updateTotal();
                    }
                });

                int total = item.getTotal();
                String formattedTotal = String.format("%,d", total);
                tvTotal.setText(formattedTotal);

                llTotal.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
                btnPayment.setVisibility(View.VISIBLE);
                btnStore.setVisibility(View.GONE);
            } else {
                llEmptyCart.setVisibility(View.VISIBLE);
                btnPayment.setVisibility(View.GONE);
                btnStore.setVisibility(View.VISIBLE);
            }
        }).exceptionally(ex -> {
            Log.e("CartPage", "Error fetching cart: " + ex.getMessage());
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);

            llEmptyCart.setVisibility(View.VISIBLE);
            btnPayment.setVisibility(View.GONE);
            btnStore.setVisibility(View.VISIBLE);
            return null;
        });
    }
}