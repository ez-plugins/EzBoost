package com.skyblockexp.ezboost.storage;

import com.github.ezframework.jaloquent.model.Model;
import com.github.ezframework.javaquerybuilder.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class YamlDataStoreTest {

    @TempDir
    Path tempDir;

    private YamlDataStore store;

    @BeforeEach
    public void setUp() {
        store = new YamlDataStore(tempDir.resolve("data.yml").toFile(), Logger.getLogger("test"));
    }

    // ── save / load ───────────────────────────────────────────────────────────

    @Test
    public void save_and_load_roundTrip() {
        store.save("leaderboard.abc123", Map.of("player_name", "Alice", "total_purchases", 5));
        Optional<Map<String, Object>> loaded = store.load("leaderboard.abc123");
        assertTrue(loaded.isPresent());
        assertEquals("Alice", loaded.get().get("player_name"));
        assertEquals(5, loaded.get().get("total_purchases"));
    }

    @Test
    public void load_missingPath_returnsEmpty() {
        assertTrue(store.load("nonexistent.id").isEmpty());
    }

    // ── exists / delete ───────────────────────────────────────────────────────

    @Test
    public void exists_returnsFalseBeforeSave_trueAfter() {
        assertFalse(store.exists("boost_states.player1"));
        store.save("boost_states.player1", Map.of("active_boost", "speed"));
        assertTrue(store.exists("boost_states.player1"));
    }

    @Test
    public void delete_removesEntry() {
        store.save("boost_states.player1", Map.of("active_boost", "speed"));
        assertTrue(store.exists("boost_states.player1"));
        store.delete("boost_states.player1");
        assertFalse(store.exists("boost_states.player1"));
        assertTrue(store.load("boost_states.player1").isEmpty());
    }

    // ── query ─────────────────────────────────────────────────────────────────

    @Test
    public void query_emptyStore_returnsEmpty() {
        List<String> paths = store.query(Model.queryBuilder().build());
        assertTrue(paths.isEmpty());
    }

    @Test
    public void query_noConditions_returnsAllRecords() {
        store.save("boost_states/uuid1", Map.of("active_boost", "speed",    "boost_end", 1000L));
        store.save("boost_states/uuid2", Map.of("active_boost", "jump",     "boost_end", 2000L));
        store.save("boost_states/uuid3", Map.of("active_boost", "strength", "boost_end", 3000L));

        List<String> ids = store.query(Model.queryBuilder().build());
        assertEquals(3, ids.size());
    }

    @Test
    public void query_returnsIds() {
        store.save("leaderboard/p1", Map.of("player_name", "Alice", "total_purchases", 1));
        List<String> ids = store.query(Model.queryBuilder().build());
        assertEquals(1, ids.size());
        assertEquals("p1", ids.get(0));
    }

    @Test
    public void query_withOrderByDescending_sortsByField() {
        store.save("leaderboard/p1", Map.of("player_name", "Alice", "total_purchases", 3));
        store.save("leaderboard/p2", Map.of("player_name", "Bob",   "total_purchases", 10));
        store.save("leaderboard/p3", Map.of("player_name", "Carol", "total_purchases", 1));

        Query q = Model.queryBuilder().orderBy("total_purchases", false).build();
        List<String> ids = store.query(q);

        assertEquals(3, ids.size());
        // Bob has the highest total_purchases — must come first with descending order
        assertEquals("p2", ids.get(0));
    }

    @Test
    public void query_withOrderByAscending_sortsByField() {
        store.save("leaderboard/p1", Map.of("player_name", "Alice", "total_purchases", 3));
        store.save("leaderboard/p2", Map.of("player_name", "Bob",   "total_purchases", 10));
        store.save("leaderboard/p3", Map.of("player_name", "Carol", "total_purchases", 1));

        Query q = Model.queryBuilder().orderBy("total_purchases", true).build();
        List<String> ids = store.query(q);

        assertEquals(3, ids.size());
        // Carol has the lowest total_purchases — must come first with ascending order
        assertEquals("p3", ids.get(0));
    }

    // ── non-existent file ─────────────────────────────────────────────────────

    @Test
    public void nonexistentFile_createsEmptyStore() {
        YamlDataStore fresh = new YamlDataStore(
                tempDir.resolve("missing.yml").toFile(), Logger.getLogger("test"));
        assertTrue(fresh.query(Model.queryBuilder().build()).isEmpty());
        assertFalse(fresh.exists("any.key"));
    }

    // ── cross-instance persistence ────────────────────────────────────────────

    @Test
    public void dataPersistedToDisk_isReadableBySecondInstance() {
        store.save("leaderboard.p1", Map.of("player_name", "Alice", "total_purchases", 5));

        // Second instance pointing at the same file
        YamlDataStore store2 = new YamlDataStore(
                tempDir.resolve("data.yml").toFile(), Logger.getLogger("test"));
        Optional<Map<String, Object>> loaded = store2.load("leaderboard.p1");
        assertTrue(loaded.isPresent());
        assertEquals("Alice", loaded.get().get("player_name"));
    }
}
