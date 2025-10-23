package com.example.apexsupplypos.admin;

import android.content.Context;
import android.util.Log;

import com.example.apexsupplypos.data.SharedPrefsManager;
import com.example.apexsupplypos.models.DiscountRule;
import com.example.apexsupplypos.models.ItemModel;
import com.example.apexsupplypos.models.SaleItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PricingManager {
    private static final String DISCOUNT_RULES_KEY = "discount_rules";
    private static final String TAG = "ApexPOS_PricingMgr";
    private SharedPrefsManager sharedPrefsManager;
    private Gson gson;

    public PricingManager(Context context) {
        sharedPrefsManager = new SharedPrefsManager(context);
        gson = new Gson();
    }

    public List<DiscountRule> getAllDiscountRules() {
        String json = sharedPrefsManager.getData(DISCOUNT_RULES_KEY);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<DiscountRule>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void saveAllDiscountRules(List<DiscountRule> rules) {
        String json = gson.toJson(rules);
        sharedPrefsManager.saveData(DISCOUNT_RULES_KEY, json);
        Log.d(TAG, "All discount rules saved. Total: " + rules.size());
    }

    public void addDiscountRule(DiscountRule rule) {
        List<DiscountRule> rules = getAllDiscountRules();
        rules.add(rule);
        saveAllDiscountRules(rules);
        Log.d(TAG, "Added new discount rule: " + rule.getRuleName());
    }

    public void updateDiscountRule(DiscountRule updatedRule) {
        List<DiscountRule> rules = getAllDiscountRules();
        boolean found = false;
        for (int i = 0; i < rules.size(); i++) {
            if (rules.get(i).getRuleId().equals(updatedRule.getRuleId())) {
                rules.set(i, updatedRule);
                found = true;
                break;
            }
        }
        if (found) {
            saveAllDiscountRules(rules);
            Log.d(TAG, "Updated discount rule: " + updatedRule.getRuleName());
        } else {
            Log.w(TAG, "Attempted to update non-existent discount rule: " + updatedRule.getRuleId());
        }
    }

    public void deleteDiscountRule(String ruleId) {
        List<DiscountRule> rules = getAllDiscountRules();
        List<DiscountRule> updatedList = rules.stream()
                .filter(rule -> !rule.getRuleId().equals(ruleId))
                .collect(Collectors.toList());
        if (updatedList.size() < rules.size()) { // Rule was actually removed
            saveAllDiscountRules(updatedList);
            Log.d(TAG, "Deleted discount rule with ID: " + ruleId);
        } else {
            Log.w(TAG, "Attempted to delete non-existent discount rule with ID: " + ruleId);
        }
    }

    /**
     * Applies the most favorable discount rule to a list of SaleItems.
     * This method assumes ItemModel has a 'category' field.
     * @param items List of SaleItem currently in the cart.
     * @param inventoryManager Required to get item category.
     * @return The total discount applied to the entire cart.
     */
    public double applyDiscounts(List<SaleItem> items, InventoryManager inventoryManager) {
        double totalDiscount = 0.0;
        List<DiscountRule> activeRules = getAllDiscountRules().stream()
                .filter(DiscountRule::isActive)
                .sorted(Comparator.comparingDouble(DiscountRule::getPercentageDiscount).reversed()) // Prioritize higher percentage
                .collect(Collectors.toList());

        for (SaleItem saleItem : items) {
            ItemModel originalItem = inventoryManager.getItemById(saleItem.getItemId());
            if (originalItem == null) {
                Log.e(TAG, "Original item not found for SaleItem ID: " + saleItem.getItemId());
                continue;
            }

            double itemDiscount = 0.0;
            // Apply rule specific to item's category or general rules
            for (DiscountRule rule : activeRules) {
                boolean categoryMatches = rule.getAppliesToCategory() == null ||
                        rule.getAppliesToCategory().isEmpty() ||
                        rule.getAppliesToCategory().equalsIgnoreCase(originalItem.getCategory());

                if (categoryMatches && saleItem.getQuantity() >= rule.getMinQuantity()) {
                    double currentItemTotal = saleItem.getSubtotal();
                    double potentialDiscount = 0.0;

                    if (rule.getPercentageDiscount() > 0) {
                        potentialDiscount = currentItemTotal * rule.getPercentageDiscount();
                    } else if (rule.getFixedDiscount() > 0) {
                        potentialDiscount = rule.getFixedDiscount();
                    }

                    // Apply only the largest discount per item (or item category group)
                    if (potentialDiscount > itemDiscount) {
                        itemDiscount = potentialDiscount;
                        Log.d(TAG, "Applied rule '" + rule.getRuleName() + "' to " + saleItem.getItemName() + ". Discount: " + itemDiscount);
                    }
                }
            }
            totalDiscount += itemDiscount;
        }
        return totalDiscount;
    }
}
