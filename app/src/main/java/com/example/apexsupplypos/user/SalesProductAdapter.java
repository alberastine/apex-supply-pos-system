package com.example.apexsupplypos.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apexsupplypos.R;
import com.example.apexsupplypos.models.ItemModel;
import com.example.apexsupplypos.models.SaleItem;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class SalesProductAdapter extends RecyclerView.Adapter<SalesProductAdapter.ProductViewHolder> {

    private List<ItemModel> products;
    private CartManager cartManager;
    private OnProductActionListener listener;

    public SalesProductAdapter(List<ItemModel> products, CartManager cartManager, OnProductActionListener listener) {
        this.products = products;
        this.cartManager = cartManager;
        this.listener = listener;
    }

    public interface OnProductActionListener {
        void onAddProductToCart(ItemModel item);
        void onRemoveProductFromCart(ItemModel item);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sales_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ItemModel product = products.get(position);
        holder.productNameTv.setText(String.format(Locale.getDefault(), "%s (%s)", product.getName(), product.getCategory()));
        holder.productPriceTv.setText(String.format(Locale.getDefault(), "$%.2f", product.getPrice()));
        holder.productStockTv.setText(String.format(Locale.getDefault(), "Stock: %d", product.getStockQty()));

        // Get current quantity from cart
        int currentQuantityInCart = cartManager.getCartItems().stream()
                .filter(saleItem -> saleItem.getItemId().equals(product.getId()))
                .mapToInt(SaleItem::getQuantity)
                .sum();

        holder.quantityTv.setText(String.valueOf(currentQuantityInCart));

        holder.addOneButton.setOnClickListener(v -> listener.onAddProductToCart(product));
        holder.removeOneButton.setOnClickListener(v -> {
            if (currentQuantityInCart > 0) {
                listener.onRemoveProductFromCart(product);
            }
        });

        // Disable add button if out of stock or if it's a serialized item and already in cart
        if (product.getStockQty() == 0 ||
                (product.getSerialNumber() != null && !product.getSerialNumber().isEmpty() && currentQuantityInCart > 0) ||
                (product.getStockQty() <= currentQuantityInCart && (product.getSerialNumber() == null || product.getSerialNumber().isEmpty()))) {
            holder.addOneButton.setEnabled(false);
        } else {
            holder.addOneButton.setEnabled(true);
        }

        // Disable remove button if not in cart
        holder.removeOneButton.setEnabled(currentQuantityInCart > 0);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateList(List<ItemModel> newList) {
        products = newList;
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productNameTv, productPriceTv, productStockTv, quantityTv;
        MaterialButton addOneButton, removeOneButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTv = itemView.findViewById(R.id.productNameTv);
            productPriceTv = itemView.findViewById(R.id.productPriceTv);
            productStockTv = itemView.findViewById(R.id.productStockTv);
            quantityTv = itemView.findViewById(R.id.quantityTv);
            addOneButton = itemView.findViewById(R.id.addOneButton);
            removeOneButton = itemView.findViewById(R.id.removeOneButton);
        }
    }
}
