package com.skyblockexp.ezboost.storage;

import com.github.ezframework.jaloquent.model.Model;
import com.github.ezframework.jaloquent.model.ModelFactory;
import java.util.UUID;

/**
 * Jaloquent model representing a single cooldown entry for one player + boost combination.
 *
 * <p>The record ID uses a composite key: {@code "<uuid>:<boostKey>"}.
 * Stored under the {@code cooldowns} repository prefix.
 */
public final class PlayerCooldownRecord extends Model {

    public static final ModelFactory<PlayerCooldownRecord> FACTORY =
            (id, data) -> {
                PlayerCooldownRecord r = new PlayerCooldownRecord(id);
                r.fromMap(data);
                return r;
            };

    public PlayerCooldownRecord(String compositeId) {
        super(compositeId);
    }

    // ── ID helpers ───────────────────────────────────────────────────────────

    /** Compose the record ID from a player UUID and boost key. */
    public static String makeId(UUID uuid, String boostKey) {
        return uuid.toString() + ":" + boostKey;
    }

    /**
     * Extract the player UUID from a composite record ID.
     * UUID occupies the first 36 characters of the composite string.
     */
    public static UUID extractUUID(String compositeId) {
        return UUID.fromString(compositeId.substring(0, 36));
    }

    /**
     * Extract the boost key from a composite record ID.
     * The boost key starts at index 37 (after {@code "uuid:"}).
     */
    public static String extractBoostKey(String compositeId) {
        return compositeId.substring(37);
    }

    // ── Field accessors ──────────────────────────────────────────────────────

    /** @return epoch-millis timestamp when the cooldown ends. */
    public long getCooldownEnd() {
        Object v = get("cooldown_end");
        if (v instanceof Number n) return n.longValue();
        return 0L;
    }

    public void setCooldownEnd(long end) {
        set("cooldown_end", end);
    }
}
