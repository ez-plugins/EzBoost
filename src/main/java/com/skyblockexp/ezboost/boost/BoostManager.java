package com.skyblockexp.ezboost.boost;

import com.skyblockexp.ezboost.config.EzBoostConfig;
import com.skyblockexp.ezboost.config.Messages;
import com.skyblockexp.ezboost.economy.EconomyService;
import com.skyblockexp.ezboost.event.BoostEndEvent;
import com.skyblockexp.ezboost.event.BoostStartEvent;
import com.skyblockexp.ezboost.storage.BoostStorage;
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
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import com.skyblockexp.ezboost.FoliaScheduler;

import java.util.concurrent.ConcurrentMap;
import java.util.Collections;
import com.skyblockexp.ezboost.boost.CustomBoostEffect;
import com.skyblockexp.ezboost.economy.CurrencyFormatter;

public final class BoostManager {
    private static final String GLOBAL_COOLDOWN_KEY = "global";
    private final JavaPlugin plugin;
    private EzBoostConfig config;
    private Messages messages;
    private EconomyService economyService;
    private final BoostStorage storage;
    private final Logger logger;
    private final Map<UUID, BoostState> states = new ConcurrentHashMap<>();
    private final Map<UUID, FoliaScheduler.TaskHandle> expiryTasks = new HashMap<>();
    private final Map<UUID, FoliaScheduler.TaskHandle> actionbarTasks = new HashMap<>();
    private CurrencyFormatter currencyFormatter;

    // Custom effect registry
    private final ConcurrentMap<String, CustomBoostEffect> customEffects = new ConcurrentHashMap<>();
    /**
     * Register a custom boost effect from another plugin.
     * @param effect CustomBoostEffect implementation
     * @return true if registered, false if name already exists
     */
    public boolean registerCustomEffect(CustomBoostEffect effect) {
        Objects.requireNonNull(effect, "effect");
        String name = effect.getName().toLowerCase(Locale.ROOT);
        if (customEffects.containsKey(name)) {
            return false;
        }
        customEffects.put(name, effect);
        logger.info("Registered custom boost effect: " + name);
        return true;
    }

    /**
     * Adds a new boost dynamically and persists it to config.
     * @param boost The BoostDefinition to add
     * @return true if added successfully, false if key already exists
     */
    public boolean addBoost(BoostDefinition boost) {
        return config.addBoost(boost);
    }

    /**
     * Get a registered custom effect by name.
     */
    public CustomBoostEffect getCustomEffect(String name) {
        return customEffects.get(name.toLowerCase(Locale.ROOT));
    }

    /**
     * List all registered custom effects.
     */
    public Map<String, CustomBoostEffect> getCustomEffects() {
        return Collections.unmodifiableMap(customEffects);
    }

