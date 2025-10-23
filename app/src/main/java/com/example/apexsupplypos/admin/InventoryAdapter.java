package com.example.apexsupplypos.admin;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apexsupplypos.R;
import com.example.apexsupplypos.models.ItemModel;

import java.util.List;
import java.util.Locale;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ItemViewHolder> {

    private List<ItemModel> items;
    private OnItemActionListener listener;

    public InventoryAdapter(List<ItemModel> items, OnItemActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public interface OnItemActionListener {
        void onEditItem(ItemModel item);
        void onDeleteItem(ItemModel item);
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ItemModel item = items.get(position);
        holder.itemName.setText(item.getName());
        holder.itemCategory.setText(String.format("Category: %s", item.getCategory()));
        holder.itemPrice.setText(String.format(Locale.getDefault(), "Price: $%.2f", item.getPrice()));
        holder.itemStock.setText(String.format(Locale.getDefault(), "Stock: %d units", item.getStockQty()));

        if (item.getSerialNumber() != null && !item.getSerialNumber().isEmpty()) {
            holder.itemSerialNumber.setText(String.format("S/N: %s", item.getSerialNumber()));
            holder.itemSerialNumber.setVisibility(View.VISIBLE);
        } else {
            holder.itemSerialNumber.setVisibility(View.GONE);
        }

        if (item.getStockQty() <= 5) { // Low stock alert visual cue
            holder.itemStock.setTextColor(Color.RED);
        } else {
            holder.itemStock.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.dark_gray));
        }

        holder.editButton.setOnClickListener(v -> listener.onEditItem(item));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteItem(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateList(List<ItemModel> newList) {
        items = newList;
        notifyDataSetChanged();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemCategory, itemPrice, itemStock, itemSerialNumber;
        Button editButton, deleteButton;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            itemCategory = itemView.findViewById(R.id.itemCategory);
            itemPrice = itemView.findViewById(R.id.itemPrice);
            itemStock = itemView.findViewById(R.id.itemStock);
            itemSerialNumber = itemView.findViewById(R.id.itemSerialNumber);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
