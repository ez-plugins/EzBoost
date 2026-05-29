package com.skyblockexp.ezboost.listener;

import com.skyblockexp.ezboost.boost.BoostManager;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public final class GuardianReviveListener implements Listener {
    private final BoostManager boostManager;

    public GuardianReviveListener(BoostManager boostManager) {
        this.boostManager = Objects.requireNonNull(boostManager, "boostManager");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (boostManager.consumeReviveIfLethal(player, event.getFinalDamage())) {
            event.setCancelled(true);
        }
    }
}
