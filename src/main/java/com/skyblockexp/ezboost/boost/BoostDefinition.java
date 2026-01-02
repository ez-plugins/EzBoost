package com.skyblockexp.ezboost.boost;

import java.util.List;
import org.bukkit.Material;

/**
 * Represents a single boost definition, including its display, effects, cost, cooldown, and permissions.
 * Immutable and configured via config.yml.
 */
public final class BoostDefinition {
    private final String key;
    private final String displayName;
    private final Material icon;
    private final List<BoostEffect> effects;
    private final BoostCommands commands;
    private final int durationSeconds;
    private final int cooldownSeconds;
    private final double cost;
    private final String permission;
    private final boolean enabled;

    /**
     * Constructs a new BoostDefinition.
     * @param key Unique boost key (config section name)
     * @param displayName Display name for GUI and messages
     * @param icon Icon material for GUI
     * @param effects List of potion effects applied by this boost
     * @param commands Optional commands executed on activation
     * @param durationSeconds Duration in seconds
     * @param cooldownSeconds Cooldown in seconds
     * @param cost Vault cost to activate (if enabled)
     * @param permission Required permission to use this boost
     * @param enabled Whether this boost is enabled
     */
    public BoostDefinition(String key,
                          String displayName,
                          Material icon,
                          List<BoostEffect> effects,
                          BoostCommands commands,
                          int durationSeconds,
                          int cooldownSeconds,
                          double cost,
                          String permission,
                          boolean enabled) {
        this.key = key;
        this.displayName = displayName;
        this.icon = icon;
        this.effects = List.copyOf(effects);
        this.commands = commands;
        this.durationSeconds = durationSeconds;
        this.cooldownSeconds = cooldownSeconds;
        this.cost = cost;
        this.permission = permission;
        this.enabled = enabled;
    }

    /**
     * @return Unique boost key (config section name)
     */
    public String key() {
        return key;
    }

    /**
     * @return Display name for GUI and messages
     */
    public String displayName() {
        return displayName;
    }

    /**
     * @return Icon material for GUI
     */
    public Material icon() {
        return icon;
    }

    /**
     * @return List of potion effects applied by this boost
     */
    public List<BoostEffect> effects() {
        return effects;
    }

    /**
     * @return Optional commands executed on activation
     */
    public BoostCommands commands() {
        return commands;
    }

    /**
     * @return Duration in seconds
     */
    public int durationSeconds() {
        return durationSeconds;
    }

    /**
     * @return Cooldown in seconds
     */
    public int cooldownSeconds() {
        return cooldownSeconds;
    }

    /**
     * @return Vault cost to activate (if enabled)
     */
    public double cost() {
        return cost;
    }

    /**
     * @return Required permission to use this boost
     */
    public String permission() {
        return permission;
    }

    /**
     * @return Whether this boost is enabled
     */
    public boolean enabled() {
        return enabled;
    }
}
