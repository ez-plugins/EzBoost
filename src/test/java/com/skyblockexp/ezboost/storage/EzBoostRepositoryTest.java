package com.skyblockexp.ezboost.storage;

import com.github.ezframework.jaloquent.model.ModelRepository;
import com.skyblockexp.ezboost.boost.BoostState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class EzBoostRepositoryTest {

    @TempDir
    Path tempDir;

    private EzBoostRepository repo;

    @BeforeEach
    public void setUp() {
        Logger logger = Logger.getLogger("test");
        YamlDataStore store = new YamlDataStore(tempDir.resolve("data.yml").toFile(), logger);
        ModelRepository<PlayerBoostStateRecord> stateRepo =
                new ModelRepository<>(store, EzBoostRepository.prefixStates(), PlayerBoostStateRecord.FACTORY);
        ModelRepository<PlayerCooldownRecord> cooldownRepo =
                new ModelRepository<>(store, EzBoostRepository.prefixCooldowns(), PlayerCooldownRecord.FACTORY);
        repo = new EzBoostRepository(stateRepo, cooldownRepo, logger);
    }

    // ── Prefix constants ──────────────────────────────────────────────────────

    @Test
    public void prefixStates_returnsBoostStates() {
        assertEquals("boost_states", EzBoostRepository.prefixStates());
    }

    @Test
    public void prefixCooldowns_returnsCooldowns() {
        assertEquals("cooldowns", EzBoostRepository.prefixCooldowns());
    }

    // ── Empty store ───────────────────────────────────────────────────────────

    @Test
    public void load_emptyStore_returnsEmptyMap() {
        assertTrue(repo.load().isEmpty());
    }

    // ── Active boost round-trip ───────────────────────────────────────────────

    @Test
    public void saveState_and_load_preservesActiveBoost() {
        UUID uuid = UUID.randomUUID();
        BoostState state = new BoostState();
        state.setActiveBoost("speed", System.currentTimeMillis() + 60_000L);

        repo.saveState(uuid, state);
        Map<UUID, BoostState> loaded = repo.load();

        assertTrue(loaded.containsKey(uuid));
        assertEquals("speed", loaded.get(uuid).activeBoostKey());
    }

    @Test
    public void load_expiredBoost_activeKeyIsNull() {
        UUID uuid = UUID.randomUUID();
        BoostState state = new BoostState();
        state.setActiveBoost("speed", System.currentTimeMillis() - 1_000L); // already expired

        repo.saveState(uuid, state);
        Map<UUID, BoostState> loaded = repo.load();

        // UUID may appear with an empty BoostState but must not have an active boost key
        BoostState ls = loaded.get(uuid);
        assertTrue(ls == null || ls.activeBoostKey() == null);
    }

    // ── Cooldown round-trip ───────────────────────────────────────────────────

    @Test
    public void saveState_and_load_preservesCooldown() {
        UUID uuid = UUID.randomUUID();
        BoostState state = new BoostState();
        state.setCooldownEnd("jump", System.currentTimeMillis() + 30_000L);

        repo.saveState(uuid, state);
        Map<UUID, BoostState> loaded = repo.load();

        assertTrue(loaded.containsKey(uuid));
        assertTrue(loaded.get(uuid).cooldownEnd("jump") > System.currentTimeMillis());
    }

    @Test
    public void saveState_expiredCooldown_notStoredAndNotLoaded() {
        UUID uuid = UUID.randomUUID();
        BoostState state = new BoostState();
        state.setCooldownEnd("jump", System.currentTimeMillis() - 1_000L); // already expired

        repo.saveState(uuid, state);
        Map<UUID, BoostState> loaded = repo.load();

        BoostState ls = loaded.get(uuid);
        assertTrue(ls == null || ls.cooldownEnd("jump") == 0L);
    }

    // ── save (full map) ───────────────────────────────────────────────────────

    @Test
    public void save_fullStateMap_persistsAllPlayers() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();

        BoostState s1 = new BoostState();
        s1.setActiveBoost("speed", System.currentTimeMillis() + 60_000L);

        BoostState s2 = new BoostState();
        s2.setCooldownEnd("jump", System.currentTimeMillis() + 30_000L);

        repo.save(Map.of(uuid1, s1, uuid2, s2));
        Map<UUID, BoostState> loaded = repo.load();

        assertEquals("speed", loaded.get(uuid1).activeBoostKey());
        assertTrue(loaded.get(uuid2).cooldownEnd("jump") > 0L);
    }

    // ── deletePlayer ──────────────────────────────────────────────────────────

    @Test
    public void deletePlayer_removesBoostStateAndCooldowns() {
        UUID uuid = UUID.randomUUID();
        BoostState state = new BoostState();
        state.setActiveBoost("speed", System.currentTimeMillis() + 60_000L);
        state.setCooldownEnd("jump", System.currentTimeMillis() + 30_000L);

        repo.saveState(uuid, state);
        assertFalse(repo.load().isEmpty());

        repo.deletePlayer(uuid);
        Map<UUID, BoostState> loaded = repo.load();
        assertNull(loaded.get(uuid));
    }

    @Test
    public void deletePlayer_otherPlayersUnaffected() {
        UUID keepUuid   = UUID.randomUUID();
        UUID deleteUuid = UUID.randomUUID();

        BoostState keepState = new BoostState();
        keepState.setActiveBoost("speed", System.currentTimeMillis() + 60_000L);

        BoostState deleteState = new BoostState();
        deleteState.setActiveBoost("jump", System.currentTimeMillis() + 60_000L);

        repo.saveState(keepUuid, keepState);
        repo.saveState(deleteUuid, deleteState);

        repo.deletePlayer(deleteUuid);
        Map<UUID, BoostState> loaded = repo.load();

        assertNull(loaded.get(deleteUuid));
        assertNotNull(loaded.get(keepUuid));
        assertEquals("speed", loaded.get(keepUuid).activeBoostKey());
    }
}
