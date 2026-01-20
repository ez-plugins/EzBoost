package com.skyblockexp.ezboost.gui.admin;

import com.skyblockexp.ezboost.boost.BoostManager;
import com.skyblockexp.ezboost.config.Messages;
import com.skyblockexp.ezboost.gui.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Handles click events for the admin boost creation GUI.
 */
public class AdminGuiClickHandler {
    private final JavaPlugin plugin;
    private final BoostManager boostManager;
    private final Messages messages;
    private final LegacyComponentSerializer legacySerializer;
    private final AdminGuiRenderer renderer;
    private final AdminGuiInputHandler inputHandler;
    private final AdminGuiValidator validator;
    private final java.util.function.Consumer<Player> clearSavedStateCallback;

    public AdminGuiClickHandler(JavaPlugin plugin, BoostManager boostManager, Messages messages,
                              LegacyComponentSerializer legacySerializer, AdminGuiRenderer renderer,
                              AdminGuiInputHandler inputHandler, AdminGuiValidator validator,
                              java.util.function.Consumer<Player> clearSavedStateCallback) {
        this.plugin = plugin;
        this.boostManager = boostManager;
        this.messages = messages;
        this.legacySerializer = legacySerializer;
        this.renderer = renderer;
        this.inputHandler = inputHandler;
        this.validator = validator;
        this.clearSavedStateCallback = clearSavedStateCallback;
    }

    /**
     * Handles a click event on the admin GUI.
     */
    public void handleClick(Player player, String action, BoostCreationState state) {
        switch (action) {
            case "key" -> inputHandler.promptForInput(player, "Enter boost key:", state::setKey);
            case "display-name" -> inputHandler.promptForInput(player, "Enter display name:", state::setDisplayName);
            case "icon" -> openMaterialSelectionGui(player);
            case "effects" -> openEffectSelectionGui(player);
            case "duration" -> openDurationInputGui(player, state.getDuration());
            case "cooldown" -> openCooldownInputGui(player, state.getCooldown());
            case "cost" -> openCostInputGui(player, state.getCost());
            case "permission" -> openPermissionInputGui(player, state.getPermission(), state);
            case "slot" -> openSlotInputGui(player, state.getSlot());
            case "enabled" -> {
                state.setEnabled(!state.isEnabled());
                player.sendMessage(legacySerializer.serialize(Component.text("§aBoost " + (state.isEnabled() ? "enabled" : "disabled"))));
            }
            case "preview" -> showPreview(player, state);
            case "clear-effects" -> {
                state.clearEffects();
                player.sendMessage(legacySerializer.serialize(Component.text("§aEffects cleared.")));
            }
            case "submit" -> {
                if (validator.validateAndCreate(player, state)) {
                    // Clear state and saved state, then close GUI
                    clearSavedStateCallback.accept(player);
                    player.closeInventory();
                }
            }
            case "cancel" -> player.closeInventory();
        }

        // Refresh GUI if still open
        if (player.getOpenInventory().getTopInventory().getHolder() instanceof AdminBoostCreationHolder) {
            renderer.fillInventory(player.getOpenInventory().getTopInventory(), state);
        }
    }

    private void openMaterialSelectionGui(Player player) {
        MaterialSelectionGui materialGui = new MaterialSelectionGui(plugin, material -> {
            // This will be handled in the openMaterialSelectionGui method
        });
        materialGui.open(player);
    }

    private void openEffectSelectionGui(Player player) {
        EffectSelectionGui effectGui = new EffectSelectionGui(plugin, effect -> {
            // Effect addition is now handled in handleEffectAmplifierInput
        });
        effectGui.open(player);
    }

    private void openDurationInputGui(Player player, int currentValue) {
        NumberInputGui durationGui = new NumberInputGui(plugin, "Duration", 1, 86400, 60, value -> {
            // Callback handled in handleNumberInput
        });
        durationGui.open(player, currentValue);
    }

    private void openCooldownInputGui(Player player, int currentValue) {
        NumberInputGui cooldownGui = new NumberInputGui(plugin, "Cooldown", 0, 86400, 0, value -> {
            // Callback handled in handleNumberInput
        });
        cooldownGui.open(player, currentValue);
    }

    private void openSlotInputGui(Player player, Integer currentValue) {
        int current = currentValue != null ? currentValue : 0;
        NumberInputGui slotGui = new NumberInputGui(plugin, "GUI Slot", 0, 53, 0, value -> {
            // Callback handled in handleNumberInput
        });
        slotGui.open(player, current);
    }

    private void openCostInputGui(Player player, Double currentValue) {
        int intValue = currentValue != null ? (int) (currentValue * 100) : 0;
        NumberInputGui costGui = new NumberInputGui(plugin, "Cost", 0, 100000000, 0, value -> {
            // Callback handled in handleNumberInput
        });
        costGui.open(player, intValue);
    }

    private void openPermissionInputGui(Player player, String currentPermission, BoostCreationState state) {
        PermissionInputGui permissionGui = new PermissionInputGui(plugin, permission -> {
            state.setPermission(permission);
            // Close permission GUI and reopen main GUI
            player.closeInventory();
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                Inventory newInventory = renderer.createInventory();
                renderer.fillInventory(newInventory, state);
                player.openInventory(newInventory);
            });
        }, () -> {
            // Reopen main GUI on back button
            player.closeInventory();
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                Inventory newInventory = renderer.createInventory();
                renderer.fillInventory(newInventory, state);
                player.openInventory(newInventory);
            }, 1L);
        });
        permissionGui.open(player, currentPermission);
    }

    private void showPreview(Player player, BoostCreationState state) {
        AdminGuiPreview.showPreview(player, state, renderer, plugin);
    }
}