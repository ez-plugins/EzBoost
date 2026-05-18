package com.skyblockexp.ezboost.storage;

import com.github.ezframework.jaloquent.model.Model;
import com.github.ezframework.jaloquent.model.ModelFactory;

/**
 * Jaloquent model representing a player's total boost purchase count.
 * The record ID is the player's UUID string.
 */
public final class BoostPurchaseRecord extends Model {

    /** Factory reference used by {@link ModelFactory}. */
    public static final ModelFactory<BoostPurchaseRecord> FACTORY =
            (id, data) -> {
                BoostPurchaseRecord r = new BoostPurchaseRecord(id);
                r.fromMap(data);
                return r;
            };

    public BoostPurchaseRecord(String uuid) {
        super(uuid);
    }

    public String getPlayerName() {
        return getAs("player_name", String.class, "");
    }

    public void setPlayerName(String name) {
        set("player_name", name);
    }

    public int getTotalPurchases() {
        return getAs("total_purchases", Integer.class, 0);
    }

    public void setTotalPurchases(int count) {
        set("total_purchases", count);
    }

    public void incrementPurchases() {
        setTotalPurchases(getTotalPurchases() + 1);
    }
}
