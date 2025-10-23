package com.example.apexsupplypos.data;

import android.content.Context;
import android.util.Log;

import com.example.apexsupplypos.models.TransactionModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionManager {
    private static final String TRANSACTIONS_KEY = "pos_transactions";
    private static final String TAG = "ApexPOS_TransactionMgr";
    private SharedPrefsManager sharedPrefsManager;
    private Gson gson;

    public TransactionManager(Context context) {
        sharedPrefsManager = new SharedPrefsManager(context);
        gson = new Gson();
    }

    public List<TransactionModel> getAllTransactions() {
        String json = sharedPrefsManager.getData(TRANSACTIONS_KEY);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<TransactionModel>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void saveAllTransactions(List<TransactionModel> transactions) {
        String json = gson.toJson(transactions);
        sharedPrefsManager.saveData(TRANSACTIONS_KEY, json);
        Log.d(TAG, "All transactions saved. Total: " + transactions.size());
    }

    public void addTransaction(TransactionModel transaction) {
        List<TransactionModel> transactions = getAllTransactions();
        transactions.add(transaction);
        saveAllTransactions(transactions);
        Log.d(TAG, "Added new transaction: " + transaction.getTransactionId());
    }

    // You could add methods to filter by date, user, etc. for reporting purposes
    public List<TransactionModel> getTransactionsForUser(String userId) {
        return getAllTransactions().stream()
                .filter(t -> t.getUserId().equals(userId))
                .collect(Collectors.toList());
    }
}
