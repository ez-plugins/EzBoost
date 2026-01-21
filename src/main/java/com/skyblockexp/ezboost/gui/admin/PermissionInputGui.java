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

public class PermissionInputGui {
    private final JavaPlugin plugin;
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    private final NamespacedKey actionKey;
    private final Consumer<String> onPermissionSelected;
    private final Runnable onBackPressed;

    // GUI Constants
    private static final int INVENTORY_SIZE = 27;
    private static final int CURRENT_PERMISSION_SLOT = 13;
    private static final int CLEAR_BUTTON_SLOT = 17;
    private static final int CANCEL_BUTTON_SLOT = 18;
    private static final int CONFIRM_BUTTON_SLOT = 22;
    private static final int BACK_BUTTON_SLOT = 26;
    private static final int COMMON_PERMISSIONS_START_SLOT = 9;
    private static final int COMMON_PERMISSIONS_SECOND_ROW_OFFSET = 5;

    // Action Constants
    private static final String ACTION_CONFIRM = "confirm";
    private static final String ACTION_CANCEL = "cancel";
    private static final String ACTION_CLEAR = "clear";
    private static final String ACTION_CUSTOM = "custom";
    private static final String ACTION_BACK = "back";
    private static final String ACTION_SELECT_PREFIX = "select:";

    // Common permission patterns
    private static final String[] COMMON_PERMISSIONS = {
        "ezboost.use", "ezboost.admin", "ezboost.vip", "ezboost.premium",
        "ezboost.basic", "ezboost.pro", "ezboost.elite", "ezboost.legendary",
        "ezboost.speed", "ezboost.strength", "ezboost.regeneration", "ezboost.fire_resistance",
        "ezboost.night_vision", "ezboost.invisibility", "ezboost.flight", "ezboost.creative"
    };

    public PermissionInputGui(JavaPlugin plugin, Consumer<String> onPermissionSelected, Runnable onBackPressed) {
        this.plugin = plugin;
        this.onPermissionSelected = onPermissionSelected;
        this.onBackPressed = onBackPressed;
        this.actionKey = new NamespacedKey(plugin, "permission-action");
    }

    public void open(Player player, String currentPermission) {
        PermissionInputHolder holder = new PermissionInputHolder();
        Inventory inventory = createInventory(holder, currentPermission);
        holder.setInventory(inventory);
        holder.setCurrentPermission(currentPermission);
        fillInventory(inventory, currentPermission);
        player.openInventory(inventory);
    }

    private Inventory createInventory(PermissionInputHolder holder, String currentPermission) {
        String title = currentPermission != null && !currentPermission.isEmpty() ?
            "Set Permission: " + currentPermission : "Set Permission";
        Inventory inventory = Bukkit.createInventory(holder, INVENTORY_SIZE, title);
        holder.setCurrentPermission(currentPermission);
        return inventory;
    }

    private void fillInventory(Inventory inventory, String currentPermission) {
        inventory.clear();

        addCurrentPermissionDisplay(inventory, currentPermission);
        addCommonPermissions(inventory, currentPermission);
        addActionButtons(inventory, currentPermission);
        fillEmptySlots(inventory);
    }

