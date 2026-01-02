package com.skyblockexp.ezboost.config;

import com.skyblockexp.ezboost.boost.BoostCommands;
import com.skyblockexp.ezboost.boost.BoostDefinition;
import com.skyblockexp.ezboost.boost.BoostEffect;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import com.skyblockexp.ezboost.config.MultiFileConfigLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

public final class EzBoostConfig {
    private static final Material DEFAULT_ICON = Material.NETHER_STAR;
    private final JavaPlugin plugin;
    private final Logger logger;
    private Settings settings;
    private Limits limits;
    private WorldSettings worldSettings;
    private EconomySettings economySettings;
    private GuiSettings guiSettings;
    private Map<String, BoostDefinition> boosts = new LinkedHashMap<>();

    public EzBoostConfig(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.logger = plugin.getLogger();
        reload();
    }

    public void reload() {
        // Load and merge all config files
        FileConfiguration config = new MultiFileConfigLoader(
                plugin,
                "settings.yml",
                "limits.yml",
                "worlds.yml",
                "economy.yml",
                "gui.yml",
                "boosts.yml"
        ).loadMerged();
        settings = new Settings(
                config.getBoolean("settings.replace-active-boost", false),
                config.getBoolean("settings.refund-on-fail", true),
                config.getBoolean("settings.keep-boost-on-death", true),
                config.getBoolean("settings.reapply-on-join", true),
                config.getBoolean("settings.send-expired-message", true),
                config.getBoolean("settings.cooldown-per-boost-type", true)
        );
        limits = new Limits(
                config.getInt("limits.duration-min", 5),
                config.getInt("limits.duration-max", 3600),
                config.getInt("limits.amplifier-min", 0),
                config.getInt("limits.amplifier-max", 5)
        );
        worldSettings = new WorldSettings(
                config.getStringList("worlds.allow-list"),
                config.getStringList("worlds.deny-list")
        );
        economySettings = new EconomySettings(
                config.getBoolean("economy.enabled", true),
                config.getBoolean("economy.vault", true)
        );
        guiSettings = loadGuiSettings(config.getConfigurationSection("gui"));
        boosts = loadBoosts(config.getConfigurationSection("boosts"));
    }

    public Settings settings() {
        return settings;
    }

    public Limits limits() {
        return limits;
    }

    public WorldSettings worldSettings() {
        return worldSettings;
    }

    public EconomySettings economySettings() {
        return economySettings;
    }

    public GuiSettings guiSettings() {
        return guiSettings;
    }

    public Map<String, BoostDefinition> boosts() {
        return boosts;
    }

    private GuiSettings loadGuiSettings(ConfigurationSection section) {
        if (section == null) {
            return GuiSettings.disabled();
        }
        boolean enabled = section.getBoolean("enabled", true);
        String title = section.getString("title", "EzBoost");
        int size = Math.max(9, Math.min(54, section.getInt("size", 45)));
        boolean closeOnClick = section.getBoolean("close-on-click", true);
        ConfigurationSection fillerSection = section.getConfigurationSection("filler");
        FillerItem filler = null;
        if (fillerSection != null) {
            Material material = resolveMaterial(fillerSection.getString("material"), Material.BLACK_STAINED_GLASS_PANE);
            String name = fillerSection.getString("name", " ");
            List<String> lore = fillerSection.getStringList("lore");
            filler = new FillerItem(material, name, lore);
        }
        List<String> loreLines = section.getStringList("lore");
        Map<String, String> status = new HashMap<>();
        ConfigurationSection statusSection = section.getConfigurationSection("status");
        if (statusSection != null) {
            for (String key : statusSection.getKeys(false)) {
                status.put(key.toLowerCase(Locale.ROOT), statusSection.getString(key, ""));
            }
        }
        Map<String, Integer> slots = new HashMap<>();
        ConfigurationSection slotsSection = section.getConfigurationSection("slots");
        if (slotsSection != null) {
            for (String key : slotsSection.getKeys(false)) {
                slots.put(key.toLowerCase(Locale.ROOT), slotsSection.getInt(key));
            }
        }
        return new GuiSettings(enabled, title, size, closeOnClick, filler, loreLines, status, slots);
    }

