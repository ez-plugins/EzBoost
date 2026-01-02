package com.skyblockexp.ezboost.gui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.meta.ItemMeta;

public final class ItemMetaCompat {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private ItemMetaCompat() {
    }

    public static void setDisplayName(ItemMeta meta, Component name) {
        meta.setDisplayName(LEGACY.serialize(name));
    }

    public static void setLore(ItemMeta meta, List<Component> lore) {
        List<String> legacyLore = lore.stream()
                .map(LEGACY::serialize)
                .toList();
        meta.setLore(legacyLore);
    }

    // Reflection methods removed for compatibility
}
