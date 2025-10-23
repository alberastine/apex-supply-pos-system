package com.example.apexsupplypos.user;

import android.content.Context;
import android.util.Log;

import com.example.apexsupplypos.admin.InventoryManager;
import com.example.apexsupplypos.models.ItemModel;
import com.example.apexsupplypos.models.SaleItem;

import java.util.List;

public class TransactionValidator {
    private static final String TAG = "ApexPOS_TransValidator";
    private InventoryManager inventoryManager;

    public TransactionValidator(Context context) {
        inventoryManager = new InventoryManager(context);
    }

    /**
     * Validates if items in the cart meet the necessary criteria (stock, serial numbers).
     * @param cartItems The list of items in the current cart.
     * @return true if all items are valid for checkout, false otherwise.
     */
    public boolean validateCart(List<SaleItem> cartItems) {
        for (SaleItem saleItem : cartItems) {
            ItemModel originalItem = inventoryManager.getItemById(saleItem.getItemId());

            if (originalItem == null) {
                Log.e(TAG, "Validation failed: Item not found in inventory for ID: " + saleItem.getItemId());
                return false;
            }

            // Check stock quantity
            if (originalItem.getStockQty() < saleItem.getQuantity()) {
                Log.e(TAG, "Validation failed: Insufficient stock for " + originalItem.getName() +
                        ". Requested: " + saleItem.getQuantity() + ", Available: " + originalItem.getStockQty());
                return false;
            }

            // Check serial number uniqueness for serialized items
            if (originalItem.getSerialNumber() != null && !originalItem.getSerialNumber().isEmpty()) {
                // If it's a serialized item, its serial number must match the one in saleItem
                // and for a sale, it means this specific serialized item is being sold.
                // We assume `originalItem.getSerialNumber()` holds the serial number for a single serialized item.
                // If the system allows selling multiple of the *same* serialized item, this logic would need adjustment.
                // For now, let's assume one serialized item means one unique serial, so quantity should be 1.
                if (saleItem.getQuantity() > 1) {
                    Log.e(TAG, "Validation failed: Cannot sell more than one of serialized item " + originalItem.getName() + " (" + originalItem.getSerialNumber() + ") at once.");
                    return false;
                }
                if (!originalItem.getSerialNumber().equals(saleItem.getItemSerialNumber())) {
                    Log.e(TAG, "Validation failed: Serial number mismatch for " + originalItem.getName() +
                            ". Expected: " + originalItem.getSerialNumber() + ", Got: " + saleItem.getItemSerialNumber());
                    return false;
                }
                // Also ensure this serial number is actually in stock (exists)
                if (inventoryManager.getItemBySerialNumber(originalItem.getSerialNumber()) == null) {
                    Log.e(TAG, "Validation failed: Serialized item " + originalItem.getSerialNumber() + " not found in active inventory.");
                    return false;
                }
            }
        }
        Log.d(TAG, "Cart validation successful.");
        return true;
    }
}
