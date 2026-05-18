package com.skyblockexp.ezboost.command;

import com.skyblockexp.ezboost.boost.BoostDefinition;
import com.skyblockexp.ezboost.boost.BoostManager;
import com.skyblockexp.ezboost.config.Messages;
import com.skyblockexp.ezboost.gui.AdminBoostCreationGui;
import com.skyblockexp.ezboost.gui.BoostTokenFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Handles the /ezboost admin command, including reload and token-give functionality.
 * Provides tab completion for subcommands and arguments.
 */
public final class EzBoostCommand implements CommandExecutor, TabCompleter {
    private final BoostManager boostManager;
    private final Messages messages;
    private final BoostTokenFactory tokenFactory;
    private final AdminBoostCreationGui adminGui;
    private final Runnable reloadAction;

    /**
     * Constructs a new EzBoostCommand handler.
     * @param boostManager The boost manager instance
     * @param messages The messages configuration
     * @param tokenFactory The boost token item factory
     * @param adminGui The admin boost creation GUI
     * @param reloadAction Runnable to reload plugin configuration
     */
    public EzBoostCommand(BoostManager boostManager, Messages messages, BoostTokenFactory tokenFactory, AdminBoostCreationGui adminGui, Runnable reloadAction) {
        this.boostManager = Objects.requireNonNull(boostManager, "boostManager");
        this.messages = Objects.requireNonNull(messages, "messages");
        this.tokenFactory = Objects.requireNonNull(tokenFactory, "tokenFactory");
        this.adminGui = Objects.requireNonNull(adminGui, "adminGui");
        this.reloadAction = Objects.requireNonNull(reloadAction, "reloadAction");
    }

    /**
     * Handles the /ezboost command execution.
     * Supported: reload, give <player> <boostKey> [amount]
     * @param sender Command sender
     * @param command Command object
     * @param label Command label
     * @param args Command arguments
     * @return true if handled
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /ezboost reload|give <player> <boostKey> [amount]|create [continue]");
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("reload")) {
            if (!sender.hasPermission("ezboost.reload")) {
                sender.sendMessage(messages.message("no-permission"));
                return true;
            }
            reloadAction.run();
            sender.sendMessage(messages.message("reload"));
            return true;
        }
        if (sub.equals("give")) {
            if (!sender.hasPermission("ezboost.give")) {
                sender.sendMessage(messages.message("no-permission"));
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage("Usage: /ezboost give <player> <boostKey> [amount]");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(messages.message("invalid-target"));
                return true;
            }
            String boostKey = args[2].toLowerCase(Locale.ROOT);
            BoostDefinition definition = boostManager.getBoost(boostKey, target).orElse(null);
            if (definition == null) {
                sender.sendMessage(messages.message("boost-not-found"));
                return true;
            }
            int amount = 1;
            if (args.length > 3) {
                try {
                    amount = Integer.parseInt(args[3]);
                } catch (NumberFormatException ignored) {
                    amount = 1;
                }
            }
            if (amount <= 0) {
                amount = 1;
            }
            ItemStack item = tokenFactory.createToken(definition, amount);
            target.getInventory().addItem(item);
            sender.sendMessage(messages.message("token-given",
                    Placeholder.parsed("player", target.getName()),
                    Placeholder.parsed("boost", definition.key()),
                    Placeholder.parsed("amount", String.valueOf(amount))));
            return true;
        }
        if (sub.equals("create")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("This command can only be used by players.");
                return true;
            }
            if (!sender.hasPermission("ezboost.admin")) {
                sender.sendMessage(messages.message("no-permission"));
                return true;
            }
            if (args.length > 1 && "continue".equalsIgnoreCase(args[1])) {
                // Continue last session
                if (adminGui.hasSavedState(player)) {
                    adminGui.open(player); // This will load the saved state
                } else {
                    sender.sendMessage("§cNo previous boost creation session found to continue.");
                }
            } else {
                // Start new session
                adminGui.open(player);
            }
            return true;
        }
        if (sub.equals("about")) {
            if (!sender.hasPermission("ezboost.admin")) {
                sender.sendMessage(messages.message("no-permission"));
                return true;
            }
            org.bukkit.plugin.Plugin plugin = org.bukkit.Bukkit.getPluginManager().getPlugin("EzBoost");
            String pluginVersion = plugin != null ? plugin.getDescription().getVersion() : "unknown";
            String serverVersion = org.bukkit.Bukkit.getBukkitVersion();
            String serverType = org.bukkit.Bukkit.getVersion();
            String database = "data.yml (file)";
            String about = messages.plain("about",
                    Placeholder.parsed("server-version", serverVersion),
                    Placeholder.parsed("server-type", serverType),
                    Placeholder.parsed("database", database),
                    Placeholder.parsed("plugin-version", pluginVersion),
                    Placeholder.parsed("boosts", String.valueOf(boostManager.totalBoostCount())),
                    Placeholder.parsed("replace-active-boost", String.valueOf(boostManager.replaceActiveBoostEnabled())),
                    Placeholder.parsed("refund-on-fail", String.valueOf(boostManager.refundOnFailEnabled())),
                    Placeholder.parsed("keep-boost-on-death", String.valueOf(boostManager.keepBoostOnDeathEnabled())),
                    Placeholder.parsed("reapply-on-join", String.valueOf(boostManager.reapplyOnJoinEnabled())),
                    Placeholder.parsed("send-expired-message", String.valueOf(boostManager.sendExpiredMessageEnabled())),
                    Placeholder.parsed("cooldown-per-boost-type", String.valueOf(boostManager.cooldownPerBoostTypeEnabled())),
                    Placeholder.parsed("economy-enabled", String.valueOf(boostManager.economyEnabledInConfig())),
                    Placeholder.parsed("vault-hook", String.valueOf(boostManager.vaultHookAvailable())),
                    Placeholder.parsed("duration-min", String.valueOf(boostManager.limitsDurationMin())),
                    Placeholder.parsed("duration-max", String.valueOf(boostManager.limitsDurationMax())),
                    Placeholder.parsed("amplifier-min", String.valueOf(boostManager.limitsAmplifierMin())),
                    Placeholder.parsed("amplifier-max", String.valueOf(boostManager.limitsAmplifierMax()))
            );
            for (String line : about.split("\\n")) {
                sender.sendMessage(line);
            }
            return true;
        }
        sender.sendMessage("Usage: /ezboost reload|give <player> <boostKey> [amount]|create");
        return true;
    }

    /**
     * Provides tab completion for /ezboost subcommands and arguments.
     * @param sender Command sender
     * @param command Command object
     * @param alias Command alias
     * @param args Command arguments
     * @return List of tab completions
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission("ezboost.reload")) {
                completions.add("reload");
            }
            if (sender.hasPermission("ezboost.give")) {
                completions.add("give");
            }
            if (sender.hasPermission("ezboost.admin")) {
                completions.add("create");
                completions.add("about");
            }
            return completions;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("create") && sender.hasPermission("ezboost.admin")) {
            return List.of("continue");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give") && sender.hasPermission("ezboost.give")) {
            List<String> names = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                names.add(player.getName());
            }
            return names;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give") && sender.hasPermission("ezboost.give") && sender instanceof Player) {
            return new ArrayList<>(boostManager.getBoosts((Player)sender).keySet());
        }
        return List.of();
    }
}
