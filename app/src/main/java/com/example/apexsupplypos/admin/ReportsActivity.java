package com.example.apexsupplypos.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.apexsupplypos.R;
import com.example.apexsupplypos.data.TransactionManager;
import com.example.apexsupplypos.models.SaleItem;
import com.example.apexsupplypos.models.TransactionModel;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ReportsActivity extends AppCompatActivity {

    private static final String TAG = "ApexPOS_Reports";
    private TransactionManager transactionManager;
    private TextView dailyReportTv, weeklyReportTv, totalSalesTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Sales Reports");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        transactionManager = new TransactionManager(this);
        dailyReportTv = findViewById(R.id.dailyReportTv);
        weeklyReportTv = findViewById(R.id.weeklyReportTv);
        totalSalesTv = findViewById(R.id.totalSalesTv);

        generateReports();
    }

    private void generateReports() {
        List<TransactionModel> allTransactions = transactionManager.getAllTransactions();
        Log.d(TAG, "Total transactions retrieved: " + allTransactions.size());

        double totalSales = allTransactions.stream()
                .mapToDouble(TransactionModel::getTotalAmount)
                .sum();
        totalSalesTv.setText(String.format(Locale.getDefault(), "Overall Total Sales: $%.2f", totalSales));

        // Daily Report
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        long startOfDayMillis = today.getTimeInMillis();

        double dailySales = allTransactions.stream()
                .filter(t -> t.getTimestamp() >= startOfDayMillis)
                .mapToDouble(TransactionModel::getTotalAmount)
                .sum();
        dailyReportTv.setText(String.format(Locale.getDefault(), "Today's Sales: $%.2f", dailySales));

        // Weekly Report (Last 7 days including today)
        Calendar sevenDaysAgo = Calendar.getInstance();
        sevenDaysAgo.add(Calendar.DAY_OF_YEAR, -6); // Go back 6 days to include today for 7 days total
        sevenDaysAgo.set(Calendar.HOUR_OF_DAY, 0);
        sevenDaysAgo.set(Calendar.MINUTE, 0);
        sevenDaysAgo.set(Calendar.SECOND, 0);
        sevenDaysAgo.set(Calendar.MILLISECOND, 0);
        long startOfWeekMillis = sevenDaysAgo.getTimeInMillis();

        double weeklySales = allTransactions.stream()
                .filter(t -> t.getTimestamp() >= startOfWeekMillis)
                .mapToDouble(TransactionModel::getTotalAmount)
                .sum();
        weeklyReportTv.setText(String.format(Locale.getDefault(), "Last 7 Days Sales: $%.2f", weeklySales));

        Log.d(TAG, "Reports generated: Daily=$" + dailySales + ", Weekly=$" + weeklySales + ", Total=$" + totalSales);
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
