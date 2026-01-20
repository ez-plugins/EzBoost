package com.skyblockexp.ezboost.gui;

import com.skyblockexp.ezboost.boost.BoostManager;
import com.skyblockexp.ezboost.config.Messages;
import com.skyblockexp.ezboost.gui.admin.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Main controller for the admin boost creation GUI.
 * This class orchestrates the various components for creating boosts.
 */
import com.skyblockexp.ezboost.config.EzBoostConfig;

public class AdminBoostCreationGui {
    private final JavaPlugin plugin;
    private final BoostManager boostManager;
    private final EzBoostConfig config;
    private final Messages messages;
    private final BoostGui boostGui;
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    private final NamespacedKey actionKey;
    private final Map<UUID, BoostCreationState> states = new HashMap<>();
    private final Map<UUID, String> inputModes = new HashMap<>(); // player -> field
    private final Map<UUID, java.util.function.Consumer<String>> inputCallbacks = new HashMap<>();
    private final File adminStatesFile;

    // Component classes
    private final AdminGuiRenderer renderer;
    private final AdminGuiClickHandler clickHandler;
    private final AdminGuiInputHandler inputHandler;
    private final AdminGuiValidator validator;

    public AdminBoostCreationGui(JavaPlugin plugin, BoostManager boostManager, EzBoostConfig config, Messages messages, BoostGui boostGui) {
        this.plugin = plugin;
        this.boostManager = boostManager;
        this.config = config;
        this.messages = messages;
        this.boostGui = boostGui;
        this.actionKey = new NamespacedKey(plugin, "admin-gui-action");
        this.adminStatesFile = new File(plugin.getDataFolder(), "admin-states.yml");

        // Initialize components
        this.renderer = new AdminGuiRenderer(actionKey);
        this.validator = new AdminGuiValidator(boostManager, messages, legacySerializer, config, boostGui);
        this.inputHandler = new AdminGuiInputHandler(plugin, legacySerializer, renderer,
                                                   (player, callback) -> {
                                                       UUID uuid = player.getUniqueId();
                                                       inputModes.put(uuid, "input");
                                                       inputCallbacks.put(uuid, callback);
                                                   });
        this.clickHandler = new AdminGuiClickHandler(plugin, boostManager, messages, legacySerializer,
                                                   renderer, inputHandler, validator, this::clearSavedState);
    }

    /**
     * Opens the admin boost creation GUI for a player.
     */
    public void open(Player player) {
        if (!player.hasPermission("ezboost.admin")) {
            player.sendMessage(messages.message("no-permission"));
            return;
        }
        UUID uuid = player.getUniqueId();
        BoostCreationState state = states.get(uuid);
        if (state == null) {
            // Try to load saved state
            state = loadState(player);
            if (state != null) {
                states.put(uuid, state);
                player.sendMessage(legacySerializer.serialize(Component.text("§aLoaded your previous boost creation session.")));
            } else {
                state = new BoostCreationState();
                states.put(uuid, state);
            }
        }
        Inventory inventory = renderer.createInventory();
        renderer.fillInventory(inventory, state);
        player.openInventory(inventory);
    }

    /**
     * Opens the admin boost creation GUI for a player with a specific state.
     */
    public void openWithState(Player player, BoostCreationState state) {
        if (!player.hasPermission("ezboost.admin")) {
            player.sendMessage(messages.message("no-permission"));
            return;
        }
        UUID uuid = player.getUniqueId();
        states.put(uuid, state);
        Inventory inventory = renderer.createInventory();
        renderer.fillInventory(inventory, state);
        player.openInventory(inventory);
    }

    /**
     * Checks if a player has a saved admin creation state.
     */
    public boolean hasSavedState(Player player) {
        return loadState(player) != null;
    }

    /**
     * Handles a click event on the admin GUI.
     */
    public void handleClick(Player player, String action) {
        UUID uuid = player.getUniqueId();
        BoostCreationState state = states.get(uuid);
        if (state == null) return;

        clickHandler.handleClick(player, action, state);
    }

