package com.skyblockexp.ezboost.listener;

import com.skyblockexp.ezboost.gui.AdminBoostCreationGui;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        // Check for custom permission input
        if (player.getPersistentDataContainer().has(
            new NamespacedKey(plugin, "custom-permission-input"),
            PersistentDataType.STRING
        )) {
            event.setCancelled(true);
            adminGui.handleCustomPermissionInput(player, message);
            return;
        }

        // Check if player has a selected effect (waiting for amplifier input)
        NamespacedKey selectedEffectKey = new NamespacedKey(plugin, "selected-effect");
        String selectedEffect = player.getPersistentDataContainer().get(selectedEffectKey, PersistentDataType.STRING);

        if (selectedEffect != null) {
            // Player is entering amplifier for effect selection
            event.setCancelled(true);
            adminGui.handleEffectAmplifierInput(player, message);
            return;
        }

        // Check if player is in input mode for regular boost creation
        if (adminGui.isPlayerInInputMode(player.getUniqueId())) {
            event.setCancelled(true);
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