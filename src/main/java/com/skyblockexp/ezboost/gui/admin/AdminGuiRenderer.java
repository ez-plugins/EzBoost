package com.skyblockexp.ezboost.gui.admin;

import com.skyblockexp.ezboost.boost.BoostEffect;
import com.skyblockexp.ezboost.gui.AdminBoostCreationHolder;
import com.skyblockexp.ezboost.gui.ItemMetaCompat;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the rendering and layout of the admin boost creation GUI.
 */
public class AdminGuiRenderer {
    private final NamespacedKey actionKey;

    public AdminGuiRenderer(NamespacedKey actionKey) {
        this.actionKey = actionKey;
    }

    /**
     * Creates the main inventory for boost creation.
     */
    public Inventory createInventory() {
        AdminBoostCreationHolder holder = new AdminBoostCreationHolder();
        Inventory inventory = Bukkit.createInventory(holder, 27, "Create Boost");
        holder.setInventory(inventory);
        return inventory;
    }

    /**
     * Fills the inventory with items based on the current state.
     */
    public void fillInventory(Inventory inventory, BoostCreationState state) {
        inventory.clear();
        addBorders(inventory);
        addSectionHeaders(inventory);
        addBasicSettings(inventory, state);
        addEffectsSection(inventory, state);
        addTimingAndCostSection(inventory, state);
        addPermissionsSection(inventory, state);
        addSlotSection(inventory, state);
        addActionButtons(inventory);
    }

