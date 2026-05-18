package com.skyblockexp.ezboost;

import java.util.logging.Logger;

public final class StartupLogger {

    private StartupLogger() {}

    public static void logEnable(Logger log, String version,
                                  int boosts, boolean vault, boolean papi) {
        log.info("EzBoost v" + version + " enabled — boosts: " + boosts
                + ", economy: " + (vault ? "Vault" : "disabled")
                + ", PlaceholderAPI: " + (papi ? "hooked" : "not found"));
    }

    public static void logDisable(Logger log) {
        log.info("EzBoost disabled.");
    }
}
