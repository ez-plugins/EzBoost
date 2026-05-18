package com.skyblockexp.ezboost.gui.admin;

import com.skyblockexp.ezboost.config.EzBoostConfig;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminGuiRendererTest {

    private ServerMock server;
    private JavaPlugin mockPlugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        mockPlugin = MockBukkit.createMockPlugin();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void createInventory_returnsInventoryWithCorrectSize() {
        EzBoostConfig config = mock(EzBoostConfig.class);
        AdminGuiRenderer renderer = new AdminGuiRenderer(
                new NamespacedKey(mockPlugin, "action"), config);
        Inventory inv = renderer.createInventory();
        assertNotNull(inv);
        assertEquals(27, inv.getSize());
    }
}