    private void addBorders(Inventory inventory) {
        // Top border only
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, createBorderItem());
        }
    }

    private void addSectionHeaders(Inventory inventory) {
        // Simplified headers for 27-slot layout
        inventory.setItem(9, createSectionHeader("§6§lBoost Settings"));
    }

    private void addBasicSettings(Inventory inventory, BoostCreationState state) {
        // Key input
        inventory.setItem(10, createInputItem("§eKey", state.getKey(), Material.NAME_TAG, "key",
            "§7Unique identifier for this boost"));

        // Display name
        inventory.setItem(11, createInputItem("§eDisplay Name", state.getDisplayName(), Material.PAPER, "display-name",
            "§7Name shown to players"));

        // Icon
        ItemStack iconItem = new ItemStack(state.getIcon() != null ? state.getIcon() : Material.STONE);
        ItemMeta iconMeta = iconItem.getItemMeta();
        if (iconMeta != null) {
            String iconName = state.getIcon() != null ? formatMaterialName(state.getIcon().name()) : "None";
            ItemMetaCompat.setDisplayName(iconMeta, Component.text("§eIcon: §6" + iconName));
            ItemMetaCompat.setLore(iconMeta, java.util.List.of(
                Component.text("§7Click to change the boost icon"),
                Component.text("§7Current: §f" + iconName)
            ));
            iconMeta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, "icon");
            iconItem.setItemMeta(iconMeta);
        }
        inventory.setItem(12, iconItem);
    }

    private void addEffectsSection(Inventory inventory, BoostCreationState state) {
        // Effects display
        ItemStack effectsItem = new ItemStack(state.getEffects().isEmpty() ? Material.GLASS_BOTTLE : Material.POTION);
        ItemMeta effectsMeta = effectsItem.getItemMeta();
        if (effectsMeta != null) {
            ItemMetaCompat.setDisplayName(effectsMeta, Component.text("§eEffects (§6" + state.getEffects().size() + "§e)"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("§7Click to add potion effects"));
            if (!state.getEffects().isEmpty()) {
                lore.add(Component.text("§7Current effects:"));
                for (BoostEffect effect : state.getEffects()) {
                    String effectName = effect.type() != null ? formatEffectName(effect.type().getName()) : "Custom";
                    lore.add(Component.text("§8• §f" + effectName + " §6" + effect.amplifier()));
                }
            } else {
                lore.add(Component.text("§8• §7No effects added yet"));
            }
            ItemMetaCompat.setLore(effectsMeta, lore);
            effectsMeta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, "effects");
            effectsItem.setItemMeta(effectsMeta);
        }
        inventory.setItem(13, effectsItem);

        // Clear Effects
        ItemStack clearEffectsItem = new ItemStack(Material.BARRIER);
        ItemMeta clearMeta = clearEffectsItem.getItemMeta();
        if (clearMeta != null) {
            ItemMetaCompat.setDisplayName(clearMeta, Component.text("§cClear All Effects"));
            ItemMetaCompat.setLore(clearMeta, java.util.List.of(
                Component.text("§7Remove all potion effects")
            ));
            clearMeta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, "clear-effects");
            clearEffectsItem.setItemMeta(clearMeta);
        }
        inventory.setItem(14, clearEffectsItem);
    }

    private void addTimingAndCostSection(Inventory inventory, BoostCreationState state) {
        // Duration
        inventory.setItem(15, createNumberItem("§eDuration", state.getDuration() + "s", Material.CLOCK, "duration",
            "§7How long the boost lasts"));

        // Cooldown
        inventory.setItem(16, createNumberItem("§eCooldown", state.getCooldown() + "s", Material.CLOCK, "cooldown",
            "§7Time between uses"));

        // Cost
        inventory.setItem(17, createNumberItem("§eCost", String.format("%.2f", state.getCost()), Material.GOLD_INGOT, "cost",
            "§7Economy cost to activate"));
    }

    private void addPermissionsSection(Inventory inventory, BoostCreationState state) {
        // Permission
        String permDisplay = state.getPermission() != null && !state.getPermission().isEmpty() ? state.getPermission() : "None";
        inventory.setItem(19, createInputItem("§ePermission", permDisplay, Material.TRIPWIRE_HOOK, "permission",
            "§7Required permission (optional)"));

        // Enabled toggle
        ItemStack enabledItem = new ItemStack(state.isEnabled() ? Material.GREEN_WOOL : Material.RED_WOOL);
        ItemMeta enabledMeta = enabledItem.getItemMeta();
        if (enabledMeta != null) {
            String status = state.isEnabled() ? "§aEnabled" : "§cDisabled";
            ItemMetaCompat.setDisplayName(enabledMeta, Component.text("§eStatus: " + status));
            ItemMetaCompat.setLore(enabledMeta, java.util.List.of(
                Component.text("§7Click to toggle boost availability"),
                Component.text("§7Current: " + status)
            ));
            enabledMeta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, "enabled");
            enabledItem.setItemMeta(enabledMeta);
        }
        inventory.setItem(20, enabledItem);
    }

    private void addSlotSection(Inventory inventory, BoostCreationState state) {
        // Slot selection
        String slotDisplay = state.getSlot() != null ? state.getSlot().toString() : "Auto";
        inventory.setItem(18, createNumberItem("§eGUI Slot", slotDisplay, Material.COMPASS, "slot",
            "§7Slot position in /boost GUI (optional)"));
    }

    private void addActionButtons(Inventory inventory) {
        // Preview button
        ItemStack previewItem = new ItemStack(Material.ITEM_FRAME);
        ItemMeta previewMeta = previewItem.getItemMeta();
        if (previewMeta != null) {
            ItemMetaCompat.setDisplayName(previewMeta, Component.text("§b§lPreview Boost"));
            ItemMetaCompat.setLore(previewMeta, java.util.List.of(
                Component.text("§7See how this boost will appear"),
                Component.text("§7to players in the boost GUI")
            ));
            previewMeta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, "preview");
            previewItem.setItemMeta(previewMeta);
        }
        inventory.setItem(21, previewItem);

        // Submit
        ItemStack submitItem = new ItemStack(Material.GREEN_CONCRETE);
        ItemMeta submitMeta = submitItem.getItemMeta();
        if (submitMeta != null) {
            ItemMetaCompat.setDisplayName(submitMeta, Component.text("§a§lCreate Boost"));
            ItemMetaCompat.setLore(submitMeta, java.util.List.of(
                Component.text("§7Save this boost configuration")
            ));
            submitMeta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, "submit");
            submitItem.setItemMeta(submitMeta);
        }
        inventory.setItem(22, submitItem);

        // Cancel
        ItemStack cancelItem = new ItemStack(Material.RED_CONCRETE);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        if (cancelMeta != null) {
            ItemMetaCompat.setDisplayName(cancelMeta, Component.text("§c§lCancel"));
            ItemMetaCompat.setLore(cancelMeta, java.util.List.of(
                Component.text("§7Discard changes and close")
            ));
            cancelMeta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, "cancel");
            cancelItem.setItemMeta(cancelMeta);
        }
        inventory.setItem(23, cancelItem);
    }

    private ItemStack createBorderItem() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            ItemMetaCompat.setDisplayName(meta, Component.text("§8§l≡"));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createDividerItem() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            ItemMetaCompat.setDisplayName(meta, Component.text("§7§l─"));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createSectionHeader(String title) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            ItemMetaCompat.setDisplayName(meta, Component.text(title));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createInputItem(String label, String value, Material material, String action, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayValue = value != null ? value : "§7Not set";
            ItemMetaCompat.setDisplayName(meta, Component.text(label + ": §6" + displayValue));
            ItemMetaCompat.setLore(meta, java.util.List.of(
                Component.text(description),
                Component.text("§eClick to edit")
            ));
            meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createNumberItem(String label, String value, Material material, String action, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            ItemMetaCompat.setDisplayName(meta, Component.text(label + ": §6" + value));
            ItemMetaCompat.setLore(meta, java.util.List.of(
                Component.text(description),
                Component.text("§eClick to edit")
            ));
            meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String formatMaterialName(String name) {
        String formatted = name.toLowerCase().replace("_", " ");
        if (!formatted.isEmpty()) {
            formatted = formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
        }
        return formatted;
    }

    private String formatEffectName(String name) {
        String formatted = name.toLowerCase().replace("_", " ");
        if (!formatted.isEmpty()) {
            formatted = formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
        }
        return formatted;
    }
}