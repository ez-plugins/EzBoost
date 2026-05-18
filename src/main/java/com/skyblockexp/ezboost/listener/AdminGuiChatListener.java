package com.skyblockexp.ezboost.listener;

import com.skyblockexp.ezboost.FoliaScheduler;
import com.skyblockexp.ezboost.gui.AdminBoostCreationGui;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
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
        // Use thread-safe check before touching any entity state
        if (!adminGui.isPlayerPendingAnyInput(player.getUniqueId())) {
            return;
        }
        event.setCancelled(true);
        final String message = event.getMessage();
        if (FoliaScheduler.FOLIA) {
            // On Folia, PDC must be accessed on the entity's region thread
            player.getScheduler().run(plugin, t -> processInput(player, message), null);
        } else {
            processInput(player, message);
        }
    }

    /**
     * Processes the chat input for admin GUI interaction.
     * Must be called from the entity region thread on Folia (main thread on Paper).
     */
    private void processInput(Player player, String message) {
        // Check for custom permission input (PDC access safe here)
        if (player.getPersistentDataContainer().has(
            new NamespacedKey(plugin, "custom-permission-input"),
            PersistentDataType.STRING
        )) {
            adminGui.handleCustomPermissionInput(player, message);
            return;
        }

        // Check if player has a selected effect (waiting for amplifier input)
        NamespacedKey selectedEffectKey = new NamespacedKey(plugin, "selected-effect");
        String selectedEffect = player.getPersistentDataContainer().get(selectedEffectKey, PersistentDataType.STRING);
        if (selectedEffect != null) {
            adminGui.handleEffectAmplifierInput(player, message);
            return;
        }

        // Check regular text input mode
        if (adminGui.isPlayerInInputMode(player.getUniqueId())) {
            adminGui.handleChatInput(player, message);
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