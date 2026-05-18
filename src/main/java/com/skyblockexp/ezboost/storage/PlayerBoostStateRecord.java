package com.skyblockexp.ezboost.storage;

import com.github.ezframework.jaloquent.model.Model;
import com.github.ezframework.jaloquent.model.ModelFactory;

/**
 * Jaloquent model representing a player's active boost state.
 * The record ID is the player's UUID string.
 * Stored under the {@code boost_states} repository prefix.
 */
public final class PlayerBoostStateRecord extends Model {

    public static final ModelFactory<PlayerBoostStateRecord> FACTORY =
            (id, data) -> {
                PlayerBoostStateRecord r = new PlayerBoostStateRecord(id);
                r.fromMap(data);
                return r;
            };

    public PlayerBoostStateRecord(String uuid) {
        super(uuid);
    }

    /** @return the active boost key, or {@code null} if no boost is active. */
    public String getActiveBoost() {
        return getAs("active_boost", String.class, null);
    }

    public void setActiveBoost(String key) {
        set("active_boost", key);
    }

    /** @return epoch-millis timestamp when the active boost expires, or 0 if none. */
    public long getBoostEnd() {
        Object v = get("boost_end");
        if (v instanceof Number n) return n.longValue();
        return 0L;
    }

    public void setBoostEnd(long end) {
        set("boost_end", end);
    }
}
