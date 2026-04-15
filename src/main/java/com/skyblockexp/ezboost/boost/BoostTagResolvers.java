package com.skyblockexp.ezboost.boost;

import com.skyblockexp.ezboost.economy.CurrencyFormatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

/**
 * Builds a MiniMessage {@link TagResolver} that exposes common boost-definition tags
 * for use in EzBoost messages — no PlaceholderAPI required.
 *
 * <p>Available tags (usable in any message that receives this resolver):
 * <ul>
 *   <li>{@code <boost>}            — display name (backward-compat alias for {@code <boost_display>})</li>
 *   <li>{@code <boost_display>}    — display name</li>
 *   <li>{@code <boost_key>}        — internal config key</li>
 *   <li>{@code <boost_cost>}       — fully formatted cost (e.g. {@code 50,000})</li>
 *   <li>{@code <boost_cost_compact>} — compact cost (e.g. {@code 50K})</li>
 *   <li>{@code <boost_cost_raw>}   — raw numeric cost (no formatting, no label)</li>
 *   <li>{@code <boost_duration>}   — duration in seconds</li>
 *   <li>{@code <boost_cooldown>}   — cooldown in seconds</li>
 *   <li>{@code <cost>}             — alias for {@code <boost_cost>} (backward compat)</li>
 *   <li>{@code <cost_compact>}     — alias for {@code <boost_cost_compact>} (backward compat)</li>
 * </ul>
 */
public final class BoostTagResolvers {

    private BoostTagResolvers() {}

    /**
     * Creates a {@link TagResolver} with all boost-definition tags populated from {@code boost}.
     *
     * @param boost     the boost definition to expose
     * @param formatter the currency formatter used to format cost values
     * @return a resolver ready to pass to {@code Messages#message()} or any MiniMessage call
     */
    public static TagResolver forBoost(BoostDefinition boost, CurrencyFormatter formatter) {
        double cost = boost.cost();
        String costFormatted = formatter.format(cost);
        String costCompact = formatter.formatCompact(cost);
        String costRaw = (cost == Math.floor(cost) && !Double.isInfinite(cost))
                ? String.valueOf((long) cost)
                : String.valueOf(cost);

        return TagResolver.resolver(
                Placeholder.parsed("boost", boost.displayName()),
                Placeholder.parsed("boost_display", boost.displayName()),
                Placeholder.parsed("boost_key", boost.key()),
                Placeholder.parsed("boost_cost", costFormatted),
                Placeholder.parsed("boost_cost_compact", costCompact),
                Placeholder.parsed("boost_cost_raw", costRaw),
                Placeholder.parsed("boost_duration", String.valueOf(boost.durationSeconds())),
                Placeholder.parsed("boost_cooldown", String.valueOf(boost.cooldownSeconds())),
                // backward-compat aliases kept so existing messages.yml values keep working
                Placeholder.parsed("cost", costFormatted),
                Placeholder.parsed("cost_compact", costCompact)
        );
    }
}
