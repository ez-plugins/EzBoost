package com.skyblockexp.ezboost.economy;

import com.skyblockexp.ezboost.config.EzBoostConfig;
import java.util.Locale;

/**
 * Responsible for formatting currency values for display.
 * Reads provider currency label from configuration when available.
 */
public final class CurrencyFormatter {
    private final EzBoostConfig config;

    public CurrencyFormatter(EzBoostConfig config) {
        this.config = config;
    }

    /**
     * Format an amount for display. Returns "Free" for non-positive amounts.
     */
    public String format(double amount) {
        if (amount <= 0.0) return "Free";
        String base;
        if (amount == Math.floor(amount)) {
            base = String.valueOf((int) amount);
        } else {
            base = String.format(Locale.US, "%.2f", amount);
        }
        String label = null;
        if (config != null && config.economySettings() != null) {
            label = config.economySettings().providerCurrency();
        }
        if (label != null && !label.isBlank()) {
            return label + base;
        }
        return base;
    }
}
