package com.skyblockexp.ezboost.listener;

import com.skyblockexp.ezboost.boost.BoostManager;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class BoostPlayerListener implements Listener {
    private final BoostManager boostManager;

    public BoostPlayerListener(BoostManager boostManager) {
        this.boostManager = Objects.requireNonNull(boostManager, "boostManager");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        boostManager.handleJoin(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        boostManager.handleQuit(event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        boostManager.handleDeath(event.getEntity());
    }
}
