package com.example.apexsupplypos.models;

import java.util.List;
import java.util.UUID;

public class TransactionModel {
    private String transactionId;
    private long timestamp; // epoch time
    private String userId; // Or adminId
    private List<SaleItem> itemsSold;
    private double totalAmount;
    private double discountApplied;
    private String customerName; // Optional

    public TransactionModel(String userId, List<SaleItem> itemsSold, double totalAmount, double discountApplied, String customerName) {
        this.transactionId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.userId = userId;
        this.itemsSold = itemsSold;
        this.totalAmount = totalAmount;
        this.discountApplied = discountApplied;
        this.customerName = customerName;
    }

    // Getters
    public String getTransactionId() {
        return transactionId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public List<SaleItem> getItemsSold() {
        return itemsSold;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getDiscountApplied() {
        return discountApplied;
    }

    public String getCustomerName() {
        return customerName;
    }

    // You might want to add setters if transactions can be edited, but typically they are immutable
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    @Override
    public String toString() {
        return "TransactionModel{" +
                "transactionId='" + transactionId + '\'' +
                ", timestamp=" + timestamp +
                ", userId='" + userId + '\'' +
                ", itemsSold=" + itemsSold +
                ", totalAmount=" + totalAmount +
                ", discountApplied=" + discountApplied +
                ", customerName='" + customerName + '\'' +
                '}';
    }
}
