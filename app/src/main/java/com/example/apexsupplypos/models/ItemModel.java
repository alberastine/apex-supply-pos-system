package com.example.apexsupplypos.models;

import java.util.UUID;

public class ItemModel {
    private String id;
    private String name;
    private String serialNumber; // Can be null for non-serialized items
    private int stockQty;
    private double price;
    private String category;

    public ItemModel(String name, String serialNumber, int stockQty, double price, String category) {
        this.id = UUID.randomUUID().toString(); // Generate unique ID
        this.name = name;
        this.serialNumber = serialNumber;
        this.stockQty = stockQty;
        this.price = price;
        this.category = category;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getStockQty() {
        return stockQty;
    }

    public void setStockQty(int stockQty) {
        this.stockQty = stockQty;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "ItemModel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", stockQty=" + stockQty +
                ", price=" + price +
                ", category='" + category + '\'' +
                '}';
    }
}
