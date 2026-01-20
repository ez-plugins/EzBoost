package com.skyblockexp.ezboost.listener;

import com.skyblockexp.ezboost.gui.AdminBoostCreationGui;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class AdminGuiChatListener implements Listener {
    private final AdminBoostCreationGui adminGui;
    private final JavaPlugin plugin;

    public AdminGuiChatListener(AdminBoostCreationGui adminGui, JavaPlugin plugin) {
        this.adminGui = adminGui;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // Check for custom permission input
        if (player.getPersistentDataContainer().has(
            new NamespacedKey(plugin, "custom-permission-input"),
            PersistentDataType.STRING
        )) {
            event.setCancelled(true);
            adminGui.handleCustomPermissionInput(player, event.getMessage());
            return;
        }

        // Check if player has a selected effect (waiting for amplifier input)
        NamespacedKey selectedEffectKey = new NamespacedKey(plugin, "selected-effect");
        String selectedEffect = player.getPersistentDataContainer().get(selectedEffectKey, PersistentDataType.STRING);

        if (selectedEffect != null) {
            // Player is entering amplifier for effect selection
            event.setCancelled(true);
            adminGui.handleEffectAmplifierInput(player, event.getMessage());
            return;
        }

        // Check if player is in input mode for regular boost creation
        if (adminGui.isPlayerInInputMode(player.getUniqueId())) {
            event.setCancelled(true);
            adminGui.handleChatInput(player, event.getMessage());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof com.skyblockexp.ezboost.gui.AdminBoostCreationHolder) {
            adminGui.clearInputMode(event.getPlayer().getUniqueId());
            // Save the current state when admin GUI is closed
            if (event.getPlayer() instanceof org.bukkit.entity.Player player) {
                adminGui.saveState(player);
            }
        }
    }
}