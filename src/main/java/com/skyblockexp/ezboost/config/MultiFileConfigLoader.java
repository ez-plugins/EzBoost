package com.skyblockexp.ezboost.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for loading multiple YAML config files and merging them into a single FileConfiguration.
 */
public class MultiFileConfigLoader {
    private final JavaPlugin plugin;
    private final String[] configFiles;

    public MultiFileConfigLoader(JavaPlugin plugin, String... configFiles) {
        this.plugin = plugin;
        this.configFiles = configFiles;
    }

    /**
     * Loads and merges all specified config files into a single FileConfiguration.
     */
    public FileConfiguration loadMerged() {
        FileConfiguration merged = new YamlConfiguration();
        for (String fileName : configFiles) {
            File file = new File(plugin.getDataFolder(), fileName);
            if (!file.exists()) {
                plugin.saveResource(fileName, false);
            }
            FileConfiguration part = YamlConfiguration.loadConfiguration(file);
            mergeSections(merged, part);
        }
        return merged;
    }

    /**
     * Recursively merges all keys from source into target.
     */
    private void mergeSections(FileConfiguration target, FileConfiguration source) {
        for (String key : source.getKeys(true)) {
            Object value = source.get(key);
            target.set(key, value);
        }
    }
}
