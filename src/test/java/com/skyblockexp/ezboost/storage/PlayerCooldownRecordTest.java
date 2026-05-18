package com.skyblockexp.ezboost.storage;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerCooldownRecordTest {

    private static final UUID   PLAYER_UUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final String BOOST_KEY   = "speed_boost";

    @Test
    public void makeId_formatsAsUuidColonKey() {
        String id = PlayerCooldownRecord.makeId(PLAYER_UUID, BOOST_KEY);
        assertEquals(PLAYER_UUID.toString() + ":" + BOOST_KEY, id);
    }

    @Test
    public void extractUUID_roundTrips() {
        String id = PlayerCooldownRecord.makeId(PLAYER_UUID, BOOST_KEY);
        assertEquals(PLAYER_UUID, PlayerCooldownRecord.extractUUID(id));
    }

    @Test
    public void extractBoostKey_roundTrips() {
        String id = PlayerCooldownRecord.makeId(PLAYER_UUID, BOOST_KEY);
        assertEquals(BOOST_KEY, PlayerCooldownRecord.extractBoostKey(id));
    }

    @Test
    public void extractBoostKey_withColonInKey() {
        // Boost keys that themselves contain ':' should still be preserved in full
        String key = "super:boost";
        String id  = PlayerCooldownRecord.makeId(PLAYER_UUID, key);
        assertEquals(key, PlayerCooldownRecord.extractBoostKey(id));
    }

    @Test
    public void cooldownEnd_defaultIsZero() {
        PlayerCooldownRecord r = new PlayerCooldownRecord("x");
        assertEquals(0L, r.getCooldownEnd());
    }

    @Test
    public void cooldownEnd_roundTrip() {
        PlayerCooldownRecord r = new PlayerCooldownRecord("x");
        r.setCooldownEnd(77_777L);
        assertEquals(77_777L, r.getCooldownEnd());
    }

    @Test
    public void factory_createsRecordFromDataMap() {
        String id = PlayerCooldownRecord.makeId(PLAYER_UUID, BOOST_KEY);
        Map<String, Object> data = new HashMap<>();
        data.put("cooldown_end", 54321L);
        PlayerCooldownRecord r = PlayerCooldownRecord.FACTORY.create(id, data);
        assertEquals(id, r.getId());
        assertEquals(54321L, r.getCooldownEnd());
    }
}
