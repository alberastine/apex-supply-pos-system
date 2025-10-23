package com.example.apexsupplypos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.apexsupplypos.admin.AdminDashboardActivity;
import com.example.apexsupplypos.admin.InventoryManager;
import com.example.apexsupplypos.admin.PricingManager;
import com.example.apexsupplypos.data.SharedPrefsManager;
import com.example.apexsupplypos.models.DiscountRule;
import com.example.apexsupplypos.models.ItemModel;
import com.example.apexsupplypos.user.SalesActivity;
import com.example.apexsupplypos.utils.UserSessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "ApexPOS_Login";
    private TextInputEditText usernameEditText, passwordEditText;
    private Button loginButton;
    private UserSessionManager sessionManager;
    private SharedPrefsManager sharedPrefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new UserSessionManager(this);
        sharedPrefsManager = new SharedPrefsManager(this);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            Log.d(TAG, "User already logged in. Redirecting...");
            redirectToAppropriateActivity();
        } else {
            // Initialize default data if not already set (first launch)
            initializeDefaultData();
        }

        loginButton.setOnClickListener(v -> attemptLogin());
        Button registerRedirectButton = findViewById(R.id.registerRedirectButton);
        registerRedirectButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void initializeDefaultData() {
        // Use a flag to ensure default data is added only once
        if (!sharedPrefsManager.contains("initial_setup_done")) {
            Log.d(TAG, "Performing initial setup: Adding default items and discount rules.");

            // Add default inventory items
            List<ItemModel> defaultItems = new ArrayList<>();
            defaultItems.add(new ItemModel("Hydraulic Excavator", null, 9, 120000.0, "Excavating"));
            defaultItems.add(new ItemModel("Mining Dump Truck", null, 50, 85000.0, "Hauling"));
            defaultItems.add(new ItemModel("Rock Drill", "SN-DRILL-001", 1, 30000.0, "Drilling"));
            defaultItems.add(new ItemModel("Bulldozer D9", "SN-BULL-089", 0, 350000.0, "Excavating"));
            defaultItems.add(new ItemModel("Heavy-Duty Shovel", null, 200, 150.0, "Tools"));
            defaultItems.add(new ItemModel("Safety Helmet", null, 30, 45.0, "Accessories"));

            // Save items using InventoryManager (instantiate temporarily)
            new InventoryManager(this).saveAllItems(defaultItems);
            Log.d(TAG, "Default inventory items added.");


            // Add default discount rules
            List<DiscountRule> defaultRules = new ArrayList<>();
            defaultRules.add(new DiscountRule("Excavating Equipment 5% Off (Qty 2+)", "Excavating", 2, 0.05, 0.0, true));
            defaultRules.add(new DiscountRule("Hauling Equipment $10,000 Off (Qty 3+)", "Hauling", 3, 0.0, 10000.0, true));
            defaultRules.add(new DiscountRule("Bulk Drilling Tools 20% Off (Qty 10+)", "Drilling", 10, 0.20, 0.0, true));

            // Save rules using PricingManager (instantiate temporarily)
            new PricingManager(this).saveAllDiscountRules(defaultRules);
            Log.d(TAG, "Default discount rules added.");

            // Mark initial setup as done
            sharedPrefsManager.saveData("initial_setup_done", "true");
            Log.d(TAG, "Initial setup completed and marked.");
        } else {
            Log.d(TAG, "Initial setup already performed. Skipping default data initialization.");
        }
    }

    private void attemptLogin() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hardcoded Admin Login
        if (username.equals("admin") && password.equals("adminpass")) {
            sessionManager.createLoginSession(username, UserSessionManager.USER_TYPE_ADMIN);
            Toast.makeText(this, "Admin Login Successful", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Admin logged in.");
            startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
            finish();
            return;
        }

        // Check if user exists in SharedPreferences
        String savedPassword = sharedPrefsManager.getData("user_" + username);
        if (savedPassword != null && savedPassword.equals(password)) {
            sessionManager.createLoginSession(username, UserSessionManager.USER_TYPE_USER);
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "User logged in: " + username);
            startActivity(new Intent(LoginActivity.this, SalesActivity.class));
            finish();
            return;
        }

        // Invalid credentials
        Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        Log.w(TAG, "Login failed for username: " + username);
    }

    private void redirectToAppropriateActivity() {
        if (sessionManager.getUserType().equals(UserSessionManager.USER_TYPE_ADMIN)) {
            startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
        } else {
            startActivity(new Intent(LoginActivity.this, SalesActivity.class));
        }
        finish();
    }
}
