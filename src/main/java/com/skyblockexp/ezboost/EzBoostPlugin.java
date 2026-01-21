package com.skyblockexp.ezboost;

import com.skyblockexp.ezboost.api.EzBoostAPI;
import com.skyblockexp.ezboost.boost.BoostManager;
import com.skyblockexp.ezboost.command.BoostCommand;
import com.skyblockexp.ezboost.command.EzBoostCommand;
import com.skyblockexp.ezboost.config.EzBoostConfig;
import com.skyblockexp.ezboost.config.Messages;
import com.skyblockexp.ezboost.economy.EconomyService;
import com.skyblockexp.ezboost.gui.AdminBoostCreationGui;
import com.skyblockexp.ezboost.gui.BoostGui;
import com.skyblockexp.ezboost.gui.BoostTokenFactory;
import com.skyblockexp.ezboost.listener.AdminGuiChatListener;
import com.skyblockexp.ezboost.listener.BoostGuiListener;
import com.skyblockexp.ezboost.listener.BoostPlayerListener;
import com.skyblockexp.ezboost.listener.BoostTokenListener;
import com.skyblockexp.ezboost.listener.EconomyServiceListener;
import com.skyblockexp.ezboost.storage.BoostStorage;
import com.skyblockexp.ezboost.update.SpigotUpdateChecker;
import java.io.File;
import java.util.Objects;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class EzBoostPlugin extends JavaPlugin {
    private static final int BSTATS_PLUGIN_ID = 28496;
    private static final int SPIGOT_RESOURCE_ID = 131030;
    private EzBoostConfig config;
    private Messages messages;
    private EconomyService economyService;
    private BoostStorage storage;
    private BoostManager boostManager;
    private BoostGui boostGui;
    private AdminBoostCreationGui adminGui;
    private BoostTokenFactory tokenFactory;

    @Override
    public void onEnable() {
        ensureResource("messages.yml");
        ensureDataFile();

        // Initialize basic services first
        messages = new Messages(this);
        economyService = new EconomyService();
        storage = new BoostStorage(this);

        // Create boost manager first (without config initially)
        boostManager = new BoostManager(this, null, messages, economyService, storage);
        // Initialize API so custom effects can be registered
        EzBoostAPI.init(boostManager);

        // Now load config after API is initialized
        config = new EzBoostConfig(this);
        // Update boost manager with config
        boostManager.reload(config, messages, economyService);
        economyService.setup(config.economySettings());

        boostManager.loadStates();

        boostGui = new BoostGui(this, boostManager, config.guiSettings());
        adminGui = new AdminBoostCreationGui(this, boostManager, config, messages, boostGui);
        tokenFactory = new BoostTokenFactory(this);

        registerCommands();
        registerListeners();
        initializeMetrics();
        new SpigotUpdateChecker(this, SPIGOT_RESOURCE_ID).checkForUpdates();

        getLogger().info("EzBoost plugin has been enabled.");
    }

    @Override
    public void onDisable() {
        if (boostManager != null) {
            boostManager.saveStates();
        }
        HandlerList.unregisterAll(this);
        getLogger().info("EzBoost plugin has been disabled.");
    }

    public void reloadPlugin() {
        config.reload();
        messages.reload();
        economyService.setup(config.economySettings());
        boostManager.reload(config, messages, economyService);
        boostGui.reload(config.guiSettings());
    }

    private void registerCommands() {
        BoostCommand boostCommand = new BoostCommand(boostManager, boostGui, messages);
        PluginCommand boost = getCommand("boost");
        if (boost != null) {
            boost.setExecutor(boostCommand);
            boost.setTabCompleter(boostCommand);
        }
        EzBoostCommand ezBoostCommand = new EzBoostCommand(boostManager, messages, tokenFactory, adminGui, this::reloadPlugin);
        PluginCommand ezboost = getCommand("ezboost");
        if (ezboost != null) {
            ezboost.setExecutor(ezBoostCommand);
            ezboost.setTabCompleter(ezBoostCommand);
        }
    }

    private void registerListeners() {
        Objects.requireNonNull(getServer().getPluginManager()).registerEvents(new BoostGuiListener(boostGui, adminGui, boostManager), this);
        Objects.requireNonNull(getServer().getPluginManager()).registerEvents(new AdminGuiChatListener(adminGui, this), this);
        getServer().getPluginManager().registerEvents(new BoostTokenListener(boostManager, tokenFactory), this);
        getServer().getPluginManager().registerEvents(new BoostPlayerListener(boostManager), this);
        getServer().getPluginManager().registerEvents(new EconomyServiceListener(config, economyService, getLogger()), this);
    }

    private void ensureResource(String name) {
        File file = new File(getDataFolder(), name);
        if (!file.exists()) {
            saveResource(name, false);
        }
    }

    private void ensureDataFile() {
        File file = new File(getDataFolder(), "data.yml");
        if (!file.exists()) {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            try {
                file.createNewFile();
            } catch (Exception ex) {
                getLogger().warning("Failed to create data.yml: " + ex.getMessage());
            }
        }
    }

    private void initializeMetrics() {
        try {
            new Metrics(this, BSTATS_PLUGIN_ID);
        } catch (Exception ex) {
            getLogger().warning("Failed to start bStats metrics: " + ex.getMessage());
        }
    }

    // Static API: use EzBoostAPI static methods for integration
}
