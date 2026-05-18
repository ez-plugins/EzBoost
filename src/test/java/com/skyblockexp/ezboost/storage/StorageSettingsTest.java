package com.skyblockexp.ezboost.storage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StorageSettingsTest {

    @Test
    public void defaults_returnExpectedValues() {
        StorageSettings s = StorageSettings.defaults();
        assertEquals("yaml", s.backend());
        assertEquals("ezboost.db", s.dbFile());
        assertEquals("localhost", s.host());
        assertEquals(3306, s.port());
        assertEquals("ezboost", s.database());
        assertNotNull(s.username());
        assertNotNull(s.password());
        assertEquals(10, s.poolSize());
    }

    @Test
    public void constructor_storesAllFields() {
        StorageSettings s = new StorageSettings(
                "sqlite", "my.db", "db.host", 5432, "mydb", "admin", "pass", 5);
        assertEquals("sqlite", s.backend());
        assertEquals("my.db", s.dbFile());
        assertEquals("db.host", s.host());
        assertEquals(5432, s.port());
        assertEquals("mydb", s.database());
        assertEquals("admin", s.username());
        assertEquals("pass", s.password());
        assertEquals(5, s.poolSize());
    }
}
