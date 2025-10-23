package com.example.apexsupplypos.admin;

import android.content.Context;
import android.util.Log;

import com.example.apexsupplypos.data.SharedPrefsManager;
import com.example.apexsupplypos.models.ItemModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class InventoryManager {
    private static final String INVENTORY_KEY = "inventory_items";
    private static final String TAG = "ApexPOS_InventoryMgr";
    private SharedPrefsManager sharedPrefsManager;
    private Gson gson;

    public InventoryManager(Context context) {
        sharedPrefsManager = new SharedPrefsManager(context);
        gson = new Gson();
    }

    public List<ItemModel> getAllItems() {
        String json = sharedPrefsManager.getData(INVENTORY_KEY);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<ItemModel>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void saveAllItems(List<ItemModel> items) {
        String json = gson.toJson(items);
        sharedPrefsManager.saveData(INVENTORY_KEY, json);
        Log.d(TAG, "All inventory items saved. Total: " + items.size());
    }

    public void addItem(ItemModel item) {
        List<ItemModel> items = getAllItems();
        items.add(item);
        saveAllItems(items);
        Log.d(TAG, "Added new item: " + item.getName());
    }

    public void updateItem(ItemModel updatedItem) {
        List<ItemModel> items = getAllItems();
        boolean found = false;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(updatedItem.getId())) {
                items.set(i, updatedItem);
                found = true;
                break;
            }
        }
        if (found) {
            saveAllItems(items);
            Log.d(TAG, "Updated item: " + updatedItem.getName());
        } else {
            Log.w(TAG, "Attempted to update non-existent item: " + updatedItem.getId());
        }
    }

    public void deleteItem(String itemId) {
        List<ItemModel> items = getAllItems();
        List<ItemModel> updatedList = items.stream()
                .filter(item -> !item.getId().equals(itemId))
                .collect(Collectors.toList());
        if (updatedList.size() < items.size()) { // Item was actually removed
            saveAllItems(updatedList);
            Log.d(TAG, "Deleted item with ID: " + itemId);
        } else {
            Log.w(TAG, "Attempted to delete non-existent item with ID: " + itemId);
        }
    }

    public ItemModel getItemById(String id) {
        return getAllItems().stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public ItemModel getItemBySerialNumber(String serialNumber) {
        return getAllItems().stream()
                .filter(item -> serialNumber.equals(item.getSerialNumber()))
                .findFirst()
                .orElse(null);
    }

    public void decrementItemStock(String itemId, int quantity) {
        ItemModel item = getItemById(itemId);
        if (item != null && item.getStockQty() >= quantity) {
            item.setStockQty(item.getStockQty() - quantity);
            updateItem(item);
            Log.d(TAG, "Decremented stock for " + item.getName() + " by " + quantity);
        } else if (item != null) {
            Log.e(TAG, "Insufficient stock for " + item.getName() + ". Requested: " + quantity + ", Available: " + item.getStockQty());
        } else {
            Log.e(TAG, "Attempted to decrement stock for non-existent item ID: " + itemId);
        }
    }

    public void incrementItemStock(String itemId, int quantity) {
        ItemModel item = getItemById(itemId);
        if (item != null) {
            item.setStockQty(item.getStockQty() + quantity);
            updateItem(item);
            Log.d(TAG, "Incremented stock for " + item.getName() + " by " + quantity);
        } else {
            Log.e(TAG, "Attempted to increment stock for non-existent item ID: " + itemId);
        }
    }

    public boolean isSerialNumberAvailable(String serialNumber) {
        if (serialNumber == null || serialNumber.isEmpty()) {
            return true; // Non-serialized items don't need serial check
        }
        return getAllItems().stream().noneMatch(item ->
                serialNumber.equals(item.getSerialNumber()));
    }
}
