package com.skyblockexp.ezboost.listener;

import com.skyblockexp.ezboost.boost.XpBoostEffect;
import java.util.Objects;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

public final class XpBoostListener implements Listener {

    private final XpBoostEffect xpBoostEffect;

    public XpBoostListener(XpBoostEffect xpBoostEffect) {
        this.xpBoostEffect = Objects.requireNonNull(xpBoostEffect, "xpBoostEffect");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onExpChange(PlayerExpChangeEvent event) {
        if (!xpBoostEffect.isActive(event.getPlayer())) {
            return;
        }
        int multiplier = xpBoostEffect.getMultiplier(event.getPlayer());
        event.setAmount(event.getAmount() * multiplier);
    }
}
