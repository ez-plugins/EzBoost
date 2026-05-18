package com.skyblockexp.ezboost.storage;

/**
 * Immutable snapshot of the {@code storage:} section of {@code storage.yml}.
 */
public record StorageSettings(
        /** Backend type: {@code yaml | sqlite | mysql | mariadb | postgresql | h2} */
        String backend,

        // SQLite / H2
        /** Database file name (relative to plugin data folder); used for sqlite and h2 backends. */
        String dbFile,

        // SQL server backends
        String host,
        int    port,
        String database,
        String username,
        String password,
        int    poolSize
) {
    /** Default settings (YAML backend, no connection details needed). */
    public static StorageSettings defaults() {
        return new StorageSettings(
                "yaml",
                "ezboost.db",
                "localhost",
                3306,
                "ezboost",
                "root",
                "",
                10
        );
    }
}
