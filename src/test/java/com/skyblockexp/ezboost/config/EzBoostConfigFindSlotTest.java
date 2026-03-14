package com.skyblockexp.ezboost.config;

import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EzBoostConfigFindSlotTest {

    @Test
    public void findAvailableSlot_afterAssignedSlots() throws Exception {
        File tmp = Files.createTempDirectory("ezboost-test").toFile();
        tmp.deleteOnExit();
        // create required config files (mostly empty)
        for (String name : new String[]{"settings.yml","limits.yml","worlds.yml","economy.yml","gui.yml","boosts.yml"}) {
            File f = new File(tmp, name);
            try (FileWriter w = new FileWriter(f)) {
                if ("gui.yml".equals(name)) {
                    w.write("gui:\n  size: 45\n  slots:\n    boost1: 10\n    boost2: 11\n");
                } else {
                    w.write("\n");
                }
            }
        }

        JavaPlugin plugin = mock(JavaPlugin.class);
        when(plugin.getDataFolder()).thenReturn(tmp);
        // saveResource may be called for missing files — stub to do nothing
        doNothing().when(plugin).saveResource(anyString(), anyBoolean());

        EzBoostConfig cfg = new EzBoostConfig(plugin);
        int slot = cfg.findAvailableSlot();
        // default maxUsedSlot set to 9, assigned 10/11 so next should be > maxUsedSlot
        assertTrue(slot >= 12 && slot < cfg.guiSettings().size());
    }
}