    public BoostManager(JavaPlugin plugin,
            EzBoostConfig config,
            Messages messages,
            EconomyService economyService,
            BoostStorage storage) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.config = config; // Allow null initially
        this.messages = Objects.requireNonNull(messages, "messages");
        this.economyService = Objects.requireNonNull(economyService, "economyService");
        this.storage = Objects.requireNonNull(storage, "storage");
        this.logger = plugin.getLogger();
        this.currencyFormatter = new CurrencyFormatter(config);
    }

    // Info accessors used by admin commands
    public int totalBoostCount() {
        return config == null ? 0 : config.boosts().size();
    }

    public boolean replaceActiveBoostEnabled() {
        return config != null && config.settings().replaceActiveBoost();
    }

    public boolean refundOnFailEnabled() {
        return config != null && config.settings().refundOnFail();
    }

    public boolean keepBoostOnDeathEnabled() {
        return config != null && config.settings().keepBoostOnDeath();
    }

    public boolean reapplyOnJoinEnabled() {
        return config != null && config.settings().reapplyOnJoin();
    }

    public boolean sendExpiredMessageEnabled() {
        return config != null && config.settings().sendExpiredMessage();
    }

    public boolean cooldownPerBoostTypeEnabled() {
        return config != null && config.settings().cooldownPerBoostType();
    }

    public boolean economyEnabledInConfig() {
        return config != null && config.economySettings() != null && config.economySettings().enabled();
    }

    public boolean vaultHookAvailable() {
        return economyService != null && economyService.isAvailable();
    }

    public int limitsDurationMin() {
        return config == null ? 0 : config.limits().durationMin();
    }

    public int limitsDurationMax() {
        return config == null ? 0 : config.limits().durationMax();
    }

    public int limitsAmplifierMin() {
        return config == null ? 0 : config.limits().amplifierMin();
    }

    public int limitsAmplifierMax() {
        return config == null ? 0 : config.limits().amplifierMax();
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
        this.currencyFormatter = new CurrencyFormatter(config);
        for (Player player : Bukkit.getOnlinePlayers()) {
            refreshPlayer(player);
        }
    }


    /**
     * Returns the effective BoostDefinition for a player in their current world, considering overrides.
     */
    public Optional<BoostDefinition> getBoost(String key, Player player) {
        if (key == null || player == null) {
            return Optional.empty();
        }
        String region = WorldGuardHelper.getHighestPriorityRegion(player);
        BoostDefinition def = config.getEffectiveBoost(key, player.getWorld().getName(), region);
        return Optional.ofNullable(def);
    }


    /**
     * Returns the effective boost map for a player in their current world, considering overrides.
     */
    public Map<String, BoostDefinition> getBoosts(Player player) {
        String region = WorldGuardHelper.getHighestPriorityRegion(player);
        Map<String, BoostDefinition> result = new HashMap<>();
        for (String key : config.boosts().keySet()) {
            BoostDefinition def = config.getEffectiveBoost(key, player.getWorld().getName(), region);
            if (def != null) {
                result.put(key, def);
            }
        }
        return result;
    }

    public boolean isActive(Player player, String boostKey) {
        BoostState state = states.get(player.getUniqueId());
        return state != null && boostKey.equalsIgnoreCase(state.activeBoostKey()) && state.endTimestamp() > System.currentTimeMillis();
    }

    /**
     * Returns the active boost key for the player, or {@code null} if no boost is currently active.
     */
    public String getActiveBoostKey(Player player) {
        BoostState state = states.get(player.getUniqueId());
        if (state == null || state.activeBoostKey() == null) return null;
        return state.endTimestamp() > System.currentTimeMillis() ? state.activeBoostKey() : null;
    }

    /**
     * Returns the remaining duration of the player's active boost in seconds, or {@code 0} if none.
     */
    public long getActiveBoostTimeRemaining(Player player) {
        BoostState state = states.get(player.getUniqueId());
        if (state == null || state.activeBoostKey() == null) return 0L;
        return Math.max(0L, (state.endTimestamp() - System.currentTimeMillis()) / 1000L);
    }

    public long getCooldownRemaining(Player player, String boostKey) {
        BoostState state = states.get(player.getUniqueId());
        if (state == null) {
            return 0L;
        }
        long now = System.currentTimeMillis();
        // If per-effect cooldowns are enabled, compute the maximum remaining across effects for the effective boost
        if (config.settings().cooldownPerEffect()) {
            Optional<BoostDefinition> def = getBoost(boostKey, player);
            if (def.isPresent()) {
                long maxRem = 0L;
                for (BoostEffect effect : def.get().effects()) {
                    long end = state.cooldownEnd(effectCooldownKey(effect));
                    long rem = Math.max(0L, (end - now) / 1000L);
                    if (rem > maxRem) maxRem = rem;
                }
                // fallback to boost-level key as well
                long boostRem = Math.max(0L, (state.cooldownEnd(cooldownKey(boostKey)) - now) / 1000L);
                return Math.max(maxRem, boostRem);
            }
        }
        long remaining = state.cooldownEnd(cooldownKey(boostKey)) - now;
        return Math.max(0L, remaining / 1000L);
    }

    /**
     * Returns remaining cooldown in seconds for a specific effect for the given player.
     * Returns 0 if no cooldown is present.
     */
    public long getCooldownRemainingForEffect(Player player, BoostEffect effect) {
        if (player == null || effect == null) return 0L;
        BoostState state = states.get(player.getUniqueId());
        if (state == null) return 0L;
        long now = System.currentTimeMillis();
        long end = state.cooldownEnd(effectCooldownKey(effect));
        long rem = Math.max(0L, (end - now) / 1000L);
        return rem;
    }


    public boolean activate(Player player, String boostKey, ActivationSource source) {
        Optional<BoostDefinition> definition = getBoost(boostKey, player);
        if (definition.isEmpty()) {
            player.sendMessage(messages.message("boost-not-found"));
            return false;
        }
        return activate(player, definition.get(), source);
    }

    public boolean activate(Player player, BoostDefinition boost, ActivationSource source) {
        // Always use the effective boost for the player's world/region
        String region = WorldGuardHelper.getHighestPriorityRegion(player);
        BoostDefinition effective = config.getEffectiveBoost(boost.key(), player.getWorld().getName(), region);
        if (effective == null) {
            player.sendMessage(messages.message("boost-not-found"));
            return false;
        }
        if (!effective.enabled()) {
            player.sendMessage(messages.message("boost-disabled"));
            return false;
        }
        if (!config.worldSettings().isAllowed(player.getWorld().getName())) {
            player.sendMessage(messages.message("world-blocked"));
            return false;
        }
        if (effective.permission() != null && !effective.permission().isBlank() && !player.hasPermission(effective.permission())) {
            player.sendMessage(messages.message("no-permission"));
            return false;
        }
        BoostState state = states.computeIfAbsent(player.getUniqueId(), id -> new BoostState());
        long now = System.currentTimeMillis();
        // Only block if the SAME boost type is active
        if (state.activeBoostKey() != null && state.endTimestamp() > now) {
            if (state.activeBoostKey().equalsIgnoreCase(effective.key())) {
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
            if (config.settings().cooldownPerEffect()) {
                for (BoostEffect effect : effective.effects()) {
                    String key = effectCooldownKey(effect);
                    long end = state.cooldownEnd(key);
                    if (end > now) {
                        long remaining = Math.max(0L, (end - now) / 1000L);
                        // Determine a human-friendly effect name
                        String effectName;
                        if (effect.type() != null) {
                            effectName = effect.type().key().value();
                        } else {
                            CustomBoostEffect custom = customEffects.get(effect.customName().toLowerCase(Locale.ROOT));
                            effectName = custom != null ? custom.getName() : effect.customName();
                        }
                        try {
                            player.sendMessage(messages.message("boost-effect-cooldown",
                                    BoostTagResolvers.forBoost(effective, currencyFormatter),
                                    Placeholder.parsed("time", String.valueOf(remaining)),
                                    Placeholder.parsed("effect", effectName)));
                        } catch (Exception ex) {
                            // Fallback to generic message if key missing
                            player.sendMessage(messages.message("boost-cooldown",
                                    BoostTagResolvers.forBoost(effective, currencyFormatter),
                                    Placeholder.parsed("time", String.valueOf(remaining))));
                        }
                        return false;
                    }
                }
                // also check boost-level fallback
                long cooldownEnd = state.cooldownEnd(cooldownKey(effective.key()));
                if (cooldownEnd > now) {
                    long remaining = Math.max(0L, (cooldownEnd - now) / 1000L);
                    player.sendMessage(messages.message("boost-cooldown",
                            BoostTagResolvers.forBoost(effective, currencyFormatter),
                            Placeholder.parsed("time", String.valueOf(remaining))));
                    return false;
                }
            } else {
                long cooldownEnd = state.cooldownEnd(cooldownKey(effective.key()));
                if (cooldownEnd > now) {
                    long remaining = Math.max(0L, (cooldownEnd - now) / 1000L);
                    player.sendMessage(messages.message("boost-cooldown",
                            BoostTagResolvers.forBoost(effective, currencyFormatter),
                            Placeholder.parsed("time", String.valueOf(remaining))));
                    return false;
                }
            }
        }
        double cost = effective.cost();
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
                player.sendMessage(messages.message("insufficient-funds",
                    BoostTagResolvers.forBoost(effective, currencyFormatter)));
                return false;
            }
            charged = true;
        }
        try {
            applyBoost(player, effective);
        } catch (Exception ex) {
            logger.warning("EzBoost failed to apply boost " + effective.key() + ": " + ex.getMessage());
            if (charged && config.settings().refundOnFail()) {
                economyService.deposit(player, cost);
            }
            return false;
        }
        // Fire BoostStartEvent with full context
        BoostStartEvent startEvent = new BoostStartEvent(player, effective.key(), effective);
        Bukkit.getPluginManager().callEvent(startEvent);
        if (startEvent.isCancelled()) {
            return false;
        }
        long endTimestamp = now + (effective.durationSeconds() * 1000L);
        state.setActiveBoost(effective.key(), endTimestamp);
        if (config.settings().cooldownPerEffect()) {
            for (BoostEffect effect : effective.effects()) {
                int seconds = 0;
                if (effect.type() == null) {
                    CustomBoostEffect custom = customEffects.get(effect.customName().toLowerCase(Locale.ROOT));
                    if (custom != null) {
                        seconds = custom.getCooldownSeconds();
                    } else {
                        seconds = effective.cooldownSeconds();
                    }
                } else {
                    seconds = effective.cooldownSeconds();
                }
                if (seconds > 0) {
                    state.setCooldownEnd(effectCooldownKey(effect), now + (seconds * 1000L));
                }
            }
        } else {
            if (effective.cooldownSeconds() > 0) {
                state.setCooldownEnd(cooldownKey(effective.key()), now + (effective.cooldownSeconds() * 1000L));
            }
        }
        scheduleExpiry(player, effective, endTimestamp);
        scheduleActionbar(player, effective);
        runEnableCommands(player, effective);
        player.sendMessage(messages.message("boost-activated", BoostTagResolvers.forBoost(effective, currencyFormatter)));
        if (charged) {
            player.sendMessage(messages.message("cost-charged", BoostTagResolvers.forBoost(effective, currencyFormatter)));
        }
        if (source == ActivationSource.TOKEN) {
            player.sendMessage(messages.message("token-used", BoostTagResolvers.forBoost(effective, currencyFormatter)));
        }
        saveStates();
        return true;
    }

    public void handleJoin(Player player) {
        BoostState state = states.computeIfAbsent(player.getUniqueId(), id -> new BoostState());
        if (state.activeBoostKey() == null) {
            return;
        }
        String region = WorldGuardHelper.getHighestPriorityRegion(player);
        BoostDefinition definition = config.getEffectiveBoost(state.activeBoostKey(), player.getWorld().getName(), region);
        long now = System.currentTimeMillis();
        if (definition == null || state.endTimestamp() <= now) {
            state.clearActiveBoost();
            saveStates();
            return;
        }
        if (!config.settings().reapplyOnJoin()) {
            return;
        }
        BoostStartEvent startEvent = new BoostStartEvent(player, definition.key(), definition);
        Bukkit.getPluginManager().callEvent(startEvent);
        if (startEvent.isCancelled()) {
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
        String region = WorldGuardHelper.getHighestPriorityRegion(player);
        BoostDefinition definition = config.getEffectiveBoost(state.activeBoostKey(), player.getWorld().getName(), region);
        BoostEndEvent endEvent = new BoostEndEvent(player, state.activeBoostKey(), definition);
        Bukkit.getPluginManager().callEvent(endEvent);
        if (!endEvent.isCancelled()) {
            clearActiveBoost(player, state, true);
            saveStates();
        }
    }

    private void refreshPlayer(Player player) {
        BoostState state = states.get(player.getUniqueId());
        if (state == null || state.activeBoostKey() == null) {
            return;
        }
        String region = WorldGuardHelper.getHighestPriorityRegion(player);
        BoostDefinition definition = config.getEffectiveBoost(state.activeBoostKey(), player.getWorld().getName(), region);
        if (definition == null || !definition.enabled()) {
            BoostEndEvent endEvent = new BoostEndEvent(player, state.activeBoostKey(), definition);
            Bukkit.getPluginManager().callEvent(endEvent);
            if (!endEvent.isCancelled()) {
                clearActiveBoost(player, state, true);
                saveStates();
            }
        }
    }

    private void applyBoost(Player player, BoostDefinition boost) {
        applyBoost(player, boost, boost.durationSeconds());
    }

    private void applyBoost(Player player, BoostDefinition boost, int durationSeconds) {
        for (BoostEffect effect : boost.effects()) {
            if (effect.type() != null) {
                PotionEffect potionEffect = new PotionEffect(effect.type(), durationSeconds * 20, effect.amplifier(), false, true, true);
                player.addPotionEffect(potionEffect);
            } else {
                // Custom effect: look up by name
                CustomBoostEffect custom = customEffects.get(effect.customName().toLowerCase(Locale.ROOT));
                if (custom != null) {
                    custom.apply(player, effect.amplifier());
                }
            }
        }
    }

    private void clearActiveBoost(Player player, BoostState state, boolean silent) {
        String activeKey = state.activeBoostKey();
        if (activeKey == null) {
            return;
        }
        String region = WorldGuardHelper.getHighestPriorityRegion(player);
        BoostDefinition boost = config.getEffectiveBoost(activeKey, player.getWorld().getName(), region);
        if (boost != null) {
            BoostEndEvent endEvent = new BoostEndEvent(player, activeKey, boost);
            Bukkit.getPluginManager().callEvent(endEvent);
            if (!endEvent.isCancelled()) {
                for (BoostEffect effect : boost.effects()) {
                    if (effect.type() != null) {
                        player.removePotionEffect(effect.type());
                    } else {
                        CustomBoostEffect custom = customEffects.get(effect.customName().toLowerCase(Locale.ROOT));
                        if (custom != null) {
                            custom.remove(player);
                        }
                    }
                }
                runDisableCommands(player, boost);
                if (!silent && config.settings().sendExpiredMessage()) {
                    player.sendMessage(messages.message("boost-expired", BoostTagResolvers.forBoost(boost, currencyFormatter)));
                }
            }
        }
        state.clearActiveBoost();
        cancelExpiryTask(player.getUniqueId());
        cancelActionbarTask(player.getUniqueId());
    }

    private void scheduleExpiry(Player player, BoostDefinition boost, long endTimestamp) {
        cancelExpiryTask(player.getUniqueId());
        long delayTicks = Math.max(1L, (endTimestamp - System.currentTimeMillis()) / 50L);
        FoliaScheduler.TaskHandle task = FoliaScheduler.runEntityTaskLater(plugin, player, () -> {
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
        FoliaScheduler.TaskHandle task = FoliaScheduler.runEntityTaskTimer(plugin, player, () -> {
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
        FoliaScheduler.TaskHandle task = expiryTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }

    private void cancelActionbarTask(UUID uuid) {
        FoliaScheduler.TaskHandle task = actionbarTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Returns the configured CurrencyFormatter.
     */
    public com.skyblockexp.ezboost.economy.CurrencyFormatter currencyFormatter() {
        return this.currencyFormatter;
    }

    /**
     * Check if player can afford the given cost using the configured economy.
     */
    public boolean canAfford(Player player, double cost) {
        if (cost <= 0.0) return true;
        return economyService != null && economyService.isAvailable() && economyService.has(player, cost);
    }

    /**
     * Returns configured provider currency label if set, otherwise null.
     */
    public String currencyLabel() {
        EzBoostConfig.EconomySettings es = config != null ? config.economySettings() : null;
        if (es == null) return null;
        return es.providerCurrency();
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

    private String effectCooldownKey(BoostEffect effect) {
        if (effect == null) return GLOBAL_COOLDOWN_KEY;
        if (effect.type() != null) {
            String name = effect.type().key().value();
            return ("effect:potion:" + name).toLowerCase(Locale.ROOT);
        } else {
            String cname = effect.customName() != null ? effect.customName() : "";
            return ("effect:custom:" + cname).toLowerCase(Locale.ROOT);
        }
    }

    private void sendActionBar(Player player, String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        player.sendActionBar(Component.text(message));
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
                      .replace("{displayname}", player.getName())
                    .replace("{boost}", boost.key());
            if (FoliaScheduler.FOLIA) {
                FoliaScheduler.runGlobal(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed));
            } else {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
            }
        }
    }

    public enum ActivationSource {
        COMMAND,
        GUI,
        TOKEN
    }
}
