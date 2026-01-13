package com.skyblockexp.ezboost.api;

import com.skyblockexp.ezboost.boost.BoostManager;
import com.skyblockexp.ezboost.boost.CustomBoostEffect;
import java.util.Collections;
import java.util.Map;

/**
 * Static API for EzBoost extensibility. Use this to register and query custom boost effects.
 */
public class EzBoostAPI {
    private static BoostManager boostManager;

    /**
     * Called by EzBoostPlugin to initialize the API. Should not be called by other plugins.
     */
    public static void init(BoostManager manager) {
        boostManager = manager;
    }

    /**
     * Register a custom boost effect from another plugin.
     * @param effect CustomBoostEffect implementation
     * @return true if registered, false if name already exists or not initialized
     */
    public static boolean registerCustomBoostEffect(CustomBoostEffect effect) {
        return boostManager != null && boostManager.registerCustomEffect(effect);
    }

    /**
     * Returns all registered custom boost effects.
     */
    public static Map<String, CustomBoostEffect> getCustomBoostEffects() {
        if (boostManager == null) return Collections.emptyMap();
        return boostManager.getCustomEffects();
    }

    /**
     * Returns the BoostManager instance for advanced integrations.
     */
    public static BoostManager getBoostManager() {
        return boostManager;
    }
}
