package com.skyblockexp.ezboost.command;

import com.skyblockexp.ezboost.boost.BoostDefinition;
import com.skyblockexp.ezboost.boost.BoostManager;
import com.skyblockexp.ezboost.config.Messages;
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
    private final Runnable reloadAction;

    /**
     * Constructs a new EzBoostCommand handler.
     * @param boostManager The boost manager instance
     * @param messages The messages configuration
     * @param tokenFactory The boost token item factory
     * @param reloadAction Runnable to reload plugin configuration
     */
    public EzBoostCommand(BoostManager boostManager, Messages messages, BoostTokenFactory tokenFactory, Runnable reloadAction) {
        this.boostManager = Objects.requireNonNull(boostManager, "boostManager");
        this.messages = Objects.requireNonNull(messages, "messages");
        this.tokenFactory = Objects.requireNonNull(tokenFactory, "tokenFactory");
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
            sender.sendMessage("Usage: /ezboost reload|give <player> <boostKey> [amount]");
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
        sender.sendMessage("Usage: /ezboost reload|give <player> <boostKey> [amount]");
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
            return List.of("reload", "give");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            List<String> names = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                names.add(player.getName());
            }
            return names;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give") && sender instanceof Player) {
            return new ArrayList<>(boostManager.getBoosts((Player)sender).keySet());
        }
        return List.of();
    }
}
