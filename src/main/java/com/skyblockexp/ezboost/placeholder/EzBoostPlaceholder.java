package com.skyblockexp.ezboost.placeholder;

import com.skyblockexp.ezboost.EzBoostPlugin;
import com.skyblockexp.ezboost.boost.BoostDefinition;
import com.skyblockexp.ezboost.boost.BoostManager;
import java.util.Optional;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import java.util.Locale;

/**
 * PlaceholderAPI expansion for EzBoost.
 *
 * Placeholders:
 * - %ezboost_price_formatted_<amount>% -> formats a numeric amount (e.g. 50000 -> 50,000)
 * - %ezboost_price_formatted_<boostkey>% -> formats the configured cost for a boost key
 */
public class EzBoostPlaceholder extends PlaceholderExpansion {
    private final EzBoostPlugin plugin;
    private final BoostManager boostManager;

    public EzBoostPlaceholder(EzBoostPlugin plugin, BoostManager boostManager) {
        this.plugin = plugin;
        this.boostManager = boostManager;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getAuthor() {
        return String.join(",", plugin.getDescription().getAuthors());
    }

    @Override
    public String getIdentifier() {
        return "ezboost";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier == null || identifier.isBlank()) return "";

        // price_formatted_<amountOrBoostKey>
        if (identifier.startsWith("price_formatted_")) {
            String arg = identifier.substring("price_formatted_".length());
            // numeric amount?
            try {
                double amount = Double.parseDouble(arg);
                return boostManager.currencyFormatter().format(amount);
            } catch (NumberFormatException ignored) {
            }
            // treat as boost key
            if (player != null) {
                Optional<BoostDefinition> def = boostManager.getBoost(arg, player);
                if (def.isPresent()) {
                    return boostManager.currencyFormatter().format(def.get().cost());
                }
            }
            return "";
        }

        // price_raw_<boostkey>
        if (identifier.startsWith("price_raw_")) {
            String key = identifier.substring("price_raw_".length());
            if (player != null) {
                Optional<BoostDefinition> def = boostManager.getBoost(key, player);
                if (def.isPresent()) {
                    double cost = def.get().cost();
                    if (cost == Math.floor(cost)) return String.valueOf((long) cost);
                    return String.format(Locale.ROOT, "%s", cost);
                }
            }
            return "";
        }

        // boost_display_<boostkey>
        if (identifier.startsWith("boost_display_")) {
            String key = identifier.substring("boost_display_".length());
            if (player != null) {
                Optional<BoostDefinition> def = boostManager.getBoost(key, player);
                if (def.isPresent()) return def.get().displayName();
            }
            return "";
        }

        // boost_cost_<boostkey>
        if (identifier.startsWith("boost_cost_")) {
            String key = identifier.substring("boost_cost_".length());
            if (player != null) {
                Optional<BoostDefinition> def = boostManager.getBoost(key, player);
                if (def.isPresent()) return boostManager.currencyFormatter().format(def.get().cost());
            }
            return "";
        }

        // boost_duration_<boostkey>
        if (identifier.startsWith("boost_duration_")) {
            String key = identifier.substring("boost_duration_".length());
            if (player != null) {
                Optional<BoostDefinition> def = boostManager.getBoost(key, player);
                if (def.isPresent()) return String.valueOf(def.get().durationSeconds());
            }
            return "";
        }

        // boost_status_<boostkey> — status for requesting player
        if (identifier.startsWith("boost_status_")) {
            String key = identifier.substring("boost_status_".length());
            if (player == null) return "";
            Optional<BoostDefinition> maybe = boostManager.getBoost(key, player);
            if (maybe.isEmpty()) return "";
            BoostDefinition boost = maybe.get();
            // locked (disabled or missing permission)
            if (!boost.enabled()) return "locked";
            String perm = boost.permission();
            if (perm != null && !perm.isBlank() && !player.hasPermission(perm)) return "locked";
            // active
            if (boostManager.isActive(player, boost.key())) return "active";
            // cooldown
            long cd = boostManager.getCooldownRemaining(player, boost.key());
            if (cd > 0) return "cooldown";
            // affordability
            if (boost.cost() > 0.0 && !boostManager.canAfford(player, boost.cost())) return "insufficient";
            return "available";
        }

        // player_can_afford_<boostkey>
        if (identifier.startsWith("player_can_afford_")) {
            String key = identifier.substring("player_can_afford_".length());
            if (player == null) return "false";
            Optional<BoostDefinition> def = boostManager.getBoost(key, player);
            if (def.isEmpty()) return "false";
            return String.valueOf(boostManager.canAfford(player, def.get().cost()));
        }

        // currency_symbol
        if (identifier.equals("currency_symbol")) {
            String label = boostManager.currencyLabel();
            return label == null ? "" : label;
        }

        return "";
    }
}
