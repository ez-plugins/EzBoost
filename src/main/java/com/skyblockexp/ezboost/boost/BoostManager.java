package com.skyblockexp.ezboost.boost;

import com.skyblockexp.ezboost.config.EzBoostConfig;
import com.skyblockexp.ezboost.config.Messages;
import com.skyblockexp.ezboost.economy.EconomyService;
import com.skyblockexp.ezboost.storage.BoostStorage;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;

public final class BoostManager {
    private static final String GLOBAL_COOLDOWN_KEY = "global";
    private final JavaPlugin plugin;
    private EzBoostConfig config;
    private Messages messages;
    private EconomyService economyService;
    private final BoostStorage storage;
    private final Logger logger;
    private final Map<UUID, BoostState> states = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> expiryTasks = new HashMap<>();
    private final Map<UUID, BukkitTask> actionbarTasks = new HashMap<>();

    public BoostManager(JavaPlugin plugin,
            EzBoostConfig config,
            Messages messages,
            EconomyService economyService,
            BoostStorage storage) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.config = Objects.requireNonNull(config, "config");
        this.messages = Objects.requireNonNull(messages, "messages");
        this.economyService = Objects.requireNonNull(economyService, "economyService");
        this.storage = Objects.requireNonNull(storage, "storage");
        this.logger = plugin.getLogger();
    }

    public void loadStates() {
        states.clear();
        states.putAll(storage.load());
        for (Player player : Bukkit.getOnlinePlayers()) {
            handleJoin(player);
        }
    }

    public void saveStates() {
        storage.save(states);
    }

    public void reload(EzBoostConfig config, Messages messages, EconomyService economyService) {
        this.config = config;
        this.messages = messages;
        this.economyService = economyService;
        for (Player player : Bukkit.getOnlinePlayers()) {
            refreshPlayer(player);
        }
    }

    public Optional<BoostDefinition> getBoost(String key) {
        if (key == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(config.boosts().get(key.toLowerCase(Locale.ROOT)));
    }

    public Map<String, BoostDefinition> getBoosts() {
        return config.boosts();
    }

    public boolean isActive(Player player, String boostKey) {
        BoostState state = states.get(player.getUniqueId());
        return state != null && boostKey.equalsIgnoreCase(state.activeBoostKey()) && state.endTimestamp() > System.currentTimeMillis();
    }

    public long getCooldownRemaining(Player player, String boostKey) {
        BoostState state = states.get(player.getUniqueId());
        if (state == null) {
            return 0L;
        }
        long remaining = state.cooldownEnd(cooldownKey(boostKey)) - System.currentTimeMillis();
        return Math.max(0L, remaining / 1000L);
    }

    public boolean activate(Player player, String boostKey, ActivationSource source) {
        Optional<BoostDefinition> definition = getBoost(boostKey);
        if (definition.isEmpty()) {
            player.sendMessage(messages.message("boost-not-found"));
            return false;
        }
        return activate(player, definition.get(), source);
    }

    public boolean activate(Player player, BoostDefinition boost, ActivationSource source) {
        if (!boost.enabled()) {
            player.sendMessage(messages.message("boost-disabled"));
            return false;
        }
        if (!config.worldSettings().isAllowed(player.getWorld().getName())) {
            player.sendMessage(messages.message("world-blocked"));
            return false;
        }
        if (boost.permission() != null && !boost.permission().isBlank() && !player.hasPermission(boost.permission())) {
            player.sendMessage(messages.message("no-permission"));
            return false;
        }
        BoostState state = states.computeIfAbsent(player.getUniqueId(), id -> new BoostState());
        long now = System.currentTimeMillis();
        // Only block if the SAME boost type is active
        if (state.activeBoostKey() != null && state.endTimestamp() > now) {
            if (state.activeBoostKey().equalsIgnoreCase(boost.key())) {
                player.sendMessage(messages.message("boost-active"));
                return false;
            }
            boolean canReplace = config.settings().replaceActiveBoost() || config.settings().cooldownPerBoostType();
            if (!canReplace) {
                player.sendMessage(messages.message("boost-active"));
                return false;
            }
            clearActiveBoost(player, state, false);
            player.sendMessage(messages.message("boost-replaced"));
        }
        if (!player.hasPermission("ezboost.cooldown.bypass")) {
            long cooldownEnd = state.cooldownEnd(cooldownKey(boost.key()));
            if (cooldownEnd > now) {
                long remaining = Math.max(0L, (cooldownEnd - now) / 1000L);
                player.sendMessage(messages.message("boost-cooldown", Placeholder.parsed("time", String.valueOf(remaining))));
                return false;
            }
        }
        double cost = boost.cost();
        EzBoostConfig.EconomySettings economySettings = config.economySettings();
        if (cost > 0.0 && economySettings != null && economySettings.enabled() && !economyService.isAvailable()) {
            player.sendMessage(messages.message("economy-unavailable"));
            logger.warning("EzBoost economy is enabled but Vault is unavailable; blocking boost activation.");
            return false;
        }
        boolean charged = false;
        if (cost > 0.0 && economyService.isAvailable()) {
            EconomyResponse response = economyService.withdraw(player, cost);
            if (!response.transactionSuccess()) {
                player.sendMessage(messages.message("insufficient-funds", Placeholder.parsed("cost", formatCost(cost))));
                return false;
            }
            charged = true;
        }
        try {
            applyBoost(player, boost);
        } catch (Exception ex) {
            logger.warning("EzBoost failed to apply boost " + boost.key() + ": " + ex.getMessage());
            if (charged && config.settings().refundOnFail()) {
                economyService.deposit(player, cost);
            }
            return false;
        }
        long endTimestamp = now + (boost.durationSeconds() * 1000L);
        state.setActiveBoost(boost.key(), endTimestamp);
        if (boost.cooldownSeconds() > 0) {
            state.setCooldownEnd(cooldownKey(boost.key()), now + (boost.cooldownSeconds() * 1000L));
        }
        scheduleExpiry(player, boost, endTimestamp);
        scheduleActionbar(player, boost);
        runEnableCommands(player, boost);
        player.sendMessage(messages.message("boost-activated", Placeholder.parsed("boost", boost.displayName())));
        if (charged) {
            player.sendMessage(messages.message("cost-charged", Placeholder.parsed("boost", boost.key()),
                    Placeholder.parsed("cost", formatCost(cost))));
        }
        if (source == ActivationSource.TOKEN) {
            player.sendMessage(messages.message("token-used", Placeholder.parsed("boost", boost.key())));
        }
        saveStates();
        return true;
    }

    public void handleJoin(Player player) {
        BoostState state = states.computeIfAbsent(player.getUniqueId(), id -> new BoostState());
        if (state.activeBoostKey() == null) {
            return;
        }
        BoostDefinition definition = config.boosts().get(state.activeBoostKey());
        long now = System.currentTimeMillis();
        if (definition == null || state.endTimestamp() <= now) {
            state.clearActiveBoost();
            saveStates();
            return;
        }
        if (!config.settings().reapplyOnJoin()) {
            return;
        }
        applyBoost(player, definition, (int) Math.max(1, (state.endTimestamp() - now) / 1000L));
        runEnableCommands(player, definition);
        scheduleExpiry(player, definition, state.endTimestamp());
        scheduleActionbar(player, definition);
    }

    public void handleQuit(Player player) {
        cancelExpiryTask(player.getUniqueId());
        cancelActionbarTask(player.getUniqueId());
    }

    public void handleDeath(Player player) {
        if (config.settings().keepBoostOnDeath()) {
            return;
        }
        BoostState state = states.get(player.getUniqueId());
        if (state == null || state.activeBoostKey() == null) {
            return;
        }
        clearActiveBoost(player, state, true);
        saveStates();
    }

    private void refreshPlayer(Player player) {
        BoostState state = states.get(player.getUniqueId());
        if (state == null || state.activeBoostKey() == null) {
            return;
        }
        BoostDefinition definition = config.boosts().get(state.activeBoostKey());
        if (definition == null || !definition.enabled()) {
            clearActiveBoost(player, state, true);
            saveStates();
        }
    }

    private void applyBoost(Player player, BoostDefinition boost) {
        applyBoost(player, boost, boost.durationSeconds());
    }

    private void applyBoost(Player player, BoostDefinition boost, int durationSeconds) {
        for (BoostEffect effect : boost.effects()) {
            PotionEffect potionEffect = new PotionEffect(effect.type(), durationSeconds * 20, effect.amplifier(), false, true, true);
            player.addPotionEffect(potionEffect, true);
        }
    }

    private void clearActiveBoost(Player player, BoostState state, boolean silent) {
        String activeKey = state.activeBoostKey();
        if (activeKey == null) {
            return;
        }
        BoostDefinition boost = config.boosts().get(activeKey);
        if (boost != null) {
            for (BoostEffect effect : boost.effects()) {
                player.removePotionEffect(effect.type());
            }
            runDisableCommands(player, boost);
            if (!silent && config.settings().sendExpiredMessage()) {
                player.sendMessage(messages.message("boost-expired", Placeholder.parsed("boost", activeKey)));
            }
        }
        state.clearActiveBoost();
        cancelExpiryTask(player.getUniqueId());
        cancelActionbarTask(player.getUniqueId());
    }

    private void scheduleExpiry(Player player, BoostDefinition boost, long endTimestamp) {
        cancelExpiryTask(player.getUniqueId());
        long delayTicks = Math.max(1L, (endTimestamp - System.currentTimeMillis()) / 50L);
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            BoostState state = states.get(player.getUniqueId());
            if (state == null || state.activeBoostKey() == null) {
                return;
            }
            if (!boost.key().equalsIgnoreCase(state.activeBoostKey())) {
                return;
            }
            clearActiveBoost(player, state, false);
            saveStates();
        }, delayTicks);
        expiryTasks.put(player.getUniqueId(), task);
    }

    private void scheduleActionbar(Player player, BoostDefinition boost) {
        if (!messages.actionbarEnabled()) {
            return;
        }
        cancelActionbarTask(player.getUniqueId());
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            BoostState state = states.get(player.getUniqueId());
            if (state == null || state.activeBoostKey() == null) {
                cancelActionbarTask(player.getUniqueId());
                return;
            }
            long remaining = Math.max(0L, (state.endTimestamp() - System.currentTimeMillis()) / 1000L);
            if (remaining <= 0L) {
                cancelActionbarTask(player.getUniqueId());
                return;
            }
            sendActionBar(player, messages.actionbar(boost.key(), remaining));
        }, 20L, 20L);
        actionbarTasks.put(player.getUniqueId(), task);
    }

    private void cancelExpiryTask(UUID uuid) {
        BukkitTask task = expiryTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }

    private void cancelActionbarTask(UUID uuid) {
        BukkitTask task = actionbarTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }

    private String formatCost(double cost) {
        if (cost == Math.floor(cost)) {
            return String.valueOf((int) cost);
        }
        return String.format(Locale.US, "%.2f", cost);
    }

    private String cooldownKey(String boostKey) {
        if (boostKey == null) {
            return GLOBAL_COOLDOWN_KEY;
        }
        if (config.settings().cooldownPerBoostType()) {
            return boostKey.toLowerCase(Locale.ROOT);
        }
        return GLOBAL_COOLDOWN_KEY;
    }

    private void sendActionBar(Player player, String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        try {
            Method method = player.getClass().getMethod("sendActionBar", String.class);
            method.invoke(player, message);
            return;
        } catch (NoSuchMethodException ignored) {
        } catch (ReflectiveOperationException ex) {
            logger.fine("Failed to use sendActionBar(String): " + ex.getMessage());
        }
        try {
            Class<?> chatMessageType = Class.forName("net.md_5.bungee.api.ChatMessageType");
            Class<?> baseComponent = Class.forName("net.md_5.bungee.api.chat.BaseComponent");
            Class<?> textComponent = Class.forName("net.md_5.bungee.api.chat.TextComponent");
            Object actionBar = Enum.valueOf((Class<Enum>) chatMessageType, "ACTION_BAR");
            Object component = textComponent.getConstructor(String.class).newInstance(message);
            Object spigot = player.getClass().getMethod("spigot").invoke(player);
            Method sendMessage = spigot.getClass().getMethod("sendMessage", chatMessageType, Array.newInstance(baseComponent, 0).getClass());
            Object array = Array.newInstance(baseComponent, 1);
            Array.set(array, 0, component);
            sendMessage.invoke(spigot, actionBar, array);
        } catch (ReflectiveOperationException ex) {
            player.sendMessage(message);
        }
    }

    private void runEnableCommands(Player player, BoostDefinition boost) {
        runCommands(player, boost, boost.commands().enable());
        runCommands(player, boost, boost.commands().toggle());
    }

    private void runDisableCommands(Player player, BoostDefinition boost) {
        runCommands(player, boost, boost.commands().disable());
        runCommands(player, boost, boost.commands().toggle());
    }

    private void runCommands(Player player, BoostDefinition boost, List<String> commands) {
        if (commands == null || commands.isEmpty()) {
            return;
        }
        for (String command : commands) {
            if (command == null || command.isBlank()) {
                continue;
            }
            String parsed = command
                    .replace("{player}", player.getName())
                    .replace("{displayname}", player.getDisplayName())
                    .replace("{boost}", boost.key());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
        }
    }

    public enum ActivationSource {
        COMMAND,
        GUI,
        TOKEN
    }
}
