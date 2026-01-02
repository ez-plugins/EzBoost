package com.skyblockexp.ezboost.listener;

import com.skyblockexp.ezboost.config.EzBoostConfig;
import com.skyblockexp.ezboost.economy.EconomyService;
import java.util.Objects;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;

public final class EconomyServiceListener implements Listener {
    private final EzBoostConfig config;
    private final EconomyService economyService;
    private final Logger logger;

    public EconomyServiceListener(EzBoostConfig config, EconomyService economyService, Logger logger) {
        this.config = Objects.requireNonNull(config, "config");
        this.economyService = Objects.requireNonNull(economyService, "economyService");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @EventHandler
    public void onServiceRegister(ServiceRegisterEvent event) {
        if (!Economy.class.equals(event.getProvider().getService())) {
            return;
        }
        refreshEconomyProvider();
    }

    @EventHandler
    public void onServiceUnregister(ServiceUnregisterEvent event) {
        if (!Economy.class.equals(event.getProvider().getService())) {
            return;
        }
        refreshEconomyProvider();
    }

    private void refreshEconomyProvider() {
        boolean wasAvailable = economyService.isAvailable();
        economyService.setup(config.economySettings());
        boolean isAvailable = economyService.isAvailable();
        if (wasAvailable == isAvailable) {
            return;
        }
        if (isAvailable) {
            logger.info("Economy provider is now available.");
        } else {
            logger.info("Economy provider is now unavailable.");
        }
    }
}
