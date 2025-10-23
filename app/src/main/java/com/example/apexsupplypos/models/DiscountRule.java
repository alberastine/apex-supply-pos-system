package com.example.apexsupplypos.models;

public class DiscountRule {
    private String ruleId;
    private String ruleName;
    private String appliesToCategory; // e.g., "Electronics", null for all
    private int minQuantity;         // Minimum quantity for bulk discount
    private double percentageDiscount; // e.g., 0.10 for 10%
    private double fixedDiscount;      // e.g., 5.00 for $5 off
    private boolean isActive;

    public DiscountRule(String ruleName, String appliesToCategory, int minQuantity, double percentageDiscount, double fixedDiscount, boolean isActive) {
        this.ruleId = java.util.UUID.randomUUID().toString();
        this.ruleName = ruleName;
        this.appliesToCategory = appliesToCategory;
        this.minQuantity = minQuantity;
        this.percentageDiscount = percentageDiscount;
        this.fixedDiscount = fixedDiscount;
        this.isActive = isActive;
    }

    // Getters
    public String getRuleId() { return ruleId; }
    public String getRuleName() { return ruleName; }
    public String getAppliesToCategory() { return appliesToCategory; }
    public int getMinQuantity() { return minQuantity; }
    public double getPercentageDiscount() { return percentageDiscount; }
    public double getFixedDiscount() { return fixedDiscount; }
    public boolean isActive() { return isActive; }

    // Setters
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public void setAppliesToCategory(String appliesToCategory) { this.appliesToCategory = appliesToCategory; }
    public void setMinQuantity(int minQuantity) { this.minQuantity = minQuantity; }
    public void setPercentageDiscount(double percentageDiscount) { this.percentageDiscount = percentageDiscount; }
    public void setFixedDiscount(double fixedDiscount) { this.fixedDiscount = fixedDiscount; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return "DiscountRule{" +
                "ruleId='" + ruleId + '\'' +
                ", ruleName='" + ruleName + '\'' +
                ", appliesToCategory='" + appliesToCategory + '\'' +
                ", minQuantity=" + minQuantity +
                ", percentageDiscount=" + percentageDiscount +
                ", fixedDiscount=" + fixedDiscount +
                ", isActive=" + isActive +
                '}';
    }
}
