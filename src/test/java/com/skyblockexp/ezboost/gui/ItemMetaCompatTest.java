package com.skyblockexp.ezboost.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemMetaCompatTest {

    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private ItemMeta freshMeta() {
        ItemMeta meta = new ItemStack(Material.PAPER).getItemMeta();
        assertNotNull(meta, "MockBukkit must provide ItemMeta for PAPER");
        return meta;
    }

    @Test
    void setDisplayName_plainText_setsDisplayName() {
        ItemMeta meta = freshMeta();
        ItemMetaCompat.setDisplayName(meta, Component.text("Hello World"));
        assertEquals("Hello World", meta.getDisplayName());
    }

    @Test
    void setDisplayName_coloredComponent_includesLegacyColorCode() {
        ItemMeta meta = freshMeta();
        ItemMetaCompat.setDisplayName(meta, Component.text("Red Text", NamedTextColor.RED));
        String name = meta.getDisplayName();
        assertTrue(name.contains("Red Text"), "Display name must contain the original text");
        assertTrue(name.startsWith("§c"), "Red NamedTextColor must serialize to §c");
    }

    @Test
    void setLore_multipleLinesPreservedInOrder() {
        ItemMeta meta = freshMeta();
        ItemMetaCompat.setLore(meta, List.of(
                Component.text("Line one"),
                Component.text("Line two")
        ));
        List<String> lore = meta.getLore();
        assertNotNull(lore);
        assertEquals(2, lore.size());
        assertEquals("Line one", lore.get(0));
        assertEquals("Line two", lore.get(1));
    }

    @Test
    void setLore_emptyList_resultIsEmptyOrNull() {
        ItemMeta meta = freshMeta();
        ItemMetaCompat.setLore(meta, List.of());
        List<String> lore = meta.getLore();
        assertTrue(lore == null || lore.isEmpty(), "Empty lore should produce null or empty list");
    }
}
