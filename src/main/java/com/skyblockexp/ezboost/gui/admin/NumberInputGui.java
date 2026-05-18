package com.skyblockexp.ezboost.gui.admin;

import com.skyblockexp.ezboost.gui.ItemMetaCompat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

public class NumberInputGui {
    private final JavaPlugin plugin;
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    private final NamespacedKey actionKey;
    private final Consumer<Integer> onNumberSelected;
    private final String fieldName;
    private final int minValue;
    private final int maxValue;
    private final int defaultValue;

    public NumberInputGui(JavaPlugin plugin, String fieldName, int minValue, int maxValue, int defaultValue, Consumer<Integer> onNumberSelected) {
        this.plugin = plugin;
        this.fieldName = fieldName;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.defaultValue = defaultValue;
        this.onNumberSelected = onNumberSelected;
        this.actionKey = new NamespacedKey(plugin, "number-action");
    }

    public void open(Player player, int currentValue) {
        NumberInputHolder holder = new NumberInputHolder();
        Inventory inventory = createInventory(holder, currentValue);
        holder.setInventory(inventory);
        holder.setCurrentValue(currentValue);
        fillInventory(inventory, currentValue);
        player.openInventory(inventory);
    }

    private Inventory createInventory(NumberInputHolder holder, int currentValue) {
        Inventory inventory = Bukkit.createInventory(holder, 27, "Set " + fieldName + ": " + currentValue);
        holder.setCurrentValue(currentValue);
        holder.setFieldName(fieldName);
        return inventory;
    }

    private void fillInventory(Inventory inventory, int currentValue) {
        inventory.clear();

        // Current value display
        ItemStack displayItem = new ItemStack(Material.PAPER);
        ItemMeta displayMeta = displayItem.getItemMeta();
        if (displayMeta != null) {
            ItemMetaCompat.setDisplayName(displayMeta, Component.text("§eCurrent Value: §6" + currentValue));
            ItemMetaCompat.setLore(displayMeta, java.util.List.of(
                Component.text("§7Field: §f" + fieldName),
                Component.text("§7Range: §f" + minValue + " - " + maxValue)
            ));
            displayItem.setItemMeta(displayMeta);
        }
        inventory.setItem(13, displayItem);

        // Decrease buttons
        inventory.setItem(11, createAdjustmentItem("§c-100", Material.RED_STAINED_GLASS_PANE, -100, currentValue));
        inventory.setItem(12, createAdjustmentItem("§c-10", Material.ORANGE_STAINED_GLASS_PANE, -10, currentValue));
        inventory.setItem(14, createAdjustmentItem("§a+10", Material.LIME_STAINED_GLASS_PANE, 10, currentValue));
        inventory.setItem(15, createAdjustmentItem("§a+100", Material.GREEN_STAINED_GLASS_PANE, 100, currentValue));

        // Quick set buttons
        inventory.setItem(9, createQuickSetItem("§bSet to " + defaultValue, Material.LIGHT_BLUE_WOOL, defaultValue));
        inventory.setItem(17, createQuickSetItem("§dSet to " + maxValue, Material.PINK_WOOL, maxValue));

        // Confirm button
        ItemStack confirmItem = new ItemStack(Material.GREEN_CONCRETE);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        if (confirmMeta != null) {
            ItemMetaCompat.setDisplayName(confirmMeta, Component.text("§aConfirm Value: §6" + currentValue));
            confirmMeta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, "confirm");
            confirmItem.setItemMeta(confirmMeta);
        }
        inventory.setItem(22, confirmItem);

        // Cancel button
        ItemStack cancelItem = new ItemStack(Material.RED_CONCRETE);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        if (cancelMeta != null) {
            ItemMetaCompat.setDisplayName(cancelMeta, Component.text("§cCancel"));
            cancelMeta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, "cancel");
            cancelItem.setItemMeta(cancelMeta);
        }
        inventory.setItem(18, cancelItem);

        // Back button
        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            ItemMetaCompat.setDisplayName(backMeta, Component.text("§cBack to Boost Creation"));
            backMeta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, "back");
            backItem.setItemMeta(backMeta);
        }
        inventory.setItem(26, backItem);

        // Fill empty slots
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            ItemMetaCompat.setDisplayName(fillerMeta, Component.text(""));
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < 27; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }

    private ItemStack createAdjustmentItem(String name, Material material, int adjustment, int currentValue) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            int newValue = Math.max(minValue, Math.min(maxValue, currentValue + adjustment));
            ItemMetaCompat.setDisplayName(meta, Component.text(name));
            ItemMetaCompat.setLore(meta, java.util.List.of(
                Component.text("§7Click to adjust value"),
                Component.text("§7New value: §6" + newValue)
            ));
            meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, "adjust:" + adjustment);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createQuickSetItem(String name, Material material, int value) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            ItemMetaCompat.setDisplayName(meta, Component.text(name));
            ItemMetaCompat.setLore(meta, java.util.List.of(
                Component.text("§7Click to set value to §6" + value)
            ));
            meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, "set:" + value);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleClick(Player player, String action) {
        NumberInputHolder holder = (NumberInputHolder) player.getOpenInventory().getTopInventory().getHolder();
        int currentValue = holder.getCurrentValue();

        if ("cancel".equals(action)) {
            player.closeInventory();
            return;
        }

        if ("confirm".equals(action)) {
            // Confirm action is now handled by the calling GUI (AdminBoostCreationGui)
            // Don't call callback or send message here
            return;
        }

        if (action.startsWith("adjust:")) {
            int adjustment = Integer.parseInt(action.substring(7));
            int newValue = Math.max(minValue, Math.min(maxValue, currentValue + adjustment));
            open(player, newValue);
        } else if (action.startsWith("set:")) {
            int newValue = Integer.parseInt(action.substring(4));
            open(player, newValue);
        }
    }

    public static class NumberInputHolder implements org.bukkit.inventory.InventoryHolder {
        private Inventory inventory;
        private int currentValue;
        private String fieldName;

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        public int getCurrentValue() {
            return currentValue;
        }

        public void setCurrentValue(int currentValue) {
            this.currentValue = currentValue;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }
    }
}