package com.example.apexsupplypos.user;

import android.util.Log;

import com.example.apexsupplypos.models.ItemModel;
import com.example.apexsupplypos.models.SaleItem;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static final String TAG = "ApexPOS_CartMgr";
    private List<SaleItem> cartItems;

    public CartManager() {
        cartItems = new ArrayList<>();
    }

    public List<SaleItem> getCartItems() {
        return new ArrayList<>(cartItems); // Return a copy to prevent external modification
    }

    public void addItemToCart(ItemModel item, int quantity) {
        // Check if item already exists in cart, then update quantity
        for (SaleItem saleItem : cartItems) {
            if (saleItem.getItemId().equals(item.getId())) {
                saleItem.setQuantity(saleItem.getQuantity() + quantity);
                Log.d(TAG, "Updated quantity for " + item.getName() + " in cart. New quantity: " + saleItem.getQuantity());
                return;
            }
        }
        // If not in cart, add new SaleItem
        SaleItem newSaleItem = new SaleItem(item.getId(), item.getName(), item.getSerialNumber(), quantity, item.getPrice());
        cartItems.add(newSaleItem);
        Log.d(TAG, "Added new item to cart: " + item.getName() + " (Qty: " + quantity + ")");
    }

    public void removeItemFromCart(String itemId) {
        cartItems.removeIf(item -> item.getItemId().equals(itemId));
        Log.d(TAG, "Removed item with ID " + itemId + " from cart.");
    }

    public void updateItemQuantity(String itemId, int newQuantity) {
        for (SaleItem saleItem : cartItems) {
            if (saleItem.getItemId().equals(itemId)) {
                if (newQuantity <= 0) {
                    removeItemFromCart(itemId);
                    Log.d(TAG, "Removed item " + saleItem.getItemName() + " due to quantity <= 0.");
                } else {
                    saleItem.setQuantity(newQuantity);
                    Log.d(TAG, "Updated quantity for " + saleItem.getItemName() + " to " + newQuantity);
                }
                return;
            }
        }
        Log.w(TAG, "Attempted to update quantity for non-existent item in cart: " + itemId);
    }

    public double getCartTotalBeforeDiscount() {
        return cartItems.stream().mapToDouble(SaleItem::getSubtotal).sum();
    }

    public void clearCart() {
        cartItems.clear();
        Log.d(TAG, "Cart cleared.");
    }

    public boolean isEmpty() {
        return cartItems.isEmpty();
    }
}
