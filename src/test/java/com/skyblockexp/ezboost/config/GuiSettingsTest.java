package com.skyblockexp.ezboost.config;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GuiSettingsTest {

    @Test
    public void statusFallbackAndLookup() {
        Map<String, String> labels = Map.of("locked", "Locked Label", "available", "Avail");
        EzBoostConfig.GuiSettings gs = new EzBoostConfig.GuiSettings(true, "Title", 45, true, null, java.util.List.of(), labels, Map.of(), false);

        assertEquals("Locked Label", gs.status("locked", "fallback"));
        assertEquals("Avail", gs.status("available", "fallback"));
        assertEquals("fallback", gs.status("missing", "fallback"));
    }
}
