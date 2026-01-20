package com.skyblockexp.ezboost.gui.admin;

import com.skyblockexp.ezboost.boost.BoostEffect;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the state of a boost being created in the admin GUI.
 * This class encapsulates all the data for a boost configuration.
 */
public class BoostCreationState {
    private String key;
    private String displayName;
    private Material icon = Material.STONE;
    private final List<BoostEffect> effects = new ArrayList<>();
    private int duration = 60;
    private int cooldown = 0;
    private double cost = 0.0;
    private String permission;
    private boolean enabled = true;
    private Integer slot;

    // Getters and setters
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }

    public List<BoostEffect> getEffects() {
        return effects;
    }

    public void addEffect(BoostEffect effect) {
        this.effects.add(effect);
    }

    public void clearEffects() {
        this.effects.clear();
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getSlot() {
        return slot;
    }

    public void setSlot(Integer slot) {
        this.slot = slot;
    }

    /**
     * Creates a copy of this state for preview purposes.
     */
    public BoostCreationState copy() {
        BoostCreationState copy = new BoostCreationState();
        copy.key = this.key;
        copy.displayName = this.displayName;
        copy.icon = this.icon;
        copy.effects.addAll(this.effects);
        copy.duration = this.duration;
        copy.cooldown = this.cooldown;
        copy.cost = this.cost;
        copy.permission = this.permission;
        copy.enabled = this.enabled;
        copy.slot = this.slot;
        return copy;
    }

    /**
     * Saves this state to a configuration section.
     */
    public void saveToConfig(ConfigurationSection section) {
        if (key != null) section.set("key", key);
        if (displayName != null) section.set("displayName", displayName);
        if (icon != null) section.set("icon", icon.name());
        section.set("duration", duration);
        section.set("cooldown", cooldown);
        section.set("cost", cost);
        if (permission != null) section.set("permission", permission);
        section.set("enabled", enabled);
        if (slot != null) section.set("slot", slot);

        // Save effects
        List<String> effectStrings = new ArrayList<>();
        for (BoostEffect effect : effects) {
            effectStrings.add(effect.type().getName() + ":" + effect.amplifier());
        }
        section.set("effects", effectStrings);
    }

    /**
     * Loads this state from a configuration section.
     */
    public void loadFromConfig(ConfigurationSection section) {
        key = section.getString("key");
        displayName = section.getString("displayName");
        String iconName = section.getString("icon");
        if (iconName != null) {
            try {
                icon = Material.valueOf(iconName);
            } catch (IllegalArgumentException e) {
                icon = Material.STONE;
            }
        }
        duration = section.getInt("duration", 60);
        cooldown = section.getInt("cooldown", 0);
        cost = section.getDouble("cost", 0.0);
        permission = section.getString("permission");
        enabled = section.getBoolean("enabled", true);
        if (section.contains("slot")) {
            slot = section.getInt("slot");
        }

        // Load effects
        effects.clear();
        List<String> effectStrings = section.getStringList("effects");
        for (String effectString : effectStrings) {
            String[] parts = effectString.split(":");
            if (parts.length == 2) {
                try {
                    org.bukkit.potion.PotionEffectType type = org.bukkit.potion.PotionEffectType.getByName(parts[0]);
                    if (type != null) {
                        int amplifier = Integer.parseInt(parts[1]);
                        effects.add(new BoostEffect(type, amplifier));
                    }
                } catch (Exception e) {
                    // Skip invalid effects
                }
            }
        }
    }
}