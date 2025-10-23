package com.example.apexsupplypos.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apexsupplypos.R;
import com.example.apexsupplypos.models.ItemModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class InventoryFragment extends Fragment implements InventoryAdapter.OnItemActionListener {

    private static final String TAG = "ApexPOS_InvFragment";
    private InventoryManager inventoryManager;
    private RecyclerView inventoryRecyclerView;
    private InventoryAdapter inventoryAdapter;
    private List<ItemModel> currentInventoryList;
    private TextInputEditText searchEditText;
    private FloatingActionButton fabAddItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inventoryManager = new InventoryManager(requireContext());
        currentInventoryList = new ArrayList<>();

        inventoryRecyclerView = view.findViewById(R.id.inventoryRecyclerView);
        inventoryRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        inventoryAdapter = new InventoryAdapter(currentInventoryList, this);
        inventoryRecyclerView.setAdapter(inventoryAdapter);

        searchEditText = view.findViewById(R.id.searchEditText);
        fabAddItem = view.findViewById(R.id.fabAddItem);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterInventory(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        fabAddItem.setOnClickListener(v -> showAddEditItemDialog(null));

        loadInventoryItems();
    }

    private void loadInventoryItems() {
        currentInventoryList.clear();
        currentInventoryList.addAll(inventoryManager.getAllItems());
        inventoryAdapter.notifyDataSetChanged();
        Log.d(TAG, "Inventory items loaded. Total: " + currentInventoryList.size());
        checkStockAlerts();
    }

    private void filterInventory(String query) {
        List<ItemModel> filteredList = currentInventoryList.stream()
                .filter(item -> item.getName().toLowerCase().contains(query.toLowerCase()) ||
                        item.getCategory().toLowerCase().contains(query.toLowerCase()) ||
                        (item.getSerialNumber() != null && item.getSerialNumber().toLowerCase().contains(query.toLowerCase())))
                .collect(Collectors.toList());
        inventoryAdapter.updateList(filteredList);
        Log.d(TAG, "Filtered inventory for query '" + query + "'. Results: " + filteredList.size());
    }

    private void showAddEditItemDialog(@Nullable ItemModel itemToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_item, null);
        builder.setView(dialogView);

        //TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle); // Add a TextView with this ID to dialog_add_edit_item.xml if you want a custom title
        TextInputEditText etItemName = dialogView.findViewById(R.id.etItemName);
        TextInputEditText etSerialNumber = dialogView.findViewById(R.id.etSerialNumber);
        TextInputEditText etStockQuantity = dialogView.findViewById(R.id.etStockQuantity);
        TextInputEditText etPrice = dialogView.findViewById(R.id.etPrice);
        TextInputEditText etCategory = dialogView.findViewById(R.id.etCategory);
        FloatingActionButton btnSaveItem = dialogView.findViewById(R.id.btnSaveItem);

        if (itemToEdit != null) {
            // dialogTitle.setText(R.string.update_item);
            etItemName.setText(itemToEdit.getName());
            etSerialNumber.setText(itemToEdit.getSerialNumber());
            etStockQuantity.setText(String.valueOf(itemToEdit.getStockQty()));
            etPrice.setText(String.format(Locale.getDefault(), "%.2f", itemToEdit.getPrice()));
            etCategory.setText(itemToEdit.getCategory());
            Log.d(TAG, "Populating dialog for editing item: " + itemToEdit.getName());
        } else {
            // dialogTitle.setText(R.string.add_item);
            Log.d(TAG, "Opening dialog for adding new item.");
        }

        AlertDialog dialog = builder.create();

        btnSaveItem.setOnClickListener(v -> {
            String name = etItemName.getText().toString().trim();
            String serial = etSerialNumber.getText().toString().trim();
            String stockQtyStr = etStockQuantity.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String category = etCategory.getText().toString().trim();

            if (name.isEmpty() || stockQtyStr.isEmpty() || priceStr.isEmpty() || category.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int stockQty = Integer.parseInt(stockQtyStr);
                double price = Double.parseDouble(priceStr);

                if (itemToEdit == null) { // Add new item
                    ItemModel newItem = new ItemModel(name, serial.isEmpty() ? null : serial, stockQty, price, category);
                    inventoryManager.addItem(newItem);
                    Toast.makeText(requireContext(), "Item added: " + name, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "New item added: " + newItem.getName());
                } else { // Update existing item
                    itemToEdit.setName(name);
                    itemToEdit.setSerialNumber(serial.isEmpty() ? null : serial);
                    itemToEdit.setStockQty(stockQty);
                    itemToEdit.setPrice(price);
                    itemToEdit.setCategory(category);
                    inventoryManager.updateItem(itemToEdit);
                    Toast.makeText(requireContext(), "Item updated: " + name, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Item updated: " + itemToEdit.getName());
                }
                loadInventoryItems(); // Refresh list
                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid number format for stock or price", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Number format exception in add/edit item dialog: " + e.getMessage());
            }
        });
        dialog.show();
    }

    @Override
    public void onEditItem(ItemModel item) {
        showAddEditItemDialog(item);
    }

    @Override
    public void onDeleteItem(ItemModel item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete '" + item.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    inventoryManager.deleteItem(item.getId());
                    loadInventoryItems();
                    Toast.makeText(requireContext(), item.getName() + " deleted.", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Item deleted: " + item.getName());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void checkStockAlerts() {
        int ALERT_THRESHOLD = 5; // Example threshold
        for (ItemModel item : currentInventoryList) {
            if (item.getStockQty() <= ALERT_THRESHOLD) {
                Snackbar.make(requireView(), "Low stock alert: " + item.getName() + " (Qty: " + item.getStockQty() + ")", Snackbar.LENGTH_LONG)
                        .setAction("View", v -> showAddEditItemDialog(item))
                        .show();
                Log.w(TAG, "Low stock alert for " + item.getName() + " (Qty: " + item.getStockQty() + ")");
            }
        }
    }
}
