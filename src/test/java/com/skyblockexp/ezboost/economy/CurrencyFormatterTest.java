package com.skyblockexp.ezboost.economy;

import com.skyblockexp.ezboost.config.EzBoostConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CurrencyFormatterTest {

    @Test
    public void format_defaults_grouping() {
        CurrencyFormatter f = new CurrencyFormatter(null);
        assertEquals("50,000", f.format(50000));
        assertEquals("1,234.50", f.format(1234.5));
    }

    @Test
    public void formatCompact_defaults() {
        CurrencyFormatter f = new CurrencyFormatter(null);
        assertEquals("50K", f.formatCompact(50000));
        assertEquals("1.2M", f.formatCompact(1200000));
        assertEquals("999", f.formatCompact(999));
    }

    @Test
    public void format_with_custom_separators_and_label() {
        EzBoostConfig mockConfig = mock(EzBoostConfig.class);
        EzBoostConfig.EconomySettings settings = new EzBoostConfig.EconomySettings(
                true, // enabled
                true, // vaultEnabled
                "€", // providerCurrency
                true, // groupingEnabled
                ".", // groupingSeparator
                ",", // decimalSeparator
                2     // decimalPlaces
        );
        when(mockConfig.economySettings()).thenReturn(settings);

        CurrencyFormatter f = new CurrencyFormatter(mockConfig);
        assertEquals("€50.000", f.format(50000));
        assertEquals("€1.234,50", f.format(1234.5));
        assertEquals("€50K", f.formatCompact(50000));
    }
}
