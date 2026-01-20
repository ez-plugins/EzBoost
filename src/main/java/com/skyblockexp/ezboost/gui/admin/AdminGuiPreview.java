package com.skyblockexp.ezboost.gui.admin;

import com.skyblockexp.ezboost.boost.BoostCommands;
import com.skyblockexp.ezboost.boost.BoostDefinition;
import com.skyblockexp.ezboost.boost.BoostEffect;
import com.skyblockexp.ezboost.gui.ItemMetaCompat;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the preview functionality for the admin GUI.
 */
public class AdminGuiPreview {
    /**
     * Shows a preview of how the boost will appear to players.
     */
    public static void showPreview(Player player, BoostCreationState state, AdminGuiRenderer renderer, JavaPlugin plugin) {
        // Create a temporary boost definition for preview
        BoostDefinition previewBoost = new BoostDefinition(
            state.getKey() != null ? state.getKey() : "preview",
            state.getDisplayName() != null ? state.getDisplayName() : (state.getKey() != null ? state.getKey() : "Preview Boost"),
            state.getIcon() != null ? state.getIcon() : Material.STONE,
            state.getEffects(),
            new BoostCommands(java.util.List.of(), java.util.List.of(), java.util.List.of()),
            state.getDuration(),
            state.getCooldown(),
            state.getCost(),
            state.getPermission(),
            state.isEnabled()
        );

        // Open a preview inventory showing how the boost would appear
        Inventory previewInventory = Bukkit.createInventory(null, 27, "§b§lBoost Preview");

        // Create the boost item as it would appear in the player GUI
        ItemStack boostItem = new ItemStack(previewBoost.icon());
        ItemMeta boostMeta = boostItem.getItemMeta();
        if (boostMeta != null) {
            ItemMetaCompat.setDisplayName(boostMeta, Component.text("§e" + previewBoost.displayName()));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("§7Duration: §6" + previewBoost.durationSeconds() + "s"));
            if (previewBoost.cooldownSeconds() > 0) {
                lore.add(Component.text("§7Cooldown: §6" + previewBoost.cooldownSeconds() + "s"));
            }
            if (previewBoost.cost() > 0) {
                lore.add(Component.text("§7Cost: §6" + String.format("%.2f", previewBoost.cost())));
            }
            if (!previewBoost.effects().isEmpty()) {
                lore.add(Component.text("§7Effects:"));
                for (BoostEffect effect : previewBoost.effects()) {
                    String effectName = effect.type() != null ? formatEffectName(effect.type().getName()) : "Custom";
                    lore.add(Component.text("§8• §f" + effectName + " §6" + effect.amplifier()));
                }
            }
            if (previewBoost.permission() != null && !previewBoost.permission().isEmpty()) {
                lore.add(Component.text("§7Requires: §c" + previewBoost.permission()));
            }
            lore.add(Component.text(""));
            lore.add(Component.text(previewBoost.enabled() ? "§a§lClick to activate!" : "§c§lCurrently disabled"));

            ItemMetaCompat.setLore(boostMeta, lore);
            boostItem.setItemMeta(boostMeta);
        }

        previewInventory.setItem(13, boostItem);

        // Add close button
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        if (closeMeta != null) {
            ItemMetaCompat.setDisplayName(closeMeta, Component.text("§c§lClose Preview"));
            closeMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "admin-gui-action"), PersistentDataType.STRING, "close-preview");
            closeItem.setItemMeta(closeMeta);
        }
        previewInventory.setItem(22, closeItem);

        // Fill empty slots
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            ItemMetaCompat.setDisplayName(fillerMeta, Component.text(""));
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < 27; i++) {
            if (previewInventory.getItem(i) == null) {
                previewInventory.setItem(i, filler);
            }
        }

        player.openInventory(previewInventory);
    }

    private static String formatEffectName(String name) {
        String formatted = name.toLowerCase().replace("_", " ");
        if (!formatted.isEmpty()) {
            formatted = formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
        }
        return formatted;
    }
}