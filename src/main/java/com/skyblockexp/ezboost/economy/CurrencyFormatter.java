package com.skyblockexp.ezboost.economy;

import com.skyblockexp.ezboost.config.EzBoostConfig;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Responsible for formatting currency values for display.
 * Reads provider currency label and number-format settings from configuration when available.
 */
public final class CurrencyFormatter {
    private final EzBoostConfig config;

    public CurrencyFormatter(EzBoostConfig config) {
        this.config = config;
    }

    /**
     * Format an amount for display. Returns "Free" for non-positive amounts.
     * Supports configurable grouping and separators via `economy.format.*` config.
     */
    public String format(double amount) {
        if (amount <= 0.0) return "Free";

        EzBoostConfig.EconomySettings settings = config != null ? config.economySettings() : null;

        boolean grouping = true;
        char groupingSep = ',';
        char decimalSep = '.';
        int decimalPlaces = 2;
        String label = null;

        if (settings != null) {
            grouping = settings.groupingEnabled();
            String gs = settings.groupingSeparator();
            if (gs != null && !gs.isEmpty()) groupingSep = gs.charAt(0);
            String ds = settings.decimalSeparator();
            if (ds != null && !ds.isEmpty()) decimalSep = ds.charAt(0);
            decimalPlaces = Math.max(0, settings.decimalPlaces());
            label = settings.providerCurrency();
        }

        // Decide number of decimal places: if the amount is a whole number, show no decimals by default
        int placesToShow = (amount == Math.floor(amount)) ? 0 : decimalPlaces;

        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.ROOT);
        symbols.setGroupingSeparator(groupingSep);
        symbols.setDecimalSeparator(decimalSep);

        StringBuilder pattern = new StringBuilder();
        pattern.append(grouping ? "#,##0" : "0");
        if (placesToShow > 0) {
            pattern.append('.');
            for (int i = 0; i < placesToShow; i++) pattern.append('0');
        }

        DecimalFormat df = new DecimalFormat(pattern.toString(), symbols);
        df.setGroupingUsed(grouping);

        String formatted = df.format(amount);

        if (label != null && !label.isBlank()) {
            return label + formatted;
        }
        return formatted;
    }

    /**
     * Compact formatting (e.g. 50K, 1.2M). Respects provider currency label (prefix).
     */
    public String formatCompact(double amount) {
        if (amount <= 0.0) return "Free";

        EzBoostConfig.EconomySettings settings = config != null ? config.economySettings() : null;
        String label = settings != null ? settings.providerCurrency() : null;

        double value = amount;
        String[] suffixes = {"", "K", "M", "B", "T"};
        int mag = 0;
        while (value >= 1000.0 && mag < suffixes.length - 1) {
            value /= 1000.0;
            mag++;
        }

        String formatted;
        if (mag == 0) {
            // small amounts - use normal formatting
            formatted = format(amount);
        } else {
            // decide decimals: show one decimal for values < 100, otherwise no decimals
            boolean useDecimal = value < 100.0;
            if (useDecimal) {
                // one decimal place
                formatted = String.format(Locale.ROOT, "%.1f%s", value, suffixes[mag]);
                // trim trailing .0
                if (formatted.contains(".0")) {
                    formatted = formatted.replaceAll("\\.0+(?=[A-Z]?$)", "");
                }
            } else {
                formatted = String.format(Locale.ROOT, "%.0f%s", Math.floor(value), suffixes[mag]);
            }
        }

        if (label != null && !label.isBlank()) {
            return label + formatted;
        }
        return formatted;
    }
}
