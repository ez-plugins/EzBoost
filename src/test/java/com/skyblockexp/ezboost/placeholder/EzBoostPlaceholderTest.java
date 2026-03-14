package com.skyblockexp.ezboost.placeholder;

import com.skyblockexp.ezboost.EzBoostPlugin;
import com.skyblockexp.ezboost.boost.BoostDefinition;
import com.skyblockexp.ezboost.boost.BoostManager;
import com.skyblockexp.ezboost.boost.BoostCommands;
import com.skyblockexp.ezboost.boost.BoostEffect;
import com.skyblockexp.ezboost.economy.CurrencyFormatter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EzBoostPlaceholderTest {

    @Test
    public void priceFormatted_numberAndBoostKey() {
        EzBoostPlugin plugin = mock(EzBoostPlugin.class);
        PluginDescriptionFile desc = mock(PluginDescriptionFile.class);
        when(desc.getAuthors()).thenReturn(List.of("me"));
        when(desc.getVersion()).thenReturn("1.0.0");
        when(plugin.getDescription()).thenReturn(desc);

        BoostManager bm = mock(BoostManager.class);
        when(bm.currencyFormatter()).thenReturn(new CurrencyFormatter(null));

        EzBoostPlaceholder p = new EzBoostPlaceholder(plugin, bm);

        String out = p.onPlaceholderRequest(null, "price_formatted_50000");
        assertEquals("50,000", out);

        Player player = mock(Player.class);
        BoostDefinition def = new BoostDefinition("key", "Display", Material.DIAMOND, List.of(new BoostEffect(null,0,null)), new BoostCommands(List.of(),List.of(),List.of()), 60, 0, 50000, null, true);
        when(bm.getBoost("key", player)).thenReturn(Optional.of(def));
        out = p.onPlaceholderRequest(player, "price_formatted_key");
        assertEquals("50,000", out);
    }

    @Test
    public void compact_and_status_and_currencySymbol() {
        EzBoostPlugin plugin = mock(EzBoostPlugin.class);
        PluginDescriptionFile desc = mock(PluginDescriptionFile.class);
        when(desc.getAuthors()).thenReturn(List.of("me"));
        when(desc.getVersion()).thenReturn("1.0.0");
        when(plugin.getDescription()).thenReturn(desc);

        BoostManager bm = mock(BoostManager.class);
        when(bm.currencyFormatter()).thenReturn(new CurrencyFormatter(null));

        EzBoostPlaceholder p = new EzBoostPlaceholder(plugin, bm);
        String out = p.onPlaceholderRequest(null, "price_compact_50000");
        assertEquals("50K", out);

        Player player = mock(Player.class);
        BoostDefinition def = new BoostDefinition("b", "B", Material.DIAMOND, List.of(new BoostEffect(null,0,null)), new BoostCommands(List.of(),List.of(),List.of()), 60, 0, 12345, null, true);
        when(bm.getBoost("b", player)).thenReturn(Optional.of(def));
        when(bm.isActive(player, "b")).thenReturn(false);
        when(bm.getCooldownRemaining(player, "b")).thenReturn(0L);
        when(bm.canAfford(player, 12345)).thenReturn(true);
        out = p.onPlaceholderRequest(player, "boost_status_b");
        assertEquals("available", out);

        when(bm.canAfford(player, 12345)).thenReturn(false);
        out = p.onPlaceholderRequest(player, "boost_status_b");
        assertEquals("insufficient", out);

        when(bm.currencyLabel()).thenReturn("$");
        out = p.onPlaceholderRequest(null, "currency_symbol");
        assertEquals("$", out);
    }
}
