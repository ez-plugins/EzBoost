package com.skyblockexp.ezboost.boost;

import org.bukkit.entity.Player;

/**
 * Interface for custom boost effects that can be registered by other plugins.
 */
public interface CustomBoostEffect {
    /**
     * Apply the effect to the player.
     */
    void apply(Player player, int amplifier);

    /**
     * Remove the effect from the player.
     */
    void remove(Player player);

    /**
     * @return unique effect name
     */
    String getName();
}
