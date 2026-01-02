package com.skyblockexp.ezboost.listener;

import com.skyblockexp.ezboost.boost.BoostManager;
import com.skyblockexp.ezboost.boost.BoostManager.ActivationSource;
import com.skyblockexp.ezboost.gui.BoostTokenFactory;
import java.util.Objects;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class BoostTokenListener implements Listener {
    private final BoostManager boostManager;
    private final BoostTokenFactory tokenFactory;

    public BoostTokenListener(BoostManager boostManager, BoostTokenFactory tokenFactory) {
        this.boostManager = Objects.requireNonNull(boostManager, "boostManager");
        this.tokenFactory = Objects.requireNonNull(tokenFactory, "tokenFactory");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || item.getType().isAir()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        NamespacedKey key = tokenFactory.tokenKey();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String boostKey = container.get(key, PersistentDataType.STRING);
        if (boostKey == null) {
            return;
        }
        Player player = event.getPlayer();
        boolean activated = boostManager.activate(player, boostKey, ActivationSource.TOKEN);
        if (!activated) {
            return;
        }
        int amount = item.getAmount();
        if (amount <= 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            item.setAmount(amount - 1);
        }
    }
}
