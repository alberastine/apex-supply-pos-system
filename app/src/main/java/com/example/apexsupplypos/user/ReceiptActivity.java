package com.example.apexsupplypos.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.apexsupplypos.R;
import com.example.apexsupplypos.models.SaleItem;
import com.example.apexsupplypos.models.TransactionModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReceiptActivity extends AppCompatActivity {

    public static final String EXTRA_TRANSACTION_JSON = "extra_transaction_json";
    private static final String TAG = "ApexPOS_Receipt";
    private TextView receiptContentTv;
    private Button shareReceiptButton;
    private TransactionModel currentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Receipt");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        receiptContentTv = findViewById(R.id.receiptContentTv);
        shareReceiptButton = findViewById(R.id.shareReceiptButton);

        String transactionJson = getIntent().getStringExtra(EXTRA_TRANSACTION_JSON);
        if (transactionJson != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<TransactionModel>() {}.getType();
            currentTransaction = gson.fromJson(transactionJson, type);
            if (currentTransaction != null) {
                displayReceipt(currentTransaction);
            } else {
                Log.e(TAG, "Failed to parse transaction JSON.");
                receiptContentTv.setText("Error: Could not load receipt details.");
            }
        } else {
            Log.e(TAG, "No transaction JSON provided to ReceiptActivity.");
            receiptContentTv.setText("Error: No transaction data to display.");
        }

        shareReceiptButton.setOnClickListener(v -> shareReceipt());
    }

    private void displayReceipt(TransactionModel transaction) {
        StringBuilder receiptBuilder = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        receiptBuilder.append("<html><body>");
        receiptBuilder.append("<h1>Apex Supply QuickServe POS</h1>");
        receiptBuilder.append("<h2>Sales Receipt</h2>");
        receiptBuilder.append("<p><strong>Transaction ID:</strong> ").append(transaction.getTransactionId()).append("</p>");
        receiptBuilder.append("<p><strong>Date:</strong> ").append(sdf.format(new Date(transaction.getTimestamp()))).append("</p>");
        receiptBuilder.append("<p><strong>Served By:</strong> ").append(transaction.getUserId()).append("</p>");
        if (transaction.getCustomerName() != null && !transaction.getCustomerName().isEmpty()) {
            receiptBuilder.append("<p><strong>Customer:</strong> ").append(transaction.getCustomerName()).append("</p>");
        }
        receiptBuilder.append("<br>");
        receiptBuilder.append("<h3>Items:</h3>");
        receiptBuilder.append("<table border=\"0\" cellpadding=\"2\" cellspacing=\"0\" width=\"100%\">");
        receiptBuilder.append("<tr><th>Item</th><th>Qty</th><th>Unit Price</th><th>Subtotal</th></tr>");

        double subtotalBeforeDiscount = 0;
        for (SaleItem item : transaction.getItemsSold()) {
            receiptBuilder.append("<tr>");
            receiptBuilder.append("<td>").append(item.getItemName());
            if (item.getItemSerialNumber() != null && !item.getItemSerialNumber().isEmpty()) {
                receiptBuilder.append(" (S/N: ").append(item.getItemSerialNumber()).append(")");
            }
            receiptBuilder.append("</td>");
            receiptBuilder.append("<td align=\"center\">").append(item.getQuantity()).append("</td>");
            receiptBuilder.append("<td align=\"right\">$").append(String.format(Locale.getDefault(), "%.2f", item.getUnitPrice())).append("</td>");
            receiptBuilder.append("<td align=\"right\">$").append(String.format(Locale.getDefault(), "%.2f", item.getSubtotal())).append("</td>");
            receiptBuilder.append("</tr>");
            subtotalBeforeDiscount += (item.getQuantity() * item.getUnitPrice()); // Calculate original subtotal
        }
        receiptBuilder.append("</table>");
        receiptBuilder.append("<br>");

        receiptBuilder.append("<p>-----------------------------------</p>");
        receiptBuilder.append("<p><strong>Subtotal:</strong> <span style='float:right;'>$").append(String.format(Locale.getDefault(), "%.2f", subtotalBeforeDiscount)).append("</span></p>");
        if (transaction.getDiscountApplied() > 0) {
            receiptBuilder.append("<p><strong>Discount:</strong> <span style='float:right;'>-$").append(String.format(Locale.getDefault(), "%.2f", transaction.getDiscountApplied())).append("</span></p>");
        }
        receiptBuilder.append("<p><strong>Total Amount:</strong> <span style='float:right;'><b>$").append(String.format(Locale.getDefault(), "%.2f", transaction.getTotalAmount())).append("</b></span></p>");
        receiptBuilder.append("<p>-----------------------------------</p>");
        receiptBuilder.append("<p>Thank you for your business!</p>");
        receiptBuilder.append("</body></html>");

        Spanned receiptHtml;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            receiptHtml = Html.fromHtml(receiptBuilder.toString(), Html.FROM_HTML_MODE_LEGACY);
        } else {
            receiptHtml = Html.fromHtml(receiptBuilder.toString());
        }
        receiptContentTv.setText(receiptHtml);
        Log.d(TAG, "Receipt displayed for transaction ID: " + transaction.getTransactionId());
    }

    private void shareReceipt() {
        if (currentTransaction == null) {
            Toast.makeText(this, "No receipt to share.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate plain text version for sharing
        StringBuilder shareTextBuilder = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        shareTextBuilder.append("--- Apex Supply QuickServe POS Receipt ---\n");
        shareTextBuilder.append("Transaction ID: ").append(currentTransaction.getTransactionId()).append("\n");
        shareTextBuilder.append("Date: ").append(sdf.format(new Date(currentTransaction.getTimestamp()))).append("\n");
        shareTextBuilder.append("Served By: ").append(currentTransaction.getUserId()).append("\n");
        if (currentTransaction.getCustomerName() != null && !currentTransaction.getCustomerName().isEmpty()) {
            shareTextBuilder.append("Customer: ").append(currentTransaction.getCustomerName()).append("\n");
        }
        shareTextBuilder.append("\nItems:\n");
        double subtotalBeforeDiscount = 0;
        for (SaleItem item : currentTransaction.getItemsSold()) {
            shareTextBuilder.append(String.format(Locale.getDefault(),
                    "%s (x%d) @ $%.2f = $%.2f\n",
                    item.getItemName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal()));
            subtotalBeforeDiscount += (item.getQuantity() * item.getUnitPrice());
        }
        shareTextBuilder.append("\n-----------------------------------\n");
        shareTextBuilder.append(String.format(Locale.getDefault(), "Subtotal: $%.2f\n", subtotalBeforeDiscount));
        if (currentTransaction.getDiscountApplied() > 0) {
            shareTextBuilder.append(String.format(Locale.getDefault(), "Discount: -$%.2f\n", currentTransaction.getDiscountApplied()));
        }
        shareTextBuilder.append(String.format(Locale.getDefault(), "Total Amount: $%.2f\n", currentTransaction.getTotalAmount()));
        shareTextBuilder.append("-----------------------------------\n");
        shareTextBuilder.append("Thank you for your business!\n");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareTextBuilder.toString());
        startActivity(Intent.createChooser(shareIntent, "Share Receipt Via"));
        Log.d(TAG, "Share intent initiated for transaction ID: " + currentTransaction.getTransactionId());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