    private Map<String, BoostDefinition> loadBoosts(ConfigurationSection boostsSection) {
        if (boostsSection == null) {
            return Collections.emptyMap();
        }
        Map<String, BoostDefinition> loaded = new LinkedHashMap<>();
        for (String key : boostsSection.getKeys(false)) {
            ConfigurationSection boostSection = boostsSection.getConfigurationSection(key);
            if (boostSection == null) {
                continue;
            }
            String normalizedKey = key.toLowerCase(Locale.ROOT);
            String displayName = boostSection.getString("display-name", normalizedKey);
            Material icon = resolveMaterial(boostSection.getString("icon"), DEFAULT_ICON);
            List<Map<?, ?>> effectsConfig = boostSection.getMapList("effects");
            List<BoostEffect> effects = new ArrayList<>();
            for (Map<?, ?> entry : effectsConfig) {
                if (entry == null) {
                    continue;
                }
                String typeName = Objects.toString(entry.get("type"), "");
                PotionEffectType type = PotionEffectType.getByName(typeName.toUpperCase(Locale.ROOT));
                if (type == null) {
                    logger.warning("EzBoost: Invalid effect type '" + typeName + "' for boost " + normalizedKey + ".");
                    continue;
                }
                int amplifier = readInt(entry.get("amplifier"), 0);
                amplifier = clamp(amplifier, limits.amplifierMin(), limits.amplifierMax());
                effects.add(new BoostEffect(type, amplifier));
            }
            if (effects.isEmpty()) {
                logger.warning("EzBoost: Boost " + normalizedKey + " has no valid effects and will be skipped.");
                continue;
            }
            BoostCommands commands = readBoostCommands(boostSection);
            int duration = boostSection.getInt("duration", 60);
            duration = clamp(duration, limits.durationMin(), limits.durationMax());
            int cooldown = boostSection.getInt("cooldown", 0);
            double cost = boostSection.getDouble("cost", 0.0);
            String permission = boostSection.getString("permission", null);
            boolean enabled = boostSection.getBoolean("enabled", true);
            loaded.put(normalizedKey, new BoostDefinition(
                    normalizedKey,
                    displayName,
                    icon,
                    effects,
                    commands,
                    duration,
                    cooldown,
                    cost,
                    permission,
                    enabled
            ));
        }
        return loaded;
    }

    private BoostCommands readBoostCommands(ConfigurationSection boostSection) {
        List<String> enable = boostSection.getStringList("commands.enable");
        List<String> disable = boostSection.getStringList("commands.disable");
        List<String> toggle = boostSection.getStringList("commands.toggle");
        if (boostSection.isList("commands")) {
            toggle = boostSection.getStringList("commands");
        }
        return new BoostCommands(enable, disable, toggle);
    }

    private Material resolveMaterial(String name, Material fallback) {
        if (name == null || name.isBlank()) {
            return fallback;
        }
        Material material = Material.matchMaterial(name.toUpperCase(Locale.ROOT));
        if (material == null) {
            logger.warning("EzBoost: Invalid material '" + name + "', using " + fallback + ".");
            return fallback;
        }
        return material;
    }

    private int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    private int readInt(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    public record Settings(boolean replaceActiveBoost,
                           boolean refundOnFail,
                           boolean keepBoostOnDeath,
                           boolean reapplyOnJoin,
                           boolean sendExpiredMessage,
                           boolean cooldownPerBoostType) {
    }

    public record Limits(int durationMin, int durationMax, int amplifierMin, int amplifierMax) {
    }

    public record WorldSettings(List<String> allowList, List<String> denyList) {
        public boolean isAllowed(String worldName) {
            String normalized = worldName.toLowerCase(Locale.ROOT);
            if (!allowList.isEmpty()) {
                boolean allowed = allowList.stream()
                        .map(name -> name.toLowerCase(Locale.ROOT))
                        .anyMatch(name -> name.equals(normalized));
                if (!allowed) {
                    return false;
                }
            }
            return denyList.stream()
                    .map(name -> name.toLowerCase(Locale.ROOT))
                    .noneMatch(name -> name.equals(normalized));
        }
    }

    public record EconomySettings(boolean enabled, boolean vaultEnabled) {
    }

    public record FillerItem(Material material, String name, List<String> lore) {
    }

    public record GuiSettings(boolean enabled,
                              String title,
                              int size,
                              boolean closeOnClick,
                              FillerItem filler,
                              List<String> loreLines,
                              Map<String, String> statusLabels,
                              Map<String, Integer> slots) {
        public static GuiSettings disabled() {
            return new GuiSettings(false, "EzBoost", 9, true, null, List.of(), Map.of(), Map.of());
        }

        public String status(String key, String fallback) {
            return statusLabels.getOrDefault(key, fallback);
        }
    }
}
