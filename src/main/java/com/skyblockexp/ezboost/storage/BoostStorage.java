package com.skyblockexp.ezboost.storage;

import com.skyblockexp.ezboost.boost.BoostState;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class BoostStorage {
    private final JavaPlugin plugin;
    private final File file;

    public BoostStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
    }

    public Map<UUID, BoostState> load() {
        Map<UUID, BoostState> states = new HashMap<>();
        if (!file.exists()) {
            return states;
        }
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection playersSection = configuration.getConfigurationSection("players");
        if (playersSection == null) {
            return states;
        }
        for (String key : playersSection.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            } catch (IllegalArgumentException ex) {
                continue;
            }
            ConfigurationSection playerSection = playersSection.getConfigurationSection(key);
            if (playerSection == null) {
                continue;
            }
            BoostState state = new BoostState();
            String active = playerSection.getString("active", null);
            long end = playerSection.getLong("end", 0L);
            if (active != null && !active.isBlank()) {
                state.setActiveBoost(active, end);
            }
            ConfigurationSection cooldownSection = playerSection.getConfigurationSection("cooldowns");
            if (cooldownSection != null) {
                for (String boostKey : cooldownSection.getKeys(false)) {
                    long cooldownEnd = cooldownSection.getLong(boostKey, 0L);
                    state.setCooldownEnd(boostKey.toLowerCase(), cooldownEnd);
                }
            }
            states.put(uuid, state);
        }
        return states;
    }

    public void save(Map<UUID, BoostState> states) {
        FileConfiguration configuration = new YamlConfiguration();
        ConfigurationSection playersSection = configuration.createSection("players");
        for (Map.Entry<UUID, BoostState> entry : states.entrySet()) {
            ConfigurationSection playerSection = playersSection.createSection(entry.getKey().toString());
            BoostState state = entry.getValue();
            if (state.activeBoostKey() != null) {
                playerSection.set("active", state.activeBoostKey());
                playerSection.set("end", state.endTimestamp());
            }
            ConfigurationSection cooldownSection = playerSection.createSection("cooldowns");
            for (Map.Entry<String, Long> cooldown : state.cooldowns().entrySet()) {
                cooldownSection.set(cooldown.getKey(), cooldown.getValue());
            }
        }
        try {
            configuration.save(file);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save EzBoost data.yml", ex);
        }
    }
}
