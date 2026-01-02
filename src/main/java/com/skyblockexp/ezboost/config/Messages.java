package com.skyblockexp.ezboost.config;

import java.io.File;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class Messages {
    private final JavaPlugin plugin;
    private FileConfiguration configuration;
    private String prefix;
    private boolean actionbarEnabled;
    private String actionbarFormat;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    public Messages(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        configuration = YamlConfiguration.loadConfiguration(file);
        prefix = configuration.getString("prefix", "");
        actionbarEnabled = configuration.getBoolean("actionbar.enabled", false);
        actionbarFormat = configuration.getString("actionbar.format", "<boost> <time>");
    }

    public String message(String key, TagResolver... resolvers) {
        String raw = configuration.getString(key, "");
        return legacySerialize(prefix + raw, resolvers);
    }

    public String plain(String key, TagResolver... resolvers) {
        String raw = configuration.getString(key, "");
        return legacySerialize(raw, resolvers);
    }

    public boolean actionbarEnabled() {
        return actionbarEnabled;
    }

    public String actionbar(String boostName, long timeSeconds) {
        return legacySerialize(actionbarFormat,
                TagResolver.resolver(
                        Placeholder.parsed("boost", boostName),
                        Placeholder.parsed("time", String.valueOf(timeSeconds))
                ));
    }

    private String legacySerialize(String input, TagResolver... resolvers) {
        Component component = miniMessage.deserialize(input, resolvers);
        String legacy = legacySerializer.serialize(component);
        return ChatColor.translateAlternateColorCodes('&', legacy);
    }
}
