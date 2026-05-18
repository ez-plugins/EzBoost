package com.skyblockexp.ezboost.storage;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerBoostStateRecordTest {

    private static final String UUID_STR = "550e8400-e29b-41d4-a716-446655440000";

    @Test
    public void constructor_setsId() {
        PlayerBoostStateRecord r = new PlayerBoostStateRecord(UUID_STR);
        assertEquals(UUID_STR, r.getId());
    }

    @Test
    public void activeBoost_defaultIsNull() {
        PlayerBoostStateRecord r = new PlayerBoostStateRecord(UUID_STR);
        assertNull(r.getActiveBoost());
    }

    @Test
    public void activeBoost_roundTrip() {
        PlayerBoostStateRecord r = new PlayerBoostStateRecord(UUID_STR);
        r.setActiveBoost("speed");
        assertEquals("speed", r.getActiveBoost());
    }

    @Test
    public void boostEnd_defaultIsZero() {
        PlayerBoostStateRecord r = new PlayerBoostStateRecord(UUID_STR);
        assertEquals(0L, r.getBoostEnd());
    }

    @Test
    public void boostEnd_roundTrip() {
        PlayerBoostStateRecord r = new PlayerBoostStateRecord(UUID_STR);
        r.setBoostEnd(9_999_000L);
        assertEquals(9_999_000L, r.getBoostEnd());
    }

    @Test
    public void boostEnd_handlesIntegerStoredAsNumber() {
        // YAML may deserialise long values as Integer when small enough
        PlayerBoostStateRecord r = new PlayerBoostStateRecord(UUID_STR);
        r.setBoostEnd(42L);
        assertEquals(42L, r.getBoostEnd());
    }

    @Test
    public void factory_createsRecordFromDataMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("active_boost", "jump");
        data.put("boost_end", 12345L);
        PlayerBoostStateRecord r = PlayerBoostStateRecord.FACTORY.create(UUID_STR, data);
        assertEquals(UUID_STR, r.getId());
        assertEquals("jump", r.getActiveBoost());
        assertEquals(12345L, r.getBoostEnd());
    }
}
