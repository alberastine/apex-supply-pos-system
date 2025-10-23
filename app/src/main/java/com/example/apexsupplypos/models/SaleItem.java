package com.example.apexsupplypos.models;

public class SaleItem {
    private String itemId;
    private String itemName;
    private String itemSerialNumber; // Specific serial if applicable
    private int quantity;
    private double unitPrice;
    private double subtotal;

    public SaleItem(String itemId, String itemName, String itemSerialNumber, int quantity, double unitPrice) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemSerialNumber = itemSerialNumber;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = quantity * unitPrice;
    }

    // Getters and Setters
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemSerialNumber() {
        return itemSerialNumber;
    }

    public void setItemSerialNumber(String itemSerialNumber) {
        this.itemSerialNumber = itemSerialNumber;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.subtotal = quantity * unitPrice;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        this.subtotal = quantity * unitPrice;
    }

    public double getSubtotal() {
        return subtotal;
    }

    @Override
    public String toString() {
        return "SaleItem{" +
                "itemId='" + itemId + '\'' +
                ", itemName='" + itemName + '\'' +
                ", itemSerialNumber='" + itemSerialNumber + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", subtotal=" + subtotal +
                '}';
    }
}
