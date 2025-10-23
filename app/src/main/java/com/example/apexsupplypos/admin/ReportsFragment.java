package com.example.apexsupplypos.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.apexsupplypos.R;
import com.example.apexsupplypos.data.TransactionManager;
import com.example.apexsupplypos.models.TransactionModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportsFragment extends Fragment {

    private static final String TAG = "ApexPOS_ReportsFrag";
    private TransactionManager transactionManager;
    private TextView dailyReportTv, weeklyReportTv, totalSalesTv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        transactionManager = new TransactionManager(requireContext());
        dailyReportTv = view.findViewById(R.id.dailyReportTv);
        weeklyReportTv = view.findViewById(R.id.weeklyReportTv);
        totalSalesTv = view.findViewById(R.id.totalSalesTv);

        // Ensure reports are generated when the fragment is created/resumed
        generateReports();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh reports when fragment comes to foreground (e.g., after a new sale)
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
}
