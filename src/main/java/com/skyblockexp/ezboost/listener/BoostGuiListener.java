package com.skyblockexp.ezboost.listener;

import com.skyblockexp.ezboost.boost.BoostManager;
import com.skyblockexp.ezboost.boost.BoostManager.ActivationSource;
import com.skyblockexp.ezboost.gui.BoostGui;
import com.skyblockexp.ezboost.gui.BoostGuiHolder;
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
    private final BoostManager boostManager;

    public BoostGuiListener(BoostGui boostGui, BoostManager boostManager) {
        this.boostGui = Objects.requireNonNull(boostGui, "boostGui");
        this.boostManager = Objects.requireNonNull(boostManager, "boostManager");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        if (!(inventory.getHolder() instanceof BoostGuiHolder)) {
            return;
        }
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
}
