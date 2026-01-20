package com.skyblockexp.ezboost.listener;

import com.skyblockexp.ezboost.boost.BoostManager;
import com.skyblockexp.ezboost.boost.BoostManager.ActivationSource;
import com.skyblockexp.ezboost.gui.AdminBoostCreationGui;
import com.skyblockexp.ezboost.gui.AdminBoostCreationHolder;
import com.skyblockexp.ezboost.gui.BoostGui;
import com.skyblockexp.ezboost.gui.BoostGuiHolder;
import com.skyblockexp.ezboost.gui.admin.EffectSelectionGui;
import com.skyblockexp.ezboost.gui.admin.MaterialSelectionGui;
import com.skyblockexp.ezboost.gui.admin.NumberInputGui;
import com.skyblockexp.ezboost.gui.admin.PermissionInputGui;
import java.util.Objects;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class BoostGuiListener implements Listener {
    private final BoostGui boostGui;
    private final AdminBoostCreationGui adminGui;
    private final BoostManager boostManager;

    public BoostGuiListener(BoostGui boostGui, AdminBoostCreationGui adminGui, BoostManager boostManager) {
        this.boostGui = Objects.requireNonNull(boostGui, "boostGui");
        this.adminGui = Objects.requireNonNull(adminGui, "adminGui");
        this.boostManager = Objects.requireNonNull(boostManager, "boostManager");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        if (inventory.getHolder() instanceof BoostGuiHolder) {
            handleBoostGuiClick(event, inventory);
        } else if (inventory.getHolder() instanceof AdminBoostCreationHolder) {
            handleAdminGuiClick(event);
        } else if (inventory.getHolder() instanceof EffectSelectionGui.EffectSelectionHolder) {
            handleEffectSelectionClick(event);
        } else if (inventory.getHolder() instanceof MaterialSelectionGui.MaterialSelectionHolder) {
            handleMaterialSelectionClick(event);
        } else if (inventory.getHolder() instanceof NumberInputGui.NumberInputHolder) {
            handleNumberInputClick(event);
        } else if (inventory.getHolder() instanceof PermissionInputGui.PermissionInputHolder) {
            handlePermissionInputClick(event);
        } else if (event.getView().getTitle().contains("Preview")) {
            handlePreviewClick(event);
        }
    }

    private void handleBoostGuiClick(InventoryClickEvent event, Inventory inventory) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        NamespacedKey key = boostGui.boostKey();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String boostKey = container.get(key, PersistentDataType.STRING);
        if (boostKey == null) {
            return;
        }
        boostManager.activate(player, boostKey, ActivationSource.GUI);
        if (boostGui.closeOnClick()) {
            player.closeInventory();
        } else {
            boostGui.refresh(player, inventory);
        }
    }

    private void handleAdminGuiClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String action = container.get(adminGui.getActionKey(), PersistentDataType.STRING);
        if (action != null) {
            adminGui.handleClick(player, action);
        }
    }

    private void handleEffectSelectionClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();

        // Check for effect selection
        NamespacedKey effectKey = new NamespacedKey(adminGui.getActionKey().getNamespace(), "effect-type");
        String effectAction = container.get(effectKey, PersistentDataType.STRING);

        // Check for page navigation
        NamespacedKey pageKey = new NamespacedKey(adminGui.getActionKey().getNamespace(), "page");
        Integer page = container.get(pageKey, PersistentDataType.INTEGER);

        if (effectAction != null || page != null) {
            adminGui.handleEffectSelection(player, effectAction, page);
        }
    }

    private void handleMaterialSelectionClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();

        // Check for material selection
        NamespacedKey materialKey = new NamespacedKey(adminGui.getActionKey().getNamespace(), "material-type");
        String materialAction = container.get(materialKey, PersistentDataType.STRING);

        // Check for page navigation
        NamespacedKey pageKey = new NamespacedKey(adminGui.getActionKey().getNamespace(), "page");
        Integer page = container.get(pageKey, PersistentDataType.INTEGER);

        if (materialAction != null || page != null) {
            adminGui.handleMaterialSelection(player, materialAction, page);
        }
    }

    private void handleNumberInputClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey actionKey = new NamespacedKey(adminGui.getActionKey().getNamespace(), "number-action");
        String action = container.get(actionKey, PersistentDataType.STRING);

        if (action != null) {
            adminGui.handleNumberInput(player, action);
        }
    }

    private void handlePermissionInputClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey actionKey = new NamespacedKey(adminGui.getActionKey().getNamespace(), "permission-action");
        String action = container.get(actionKey, PersistentDataType.STRING);

        if (action != null) {
            adminGui.handlePermissionInput(player, action);
        }
    }

    private void handlePreviewClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String action = container.get(adminGui.getActionKey(), PersistentDataType.STRING);

        if ("close-preview".equals(action)) {
            player.closeInventory();
        }
    }
}
