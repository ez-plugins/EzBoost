package com.skyblockexp.ezboost.command;

import com.skyblockexp.ezboost.boost.BoostManager;
import com.skyblockexp.ezboost.boost.BoostManager.ActivationSource;
import com.skyblockexp.ezboost.config.Messages;
import com.skyblockexp.ezboost.gui.BoostGui;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class BoostCommand implements CommandExecutor, TabCompleter {
    private final BoostManager boostManager;
    private final BoostGui boostGui;
    private final Messages messages;

    public BoostCommand(BoostManager boostManager, BoostGui boostGui, Messages messages) {
        this.boostManager = Objects.requireNonNull(boostManager, "boostManager");
        this.boostGui = Objects.requireNonNull(boostGui, "boostGui");
        this.messages = Objects.requireNonNull(messages, "messages");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.message("only-players"));
            return true;
        }
        if (args.length == 0) {
            if (boostGui.isEnabled()) {
                boostGui.open(player);
            } else {
                player.sendMessage(ChatColor.YELLOW + "Usage: /boost <boostKey>");
            }
            return true;
        }
        String boostKey = args[0].toLowerCase(Locale.ROOT);
        boostManager.activate(player, boostKey, ActivationSource.COMMAND);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender instanceof Player) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            List<String> completions = new ArrayList<>();
            for (String key : boostManager.getBoosts((Player)sender).keySet()) {
                if (key.startsWith(prefix)) {
                    completions.add(key);
                }
            }
            return completions;
        }
        return List.of();
    }
}
