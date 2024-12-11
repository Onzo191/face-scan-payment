package com.ec337.facescanpayment.features.cart.presentation.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ec337.facescanpayment.R;
import com.ec337.facescanpayment.features.cart.data.entity.CartEntity.CartProduct;
import com.ec337.facescanpayment.features.cart.data.repository.CartRepository;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private final Context context;
    private List<CartProduct> cartItems;
    private CartRepository cartRepository;
    private boolean isProcessing = false;

    public CartAdapter(Context context) {
        this.context = context;
        this.cartRepository = new CartRepository(context);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setProducts(List<CartProduct> cartItems) {
        this.cartItems = cartItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item__cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartProduct item = cartItems.get(position);

        String productImage = item.getImage();
        if (productImage != null && !productImage.isEmpty()) {
            Picasso.get()
                    .load(productImage)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.logo_techo_without_text);
        }

        holder.tvProductName.setText(item.getName());
        holder.tvProductPrice.setText(String.valueOf(item.getPrice()));
        holder.quantityEditText.setText(String.valueOf(item.getQuantity()));

        // Disable buttons if processing
        holder.quantityEditText.setEnabled(!isProcessing);
        holder.btnIncreaseQuantity.setEnabled(!isProcessing);
        holder.btnDecreaseQuantity.setEnabled(!isProcessing);

        // Handle quantity increase
        holder.btnIncreaseQuantity.setOnClickListener(v -> {
            if (!isProcessing) {
                isProcessing = true;
                updateButtonState(holder, false);

                cartRepository.addProductToCart(item.getId(), 1).thenAccept(updatedCart -> {
                    item.setQuantity(item.getQuantity() + 1);
                    notifyItemChanged(position);
                }).exceptionally(ex -> {
                    Toast.makeText(context, "Failed to update cart: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    return null;
                }).whenComplete((result, throwable) -> {
                    isProcessing = false;
                    updateButtonState(holder, true);
                });
            }
        });

        // Handle quantity decrease
        holder.btnDecreaseQuantity.setOnClickListener(v -> {
            if (!isProcessing && item.getQuantity() > 1) {
                isProcessing = true;
                updateButtonState(holder, false);

                cartRepository.removeProductFromCart(item.getId(), 1).thenAccept(updatedCart -> {
                    item.setQuantity(item.getQuantity() - 1);
                    notifyItemChanged(position);
                }).exceptionally(ex -> {
                    Toast.makeText(context, "Failed to update cart: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    return null;
                }).whenComplete((result, throwable) -> {
                    isProcessing = false;
                    updateButtonState(holder, true);
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    private void updateButtonState(CartViewHolder holder, boolean isEnabled) {
        holder.quantityEditText.setEnabled(isEnabled);
        holder.btnIncreaseQuantity.setEnabled(isEnabled);
        holder.btnDecreaseQuantity.setEnabled(isEnabled);

        int color = isEnabled ? context.getColor(R.color.primary) : context.getColor(R.color.grey2);
        holder.quantityEditText.setBackgroundTintList(ColorStateList.valueOf(color));
        holder.btnIncreaseQuantity.setBackgroundTintList(ColorStateList.valueOf(color));
        holder.btnDecreaseQuantity.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName, tvProductPrice;
        EditText quantityEditText;
        ImageButton btnIncreaseQuantity, btnDecreaseQuantity;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            quantityEditText = itemView.findViewById(R.id.etQuantity);
            btnIncreaseQuantity = itemView.findViewById(R.id.btnIncreaseQuantity);
            btnDecreaseQuantity = itemView.findViewById(R.id.btnDecreaseQuantity);
        }
    }
}