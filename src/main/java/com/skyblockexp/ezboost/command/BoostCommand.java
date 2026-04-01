package com.skyblockexp.ezboost.command;

import com.skyblockexp.ezboost.boost.BoostManager;
import com.skyblockexp.ezboost.boost.BoostManager.ActivationSource;
import com.skyblockexp.ezboost.config.Messages;
import com.skyblockexp.ezboost.gui.BoostGui;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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
        // Support /boost about for admins
        if (args.length >= 1 && "about".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("ezboost.admin")) {
                sender.sendMessage(messages.message("no-permission"));
                return true;
            }
            org.bukkit.plugin.Plugin plugin = org.bukkit.Bukkit.getPluginManager().getPlugin("EzBoost");
            String pluginVersion = plugin != null && plugin.getDescription() != null ? plugin.getDescription().getVersion() : "unknown";
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
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission("ezboost.admin") && "about".startsWith(prefix)) {
                completions.add("about");
            }
            if (sender instanceof Player) {
                for (String key : boostManager.getBoosts((Player)sender).keySet()) {
                    if (key.startsWith(prefix)) {
                        completions.add(key);
                    }
                }
            }
            return completions;
        }
        return List.of();
    }
}
