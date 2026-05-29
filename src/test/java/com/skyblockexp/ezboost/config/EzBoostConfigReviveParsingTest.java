package com.skyblockexp.ezboost.config;

import com.skyblockexp.ezboost.boost.BoostDefinition;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EzBoostConfigReviveParsingTest {

    @Test
    public void reviveFields_parseDefaultsAndClamp() throws Exception {
        File tmp = Files.createTempDirectory("ezboost-revive-test").toFile();
        tmp.deleteOnExit();

        for (String name : new String[]{"settings.yml", "limits.yml", "worlds.yml", "economy.yml", "gui.yml"}) {
            File f = new File(tmp, name);
            try (FileWriter w = new FileWriter(f)) {
                w.write("\n");
            }
        }

        File boosts = new File(tmp, "boosts.yml");
        try (FileWriter w = new FileWriter(boosts)) {
            w.write("boosts:\n");
            w.write("  guardian:\n");
            w.write("    display-name: \"Guardian\"\n");
            w.write("    icon: TOTEM_OF_UNDYING\n");
            w.write("    effects:\n");
            w.write("      - type: DAMAGE_RESISTANCE\n");
            w.write("        amplifier: 0\n");
            w.write("    duration: 300\n");
            w.write("    cooldown: 600\n");
            w.write("    revive-enabled: true\n");
            w.write("    revive-hearts: 999\n");
            w.write("  speed:\n");
            w.write("    display-name: \"Speed\"\n");
            w.write("    icon: SUGAR\n");
            w.write("    effects:\n");
            w.write("      - type: SPEED\n");
            w.write("        amplifier: 1\n");
            w.write("    duration: 60\n");
            w.write("    cooldown: 0\n");
        }

        JavaPlugin plugin = mock(JavaPlugin.class);
        when(plugin.getDataFolder()).thenReturn(tmp);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("test"));
        doNothing().when(plugin).saveResource(anyString(), anyBoolean());

        EzBoostConfig cfg = new EzBoostConfig(plugin);
        BoostDefinition guardian = cfg.boosts().get("guardian");
        BoostDefinition speed = cfg.boosts().get("speed");

        assertTrue(guardian.reviveEnabled());
        assertEquals(20.0D, guardian.reviveHearts());

        assertFalse(speed.reviveEnabled());
        assertEquals(BoostDefinition.DEFAULT_REVIVE_HEARTS, speed.reviveHearts());
    }
}
