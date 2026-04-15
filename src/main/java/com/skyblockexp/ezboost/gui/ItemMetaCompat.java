package com.skyblockexp.ezboost.gui;

import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.meta.ItemMeta;

public final class ItemMetaCompat {
    private ItemMetaCompat() {
    }

    public static void setDisplayName(ItemMeta meta, Component name) {
        meta.displayName(name);
    }

    public static void setLore(ItemMeta meta, List<Component> lore) {
        meta.lore(lore);
    }
}
