package com.skyblockexp.ezboost.placeholder;

import com.skyblockexp.ezboost.EzBoostPlugin;
import com.skyblockexp.ezboost.api.EzBoostAPI;
import com.skyblockexp.ezboost.boost.BoostDefinition;
import com.skyblockexp.ezboost.boost.BoostManager;
import com.skyblockexp.ezboost.boost.BoostCommands;
import com.skyblockexp.ezboost.boost.BoostEffect;
import com.skyblockexp.ezboost.boost.CustomBoostEffect;
import com.skyblockexp.ezboost.boost.XpBoostEffect;
import com.skyblockexp.ezboost.economy.CurrencyFormatter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private static EzBoostPlugin mockPlugin() {
        EzBoostPlugin plugin = mock(EzBoostPlugin.class);
        PluginDescriptionFile desc = mock(PluginDescriptionFile.class);
        when(desc.getAuthors()).thenReturn(List.of("me"));
        when(desc.getVersion()).thenReturn("1.0.0");
        when(plugin.getDescription()).thenReturn(desc);
        return plugin;
    }

    // -------------------------------------------------------------------------
    // has_active_boost / active_boost / active_boost_display
    // -------------------------------------------------------------------------

    @Test
    public void hasActiveBoost_trueWhenKeyPresent_falseWhenNull() {
        BoostManager bm = mock(BoostManager.class);
        when(bm.currencyFormatter()).thenReturn(new CurrencyFormatter(null));
        Player player = mock(Player.class);
        EzBoostPlaceholder p = new EzBoostPlaceholder(mockPlugin(), bm);

        when(bm.getActiveBoostKey(player)).thenReturn("speed");
        assertEquals("true", p.onPlaceholderRequest(player, "has_active_boost"));

        when(bm.getActiveBoostKey(player)).thenReturn(null);
        assertEquals("false", p.onPlaceholderRequest(player, "has_active_boost"));

        // null player → false
        assertEquals("false", p.onPlaceholderRequest(null, "has_active_boost"));
    }

    @Test
    public void activeBoost_returnsKeyOrEmpty() {
        BoostManager bm = mock(BoostManager.class);
        when(bm.currencyFormatter()).thenReturn(new CurrencyFormatter(null));
        Player player = mock(Player.class);
        EzBoostPlaceholder p = new EzBoostPlaceholder(mockPlugin(), bm);

        when(bm.getActiveBoostKey(player)).thenReturn("speed");
        assertEquals("speed", p.onPlaceholderRequest(player, "active_boost"));

        when(bm.getActiveBoostKey(player)).thenReturn(null);
        assertEquals("", p.onPlaceholderRequest(player, "active_boost"));
    }

    @Test
    public void activeBoostDisplay_returnsDisplayNameOrEmpty() {
        BoostManager bm = mock(BoostManager.class);
        when(bm.currencyFormatter()).thenReturn(new CurrencyFormatter(null));
        Player player = mock(Player.class);
        BoostDefinition def = new BoostDefinition(
                "speed", "Speed Boost", Material.DIAMOND,
                List.of(new BoostEffect(null, 0, null)),
                new BoostCommands(List.of(), List.of(), List.of()),
                60, 0, 100, null, true);
        EzBoostPlaceholder p = new EzBoostPlaceholder(mockPlugin(), bm);

        when(bm.getActiveBoostKey(player)).thenReturn("speed");
        when(bm.getBoost("speed", player)).thenReturn(Optional.of(def));
        assertEquals("Speed Boost", p.onPlaceholderRequest(player, "active_boost_display"));

        when(bm.getActiveBoostKey(player)).thenReturn(null);
        assertEquals("", p.onPlaceholderRequest(player, "active_boost_display"));
    }

    // -------------------------------------------------------------------------
    // active_boost_time_remaining / active_boost_time_remaining_formatted
    // -------------------------------------------------------------------------

    @Test
    public void activeBoostTimeRemaining_rawAndFormatted() {
        BoostManager bm = mock(BoostManager.class);
        when(bm.currencyFormatter()).thenReturn(new CurrencyFormatter(null));
        Player player = mock(Player.class);
        EzBoostPlaceholder p = new EzBoostPlaceholder(mockPlugin(), bm);

        when(bm.getActiveBoostTimeRemaining(player)).thenReturn(90L);
        assertEquals("90", p.onPlaceholderRequest(player, "active_boost_time_remaining"));
        assertEquals("01:30", p.onPlaceholderRequest(player, "active_boost_time_remaining_formatted"));

        when(bm.getActiveBoostTimeRemaining(player)).thenReturn(3661L);
        assertEquals("3661", p.onPlaceholderRequest(player, "active_boost_time_remaining"));
        assertEquals("01:01:01", p.onPlaceholderRequest(player, "active_boost_time_remaining_formatted"));

        // null player
        assertEquals("0", p.onPlaceholderRequest(null, "active_boost_time_remaining"));
        assertEquals("00:00", p.onPlaceholderRequest(null, "active_boost_time_remaining_formatted"));
    }

    // -------------------------------------------------------------------------
    // is_active_<key>
    // -------------------------------------------------------------------------

    @Test
    public void isActive_delegatesToBoostManager() {
        BoostManager bm = mock(BoostManager.class);
        when(bm.currencyFormatter()).thenReturn(new CurrencyFormatter(null));
        Player player = mock(Player.class);
        EzBoostPlaceholder p = new EzBoostPlaceholder(mockPlugin(), bm);

        when(bm.isActive(player, "speed")).thenReturn(true);
        assertEquals("true", p.onPlaceholderRequest(player, "is_active_speed"));

        when(bm.isActive(player, "speed")).thenReturn(false);
        assertEquals("false", p.onPlaceholderRequest(player, "is_active_speed"));

        assertEquals("false", p.onPlaceholderRequest(null, "is_active_speed"));
    }

    // -------------------------------------------------------------------------
    // cooldown_remaining_<key> / cooldown_remaining_formatted_<key>
    // -------------------------------------------------------------------------

    @Test
    public void cooldownRemaining_rawAndFormatted() {
        BoostManager bm = mock(BoostManager.class);
        when(bm.currencyFormatter()).thenReturn(new CurrencyFormatter(null));
        Player player = mock(Player.class);
        EzBoostPlaceholder p = new EzBoostPlaceholder(mockPlugin(), bm);

        when(bm.getCooldownRemaining(player, "speed")).thenReturn(90L);
        assertEquals("90", p.onPlaceholderRequest(player, "cooldown_remaining_speed"));
        assertEquals("01:30", p.onPlaceholderRequest(player, "cooldown_remaining_formatted_speed"));

        when(bm.getCooldownRemaining(player, "speed")).thenReturn(0L);
        assertEquals("0", p.onPlaceholderRequest(player, "cooldown_remaining_speed"));
        assertEquals("00:00", p.onPlaceholderRequest(player, "cooldown_remaining_formatted_speed"));

        // null player
        assertEquals("0", p.onPlaceholderRequest(null, "cooldown_remaining_speed"));
        assertEquals("00:00", p.onPlaceholderRequest(null, "cooldown_remaining_formatted_speed"));
    }

    // -------------------------------------------------------------------------
    // xp_multiplier
    // -------------------------------------------------------------------------

    @Test
    public void xpMultiplier_returnsOneWhenNotRegistered() {
        BoostManager bm = mock(BoostManager.class);
        when(bm.currencyFormatter()).thenReturn(new CurrencyFormatter(null));
        // Ensure EzBoostAPI has no boostManager → getCustomBoostEffects() returns empty map
        EzBoostAPI.init(null);
        EzBoostPlaceholder p = new EzBoostPlaceholder(mockPlugin(), bm);
        assertEquals("1", p.onPlaceholderRequest(mock(Player.class), "xp_multiplier"));
    }

    @Test
    public void xpMultiplier_returnsMultiplierWhenBoostActive() {
        BoostManager bm = mock(BoostManager.class);
        when(bm.currencyFormatter()).thenReturn(new CurrencyFormatter(null));

        XpBoostEffect xpEffect = new XpBoostEffect();
        Player player = mock(Player.class);
        UUID uuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(uuid);
        xpEffect.apply(player, 0); // amplifier 0 → multiplier 2

        Map<String, CustomBoostEffect> effects = new HashMap<>();
        effects.put("xpboost", xpEffect);
        when(bm.getCustomEffects()).thenReturn(effects);
        EzBoostAPI.init(bm);

        EzBoostPlaceholder p = new EzBoostPlaceholder(mockPlugin(), bm);
        assertEquals("2", p.onPlaceholderRequest(player, "xp_multiplier"));

        EzBoostAPI.init(null); // cleanup
    }
}
