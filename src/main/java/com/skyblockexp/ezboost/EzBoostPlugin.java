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
import com.skyblockexp.ezboost.boost.XpBoostEffect;
import com.skyblockexp.ezboost.listener.AdminGuiChatListener;
import com.skyblockexp.ezboost.listener.BoostGuiListener;
import com.skyblockexp.ezboost.listener.BoostPlayerListener;
import com.skyblockexp.ezboost.listener.BoostTokenListener;
import com.skyblockexp.ezboost.listener.EconomyServiceListener;
import com.skyblockexp.ezboost.listener.XpBoostListener;
import com.skyblockexp.ezboost.storage.BoostLeaderboard;
import com.skyblockexp.ezboost.storage.BoostPurchaseRecord;
import com.skyblockexp.ezboost.storage.EzBoostRepository;
import com.skyblockexp.ezboost.storage.StorageFactory;
import com.skyblockexp.ezboost.storage.StorageSettings;
import com.skyblockexp.ezboost.update.SpigotUpdateChecker;
import com.github.ezframework.jaloquent.model.ModelRepository;
import java.io.File;
import java.util.Objects;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class EzBoostPlugin extends JavaPlugin {
    private static final int BSTATS_PLUGIN_ID = 28496;
    private static final int SPIGOT_RESOURCE_ID = 131030;
    private boolean papiHooked = false;
    private EzBoostConfig config;
    private Messages messages;
    private EconomyService economyService;
    private EzBoostRepository boostRepository;
    private BoostManager boostManager;
    private BoostGui boostGui;
    private AdminBoostCreationGui adminGui;
    private BoostTokenFactory tokenFactory;
    private XpBoostEffect xpBoostEffect;
    private BoostLeaderboard boostLeaderboard;

    @Override
    public void onEnable() {
        ensureResource("messages.yml");
        ensureResource("storage.yml");

        // Initialize basic services first
        messages = new Messages(this);
        economyService = new EconomyService();

        // Build storage layer from storage.yml
        StorageSettings storageSettings = loadStorageSettings();
        getLogger().info("Storage backend: " + storageSettings.backend());
        StorageFactory.StorageBundle storageBundle =
                StorageFactory.build(storageSettings, getDataFolder(), getLogger());
        boostRepository = storageBundle.boostRepository();

        // Create boost manager first (without config initially)
        boostManager = new BoostManager(this, null, messages, economyService, boostRepository);
        // Initialize API so custom effects can be registered
        EzBoostAPI.init(boostManager);

        // Register built-in custom effects before config loads so YAML can resolve them
        xpBoostEffect = new XpBoostEffect();
        EzBoostAPI.registerCustomBoostEffect(xpBoostEffect);

        // Now load config after API is initialized
        config = new EzBoostConfig(this);
        // Update boost manager with config
        boostManager.reload(config, messages, economyService);
        economyService.setup(config.economySettings());

        boostManager.loadStates();

        // Leaderboard (Jaloquent-backed — same backend as game state)
        boostLeaderboard = new BoostLeaderboard(storageBundle.leaderboardRepo(), getLogger());
        boostManager.setLeaderboard(boostLeaderboard);

        boostGui = new BoostGui(this, boostManager, config.guiSettings());
        adminGui = new AdminBoostCreationGui(this, boostManager, config, messages, boostGui);
        tokenFactory = new BoostTokenFactory(this);

        registerCommands();
        registerListeners();
        // Register PlaceholderAPI expansion if present
        try {
            if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
                try {
                    Class<?> clazz = Class.forName("com.skyblockexp.ezboost.placeholder.EzBoostPlaceholder");
                    java.lang.reflect.Constructor<?> ctor = clazz.getConstructor(EzBoostPlugin.class, BoostManager.class);
                    Object expansion = ctor.newInstance(this, boostManager);
                    try {
                        java.lang.reflect.Method m = expansion.getClass().getMethod("register");
                        m.invoke(expansion);
                        papiHooked = true;
                    } catch (NoSuchMethodException e) {
                        try {
                            Class<?> papi = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                            Class<?> expansionClass = Class.forName("me.clip.placeholderapi.expansion.PlaceholderExpansion");
                            java.lang.reflect.Method reg = papi.getMethod("registerExpansion", expansionClass);
                            reg.invoke(null, expansion);
                            papiHooked = true;
                        } catch (Throwable ignore) {
                            getLogger().warning("PlaceholderAPI found but expansion could not be registered.");
                        }
                    }
                } catch (ClassNotFoundException cnf) {
                    // EzBoostPlaceholder class missing - treat as optional
                }
            }
        } catch (NoClassDefFoundError | Exception ex) {
            // Ignore when PlaceholderAPI is not available at compile/runtime
        }

        initializeMetrics();
        new SpigotUpdateChecker(this, SPIGOT_RESOURCE_ID).checkForUpdates();

        StartupLogger.logEnable(
            getLogger(),
            getPluginMeta().getVersion(),
            boostManager.totalBoostCount(),
            boostManager.vaultHookAvailable(),
            papiHooked
        );
    }

    @Override
    public void onDisable() {
        if (boostManager != null) {
            boostManager.saveStates();
        }
        HandlerList.unregisterAll(this);
        StartupLogger.logDisable(getLogger());
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
        // /boosttop alias — delegates straight into /boost top logic
        PluginCommand boosttop = getCommand("boosttop");
        if (boosttop != null) {
            boosttop.setExecutor((sender, cmd, label, args) -> boostCommand.onCommand(sender, cmd, label, new String[]{"top"}));
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
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            getServer().getPluginManager().registerEvents(new EconomyServiceListener(config, economyService, getLogger()), this);
        }
        getServer().getPluginManager().registerEvents(new XpBoostListener(xpBoostEffect), this);
    }

    private void ensureResource(String name) {
        File file = new File(getDataFolder(), name);
        if (!file.exists()) {
            saveResource(name, false);
        }
    }

    private StorageSettings loadStorageSettings() {
        File file = new File(getDataFolder(), "storage.yml");
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        String backend = cfg.getString("storage.backend", "yaml");
        return new StorageSettings(
                backend,
                cfg.getString("storage.sqlite.file", "ezboost.db"),
                cfg.getString("storage." + (backend.equals("postgresql") ? "postgresql" : "mysql") + ".host", "localhost"),
                cfg.getInt("storage." + (backend.equals("postgresql") ? "postgresql" : "mysql") + ".port",
                        backend.equals("postgresql") ? 5432 : 3306),
                cfg.getString("storage." + (backend.equals("postgresql") ? "postgresql" : "mysql") + ".database", "ezboost"),
                cfg.getString("storage." + (backend.equals("postgresql") ? "postgresql" : "mysql") + ".username",
                        backend.equals("postgresql") ? "postgres" : "root"),
                cfg.getString("storage." + (backend.equals("postgresql") ? "postgresql" : "mysql") + ".password", ""),
                cfg.getInt("storage." + (backend.equals("postgresql") ? "postgresql" : "mysql") + ".pool-size", 10)
        );
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
