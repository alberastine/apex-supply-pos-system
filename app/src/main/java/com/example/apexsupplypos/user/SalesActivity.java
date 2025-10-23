package com.example.apexsupplypos.user;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apexsupplypos.LoginActivity;
import com.example.apexsupplypos.R;
import com.example.apexsupplypos.admin.InventoryManager;
import com.example.apexsupplypos.admin.PricingManager;
import com.example.apexsupplypos.data.TransactionManager;
import com.example.apexsupplypos.models.ItemModel;
import com.example.apexsupplypos.models.SaleItem;
import com.example.apexsupplypos.models.TransactionModel;
import com.example.apexsupplypos.utils.UserSessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SalesActivity extends AppCompatActivity implements SalesProductAdapter.OnProductActionListener {

    private static final String TAG = "ApexPOS_SalesActivity";
    private UserSessionManager sessionManager;
    private InventoryManager inventoryManager;
    private PricingManager pricingManager;
    private TransactionManager transactionManager;
    private CartManager cartManager;
    private TransactionValidator transactionValidator;

    private RecyclerView salesItemRecyclerView;
    private SalesProductAdapter salesProductAdapter;
    private List<ItemModel> availableItems;
    private List<ItemModel> filteredItems;

    private TextInputEditText searchEditText;
    private TextInputEditText customerNameEt;
    private TextView cartSummaryTv;
    private MaterialButton checkoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales);

        sessionManager = new UserSessionManager(this);
        inventoryManager = new InventoryManager(this);
        pricingManager = new PricingManager(this);
        transactionManager = new TransactionManager(this);
        cartManager = new CartManager();
        transactionValidator = new TransactionValidator(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.user_sales_interface);
        }

        salesItemRecyclerView = findViewById(R.id.salesItemRecyclerView);
        salesItemRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize with an empty list for now, will be populated on resume
        salesProductAdapter = new SalesProductAdapter(new ArrayList<>(), cartManager, this);
        salesItemRecyclerView.setAdapter(salesProductAdapter);

        searchEditText = findViewById(R.id.searchEditText);
        customerNameEt = findViewById(R.id.customerNameEt);
        cartSummaryTv = findViewById(R.id.cartSummaryTv);
        checkoutButton = findViewById(R.id.checkoutButton);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        checkoutButton.setOnClickListener(v -> attemptCheckout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAvailableItems();
        updateCartSummary();
        Log.d(TAG, "SalesActivity resumed. Items loaded, cart summary updated.");
    }

    private void loadAvailableItems() {
        availableItems = inventoryManager.getAllItems();
        filteredItems = new ArrayList<>(availableItems); // Start with all items
        salesProductAdapter.updateList(filteredItems);
        Log.d(TAG, "Available inventory items loaded. Total: " + availableItems.size());
    }

    private void filterProducts(String query) {
        filteredItems.clear();
        String lowerCaseQuery = query.toLowerCase();
        for (ItemModel item : availableItems) {
            if (item.getName().toLowerCase().contains(lowerCaseQuery) ||
                    item.getCategory().toLowerCase().contains(lowerCaseQuery) ||
                    (item.getSerialNumber() != null && item.getSerialNumber().toLowerCase().contains(lowerCaseQuery))) {
                filteredItems.add(item);
            }
        }
        salesProductAdapter.updateList(filteredItems);
        Log.d(TAG, "Filtered products for query '" + query + "'. Results: " + filteredItems.size());
    }

    private void updateCartSummary() {
        double subtotal = cartManager.getCartTotalBeforeDiscount();
        double discount = pricingManager.applyDiscounts(cartManager.getCartItems(), inventoryManager);
        double total = subtotal - discount;

        cartSummaryTv.setText(String.format(Locale.getDefault(), "Cart Total: $%.2f (Discount: $%.2f)", total, discount));
        checkoutButton.setEnabled(!cartManager.isEmpty()); // Enable checkout only if cart is not empty
        Log.d(TAG, "Cart summary updated. Subtotal: $" + subtotal + ", Discount: $" + discount + ", Total: $" + total);
    }

    private void attemptCheckout() {
        if (cartManager.isEmpty()) {
            Toast.makeText(this, "Cart is empty, please add items.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Validate Transaction
        if (!transactionValidator.validateCart(cartManager.getCartItems())) {
            Toast.makeText(this, "Validation failed. Check item stock or serial numbers.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Checkout validation failed.");
            return;
        }
        Log.d(TAG, "Cart validation passed.");

        // 2. Calculate Final Amount with discounts
        double subtotal = cartManager.getCartTotalBeforeDiscount();
        double discount = pricingManager.applyDiscounts(cartManager.getCartItems(), inventoryManager);
        double finalAmount = subtotal - discount;

        // 3. Decrement stock
        for (SaleItem saleItem : cartManager.getCartItems()) {
            inventoryManager.decrementItemStock(saleItem.getItemId(), saleItem.getQuantity());
            Log.d(TAG, "Stock decremented for item: " + saleItem.getItemName() + " by " + saleItem.getQuantity());
        }

        // 4. Record Transaction
        String customerName = customerNameEt.getText().toString().trim();
        TransactionModel transaction = new TransactionModel(
                sessionManager.getUsername(),
                cartManager.getCartItems(),
                finalAmount,
                discount,
                customerName.isEmpty() ? null : customerName
        );
        transactionManager.addTransaction(transaction);
        Log.i(TAG, "Transaction recorded: " + transaction.getTransactionId());

        // 5. Generate and Display Receipt
        Gson gson = new Gson();
        String transactionJson = gson.toJson(transaction);
        Intent intent = new Intent(SalesActivity.this, ReceiptActivity.class);
        intent.putExtra(ReceiptActivity.EXTRA_TRANSACTION_JSON, transactionJson);
        startActivity(intent);
        Log.d(TAG, "Launched ReceiptActivity for transaction: " + transaction.getTransactionId());

        // 6. Reset Cart
        cartManager.clearCart();
        customerNameEt.setText("");
        loadAvailableItems(); // Reload items to reflect stock changes
        updateCartSummary();
        Toast.makeText(this, "Checkout successful!", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Checkout process completed and cart reset.");
    }

    @Override
    public void onAddProductToCart(ItemModel item) {
        if (item.getStockQty() > 0) {
            // For serialized items, we can only add 1 at a time initially
            if (item.getSerialNumber() != null && !item.getSerialNumber().isEmpty()) {
                if (cartManager.getCartItems().stream().anyMatch(saleItem -> saleItem.getItemId().equals(item.getId()))) {
                    Toast.makeText(this, "Serialized item already in cart. Cannot add more than one.", Toast.LENGTH_SHORT).show();
                    return;
                }
                cartManager.addItemToCart(item, 1);
            } else {
                // For non-serialized, add one and allow adapter to increase
                int currentCartQty = cartManager.getCartItems().stream()
                        .filter(saleItem -> saleItem.getItemId().equals(item.getId()))
                        .mapToInt(SaleItem::getQuantity)
                        .sum();
                if (item.getStockQty() > currentCartQty) {
                    cartManager.addItemToCart(item, 1);
                } else {
                    Toast.makeText(this, "Maximum stock reached for " + item.getName(), Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Attempted to add more of " + item.getName() + " than available stock.");
                }
            }
            salesProductAdapter.notifyDataSetChanged(); // Refresh quantities displayed in RecyclerView
            updateCartSummary();
            Log.d(TAG, "Added/updated " + item.getName() + " in cart.");
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Item out of stock!", Snackbar.LENGTH_SHORT).show();
            Log.w(TAG, "Attempted to add out-of-stock item: " + item.getName());
        }
    }

    @Override
    public void onRemoveProductFromCart(ItemModel item) {
        cartManager.updateItemQuantity(item.getId(),
                cartManager.getCartItems().stream()
                        .filter(saleItem -> saleItem.getItemId().equals(item.getId()))
                        .mapToInt(SaleItem::getQuantity)
                        .findFirst().orElse(0) - 1);
        salesProductAdapter.notifyDataSetChanged(); // Refresh quantities displayed in RecyclerView
        updateCartSummary();
        Log.d(TAG, "Removed/decremented " + item.getName() + " from cart.");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu); // Reuse the logout menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            sessionManager.logoutUser();
            startActivity(new Intent(SalesActivity.this, LoginActivity.class));
            finish();
            Log.d(TAG, "User logged out.");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
