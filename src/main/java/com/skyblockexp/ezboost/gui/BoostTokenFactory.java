package com.skyblockexp.ezboost.gui;

import com.skyblockexp.ezboost.boost.BoostDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class BoostTokenFactory {
    private final NamespacedKey tokenKey;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public BoostTokenFactory(JavaPlugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        this.tokenKey = new NamespacedKey(plugin, "boost-token");
    }

    public NamespacedKey tokenKey() {
        return tokenKey;
    }

    public ItemStack createToken(BoostDefinition boost, int amount) {
        ItemStack item = new ItemStack(Material.PAPER, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        Component name = miniMessage.deserialize("<gold>" + boost.key().toUpperCase() + " Boost Token</gold>");
        ItemMetaCompat.setDisplayName(meta, name);
        List<Component> lore = new ArrayList<>();
        lore.add(miniMessage.deserialize("<gray>Redeem to activate:</gray>"));
        lore.add(miniMessage.deserialize(boost.displayName()));
        ItemMetaCompat.setLore(meta, lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(tokenKey, PersistentDataType.STRING, boost.key());
        item.setItemMeta(meta);
        return item;
    }
}
