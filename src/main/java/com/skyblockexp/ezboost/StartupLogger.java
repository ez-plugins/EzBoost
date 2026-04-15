package com.skyblockexp.ezboost;

import java.util.logging.Logger;

/**
 * Handles all startup and shutdown console output for EzBoost.
 * <p>
 * Edit <b>this file only</b> to change what the server console shows on enable/disable.
 * All banner content, labels, and layout are defined here.
 * </p>
 */
public final class StartupLogger {

    // ── Banner layout ────────────────────────────────────────────────────────
    private static final String BORDER = "+-----------------------------------------+";
    private static final int    INNER  = 39; // usable chars between "| " and " |"

    // ── Section labels (edit these to rename anything in the banner) ─────────
    private static final String LABEL_BOOSTS = "Boosts loaded";
    private static final String LABEL_ECON   = "Economy";
    private static final String LABEL_PAPI   = "PlaceholderAPI";

    // ── Status values ────────────────────────────────────────────────────────
    private static final String STATUS_VAULT_ON  = "Vault";
    private static final String STATUS_VAULT_OFF = "Disabled";
    private static final String STATUS_PAPI_ON   = "Hooked";
    private static final String STATUS_PAPI_OFF  = "Not found";

    private StartupLogger() {}

    /**
     * Prints the startup banner after the plugin has fully initialised.
     *
     * @param log        the plugin logger
     * @param version    plugin version string
     * @param boosts     number of boosts loaded from configuration
     * @param vault      whether Vault economy was successfully hooked
     * @param papi       whether PlaceholderAPI expansion was successfully registered
     */
    public static void logEnable(Logger log, String version,
                                  int boosts, boolean vault, boolean papi) {
        log.info(BORDER);
        log.info(row(""));
        log.info(row("  EzBoost  v" + version));
        log.info(row("  by Shadow48402"));
        log.info(row(""));
        log.info(BORDER);
        log.info(entry(LABEL_BOOSTS, String.valueOf(boosts)));
        log.info(entry(LABEL_ECON,   vault ? STATUS_VAULT_ON : STATUS_VAULT_OFF));
        log.info(entry(LABEL_PAPI,   papi  ? STATUS_PAPI_ON  : STATUS_PAPI_OFF));
        log.info(row(""));
        log.info(BORDER);
    }

    /**
     * Prints the shutdown message.
     *
     * @param log the plugin logger
     */
    public static void logDisable(Logger log) {
        log.info("EzBoost has been disabled.");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Builds a labelled key-value row: {@code |  Label        value  |} */
    private static String entry(String label, String value) {
        return row("  " + padRight(label, 17) + value);
    }

    /** Wraps arbitrary text inside the fixed-width border frame. */
    private static String row(String text) {
        return "| " + padRight(text, INNER) + " |";
    }

    private static String padRight(String s, int width) {
        int pad = width - s.length();
        return pad > 0 ? s + " ".repeat(pad) : s;
    }
}
