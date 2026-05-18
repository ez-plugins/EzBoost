package com.skyblockexp.ezboost.command;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import com.skyblockexp.ezboost.boost.BoostDefinition;
import com.skyblockexp.ezboost.boost.BoostManager;
import com.skyblockexp.ezboost.boost.BoostManager.ActivationSource;
import com.skyblockexp.ezboost.config.Messages;
import com.skyblockexp.ezboost.gui.BoostGui;
import com.skyblockexp.ezboost.storage.BoostLeaderboard;
import com.skyblockexp.ezboost.storage.BoostPurchaseRecord;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class BoostCommandTest {

    private ServerMock server;
    private JavaPlugin mockPlugin;
    private BoostManager boostManager;
    private BoostGui boostGui;
    private Messages messages;
    private Command command;
    private BoostCommand boostCommand;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        mockPlugin = MockBukkit.createMockPlugin();
        boostManager = mock(BoostManager.class);
        boostGui = mock(BoostGui.class);
        messages = mock(Messages.class);
        command = mock(Command.class);
        // Default stubs – prevent NPE when the code calls sendMessage(messages.message(...))
        lenient().when(messages.message(anyString(), any(TagResolver[].class))).thenReturn("");
        lenient().when(messages.plain(anyString(), any(TagResolver[].class))).thenReturn("");
        boostCommand = new BoostCommand(boostManager, boostGui, messages);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    // ── console sender ────────────────────────────────────────────────────────

    @Test
    void onCommand_consoleSender_sendsOnlyPlayersMessage() {
        CommandSender console = server.getConsoleSender();
        boolean result = boostCommand.onCommand(console, command, "boost", new String[]{});
        assertTrue(result);
        verify(messages).message("only-players");
    }

    // ── /boost (no args) ─────────────────────────────────────────────────────

    @Test
    void onCommand_noArgs_guiEnabled_opensGui() {
        PlayerMock player = server.addPlayer();
        when(boostGui.isEnabled()).thenReturn(true);
        boolean result = boostCommand.onCommand(player, command, "boost", new String[]{});
        assertTrue(result);
        verify(boostGui).open(player);
    }

    @Test
    void onCommand_noArgs_guiDisabled_doesNotOpenGui() {
        PlayerMock player = server.addPlayer();
        when(boostGui.isEnabled()).thenReturn(false);
        boolean result = boostCommand.onCommand(player, command, "boost", new String[]{});
        assertTrue(result);
        verify(boostGui, never()).open(any());
    }

    // ── /boost about ─────────────────────────────────────────────────────────

    @Test
    void onCommand_about_noPermission_deniesAccess() {
        PlayerMock player = server.addPlayer();
        // player has no "ezboost.admin" permission by default
        boolean result = boostCommand.onCommand(player, command, "boost", new String[]{"about"});
        assertTrue(result);
        verify(messages).message("no-permission");
    }

    @Test
    void onCommand_about_withPermission_displaysAbout() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(mockPlugin, "ezboost.admin", true);
        when(messages.plain(eq("about"), any(TagResolver[].class))).thenReturn("EzBoost info line1\nline2");
        when(boostManager.totalBoostCount()).thenReturn(2);
        boolean result = boostCommand.onCommand(player, command, "boost", new String[]{"about"});
        assertTrue(result);
        verify(messages).plain(eq("about"), any(TagResolver[].class));
    }

    // ── /boost top ───────────────────────────────────────────────────────────

    @Test
    void onCommand_top_noPermission_deniesAccess() {
        PlayerMock player = server.addPlayer();
        // No ezboost.top permission
        boolean result = boostCommand.onCommand(player, command, "boost", new String[]{"top"});
        assertTrue(result);
        verify(messages).message("no-permission");
    }

    @Test
    void onCommand_top_nullLeaderboard_sendsTopUnavailable() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(mockPlugin, "ezboost.top", true);
        when(boostManager.getLeaderboard()).thenReturn(null);
        boolean result = boostCommand.onCommand(player, command, "boost", new String[]{"top"});
        assertTrue(result);
        verify(messages).message("top-unavailable");
    }

    @Test
    void onCommand_top_emptyLeaderboard_sendsTopNoData() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(mockPlugin, "ezboost.top", true);
        BoostLeaderboard lb = mock(BoostLeaderboard.class);
        when(boostManager.getLeaderboard()).thenReturn(lb);
        when(lb.getTopBuyers(10)).thenReturn(Collections.emptyList());
        boolean result = boostCommand.onCommand(player, command, "boost", new String[]{"top"});
        assertTrue(result);
        verify(messages).message("top-no-data");
    }

    @Test
    void onCommand_top_withEntries_displaysHeaderAndEntries() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(mockPlugin, "ezboost.top", true);
        BoostLeaderboard lb = mock(BoostLeaderboard.class);
        BoostPurchaseRecord entry = mock(BoostPurchaseRecord.class);
        when(entry.getPlayerName()).thenReturn("Alice");
        when(entry.getTotalPurchases()).thenReturn(7);
        when(boostManager.getLeaderboard()).thenReturn(lb);
        when(lb.getTopBuyers(10)).thenReturn(List.of(entry));
        boolean result = boostCommand.onCommand(player, command, "boost", new String[]{"top"});
        assertTrue(result);
        verify(messages).message("top-header");
        verify(messages).message(eq("top-entry"), any(TagResolver[].class));
    }

    @Test
    void onCommand_top_multipleEntries_displaysAllEntries() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(mockPlugin, "ezboost.top", true);
        BoostLeaderboard lb = mock(BoostLeaderboard.class);
        BoostPurchaseRecord e1 = mock(BoostPurchaseRecord.class);
        BoostPurchaseRecord e2 = mock(BoostPurchaseRecord.class);
        when(e1.getPlayerName()).thenReturn("Alice");
        when(e1.getTotalPurchases()).thenReturn(10);
        when(e2.getPlayerName()).thenReturn("Bob");
        when(e2.getTotalPurchases()).thenReturn(5);
        when(boostManager.getLeaderboard()).thenReturn(lb);
        when(lb.getTopBuyers(10)).thenReturn(List.of(e1, e2));
        boostCommand.onCommand(player, command, "boost", new String[]{"top"});
        verify(messages, times(2)).message(eq("top-entry"), any(), any(), any());
    }

    // ── /boost <key> ─────────────────────────────────────────────────────────

    @Test
    void onCommand_boostKey_activatesBoostViaManager() {
        PlayerMock player = server.addPlayer();
        boolean result = boostCommand.onCommand(player, command, "boost", new String[]{"speed"});
        assertTrue(result);
        verify(boostManager).activate(player, "speed", ActivationSource.COMMAND);
    }

    @Test
    void onCommand_boostKey_lowercasesKey() {
        PlayerMock player = server.addPlayer();
        boostCommand.onCommand(player, command, "boost", new String[]{"SPEED"});
        verify(boostManager).activate(player, "speed", ActivationSource.COMMAND);
    }

    // ── tab completion ────────────────────────────────────────────────────────

    @Test
    void onTabComplete_twoArgs_returnsEmpty() {
        PlayerMock player = server.addPlayer();
        List<String> completions = boostCommand.onTabComplete(
                player, command, "boost", new String[]{"speed", ""});
        assertTrue(completions.isEmpty());
    }

    @Test
    void onTabComplete_noPermissions_doesNotIncludeAboutOrTop() {
        PlayerMock player = server.addPlayer();
        when(boostManager.getBoosts(player)).thenReturn(Map.of());
        List<String> completions = boostCommand.onTabComplete(
                player, command, "boost", new String[]{""});
        assertFalse(completions.contains("about"));
        assertFalse(completions.contains("top"));
    }

    @Test
    void onTabComplete_withAdminPermission_includesAbout() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(mockPlugin, "ezboost.admin", true);
        when(boostManager.getBoosts(player)).thenReturn(Map.of());
        List<String> completions = boostCommand.onTabComplete(
                player, command, "boost", new String[]{""});
        assertTrue(completions.contains("about"));
    }

    @Test
    void onTabComplete_withTopPermission_includesTop() {
        PlayerMock player = server.addPlayer();
        player.addAttachment(mockPlugin, "ezboost.top", true);
        when(boostManager.getBoosts(player)).thenReturn(Map.of());
        List<String> completions = boostCommand.onTabComplete(
                player, command, "boost", new String[]{""});
        assertTrue(completions.contains("top"));
    }

    @Test
    void onTabComplete_withBoostKeys_includesMatchingKeys() {
        PlayerMock player = server.addPlayer();
        Map<String, BoostDefinition> boosts = new HashMap<>();
        boosts.put("speed", null);
        boosts.put("strength", null);
        when(boostManager.getBoosts(player)).thenReturn(boosts);
        List<String> completions = boostCommand.onTabComplete(
                player, command, "boost", new String[]{"sp"});
        assertTrue(completions.contains("speed"));
        assertFalse(completions.contains("strength"));
    }

    @Test
    void onTabComplete_consoleSender_doesNotIncludeBoostKeysFromPlayer() {
        CommandSender console = server.getConsoleSender();
        List<String> completions = boostCommand.onTabComplete(
                console, command, "boost", new String[]{""});
        // Console is not a Player, so player boost keys are skipped
        assertNotNull(completions);
    }
}
