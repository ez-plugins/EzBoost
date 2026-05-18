package com.skyblockexp.ezboost.storage;

import com.github.ezframework.jaloquent.model.ModelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class StorageFactoryTest {

    private static final Logger LOGGER = Logger.getLogger("test");

    // ── YAML backend ──────────────────────────────────────────────────────────

    @Test
    public void build_yamlBackend_returnsBundleWithNonNullRepos(@TempDir Path tempDir) {
        StorageSettings settings = StorageSettings.defaults(); // yaml backend
        StorageFactory.StorageBundle bundle =
                StorageFactory.build(settings, tempDir.toFile(), LOGGER);

        assertNotNull(bundle);
        assertNotNull(bundle.boostRepository());
        assertNotNull(bundle.leaderboardRepo());
    }

    @Test
    public void yamlBundle_boostRepository_startsEmpty(@TempDir Path tempDir) {
        StorageSettings settings = StorageSettings.defaults();
        StorageFactory.StorageBundle bundle =
                StorageFactory.build(settings, tempDir.toFile(), LOGGER);

        assertTrue(bundle.boostRepository().load().isEmpty());
    }

    @Test
    public void yamlBundle_leaderboardRepo_isModelRepository(@TempDir Path tempDir) {
        StorageSettings settings = StorageSettings.defaults();
        StorageFactory.StorageBundle bundle =
                StorageFactory.build(settings, tempDir.toFile(), LOGGER);

        assertInstanceOf(ModelRepository.class, bundle.leaderboardRepo());
    }

    // ── Prefix constants ──────────────────────────────────────────────────────

    @Test
    public void prefixStates_matchesEzBoostRepositoryConstant() {
        assertEquals(EzBoostRepository.prefixStates(), StorageFactory.PREFIX_STATES);
    }

    @Test
    public void prefixCooldowns_matchesEzBoostRepositoryConstant() {
        assertEquals(EzBoostRepository.prefixCooldowns(), StorageFactory.PREFIX_COOLDOWNS);
    }

    @Test
    public void prefixLeaderboard_isLeaderboard() {
        assertEquals("leaderboard", StorageFactory.PREFIX_LEADERBOARD);
    }

    // ── Unknown backend ───────────────────────────────────────────────────────

    @Test
    public void build_unknownBackend_throwsIllegalArgumentException(@TempDir Path tempDir) {
        // "oracle" is not a valid backend — the switch default case must throw
        StorageSettings settings = new StorageSettings(
                "oracle", "x.db", "localhost", 1521, "db", "user", "", 5);
        assertThrows(IllegalArgumentException.class,
                () -> StorageFactory.build(settings, tempDir.toFile(), LOGGER));
    }

    // ── SQLite backend ────────────────────────────────────────────────────────

    @Test
    public void build_sqliteBackend_returnsBundleWithNonNullRepos(@TempDir Path tempDir) {
        StorageSettings settings = new StorageSettings(
                "sqlite", "test.db", "localhost", 3306, "ezboost", "", "", 5);
        StorageFactory.StorageBundle bundle =
                StorageFactory.build(settings, tempDir.toFile(), LOGGER);

        assertNotNull(bundle);
        assertNotNull(bundle.boostRepository());
        assertNotNull(bundle.leaderboardRepo());
    }
}
