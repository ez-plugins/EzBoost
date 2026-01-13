
package com.skyblockexp.ezboost.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import com.skyblockexp.ezboost.boost.BoostDefinition;

/**
 * Called before a boost ends (expires or is removed) for a player.
 * <p>
 * Listeners can inspect the boost key, player, and boost definition, and cancel removal if needed.
 * </p>
 * <p>
 * Cancellation prevents the boost effects from being removed and the expiry message from being sent.
 * </p>
 */
public class BoostEndEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String boostKey;
    private final BoostDefinition boostDefinition;
    private boolean cancelled; // Indicates if the event is cancelled

    /**
     * Constructs a new BoostEndEvent.
     *
     * @param player the player whose boost is ending
     * @param boostKey the key of the boost being ended
     * @param boostDefinition the full boost definition (may be null if unavailable)
     */
    public BoostEndEvent(Player player, String boostKey, BoostDefinition boostDefinition) {
        this.player = player;
        this.boostKey = boostKey;
        this.boostDefinition = boostDefinition;
    }

    /**
     * Gets the full boost definition for this event, if available.
     *
     * @return the boost definition, or null if unavailable
     */
    public BoostDefinition getBoostDefinition() {
        return boostDefinition;
    }

    /**
     * Gets the player whose boost is ending.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the boost key being ended.
     *
     * @return the boost key
     */
    public String getBoostKey() {
        return boostKey;
    }

    /**
     * Checks if the event is cancelled.
     *
     * @return true if cancelled, false otherwise
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets the cancellation state of this event.
     *
     * @param cancel true to cancel, false to allow
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    /**
     * Gets the handler list for this event.
     *
     * @return the handler list
     */
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Gets the static handler list for this event class.
     *
     * @return the handler list
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
