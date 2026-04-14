package com.skyblockexp.ezboost.boost;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

/**
 * Custom boost effect that multiplies XP gained by the player.
 * Amplifier 0 = 2x XP, amplifier 1 = 3x XP, etc. (multiplier = amplifier + 2).
 */
public final class XpBoostEffect implements CustomBoostEffect {

    private static final String NAME = "xpboost";

    private final Map<UUID, Integer> activeAmplifiers = new ConcurrentHashMap<>();

    @Override
    public void apply(Player player, int amplifier) {
        activeAmplifiers.put(player.getUniqueId(), amplifier);
    }

    @Override
    public void remove(Player player) {
        activeAmplifiers.remove(player.getUniqueId());
    }

    @Override
    public String getName() {
        return NAME;
    }

    public boolean isActive(Player player) {
        return activeAmplifiers.containsKey(player.getUniqueId());
    }

    /**
     * Returns the XP multiplier for the player (amplifier + 2), or 1 if not active.
     */
    public int getMultiplier(Player player) {
        Integer amplifier = activeAmplifiers.get(player.getUniqueId());
        return amplifier == null ? 1 : amplifier + 2;
    }
}
