package com.ec337.facescanpayment.features.store.presentation.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ec337.facescanpayment.features.cart.data.entity.CartEntity;
import com.ec337.facescanpayment.features.cart.data.repository.CartRepository;
import com.ec337.facescanpayment.features.store.data.entity.ProductEntity;
import com.ec337.facescanpayment.R;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {
    private List<ProductEntity> products;
    private CartUpdateListener cartUpdateListener;
    private CartRepository cartRepository;

    public StoreAdapter(Context context) {
        this.cartRepository = new CartRepository(context);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setProducts(List<ProductEntity> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    public void setCartUpdateListener(CartUpdateListener listener) {
        this.cartUpdateListener = listener;
    }

    public CompletableFuture<Integer> getCartTotalQuantity() {
        return cartRepository.getUserCart()
                .thenApply(CartEntity::getTotalQuantity)
                .exceptionally(ex -> {
                    Log.e("StoreAdapter", "Failed to get cart total quantity: " + ex.getMessage());
                    return 0;
                });
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item__store_item, parent, false);
        return new StoreViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(StoreViewHolder holder, int position) {
        ProductEntity product = products.get(position);

        holder.tvProductName.setText(product.getName());
        holder.tvProductPrice.setText(product.getPrice() + "đ");
        holder.tvProductQuantity.setText("SL: " + product.getQuantity());

        String productImage = product.getImage();
        if (productImage != null && !productImage.isEmpty()) {
            Picasso.get()
                    .load(productImage)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.logo_techo_without_text);
        }

        holder.btnAddProduct.setOnClickListener(v -> {
            String productId = product.getId();

            if (cartUpdateListener != null) {
                cartUpdateListener.onCartUpdated(true); // Tăng badge
            }

            cartRepository.addProductToCart(productId, 1).thenAccept(cartEntity -> {
                Log.d("StoreAdapter", "Product added to cart successfully.");
            }).exceptionally(ex -> {
                Log.e("StoreAdapter", "Failed to add product: " + ex.getMessage());
                if (cartUpdateListener != null) {
                    cartUpdateListener.onCartUpdated(false);
                }
                return null;
            });
        });
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    public static class StoreViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName, tvProductPrice, tvProductQuantity;
        ImageButton btnAddProduct;

        public StoreViewHolder(View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);
            btnAddProduct = itemView.findViewById(R.id.btnAddProduct);
        }
    }

    public interface CartUpdateListener {
        void onCartUpdated(boolean increment);
    }
}