    /**
     * Handles chat input from players.
     */
    public void handleChatInput(Player player, String message) {
        UUID uuid = player.getUniqueId();
        if (!inputModes.containsKey(uuid)) return;
        inputModes.remove(uuid);
        java.util.function.Consumer<String> callback = inputCallbacks.remove(uuid);
        if (callback != null) {
            if ("cancel".equalsIgnoreCase(message)) {
                player.sendMessage(legacySerializer.serialize(Component.text("§aInput cancelled.")));
                // Reopen GUI after cancel
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.closeInventory();
                    open(player);
                });
            } else {
                callback.accept(message);
                // Reopen GUI after successful input
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.closeInventory();
                    open(player);
                });
            }
        }
    }

    /**
     * Checks if a player is currently in input mode.
     */
    public boolean isPlayerInInputMode(UUID uuid) {
        return inputModes.containsKey(uuid);
    }

    /**
     * Clears the input mode for a player.
     */
    public void clearInputMode(UUID uuid) {
        inputModes.remove(uuid);
        inputCallbacks.remove(uuid);
    }

    /**
     * Gets the action key used for GUI interactions.
     */
    public NamespacedKey getActionKey() {
        return actionKey;
    }

    // Delegate methods for sub-GUI handling
    public void handleEffectSelection(Player player, String action, Integer page) {
        UUID uuid = player.getUniqueId();
        BoostCreationState state = states.get(uuid);
        if (state != null) {
            inputHandler.handleEffectSelection(player, action, page, state);
        }
    }

    public void handleEffectAmplifierInput(Player player, String input) {
        UUID uuid = player.getUniqueId();
        BoostCreationState state = states.get(uuid);
        if (state != null) {
            inputHandler.handleEffectAmplifierInput(player, input, state);
        }
    }

    public void handleMaterialSelection(Player player, String action, Integer page) {
        UUID uuid = player.getUniqueId();
        BoostCreationState state = states.get(uuid);
        if (state != null) {
            inputHandler.handleMaterialSelection(player, action, page, state);
        }
    }

    public void handleNumberInput(Player player, String action) {
        UUID uuid = player.getUniqueId();
        BoostCreationState state = states.get(uuid);
        if (state != null) {
            inputHandler.handleNumberInput(player, action, state);
        }
    }

    public void handlePermissionInput(Player player, String action) {
        UUID uuid = player.getUniqueId();
        BoostCreationState state = states.get(uuid);
        if (state != null) {
            inputHandler.handlePermissionInput(player, action, state);
        }
    }

    public void handleCustomPermissionInput(Player player, String permission) {
        UUID uuid = player.getUniqueId();
        BoostCreationState state = states.get(uuid);
        if (state != null) {
            inputHandler.handleCustomPermissionInput(player, permission, state);
        }
    }

    /**
     * Saves the current admin creation state for a player.
     */
    public void saveState(Player player) {
        UUID uuid = player.getUniqueId();
        BoostCreationState state = states.get(uuid);
        if (state == null) return;

        try {
            org.bukkit.configuration.file.YamlConfiguration config = new org.bukkit.configuration.file.YamlConfiguration();
            if (adminStatesFile.exists()) {
                config.load(adminStatesFile);
            }
            state.saveToConfig(config.createSection(uuid.toString()));
            config.save(adminStatesFile);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save admin creation state for " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Loads a saved admin creation state for a player.
     */
    public BoostCreationState loadState(Player player) {
        UUID uuid = player.getUniqueId();
        if (!adminStatesFile.exists()) return null;

        try {
            org.bukkit.configuration.file.YamlConfiguration config = new org.bukkit.configuration.file.YamlConfiguration();
            config.load(adminStatesFile);
            org.bukkit.configuration.ConfigurationSection section = config.getConfigurationSection(uuid.toString());
            if (section != null) {
                BoostCreationState state = new BoostCreationState();
                state.loadFromConfig(section);
                return state;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load admin creation state for " + player.getName() + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Clears the saved admin creation state for a player.
     */
    public void clearSavedState(Player player) {
        UUID uuid = player.getUniqueId();
        states.remove(uuid); // Clear the in-memory state
        if (!adminStatesFile.exists()) return;

        try {
            org.bukkit.configuration.file.YamlConfiguration config = new org.bukkit.configuration.file.YamlConfiguration();
            config.load(adminStatesFile);
            config.set(uuid.toString(), null);
            config.save(adminStatesFile);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to clear admin creation state for " + player.getName() + ": " + e.getMessage());
        }
    }
}