package com.skyblockexp.ezboost.gui;

import com.skyblockexp.ezboost.boost.BoostDefinition;
import com.skyblockexp.ezboost.boost.BoostEffect;
import com.skyblockexp.ezboost.boost.BoostManager;
import com.skyblockexp.ezboost.config.EzBoostConfig.GuiSettings;
import com.skyblockexp.ezboost.config.EzBoostConfig.FillerItem;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class BoostGui {
    private final JavaPlugin plugin;
    private final BoostManager boostManager;
    private GuiSettings settings;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    private final NamespacedKey boostKey;

    public BoostGui(JavaPlugin plugin, BoostManager boostManager, GuiSettings settings) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.boostManager = Objects.requireNonNull(boostManager, "boostManager");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.boostKey = new NamespacedKey(plugin, "boost-key");
    }

    public void reload(GuiSettings settings) {
        this.settings = Objects.requireNonNull(settings, "settings");
    }

    public boolean isEnabled() {
        return settings.enabled();
    }

    public boolean closeOnClick() {
        return settings.closeOnClick();
    }

    public void open(Player player) {
        if (!settings.enabled()) {
            return;
        }
        Component title = miniMessage.deserialize(settings.title());
        BoostGuiHolder holder = new BoostGuiHolder();
        Inventory inventory = createInventory(holder, title);
        holder.setInventory(inventory);
        fillInventory(inventory, player);
        player.openInventory(inventory);
    }

    public void refresh(Player player, Inventory inventory) {
        fillInventory(inventory, player);
    }

    public NamespacedKey boostKey() {
        return boostKey;
    }

    private void fillInventory(Inventory inventory, Player player) {
        inventory.clear();
        fillFiller(inventory);
        Map<String, BoostDefinition> boosts = boostManager.getBoosts(player);
        for (Map.Entry<String, BoostDefinition> entry : boosts.entrySet()) {
            String key = entry.getKey().toLowerCase(Locale.ROOT);
            Integer slot = settings.slots().get(key);
            if (slot == null || slot < 0 || slot >= inventory.getSize()) {
                continue;
            }
            inventory.setItem(slot, createBoostItem(player, entry.getValue()));
        }
    }

    private void fillFiller(Inventory inventory) {
        FillerItem filler = settings.filler();
        if (filler == null || filler.material() == Material.AIR) {
            return;
        }
        ItemStack item = new ItemStack(filler.material());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            ItemMetaCompat.setDisplayName(meta, miniMessage.deserialize(filler.name()));
            if (filler.lore() != null && !filler.lore().isEmpty()) {
                List<Component> lore = new ArrayList<>();
                for (String line : filler.lore()) {
                    lore.add(miniMessage.deserialize(line));
                }
                ItemMetaCompat.setLore(meta, lore);
            }
            item.setItemMeta(meta);
        }
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, item);
            }
        }
    }

    private ItemStack createBoostItem(Player player, BoostDefinition boost) {
        ItemStack item = new ItemStack(boost.icon());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        ItemMetaCompat.setDisplayName(meta, miniMessage.deserialize(boost.displayName()));
        List<Component> lore = new ArrayList<>();
        // Add effect info to lore
        for (BoostEffect effect : boost.effects()) {
            if (effect.type() != null) {
                lore.add(Component.text("Effect: " + effect.type().getName() + " (" + effect.amplifier() + ")"));
            } else {
                // Try to show custom effect name
                for (var custom : boostManager.getCustomEffects().values()) {
                    lore.add(Component.text("Effect: " + custom.getName() + " (" + effect.amplifier() + ")"));
                }
            }
        }
        String status = statusFor(player, boost);
        for (String line : settings.loreLines()) {
            lore.add(miniMessage.deserialize(line,
                    Placeholder.parsed("duration", String.valueOf(boost.durationSeconds())),
                    Placeholder.parsed("cooldown", String.valueOf(boost.cooldownSeconds())),
                    Placeholder.parsed("cost", formatCost(boost.cost())),
                    Placeholder.parsed("status", status)));
        }
        ItemMetaCompat.setLore(meta, lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(boostKey, PersistentDataType.STRING, boost.key());
        item.setItemMeta(meta);
        return item;
    }

    private String statusFor(Player player, BoostDefinition boost) {
        if (!boost.enabled()) {
            return settings.status("locked", "Locked");
        }
        if (boost.permission() != null && !boost.permission().isBlank() && !player.hasPermission(boost.permission())) {
            return settings.status("locked", "Locked");
        }
        if (boostManager.isActive(player, boost.key())) {
            return settings.status("active", "Active");
        }
        long cooldown = boostManager.getCooldownRemaining(player, boost.key());
        if (cooldown > 0) {
            return settings.status("cooldown", "Cooldown");
        }
        return settings.status("available", "Available");
    }

    private String formatCost(double cost) {
        if (cost <= 0.0) {
            return "Free";
        }
        if (cost == Math.floor(cost)) {
            return String.valueOf((int) cost);
        }
        return String.format(Locale.US, "%.2f", cost);
    }

    private Inventory createInventory(BoostGuiHolder holder, Component title) {
        try {
            Method method = Bukkit.class.getMethod("createInventory", InventoryHolder.class, int.class, Component.class);
            return (Inventory) method.invoke(null, holder, settings.size(), title);
        } catch (NoSuchMethodException e) {
            String legacyTitle = legacySerializer.serialize(title);
            return Bukkit.createInventory(holder, settings.size(), legacyTitle);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unable to create boost inventory", e);
        }
    }
}
