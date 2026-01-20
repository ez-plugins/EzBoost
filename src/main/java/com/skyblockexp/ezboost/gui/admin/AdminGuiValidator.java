package com.skyblockexp.ezboost.gui.admin;

import com.skyblockexp.ezboost.boost.BoostCommands;
import com.skyblockexp.ezboost.boost.BoostDefinition;
import com.skyblockexp.ezboost.boost.BoostManager;
import com.skyblockexp.ezboost.config.EzBoostConfig;
import com.skyblockexp.ezboost.config.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

/**
 * Handles validation and creation of boosts from the admin GUI.
 */
import com.skyblockexp.ezboost.gui.BoostGui;

public class AdminGuiValidator {
    private final BoostManager boostManager;
    private final Messages messages;
    private final LegacyComponentSerializer legacySerializer;
    private final EzBoostConfig config;
    private final BoostGui boostGui;

    public AdminGuiValidator(BoostManager boostManager, Messages messages, LegacyComponentSerializer legacySerializer, EzBoostConfig config, BoostGui boostGui) {
        this.boostManager = boostManager;
        this.messages = messages;
        this.legacySerializer = legacySerializer;
        this.config = config;
        this.boostGui = boostGui;
    }

    /**
     * Validates the boost creation state and creates the boost if valid.
     * @return true if the boost was created successfully, false otherwise
     */
    public boolean validateAndCreate(Player player, BoostCreationState state) {
        if (state.getKey() == null || state.getKey().isBlank()) {
            player.sendMessage(legacySerializer.serialize(Component.text("§cKey is required!")));
            return false;
        }
        if (state.getDisplayName() == null) {
            state.setDisplayName(state.getKey());
        }
        if (state.getIcon() == null) {
            state.setIcon(org.bukkit.Material.STONE);
        }
        if (state.getEffects().isEmpty()) {
            player.sendMessage(legacySerializer.serialize(Component.text("§cAt least one effect is required!")));
            return false;
        }

        BoostDefinition boost = new BoostDefinition(
            state.getKey(),
            state.getDisplayName(),
            state.getIcon(),
            state.getEffects(),
            new BoostCommands(java.util.List.of(), java.util.List.of(), java.util.List.of()), // No commands for now
            state.getDuration(),
            state.getCooldown(),
            state.getCost(),
            state.getPermission(),
            state.isEnabled()
        );

        if (boostManager.addBoost(boost)) {
            // Save slot to GUI configuration
            Integer slotToSave = state.getSlot();
            if (slotToSave == null) {
                // Auto-assign slot
                slotToSave = config.findAvailableSlot();
                if (slotToSave == -1) {
                    player.sendMessage(legacySerializer.serialize(Component.text("§cNo available GUI slots! Boost created but not visible in GUI.")));
                    return true;
                }
            }
            config.saveGuiSlot(state.getKey(), slotToSave);
            boostGui.reload(config.guiSettings());
            boostGui.refreshAllOpenGuis();
            player.sendMessage(legacySerializer.serialize(Component.text("§aBoost created successfully!")));
            return true;
        } else {
            player.sendMessage(legacySerializer.serialize(Component.text("§cBoost key already exists!")));
            return false;
        }
    }
}