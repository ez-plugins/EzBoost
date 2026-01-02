package com.skyblockexp.ezboost.boost;

import java.util.HashMap;
import java.util.Map;

public final class BoostState {
    private String activeBoostKey;
    private long endTimestamp;
    private final Map<String, Long> cooldowns = new HashMap<>();

    public String activeBoostKey() {
        return activeBoostKey;
    }

    public void setActiveBoost(String key, long endTimestamp) {
        this.activeBoostKey = key;
        this.endTimestamp = endTimestamp;
    }

    public void clearActiveBoost() {
        this.activeBoostKey = null;
        this.endTimestamp = 0L;
    }

    public long endTimestamp() {
        return endTimestamp;
    }

    public Map<String, Long> cooldowns() {
        return cooldowns;
    }

    public long cooldownEnd(String key) {
        return cooldowns.getOrDefault(key, 0L);
    }

    public void setCooldownEnd(String key, long end) {
        cooldowns.put(key, end);
    }
}
