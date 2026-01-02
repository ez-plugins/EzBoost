package com.skyblockexp.ezboost.economy;

import com.skyblockexp.ezboost.config.EzBoostConfig.EconomySettings;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class EconomyService {
    private Economy economy;
    private boolean enabled;

    public void setup(EconomySettings settings) {
        enabled = settings != null && settings.enabled();
        economy = null;
        if (!enabled || settings == null || !settings.vaultEnabled()) {
            return;
        }
        RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (provider != null) {
            economy = provider.getProvider();
        }
    }

    public boolean isAvailable() {
        return enabled && economy != null;
    }

    public EconomyResponse withdraw(Player player, double amount) {
        if (!isAvailable()) {
            return new EconomyResponse(amount, 0.0, EconomyResponse.ResponseType.FAILURE, "Economy unavailable");
        }
        return economy.withdrawPlayer(player, amount);
    }

    public void deposit(Player player, double amount) {
        if (isAvailable()) {
            economy.depositPlayer(player, amount);
        }
    }
}
