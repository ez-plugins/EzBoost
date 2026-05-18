package com.skyblockexp.ezboost.storage;

import com.github.ezframework.jaloquent.store.DataStore;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Jaloquent {@link DataStore} backed by a single YAML file.
 * Each record is stored as a YAML section keyed by its path (e.g. "leaderboard.uuid").
 */
public final class YamlDataStore implements DataStore {

    private final File file;
    private final Logger logger;
    private YamlConfiguration yaml;

    public YamlDataStore(File file, Logger logger) {
        this.file = file;
        this.logger = logger;
        reload();
    }

    public void reload() {
        if (file.exists()) {
            yaml = YamlConfiguration.loadConfiguration(file);
        } else {
            yaml = new YamlConfiguration();
        }
    }

    @Override
    public void save(String path, Map<String, Object> data) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            yaml.set(path + "." + entry.getKey(), entry.getValue());
        }
        persist();
    }

    @Override
    public Optional<Map<String, Object>> load(String path) {
        ConfigurationSection section = yaml.getConfigurationSection(path);
        if (section == null) {
            return Optional.empty();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            result.put(key, section.get(key));
        }
        return Optional.of(result);
    }

    @Override
    public void delete(String path) {
        yaml.set(path, null);
        persist();
    }

    @Override
    public boolean exists(String path) {
        return yaml.isConfigurationSection(path) || yaml.contains(path);
    }

    private void persist() {
        try {
            file.getParentFile().mkdirs();
            yaml.save(file);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to save " + file.getName(), ex);
        }
    }
}
