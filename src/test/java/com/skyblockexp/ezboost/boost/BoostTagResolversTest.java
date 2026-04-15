package com.skyblockexp.ezboost.boost;

import com.skyblockexp.ezboost.economy.CurrencyFormatter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BoostTagResolversTest {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static String resolve(String template, TagResolver resolver) {
        return LEGACY.serialize(MM.deserialize(template, resolver));
    }

    private static BoostDefinition boost(double cost) {
        return new BoostDefinition(
                "superboost", "Super Boost", Material.DIAMOND,
                List.of(new BoostEffect(null, 0, null)),
                new BoostCommands(List.of(), List.of(), List.of()),
                120, 60, cost, null, true);
    }

    @Test
    public void allTagsResolveCorrectly() {
        TagResolver resolver = BoostTagResolvers.forBoost(boost(50000.0), new CurrencyFormatter(null));

        assertEquals("Super Boost", resolve("<boost>", resolver));
        assertEquals("Super Boost", resolve("<boost_display>", resolver));
        assertEquals("superboost", resolve("<boost_key>", resolver));
        assertEquals("50,000", resolve("<boost_cost>", resolver));
        assertEquals("50K", resolve("<boost_cost_compact>", resolver));
        assertEquals("50000", resolve("<boost_cost_raw>", resolver));
        assertEquals("120", resolve("<boost_duration>", resolver));
        assertEquals("60", resolve("<boost_cooldown>", resolver));
    }

    @Test
    public void backwardCompatAliasesMatchBoostVariants() {
        TagResolver resolver = BoostTagResolvers.forBoost(boost(1200.0), new CurrencyFormatter(null));

        assertEquals(resolve("<boost_cost>", resolver), resolve("<cost>", resolver));
        assertEquals(resolve("<boost_cost_compact>", resolver), resolve("<cost_compact>", resolver));
    }

    @Test
    public void rawCostOmitsDecimalForWholeNumbers() {
        TagResolver resolver = BoostTagResolvers.forBoost(boost(500.0), new CurrencyFormatter(null));
        assertEquals("500", resolve("<boost_cost_raw>", resolver));
    }

    @Test
    public void rawCostKeepsDecimalForFractionalValues() {
        BoostDefinition def = new BoostDefinition(
                "k", "K", Material.DIAMOND,
                List.of(new BoostEffect(null, 0, null)),
                new BoostCommands(List.of(), List.of(), List.of()),
                60, 0, 1.5, null, true);
        TagResolver resolver = BoostTagResolvers.forBoost(def, new CurrencyFormatter(null));
        assertEquals("1.5", resolve("<boost_cost_raw>", resolver));
    }

    @Test
    public void zeroOrFreeCostFormatsAsFree() {
        TagResolver resolver = BoostTagResolvers.forBoost(boost(0.0), new CurrencyFormatter(null));
        // CurrencyFormatter returns "Free" for non-positive amounts
        assertEquals("Free", resolve("<boost_cost>", resolver));
        assertEquals("Free", resolve("<cost>", resolver));
        assertEquals("0", resolve("<boost_cost_raw>", resolver));
    }
}
