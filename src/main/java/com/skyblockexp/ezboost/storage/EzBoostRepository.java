package com.skyblockexp.ezboost.storage;

import com.github.ezframework.jaloquent.exception.StorageException;
import com.github.ezframework.jaloquent.model.ModelRepository;
import com.github.ezframework.jaloquent.model.Model;
import com.skyblockexp.ezboost.boost.BoostState;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Jaloquent-backed repository for player boost state and cooldowns.
 *
 * <p>Replaces the legacy {@code BoostStorage} YAML implementation.
 * Uses two internal {@link ModelRepository} instances:
 * <ul>
 *   <li>{@code boost_states} prefix — one {@link PlayerBoostStateRecord} per player.</li>
 *   <li>{@code cooldowns} prefix — one {@link PlayerCooldownRecord} per (player, boostKey) pair.</li>
 * </ul>
 * The same {@link com.github.ezframework.jaloquent.store.DataStore} is shared by both
 * repositories so that YAML and SQL backends are both supported.
 */
public final class EzBoostRepository {

    private static final String PREFIX_STATES    = "boost_states";
    private static final String PREFIX_COOLDOWNS = "cooldowns";

    private final ModelRepository<PlayerBoostStateRecord> stateRepo;
    private final ModelRepository<PlayerCooldownRecord>   cooldownRepo;
    private final Logger logger;

    public EzBoostRepository(
            ModelRepository<PlayerBoostStateRecord> stateRepo,
            ModelRepository<PlayerCooldownRecord>   cooldownRepo,
            Logger logger) {
        this.stateRepo    = stateRepo;
        this.cooldownRepo = cooldownRepo;
        this.logger       = logger;
    }

    // ── Prefix constants used by callers ─────────────────────────────────────

    public static String prefixStates()    { return PREFIX_STATES; }
    public static String prefixCooldowns() { return PREFIX_COOLDOWNS; }

    // ── Load ─────────────────────────────────────────────────────────────────

    /**
     * Load all persisted player states (active boost + cooldowns) into a map.
     * Called once on plugin startup.
     *
     * @return map of player UUID → {@link BoostState}; never {@code null}.
     */
    public Map<UUID, BoostState> load() {
        Map<UUID, BoostState> result = new HashMap<>();

        // 1. Load all active-boost records
        try {
            List<PlayerBoostStateRecord> all =
                    stateRepo.query(Model.queryBuilder().build());
            for (PlayerBoostStateRecord rec : all) {
                UUID uuid = parseUUID(rec.getId());
                if (uuid == null) continue;

                BoostState state = result.computeIfAbsent(uuid, u -> new BoostState());
                String activeBoost = rec.getActiveBoost();
                long   boostEnd    = rec.getBoostEnd();
                if (activeBoost != null && !activeBoost.isEmpty() && boostEnd > System.currentTimeMillis()) {
                    state.setActiveBoost(activeBoost, boostEnd);
                }
            }
        } catch (StorageException ex) {
            logger.log(Level.SEVERE, "Failed to load boost states from storage", ex);
        }

        // 2. Load all cooldown records
        try {
            List<PlayerCooldownRecord> all =
                    cooldownRepo.query(Model.queryBuilder().build());
            for (PlayerCooldownRecord rec : all) {
                String id = rec.getId();
                if (id == null || id.length() < 38) continue; // malformed

                UUID   uuid     = PlayerCooldownRecord.extractUUID(id);
                String boostKey = PlayerCooldownRecord.extractBoostKey(id);
                long   end      = rec.getCooldownEnd();

                if (end > System.currentTimeMillis()) {
                    result.computeIfAbsent(uuid, u -> new BoostState())
                          .setCooldownEnd(boostKey, end);
                }
            }
        } catch (StorageException ex) {
            logger.log(Level.SEVERE, "Failed to load cooldowns from storage", ex);
        }

        return result;
    }

    // ── Save ─────────────────────────────────────────────────────────────────

    /**
     * Persist the full player state map.
     * Called on plugin disable and periodically.
     *
     * @param states current in-memory player state map.
     */
    public void save(Map<UUID, BoostState> states) {
        for (Map.Entry<UUID, BoostState> entry : states.entrySet()) {
            UUID      uuid  = entry.getKey();
            BoostState state = entry.getValue();
            saveState(uuid, state);
        }
    }

    // ── Per-player helpers ───────────────────────────────────────────────────

    /**
     * Persist a single player's boost state and cooldowns.
     * This is safe to call any time the state changes.
     */
    public void saveState(UUID uuid, BoostState state) {
        saveBoostStateRecord(uuid, state);
        saveCooldownRecords(uuid, state);
    }

    private void saveBoostStateRecord(UUID uuid, BoostState state) {
        try {
            PlayerBoostStateRecord rec = new PlayerBoostStateRecord(uuid.toString());
            String key = state.activeBoostKey();
            rec.setActiveBoost(key == null ? "" : key);
            rec.setBoostEnd(state.endTimestamp());
            stateRepo.save(rec);
        } catch (StorageException ex) {
            logger.log(Level.WARNING, "Failed to save boost state for " + uuid, ex);
        }
    }

    private void saveCooldownRecords(UUID uuid, BoostState state) {
        Map<String, Long> cooldowns = state.cooldowns();
        long now = System.currentTimeMillis();

        for (Map.Entry<String, Long> cd : cooldowns.entrySet()) {
            String boostKey = cd.getKey();
            long   end      = cd.getValue();
            String compositeId = PlayerCooldownRecord.makeId(uuid, boostKey);
            try {
                if (end <= now) {
                    // Expired — remove from storage to avoid accumulation
                    cooldownRepo.delete(compositeId);
                } else {
                    PlayerCooldownRecord rec = new PlayerCooldownRecord(compositeId);
                    rec.setCooldownEnd(end);
                    cooldownRepo.save(rec);
                }
            } catch (StorageException ex) {
                logger.log(Level.WARNING,
                        "Failed to save cooldown for " + uuid + " / " + boostKey, ex);
            }
        }
    }

    /**
     * Remove all stored data for the given player.
     * Not used during normal gameplay but available for admin commands.
     */
    public void deletePlayer(UUID uuid) {
        try { stateRepo.delete(uuid.toString()); } catch (StorageException ex) {
            logger.log(Level.WARNING, "Failed to delete boost state for " + uuid, ex);
        }
        // Cooldowns: iterate and delete – we don't have a "deleteWhere by prefix" here,
        // so load first then delete individually.
        try {
            List<PlayerCooldownRecord> playerCds =
                    cooldownRepo.query(Model.queryBuilder().build());
            String prefix = uuid.toString() + ":";
            for (PlayerCooldownRecord rec : playerCds) {
                if (rec.getId() != null && rec.getId().startsWith(prefix)) {
                    cooldownRepo.delete(rec.getId());
                }
            }
        } catch (StorageException ex) {
            logger.log(Level.WARNING, "Failed to delete cooldowns for " + uuid, ex);
        }
    }

    // ── Internal utils ───────────────────────────────────────────────────────

    private UUID parseUUID(String raw) {
        if (raw == null) return null;
        try { return UUID.fromString(raw); } catch (IllegalArgumentException e) { return null; }
    }
}
