package com.skyblockexp.ezboost.gui;

import com.skyblockexp.ezboost.boost.BoostManager;
import com.skyblockexp.ezboost.config.EzBoostConfig;
import com.skyblockexp.ezboost.gui.admin.EffectSelectionGui;
import com.skyblockexp.ezboost.gui.admin.MaterialSelectionGui;
import com.skyblockexp.ezboost.gui.admin.NumberInputGui;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GuiInventoryCreationTest {

    private ServerMock server;
    private JavaPlugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void boostGui_open_exercisesLegacyTitleCreation() {
        BoostManager bm = mock(BoostManager.class);
        when(bm.getBoosts(any())).thenReturn(Map.of());
        EzBoostConfig.GuiSettings settings = new EzBoostConfig.GuiSettings(
                true, "<green>Boosts</green>", 27, true,
                null, List.of(), Map.of(), Map.of(), false);
        BoostGui gui = new BoostGui(plugin, bm, settings);
        PlayerMock player = server.addPlayer();
        gui.open(player);
        // No assertion needed — reaching here confirms createInventory() ran without error
    }

    @Test
    void effectSelectionGui_open_createsInventory() {
        EffectSelectionGui gui = new EffectSelectionGui(plugin, effect -> {});
        PlayerMock player = server.addPlayer();
        gui.open(player, 0);
    }

    @Test
    void materialSelectionGui_open_createsInventory() {
        MaterialSelectionGui gui = new MaterialSelectionGui(plugin, mat -> {});
        PlayerMock player = server.addPlayer();
        gui.open(player, 0);
    }

    @Test
    void numberInputGui_open_createsInventory() {
        NumberInputGui gui = new NumberInputGui(plugin, "Duration", 1, 3600, 60, n -> {});
        PlayerMock player = server.addPlayer();
        gui.open(player, 60);
    }
}
