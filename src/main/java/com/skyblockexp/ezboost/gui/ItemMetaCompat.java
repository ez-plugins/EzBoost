package com.skyblockexp.ezboost.gui;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.meta.ItemMeta;

public final class ItemMetaCompat {
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private ItemMetaCompat() {
    }

    public static void setDisplayName(ItemMeta meta, Component name) {
        meta.setDisplayName(SERIALIZER.serialize(name));
    }

    public static void setLore(ItemMeta meta, List<Component> lore) {
        List<String> lines = new ArrayList<>(lore.size());
        for (Component line : lore) {
            lines.add(SERIALIZER.serialize(line));
        }
        meta.setLore(lines);
    }
}
