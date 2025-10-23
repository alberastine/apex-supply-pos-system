package com.example.apexsupplypos.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apexsupplypos.R;
import com.example.apexsupplypos.models.DiscountRule;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PricingFragment extends Fragment implements PricingAdapter.OnRuleActionListener {

    private static final String TAG = "ApexPOS_PricingFragment";
    private PricingManager pricingManager;
    private RecyclerView discountRulesRecyclerView;
    private PricingAdapter pricingAdapter;
    private List<DiscountRule> currentRulesList;
    private FloatingActionButton fabAddDiscountRule;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pricing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pricingManager = new PricingManager(requireContext());
        currentRulesList = new ArrayList<>();

        discountRulesRecyclerView = view.findViewById(R.id.discountRulesRecyclerView);
        discountRulesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        pricingAdapter = new PricingAdapter(currentRulesList, this);
        discountRulesRecyclerView.setAdapter(pricingAdapter);

        fabAddDiscountRule = view.findViewById(R.id.fabAddDiscountRule);
        fabAddDiscountRule.setOnClickListener(v -> showAddEditRuleDialog(null));

        loadDiscountRules();
    }

    private void loadDiscountRules() {
        currentRulesList.clear();
        currentRulesList.addAll(pricingManager.getAllDiscountRules());
        pricingAdapter.notifyDataSetChanged();
        Log.d(TAG, "Discount rules loaded. Total: " + currentRulesList.size());
    }

    private void showAddEditRuleDialog(@Nullable DiscountRule ruleToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_discount_rule, null);
        builder.setView(dialogView);

        //TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle); // You'd need to add this ID in the layout
        TextInputEditText etRuleName = dialogView.findViewById(R.id.etRuleName);
        TextInputEditText etAppliesToCategory = dialogView.findViewById(R.id.etAppliesToCategory);
        TextInputEditText etMinQuantity = dialogView.findViewById(R.id.etMinQuantity);
        TextInputEditText etPercentageDiscount = dialogView.findViewById(R.id.etPercentageDiscount);
        TextInputEditText etFixedDiscount = dialogView.findViewById(R.id.etFixedDiscount);
        SwitchMaterial switchActiveRule = dialogView.findViewById(R.id.switchActiveRule);
        Button btnSaveRule = dialogView.findViewById(R.id.btnSaveRule);

        if (ruleToEdit != null) {
            // dialogTitle.setText("Edit Discount Rule");
            etRuleName.setText(ruleToEdit.getRuleName());
            etAppliesToCategory.setText(ruleToEdit.getAppliesToCategory());
            etMinQuantity.setText(String.valueOf(ruleToEdit.getMinQuantity()));
            etPercentageDiscount.setText(String.format(Locale.getDefault(), "%.2f", ruleToEdit.getPercentageDiscount()));
            etFixedDiscount.setText(String.format(Locale.getDefault(), "%.2f", ruleToEdit.getFixedDiscount()));
            switchActiveRule.setChecked(ruleToEdit.isActive());
            Log.d(TAG, "Populating dialog for editing rule: " + ruleToEdit.getRuleName());
        } else {
            // dialogTitle.setText(R.string.add_discount_rule);
            Log.d(TAG, "Opening dialog for adding new discount rule.");
        }

        AlertDialog dialog = builder.create();

        btnSaveRule.setOnClickListener(v -> {
            String name = etRuleName.getText().toString().trim();
            String category = etAppliesToCategory.getText().toString().trim();
            String minQtyStr = etMinQuantity.getText().toString().trim();
            String percentDiscStr = etPercentageDiscount.getText().toString().trim();
            String fixedDiscStr = etFixedDiscount.getText().toString().trim();
            boolean isActive = switchActiveRule.isChecked();

            if (name.isEmpty() || minQtyStr.isEmpty()) {
                Toast.makeText(requireContext(), "Rule name and min quantity are required", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int minQty = Integer.parseInt(minQtyStr);
                double percentDisc = percentDiscStr.isEmpty() ? 0.0 : Double.parseDouble(percentDiscStr);
                double fixedDisc = fixedDiscStr.isEmpty() ? 0.0 : Double.parseDouble(fixedDiscStr);

                if (percentDisc > 1.0) {
                    Toast.makeText(requireContext(), "Percentage discount cannot be greater than 1 (100%)", Toast.LENGTH_LONG).show();
                    return;
                }

                if (ruleToEdit == null) {
                    DiscountRule newRule = new DiscountRule(name, category.isEmpty() ? null : category, minQty, percentDisc, fixedDisc, isActive);
                    pricingManager.addDiscountRule(newRule);
                    Toast.makeText(requireContext(), "Discount rule added: " + name, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "New discount rule added: " + newRule.getRuleName());
                } else {
                    ruleToEdit.setRuleName(name);
                    ruleToEdit.setAppliesToCategory(category.isEmpty() ? null : category);
                    ruleToEdit.setMinQuantity(minQty);
                    ruleToEdit.setPercentageDiscount(percentDisc);
                    ruleToEdit.setFixedDiscount(fixedDisc);
                    ruleToEdit.setActive(isActive);
                    pricingManager.updateDiscountRule(ruleToEdit);
                    Toast.makeText(requireContext(), "Discount rule updated: " + name, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Discount rule updated: " + ruleToEdit.getRuleName());
                }
                loadDiscountRules();
                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid number format for quantity or discount", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Number format exception in add/edit rule dialog: " + e.getMessage());
            }
        });
        dialog.show();
    }

    @Override
    public void onEditRule(DiscountRule rule) {
        showAddEditRuleDialog(rule);
    }

    @Override
    public void onDeleteRule(DiscountRule rule) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Discount Rule")
                .setMessage("Are you sure you want to delete '" + rule.getRuleName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    pricingManager.deleteDiscountRule(rule.getRuleId());
                    loadDiscountRules();
                    Toast.makeText(requireContext(), rule.getRuleName() + " deleted.", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Discount rule deleted: " + rule.getRuleName());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onToggleRuleActive(DiscountRule rule, boolean isActive) {
        rule.setActive(isActive);
        pricingManager.updateDiscountRule(rule);
        Toast.makeText(requireContext(), rule.getRuleName() + " is now " + (isActive ? "active" : "inactive"), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Discount rule '" + rule.getRuleName() + "' active state toggled to: " + isActive);
        // No need to reload entire list, adapter might update just one item or list is small enough
        loadDiscountRules(); // Simple refresh for now
    }
}
