package com.skyblockexp.ezboost.command;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import com.skyblockexp.ezboost.boost.BoostDefinition;
import com.skyblockexp.ezboost.boost.BoostManager;
import com.skyblockexp.ezboost.storage.BoostLeaderboard;
import com.skyblockexp.ezboost.config.Messages;
import com.skyblockexp.ezboost.gui.AdminBoostCreationGui;
import com.skyblockexp.ezboost.gui.BoostTokenFactory;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class EzBoostCommandTest {

    private ServerMock server;
    private JavaPlugin mockPlugin;
    private BoostManager boostManager;
    private Messages messages;
    private BoostTokenFactory tokenFactory;
    private AdminBoostCreationGui adminGui;
    private Runnable reloadAction;
    private Command command;
    private EzBoostCommand ezBoostCommand;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        mockPlugin = MockBukkit.createMockPlugin();
        boostManager = mock(BoostManager.class);
        messages = mock(Messages.class);
        tokenFactory = mock(BoostTokenFactory.class);
        adminGui = mock(AdminBoostCreationGui.class);
        reloadAction = mock(Runnable.class);
        command = mock(Command.class);
        // Default stubs – prevent NPE on sendMessage(messages.message(...))
        lenient().when(messages.message(anyString(), any(TagResolver[].class))).thenReturn("");
        lenient().when(messages.plain(anyString(), any(TagResolver[].class))).thenReturn("");
        // Default stub for createToken – prevents NPE when addItem is called
        lenient().when(tokenFactory.createToken(any(), anyInt()))
                .thenReturn(new ItemStack(Material.PAPER));
        ezBoostCommand = new EzBoostCommand(boostManager, messages, tokenFactory, adminGui, reloadAction);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    // ── no args ───────────────────────────────────────────────────────────────

    @Test
    void onCommand_noArgs_sendsUsageAndReturnsTrue() {
        CommandSender console = server.getConsoleSender();
        boolean result = ezBoostCommand.onCommand(console, command, "ezboost", new String[]{});
        assertTrue(result);
    }

    // ── /ezboost reload ──────────────────────────────────────────────────────

    @Test
    void onCommand_reload_noPermission_deniesAccess() {
        PlayerMock player = server.addPlayer();
        // No ezboost.reload permission
        boolean result = ezBoostCommand.onCommand(player, command, "ezboost", new String[]{"reload"});
        assertTrue(result);
        verify(messages).message("no-permission");
        verify(reloadAction, never()).run();
    }

    @Test
    void onCommand_reload_withPermission_runsReloadAndSendsMessage() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(mockPlugin, "ezboost.reload", true);
        boolean result = ezBoostCommand.onCommand(player, command, "ezboost", new String[]{"reload"});
        assertTrue(result);
        verify(reloadAction).run();
        verify(messages).message("reload");
    }

    @Test
    void onCommand_reload_consoleSender_runsReload() {
        // Console has all permissions
        CommandSender console = server.getConsoleSender();
        ezBoostCommand.onCommand(console, command, "ezboost", new String[]{"reload"});
        verify(reloadAction).run();
    }

    // ── /ezboost give ────────────────────────────────────────────────────────

    @Test
    void onCommand_give_noPermission_deniesAccess() {
        PlayerMock player = server.addPlayer();
        // No ezboost.give permission
        boolean result = ezBoostCommand.onCommand(player, command, "ezboost",
                new String[]{"give", "Alice", "speed"});
        assertTrue(result);
        verify(messages).message("no-permission");
    }

    @Test
    void onCommand_give_tooFewArgs_sendsUsage() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(mockPlugin, "ezboost.give", true);
        boolean result = ezBoostCommand.onCommand(player, command, "ezboost",
                new String[]{"give", "Alice"});
        assertTrue(result);
        verify(tokenFactory, never()).createToken(any(), anyInt());
    }

    @Test
    void onCommand_give_playerNotOnline_sendsInvalidTarget() {
        CommandSender console = server.getConsoleSender();
        // "OfflinePlayer" is not an online player in the server
        boolean result = ezBoostCommand.onCommand(console, command, "ezboost",
                new String[]{"give", "OfflinePlayer", "speed"});
        assertTrue(result);
        verify(messages).message("invalid-target");
    }

    @Test
    void onCommand_give_invalidBoostKey_sendsBoostNotFound() {
        PlayerMock target = server.addPlayer("Target");
        CommandSender console = server.getConsoleSender();
        when(boostManager.getBoost(eq("unknown"), eq(target))).thenReturn(Optional.empty());
        boolean result = ezBoostCommand.onCommand(console, command, "ezboost",
                new String[]{"give", "Target", "unknown"});
        assertTrue(result);
        verify(messages).message("boost-not-found");
    }

    @Test
    void onCommand_give_validArgs_givesTokenToTarget() {
        PlayerMock target = server.addPlayer("Target");
        CommandSender console = server.getConsoleSender();
        BoostDefinition definition = mock(BoostDefinition.class);
        when(definition.key()).thenReturn("speed");
        when(boostManager.getBoost(eq("speed"), eq(target))).thenReturn(Optional.of(definition));
        ItemStack token = new ItemStack(Material.PAPER);
        when(tokenFactory.createToken(definition, 1)).thenReturn(token);
        boolean result = ezBoostCommand.onCommand(console, command, "ezboost",
                new String[]{"give", "Target", "speed"});
        assertTrue(result);
        verify(tokenFactory).createToken(definition, 1);
        verify(messages).message(eq("token-given"), any(TagResolver[].class));
    }

    @Test
    void onCommand_give_customAmount_givesCorrectCount() {
        PlayerMock target = server.addPlayer("TargetAmt");
        CommandSender console = server.getConsoleSender();
        BoostDefinition definition = mock(BoostDefinition.class);
        when(definition.key()).thenReturn("speed");
        when(boostManager.getBoost(eq("speed"), eq(target))).thenReturn(Optional.of(definition));
        when(tokenFactory.createToken(definition, 3)).thenReturn(new ItemStack(Material.PAPER, 3));
        ezBoostCommand.onCommand(console, command, "ezboost",
                new String[]{"give", "TargetAmt", "speed", "3"});
        verify(tokenFactory).createToken(definition, 3);
    }

    @Test
    void onCommand_give_invalidAmountString_defaultsToOne() {
        PlayerMock target = server.addPlayer("TargetBad");
        CommandSender console = server.getConsoleSender();
        BoostDefinition definition = mock(BoostDefinition.class);
        when(definition.key()).thenReturn("speed");
        when(boostManager.getBoost(eq("speed"), eq(target))).thenReturn(Optional.of(definition));
        ezBoostCommand.onCommand(console, command, "ezboost",
                new String[]{"give", "TargetBad", "speed", "notanumber"});
        verify(tokenFactory).createToken(definition, 1);
    }

    @Test
    void onCommand_give_zeroAmount_defaultsToOne() {
        PlayerMock target = server.addPlayer("TargetZero");
        CommandSender console = server.getConsoleSender();
        BoostDefinition definition = mock(BoostDefinition.class);
        when(definition.key()).thenReturn("speed");
        when(boostManager.getBoost(eq("speed"), eq(target))).thenReturn(Optional.of(definition));
        ezBoostCommand.onCommand(console, command, "ezboost",
                new String[]{"give", "TargetZero", "speed", "0"});
        verify(tokenFactory).createToken(definition, 1);
    }

    // ── /ezboost create ──────────────────────────────────────────────────────

    @Test
    void onCommand_create_consoleSender_sendsPlayerOnlyError() {
        CommandSender console = server.getConsoleSender();
        boolean result = ezBoostCommand.onCommand(console, command, "ezboost",
                new String[]{"create"});
        assertTrue(result);
        verify(adminGui, never()).open(any());
    }

    @Test
    void onCommand_create_noPermission_deniesAccess() {
        PlayerMock player = server.addPlayer();
        // No ezboost.admin permission
        boolean result = ezBoostCommand.onCommand(player, command, "ezboost",
                new String[]{"create"});
        assertTrue(result);
        verify(messages).message("no-permission");
        verify(adminGui, never()).open(any());
    }

    @Test
    void onCommand_create_withPermission_opensAdminGui() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(mockPlugin, "ezboost.admin", true);
        boolean result = ezBoostCommand.onCommand(player, command, "ezboost",
                new String[]{"create"});
        assertTrue(result);
        verify(adminGui).open(player);
    }

    @Test
    void onCommand_createContinue_withSavedState_opensGui() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(mockPlugin, "ezboost.admin", true);
        when(adminGui.hasSavedState(player)).thenReturn(true);
        boolean result = ezBoostCommand.onCommand(player, command, "ezboost",
                new String[]{"create", "continue"});
        assertTrue(result);
        verify(adminGui).open(player);
    }

    @Test
    void onCommand_createContinue_noSavedState_sendsError() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(mockPlugin, "ezboost.admin", true);
        when(adminGui.hasSavedState(player)).thenReturn(false);
        boolean result = ezBoostCommand.onCommand(player, command, "ezboost",
                new String[]{"create", "continue"});
        assertTrue(result);
        verify(adminGui, never()).open(any());
    }

    // ── /ezboost about ───────────────────────────────────────────────────────

    @Test
    void onCommand_about_noPermission_deniesAccess() {
        PlayerMock player = server.addPlayer();
        // No ezboost.admin permission
        boolean result = ezBoostCommand.onCommand(player, command, "ezboost",
                new String[]{"about"});
        assertTrue(result);
        verify(messages).message("no-permission");
    }

    @Test
    void onCommand_about_withPermission_displaysInfo() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(mockPlugin, "ezboost.admin", true);
        when(messages.plain(eq("about"), any(TagResolver[].class))).thenReturn("Plugin info");
        when(boostManager.totalBoostCount()).thenReturn(3);
        boolean result = ezBoostCommand.onCommand(player, command, "ezboost",
                new String[]{"about"});
        assertTrue(result);
        verify(messages).plain(eq("about"), any(TagResolver[].class));
    }

    // ── unknown subcommand ────────────────────────────────────────────────────

    @Test
    void onCommand_unknownSubcommand_sendsUsageAndReturnsTrue() {
        CommandSender console = server.getConsoleSender();
        boolean result = ezBoostCommand.onCommand(console, command, "ezboost",
                new String[]{"unknownsub"});
        assertTrue(result);
    }

    // ── tab completion ────────────────────────────────────────────────────────

    @Test
    void onTabComplete_withReloadPerm_includesReload() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(mockPlugin, "ezboost.reload", true);
        var completions = ezBoostCommand.onTabComplete(player, command, "ezboost",
                new String[]{""});
        assertTrue(completions.contains("reload"));
    }

    @Test
    void onTabComplete_withGivePerm_includesGive() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(mockPlugin, "ezboost.give", true);
        var completions = ezBoostCommand.onTabComplete(player, command, "ezboost",
                new String[]{""});
        assertTrue(completions.contains("give"));
    }

    @Test
    void onTabComplete_withAdminPerm_includesCreateAndAbout() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(mockPlugin, "ezboost.admin", true);
        var completions = ezBoostCommand.onTabComplete(player, command, "ezboost",
                new String[]{""});
        assertTrue(completions.contains("create"));
        assertTrue(completions.contains("about"));
    }

    @Test
    void onTabComplete_noPermissions_returnsNoSubcommands() {
        PlayerMock player = server.addPlayer();
        var completions = ezBoostCommand.onTabComplete(player, command, "ezboost",
                new String[]{""});
        assertFalse(completions.contains("reload"));
        assertFalse(completions.contains("give"));
        assertFalse(completions.contains("create"));
    }

    @Test
    void onTabComplete_createArg_returnsContinueOption() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(mockPlugin, "ezboost.admin", true);
        var completions = ezBoostCommand.onTabComplete(player, command, "ezboost",
                new String[]{"create", ""});
        assertTrue(completions.contains("continue"));
    }

    @Test
    void onTabComplete_giveArg_returnsOnlinePlayers() {
        PlayerMock target = server.addPlayer("OnlineTarget");
        PlayerMock sender = server.addPlayer();
        sender.addAttachment(mockPlugin, "ezboost.give", true);
        var completions = ezBoostCommand.onTabComplete(sender, command, "ezboost",
                new String[]{"give", ""});
        assertTrue(completions.contains("OnlineTarget"));
    }

    @Test
    void onTabComplete_giveThirdArg_returnsBoostKeys() {
        PlayerMock sender = server.addPlayer();
        sender.addAttachment(mockPlugin, "ezboost.give", true);
        when(boostManager.getBoosts(sender))
                .thenReturn(java.util.Map.of("speed", mock(BoostDefinition.class)));
        var completions = ezBoostCommand.onTabComplete(sender, command, "ezboost",
                new String[]{"give", "Alice", ""});
        assertTrue(completions.contains("speed"));
    }

    // ── /ezboost stats ───────────────────────────────────────────────────────────

    @Test
    void onCommand_stats_consoleSender_sendsOnlyPlayersMessage() {
        CommandSender console = server.getConsoleSender();
        boolean result = ezBoostCommand.onCommand(console, command, "ezboost", new String[]{"stats"});
        assertTrue(result);
        verify(messages).message("only-players");
    }

    @Test
    void onCommand_stats_noPermission_deniesAccess() {
        PlayerMock player = server.addPlayer();
        boolean result = ezBoostCommand.onCommand(player, command, "ezboost", new String[]{"stats"});
        assertTrue(result);
        verify(messages).message("no-permission");
    }

    @Test
    void onCommand_stats_withPermission_nullLeaderboard_noActiveBoost() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(mockPlugin, "ezboost.stats", true);
        when(boostManager.getLeaderboard()).thenReturn(null);
        when(boostManager.getActiveBoostKey(player)).thenReturn(null);
        boolean result = ezBoostCommand.onCommand(player, command, "ezboost", new String[]{"stats"});
        assertTrue(result);
        verify(messages).message("stats-header");
        verify(messages).message(eq("stats-purchases"), any(TagResolver[].class));
        verify(messages).message("stats-no-active-boost");
    }

    @Test
    void onCommand_stats_withPermission_leaderboardReturnsCount() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(mockPlugin, "ezboost.stats", true);
        BoostLeaderboard lb = mock(BoostLeaderboard.class);
        when(lb.getPurchases(player.getUniqueId())).thenReturn(7);
        when(boostManager.getLeaderboard()).thenReturn(lb);
        when(boostManager.getActiveBoostKey(player)).thenReturn(null);
        boolean result = ezBoostCommand.onCommand(player, command, "ezboost", new String[]{"stats"});
        assertTrue(result);
        verify(lb).getPurchases(player.getUniqueId());
        verify(messages).message(eq("stats-purchases"), any(TagResolver[].class));
        verify(messages).message("stats-no-active-boost");
    }

    @Test
    void onCommand_stats_withActiveBoost_showsBoostAndTime() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(mockPlugin, "ezboost.stats", true);
        when(boostManager.getLeaderboard()).thenReturn(null);
        when(boostManager.getActiveBoostKey(player)).thenReturn("speed");
        when(boostManager.getActiveBoostTimeRemaining(player)).thenReturn(300L);
        BoostDefinition def = mock(BoostDefinition.class);
        when(def.displayName()).thenReturn("Speed Boost");
        when(boostManager.getBoost(eq("speed"), eq(player))).thenReturn(Optional.of(def));
        boolean result = ezBoostCommand.onCommand(player, command, "ezboost", new String[]{"stats"});
        assertTrue(result);
        verify(messages).message(eq("stats-active-boost"), any(TagResolver[].class));
    }

    @Test
    void onCommand_stats_activeBoostDefinitionMissing_fallsBackToKey() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(mockPlugin, "ezboost.stats", true);
        when(boostManager.getLeaderboard()).thenReturn(null);
        when(boostManager.getActiveBoostKey(player)).thenReturn("xp");
        when(boostManager.getActiveBoostTimeRemaining(player)).thenReturn(60L);
        when(boostManager.getBoost(eq("xp"), eq(player))).thenReturn(Optional.empty());
        boolean result = ezBoostCommand.onCommand(player, command, "ezboost", new String[]{"stats"});
        assertTrue(result);
        verify(messages).message(eq("stats-active-boost"), any(TagResolver[].class));
    }

    @Test
    void onTabComplete_withStatsPerm_includesStats() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(mockPlugin, "ezboost.stats", true);
        var completions = ezBoostCommand.onTabComplete(player, command, "ezboost", new String[]{""});
        assertTrue(completions.contains("stats"));
    }

    @Test
    void onTabComplete_withoutStatsPerm_doesNotIncludeStats() {
        PlayerMock player = server.addPlayer();
        var completions = ezBoostCommand.onTabComplete(player, command, "ezboost", new String[]{""});
        assertFalse(completions.contains("stats"));
    }
}
