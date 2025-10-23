package com.example.apexsupplypos.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apexsupplypos.R;
import com.example.apexsupplypos.models.DiscountRule;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;
import java.util.Locale;

public class PricingAdapter extends RecyclerView.Adapter<PricingAdapter.RuleViewHolder> {

    private List<DiscountRule> rules;
    private OnRuleActionListener listener;

    public PricingAdapter(List<DiscountRule> rules, OnRuleActionListener listener) {
        this.rules = rules;
        this.listener = listener;
    }

    public interface OnRuleActionListener {
        void onEditRule(DiscountRule rule);
        void onDeleteRule(DiscountRule rule);
        void onToggleRuleActive(DiscountRule rule, boolean isActive);
    }

    @NonNull
    @Override
    public RuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_discount_rule, parent, false);
        return new RuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RuleViewHolder holder, int position) {
        DiscountRule rule = rules.get(position);
        holder.ruleNameTv.setText(rule.getRuleName());

        String categoryText = rule.getAppliesToCategory() != null && !rule.getAppliesToCategory().isEmpty() ?
                rule.getAppliesToCategory() : "All Categories";
        holder.ruleCategoryTv.setText(String.format(Locale.getDefault(), "Applies to: %s (Min Qty: %d)", categoryText, rule.getMinQuantity()));

        String discountValue;
        if (rule.getPercentageDiscount() > 0) {
            discountValue = String.format(Locale.getDefault(), "Discount: %.0f%% OFF", rule.getPercentageDiscount() * 100);
        } else if (rule.getFixedDiscount() > 0) {
            discountValue = String.format(Locale.getDefault(), "Discount: $%.2f OFF", rule.getFixedDiscount());
        } else {
            discountValue = "No Discount";
        }
        holder.ruleDiscountValueTv.setText(discountValue);

        holder.ruleActiveSwitch.setChecked(rule.isActive());
        holder.ruleActiveSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onToggleRuleActive(rule, isChecked);
            }
        });

        holder.editRuleButton.setOnClickListener(v -> listener.onEditRule(rule));
        holder.deleteRuleButton.setOnClickListener(v -> listener.onDeleteRule(rule));
    }

    @Override
    public int getItemCount() {
        return rules.size();
    }

    public void updateList(List<DiscountRule> newList) {
        rules = newList;
        notifyDataSetChanged();
    }

    static class RuleViewHolder extends RecyclerView.ViewHolder {
        TextView ruleNameTv, ruleCategoryTv, ruleDiscountValueTv;
        SwitchMaterial ruleActiveSwitch;
        Button editRuleButton, deleteRuleButton;

        public RuleViewHolder(@NonNull View itemView) {
            super(itemView);
            ruleNameTv = itemView.findViewById(R.id.ruleNameTv);
            ruleCategoryTv = itemView.findViewById(R.id.ruleCategoryTv);
            ruleDiscountValueTv = itemView.findViewById(R.id.ruleDiscountValueTv);
            ruleActiveSwitch = itemView.findViewById(R.id.ruleActiveSwitch);
            editRuleButton = itemView.findViewById(R.id.editRuleButton);
            deleteRuleButton = itemView.findViewById(R.id.deleteRuleButton);
        }
    }
}
