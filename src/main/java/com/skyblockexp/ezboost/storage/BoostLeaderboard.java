package com.skyblockexp.ezboost.storage;

import com.github.ezframework.jaloquent.exception.StorageException;
import com.github.ezframework.jaloquent.model.Model;
import com.github.ezframework.jaloquent.model.ModelRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tracks per-player boost purchase counts and provides a ranked leaderboard.
 *
 * <p>Uses Jaloquent's {@link ModelRepository} with a {@link YamlDataStore} backend.
 * An in-memory cache is rebuilt every time a purchase is recorded so
 * {@link #getTopBuyers(int)} remains O(1) with no disk reads at call time.
 */
public final class BoostLeaderboard {

    private final ModelRepository<BoostPurchaseRecord> repo;
    private final Logger logger;
    /** In-memory snapshot sorted by total_purchases desc. Rebuilt on every write. */
    private volatile List<BoostPurchaseRecord> cachedTop = List.of();

    public BoostLeaderboard(ModelRepository<BoostPurchaseRecord> repo, Logger logger) {
        this.repo = repo;
        this.logger = logger;
        rebuildCache();
    }

    /**
     * Record one boost purchase/activation for the given player.
     * Creates a new record if none exists, otherwise increments the counter.
     */
    public synchronized void recordPurchase(UUID uuid, String playerName) {
        String id = uuid.toString();
        try {
            Optional<BoostPurchaseRecord> existing = repo.find(id);
            BoostPurchaseRecord record;
            if (existing.isPresent()) {
                record = existing.get();
            } else {
                record = new BoostPurchaseRecord(id);
            }
            record.setPlayerName(playerName);
            record.incrementPurchases();
            repo.save(record);
            rebuildCache();
        } catch (StorageException ex) {
            logger.log(Level.SEVERE, "Failed to record boost purchase for " + uuid, ex);
        }
    }

    /**
     * Returns the top {@code limit} players ordered by total purchases descending.
     */
    public List<BoostPurchaseRecord> getTopBuyers(int limit) {
        List<BoostPurchaseRecord> snapshot = cachedTop;
        return snapshot.subList(0, Math.min(limit, snapshot.size()));
    }

    /**
     * Returns the 1-based rank of a player, or -1 if they have no purchases recorded.
     */
    public int getRank(UUID uuid) {
        String id = uuid.toString();
        List<BoostPurchaseRecord> snapshot = cachedTop;
        for (int i = 0; i < snapshot.size(); i++) {
            if (snapshot.get(i).getId().equals(id)) {
                return i + 1;
            }
        }
        return -1;
    }

    /**
     * Returns the total purchase count for the given player, or 0 if not found.
     */
    public int getPurchases(UUID uuid) {
        try {
            Optional<BoostPurchaseRecord> record = repo.find(uuid.toString());
            return record.map(BoostPurchaseRecord::getTotalPurchases).orElse(0);
        } catch (StorageException ex) {
            logger.log(Level.WARNING, "Failed to look up purchase count for " + uuid, ex);
            return 0;
        }
    }

    private void rebuildCache() {
        try {
            List<BoostPurchaseRecord> all = repo.query(
                    Model.queryBuilder()
                            .orderBy("total_purchases", false)
                            .build()
            );
            List<BoostPurchaseRecord> sorted = new ArrayList<>(all);
            sorted.sort(Comparator.comparingInt(BoostPurchaseRecord::getTotalPurchases).reversed());
            this.cachedTop = List.copyOf(sorted);
        } catch (StorageException ex) {
            logger.log(Level.WARNING, "Failed to rebuild leaderboard cache", ex);
        }
    }
}

