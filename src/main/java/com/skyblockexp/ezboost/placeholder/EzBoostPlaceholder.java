package com.skyblockexp.ezboost.placeholder;

import com.skyblockexp.ezboost.EzBoostPlugin;
import com.skyblockexp.ezboost.boost.BoostDefinition;
import com.skyblockexp.ezboost.boost.BoostManager;
import java.util.Optional;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

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
        if (identifier == null) return "";

        // support: price_formatted_<valueOrBoostKey>
        if (identifier.startsWith("price_formatted_")) {
            String arg = identifier.substring("price_formatted_".length());

            // try parse as number first
            try {
                double amount = Double.parseDouble(arg);
                return boostManager.currencyFormatter().format(amount);
            } catch (NumberFormatException ignored) {
                // not a number, fallthrough to treat as boost key
            }

            // treat as boost key and attempt to resolve for this player
            if (player != null) {
                Optional<BoostDefinition> def = boostManager.getBoost(arg, player);
                if (def.isPresent()) {
                    return boostManager.currencyFormatter().format(def.get().cost());
                }
            }
            return "";
        }

        // Fallback: no support for bare %ezboost_price_formatted%
        return "";
    }
}
