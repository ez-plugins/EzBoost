package com.skyblockexp.ezboost.placeholder;

import com.skyblockexp.ezboost.EzBoostPlugin;
import com.skyblockexp.ezboost.api.EzBoostAPI;
import com.skyblockexp.ezboost.boost.BoostDefinition;
import com.skyblockexp.ezboost.boost.BoostManager;
import com.skyblockexp.ezboost.boost.XpBoostEffect;
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

        // price_compact_<amountOrBoostKey>
        if (identifier.startsWith("price_compact_")) {
            String arg = identifier.substring("price_compact_".length());
            try {
                double amount = Double.parseDouble(arg);
                return boostManager.currencyFormatter().formatCompact(amount);
            } catch (NumberFormatException ignored) {
            }
            if (player != null) {
                Optional<BoostDefinition> def = boostManager.getBoost(arg, player);
                if (def.isPresent()) {
                    return boostManager.currencyFormatter().formatCompact(def.get().cost());
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

        // has_active_boost — true/false whether the player has a running boost
        if (identifier.equals("has_active_boost")) {
            if (player == null) return "false";
            return String.valueOf(boostManager.getActiveBoostKey(player) != null);
        }

        // active_boost — key of the player's current boost, or empty
        if (identifier.equals("active_boost")) {
            if (player == null) return "";
            String key = boostManager.getActiveBoostKey(player);
            return key != null ? key : "";
        }

        // active_boost_display — display name of the current boost, or empty
        if (identifier.equals("active_boost_display")) {
            if (player == null) return "";
            String key = boostManager.getActiveBoostKey(player);
            if (key == null) return "";
            return boostManager.getBoost(key, player).map(BoostDefinition::displayName).orElse("");
        }

        // active_boost_time_remaining_formatted — MM:SS / HH:MM:SS, checked before raw
        if (identifier.equals("active_boost_time_remaining_formatted")) {
            if (player == null) return formatSeconds(0L);
            return formatSeconds(boostManager.getActiveBoostTimeRemaining(player));
        }

        // active_boost_time_remaining — seconds as integer string
        if (identifier.equals("active_boost_time_remaining")) {
            if (player == null) return "0";
            return String.valueOf(boostManager.getActiveBoostTimeRemaining(player));
        }

        // is_active_<boostkey> — plain boolean, no permission/cost logic
        if (identifier.startsWith("is_active_")) {
            if (player == null) return "false";
            String key = identifier.substring("is_active_".length());
            return String.valueOf(boostManager.isActive(player, key));
        }

        // cooldown_remaining_formatted_<boostkey> — checked before raw variant
        if (identifier.startsWith("cooldown_remaining_formatted_")) {
            if (player == null) return formatSeconds(0L);
            String key = identifier.substring("cooldown_remaining_formatted_".length());
            return formatSeconds(boostManager.getCooldownRemaining(player, key));
        }

        // cooldown_remaining_<boostkey> — seconds as integer string
        if (identifier.startsWith("cooldown_remaining_")) {
            if (player == null) return "0";
            String key = identifier.substring("cooldown_remaining_".length());
            return String.valueOf(boostManager.getCooldownRemaining(player, key));
        }

        // xp_multiplier — active XP multiplier for the player (1 if no xpboost active)
        if (identifier.equals("xp_multiplier")) {
            if (player == null) return "1";
            Object effect = EzBoostAPI.getCustomBoostEffects().get("xpboost");
            if (effect instanceof XpBoostEffect xpEffect) {
                return String.valueOf(xpEffect.getMultiplier(player));
            }
            return "1";
        }

        return "";
    }

    private static String formatSeconds(long seconds) {
        if (seconds >= 3600) {
            return String.format("%02d:%02d:%02d",
                    seconds / 3600, (seconds % 3600) / 60, seconds % 60);
        }
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }
}