    private void addCurrentPermissionDisplay(Inventory inventory, String currentPermission) {
        ItemStack displayItem = new ItemStack(Material.PAPER);
        ItemMeta displayMeta = displayItem.getItemMeta();
        if (displayMeta != null) {
            String displayPerm = currentPermission != null && !currentPermission.isEmpty() ? currentPermission : "None";
            ItemMetaCompat.setDisplayName(displayMeta, Component.text("§eCurrent Permission: §6" + displayPerm));
            ItemMetaCompat.setLore(displayMeta, java.util.List.of(
                Component.text("§7Click to enter custom permission"),
                Component.text("§7Or select from common permissions below")
            ));
            displayMeta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, ACTION_CUSTOM);
            displayItem.setItemMeta(displayMeta);
        }
        inventory.setItem(CURRENT_PERMISSION_SLOT, displayItem);
    }

    private void addCommonPermissions(Inventory inventory, String currentPermission) {
        int maxPermissions = Math.min(10, COMMON_PERMISSIONS.length);
        for (int i = 0; i < maxPermissions; i++) {
            String permission = COMMON_PERMISSIONS[i];
            ItemStack permItem = createPermissionItem(permission, permission.equals(currentPermission));
            int slot = i < COMMON_PERMISSIONS_SECOND_ROW_OFFSET ?
                COMMON_PERMISSIONS_START_SLOT + i :
                COMMON_PERMISSIONS_START_SLOT + i + COMMON_PERMISSIONS_SECOND_ROW_OFFSET;
            inventory.setItem(slot, permItem);
        }
    }

    private ItemStack createPermissionItem(String permission, boolean isSelected) {
        ItemStack permItem = new ItemStack(Material.BOOK);
        ItemMeta permMeta = permItem.getItemMeta();
        if (permMeta != null) {
            ItemMetaCompat.setDisplayName(permMeta, Component.text((isSelected ? "§a✓ " : "§e") + permission));
            ItemMetaCompat.setLore(permMeta, java.util.List.of(
                Component.text("§7Click to select this permission"),
                Component.text(isSelected ? "§aCurrently selected" : "§7Not selected")
            ));
            permMeta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, ACTION_SELECT_PREFIX + permission);
            permItem.setItemMeta(permMeta);
        }
        return permItem;
    }

    private void addActionButtons(Inventory inventory, String currentPermission) {
        // Clear permission button
        ItemStack clearItem = new ItemStack(Material.BARRIER);
        ItemMeta clearMeta = clearItem.getItemMeta();
        if (clearMeta != null) {
            ItemMetaCompat.setDisplayName(clearMeta, Component.text("§cClear Permission"));
            ItemMetaCompat.setLore(clearMeta, java.util.List.of(
                Component.text("§7Remove permission requirement")
            ));
            clearMeta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, ACTION_CLEAR);
            clearItem.setItemMeta(clearMeta);
        }
        inventory.setItem(CLEAR_BUTTON_SLOT, clearItem);

        // Confirm button
        ItemStack confirmItem = new ItemStack(Material.GREEN_CONCRETE);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        if (confirmMeta != null) {
            String displayPerm = currentPermission != null && !currentPermission.isEmpty() ? currentPermission : "None";
            ItemMetaCompat.setDisplayName(confirmMeta, Component.text("§aConfirm: §6" + displayPerm));
            confirmMeta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, ACTION_CONFIRM);
            confirmItem.setItemMeta(confirmMeta);
        }
        inventory.setItem(CONFIRM_BUTTON_SLOT, confirmItem);

        // Cancel button
        ItemStack cancelItem = new ItemStack(Material.RED_CONCRETE);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        if (cancelMeta != null) {
            ItemMetaCompat.setDisplayName(cancelMeta, Component.text("§cCancel"));
            cancelMeta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, ACTION_CANCEL);
            cancelItem.setItemMeta(cancelMeta);
        }
        inventory.setItem(CANCEL_BUTTON_SLOT, cancelItem);

        // Back button
        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            ItemMetaCompat.setDisplayName(backMeta, Component.text("§cBack to Boost Creation"));
            backMeta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, ACTION_BACK);
            backItem.setItemMeta(backMeta);
        }
        inventory.setItem(BACK_BUTTON_SLOT, backItem);
    }

    private void fillEmptySlots(Inventory inventory) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            ItemMetaCompat.setDisplayName(fillerMeta, Component.text(""));
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }

    public void handleClick(Player player, String action) {
        PermissionInputHolder holder = (PermissionInputHolder) player.getOpenInventory().getTopInventory().getHolder();
        String currentPermission = holder.getCurrentPermission();

        if (ACTION_CANCEL.equals(action)) {
            player.closeInventory();
            return;
        }

        if (ACTION_CONFIRM.equals(action)) {
            onPermissionSelected.accept(currentPermission);
            String displayPerm = currentPermission != null && !currentPermission.isEmpty() ? currentPermission : "None";
            player.sendMessage(legacySerializer.serialize(Component.text("§aPermission set to: §6" + displayPerm)));
            // Don't close inventory here - let the callback handle GUI transitions
            return;
        }

        if (ACTION_CLEAR.equals(action)) {
            open(player, null);
        } else if (ACTION_CUSTOM.equals(action)) {
            // Prompt for custom permission via chat
            player.closeInventory();
            player.sendMessage(legacySerializer.serialize(Component.text("§eEnter custom permission in chat:")));
            player.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "custom-permission-input"),
                PersistentDataType.STRING,
                "true"
            );
        } else if (action.startsWith(ACTION_SELECT_PREFIX)) {
            String permission = action.substring(ACTION_SELECT_PREFIX.length());
            open(player, permission);
        } else if (ACTION_BACK.equals(action)) {
            // Handle back button - close this GUI and let the callback handle reopening the main GUI
            player.closeInventory();
            onBackPressed.run();
        }
    }

    public void handleCustomPermissionInput(Player player, String permission) {
        player.getPersistentDataContainer().remove(new NamespacedKey(plugin, "custom-permission-input"));
        onPermissionSelected.accept(permission);
        player.sendMessage(legacySerializer.serialize(Component.text("§aPermission set to: §6" + permission)));
    }

    public static class PermissionInputHolder implements org.bukkit.inventory.InventoryHolder {
        private Inventory inventory;
        private String currentPermission;

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        public String getCurrentPermission() {
            return currentPermission;
        }

        public void setCurrentPermission(String currentPermission) {
            this.currentPermission = currentPermission;
        }
    }
}