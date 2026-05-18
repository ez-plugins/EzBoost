package com.skyblockexp.ezboost.storage;

import com.github.ezframework.jaloquent.config.DatabaseSettings;
import com.github.ezframework.jaloquent.config.JaloquentConfig;
import com.github.ezframework.jaloquent.config.JdbcScheme;
import com.github.ezframework.jaloquent.exception.MigrationException;
import com.github.ezframework.jaloquent.migration.MigrationBlueprint;
import com.github.ezframework.jaloquent.migration.MigrationRunner;
import com.github.ezframework.jaloquent.migration.Schema;
import com.github.ezframework.jaloquent.model.ModelRepository;
import com.github.ezframework.jaloquent.model.TableRegistry;
import com.github.ezframework.jaloquent.store.DataStore;
import com.github.ezframework.jaloquent.store.sql.DataSourceJdbcStore;
import com.github.ezframework.jaloquent.store.sql.DriverManagerDataSource;
import com.github.ezframework.javaquerybuilder.query.sql.SqlDialect;
import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Builds all {@link ModelRepository} instances needed by the plugin based on
 * {@link StorageSettings}.
 *
 * <p>For SQL backends the factory also:
 * <ul>
 *   <li>Registers table mappings with {@link TableRegistry}.</li>
 *   <li>Runs Jaloquent migrations to create tables if they do not exist.</li>
 * </ul>
 *
 * <p>Use {@link #build(StorageSettings, File, Logger)} and consume the returned
 * {@link StorageBundle}.
 *
 * @see <a href="https://github.com/EzFramework/Jaloquent">Jaloquent on GitHub</a>
 */
public final class StorageFactory {

    // Repository prefix constants — must match EzBoostRepository
    static final String PREFIX_STATES      = EzBoostRepository.prefixStates();
    static final String PREFIX_COOLDOWNS   = EzBoostRepository.prefixCooldowns();
    static final String PREFIX_LEADERBOARD = "leaderboard";

    // SQL table names
    static final String TABLE_STATES      = "ezboost_boost_states";
    static final String TABLE_COOLDOWNS   = "ezboost_cooldowns";
    static final String TABLE_LEADERBOARD = "ezboost_leaderboard";

    private StorageFactory() {}

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Holds all repositories built by {@link StorageFactory#build}.
     *
     * @param boostRepository    boost-state + cooldown persistence
     * @param leaderboardRepo    purchase-count leaderboard persistence
     */
    public record StorageBundle(
            EzBoostRepository boostRepository,
            ModelRepository<BoostPurchaseRecord> leaderboardRepo) {}

    /**
     * Build the full storage layer from the given settings.
     *
     * @param settings   storage settings loaded from {@code storage.yml}
     * @param dataFolder plugin data folder (used for YAML/SQLite file paths)
     * @param logger     plugin logger
     * @return fully initialised {@link StorageBundle}
     */
    public static StorageBundle build(
            StorageSettings settings,
            File dataFolder,
            Logger logger) {

        String backend = settings.backend().toLowerCase(Locale.ROOT);

        JaloquentConfig.enableLogging(settings.debugLogging());

        if ("yaml".equals(backend)) {
            return buildYaml(dataFolder, logger);
        }
        return buildSql(settings, backend, dataFolder, logger);
    }

    // ── YAML backend ─────────────────────────────────────────────────────────

    private static StorageBundle buildYaml(File dataFolder, Logger logger) {
        // Separate YAML files keep game state and leaderboard data distinct
        YamlDataStore gameStore = new YamlDataStore(
                new File(dataFolder, "data.yml"), logger);
        YamlDataStore leaderboardStore = new YamlDataStore(
                new File(dataFolder, "leaderboard.yml"), logger);

        ModelRepository<PlayerBoostStateRecord> stateRepo =
                new ModelRepository<>(gameStore, PREFIX_STATES, PlayerBoostStateRecord.FACTORY);
        ModelRepository<PlayerCooldownRecord> cooldownRepo =
                new ModelRepository<>(gameStore, PREFIX_COOLDOWNS, PlayerCooldownRecord.FACTORY);
        ModelRepository<BoostPurchaseRecord> leaderboardRepo =
                new ModelRepository<>(leaderboardStore, PREFIX_LEADERBOARD, BoostPurchaseRecord.FACTORY);

        return new StorageBundle(
                new EzBoostRepository(stateRepo, cooldownRepo, logger),
                leaderboardRepo);
    }

    // ── SQL backend ──────────────────────────────────────────────────────────

    private static StorageBundle buildSql(
            StorageSettings settings,
            String backend,
            File dataFolder,
            Logger logger) {

        DatabaseSettings dbSettings = buildDatabaseSettings(settings, backend, dataFolder);
        SqlDialect dialect = mapDialect(backend);

        DriverManagerDataSource ds = new DriverManagerDataSource(dbSettings);
        DataSourceJdbcStore store  = new DataSourceJdbcStore(ds);

        // Register table mappings so Jaloquent knows which SQL table to use per prefix
        registerTables();

        // Run migrations (CREATE TABLE IF NOT EXISTS)
        runMigrations(store, dialect, logger);

        ModelRepository<PlayerBoostStateRecord> stateRepo =
                new ModelRepository<>(store, PREFIX_STATES, PlayerBoostStateRecord.FACTORY, dialect);
        ModelRepository<PlayerCooldownRecord> cooldownRepo =
                new ModelRepository<>(store, PREFIX_COOLDOWNS, PlayerCooldownRecord.FACTORY, dialect);
        ModelRepository<BoostPurchaseRecord> leaderboardRepo =
                new ModelRepository<>(store, PREFIX_LEADERBOARD, BoostPurchaseRecord.FACTORY, dialect);

        return new StorageBundle(
                new EzBoostRepository(stateRepo, cooldownRepo, logger),
                leaderboardRepo);
    }

    // ── SQL helpers ──────────────────────────────────────────────────────────

    private static DatabaseSettings buildDatabaseSettings(
            StorageSettings s, String backend, File dataFolder) {

        DatabaseSettings.Builder b = DatabaseSettings.builder();

        switch (backend) {
            case "sqlite" -> {
                String filePath = new File(dataFolder, s.dbFile()).getAbsolutePath();
                b.jdbcScheme(JdbcScheme.SQLITE)
                 .driverClassName("org.sqlite.JDBC")
                 .url("jdbc:sqlite:" + filePath)
                 .username("")
                 .password("");
            }
            case "h2" -> {
                String filePath = new File(dataFolder, s.dbFile()).getAbsolutePath();
                b.jdbcScheme("h2")
                 .driverClassName("org.h2.Driver")
                 .url("jdbc:h2:file:" + filePath + ";AUTO_SERVER=TRUE")
                 .username("sa")
                 .password("");
            }
            case "mysql" -> b
                    .jdbcScheme(JdbcScheme.MYSQL)
                    .driverClassName("com.mysql.cj.jdbc.Driver")
                    .host(s.host()).port(s.port())
                    .databaseName(s.database())
                    .username(s.username()).password(s.password())
                    .maximumPoolSize(s.poolSize());
            case "mariadb" -> b
                    .jdbcScheme(JdbcScheme.MARIADB)
                    .driverClassName("org.mariadb.jdbc.Driver")
                    .host(s.host()).port(s.port())
                    .databaseName(s.database())
                    .username(s.username()).password(s.password())
                    .maximumPoolSize(s.poolSize());
            case "postgresql" -> b
                    .jdbcScheme(JdbcScheme.POSTGRESQL)
                    .driverClassName("org.postgresql.Driver")
                    .host(s.host()).port(s.port())
                    .databaseName(s.database())
                    .username(s.username()).password(s.password())
                    .maximumPoolSize(s.poolSize());
            default -> throw new IllegalArgumentException(
                    "Unknown storage backend: '" + backend
                    + "'. Valid values: yaml, sqlite, mysql, mariadb, postgresql, h2");
        }

        return b.build();
    }

    private static SqlDialect mapDialect(String backend) {
        return switch (backend) {
            case "mysql", "mariadb" -> SqlDialect.MYSQL;
            case "sqlite"           -> SqlDialect.SQLITE;
            case "postgresql"       -> SqlDialect.POSTGRESQL;
            default                 -> SqlDialect.STANDARD;
        };
    }

    private static void registerTables() {
        TableRegistry.register(PREFIX_STATES, TABLE_STATES,
                Map.of("active_boost", "active_boost", "boost_end", "boost_end"));
        TableRegistry.register(PREFIX_COOLDOWNS, TABLE_COOLDOWNS,
                Map.of("cooldown_end", "cooldown_end"));
        TableRegistry.register(PREFIX_LEADERBOARD, TABLE_LEADERBOARD,
                Map.of("player_name", "player_name", "total_purchases", "total_purchases"));
    }

    private static void runMigrations(
            DataSourceJdbcStore store, SqlDialect dialect, Logger logger) {
        Schema schema = new Schema(store, dialect);

        List<com.github.ezframework.jaloquent.migration.Migration> migrations = List.of(
                new com.github.ezframework.jaloquent.migration.Migration() {
                    @Override public String getId() { return "001_create_boost_states"; }
                    @Override public void up(Schema s) throws MigrationException {
                        s.create(TABLE_STATES, (MigrationBlueprint t) -> t
                                .ifNotExists()
                                .string("id", 36)
                                .primaryKey("id")
                                .string("active_boost", 100)
                                .bigInteger("boost_end"));
                    }
                    @Override public void down(Schema s) throws MigrationException {
                        s.dropIfExists(TABLE_STATES);
                    }
                },
                new com.github.ezframework.jaloquent.migration.Migration() {
                    @Override public String getId() { return "002_create_cooldowns"; }
                    @Override public void up(Schema s) throws MigrationException {
                        s.create(TABLE_COOLDOWNS, (MigrationBlueprint t) -> t
                                .ifNotExists()
                                .string("id", 200)
                                .primaryKey("id")
                                .bigInteger("cooldown_end"));
                    }
                    @Override public void down(Schema s) throws MigrationException {
                        s.dropIfExists(TABLE_COOLDOWNS);
                    }
                },
                new com.github.ezframework.jaloquent.migration.Migration() {
                    @Override public String getId() { return "003_create_leaderboard"; }
                    @Override public void up(Schema s) throws MigrationException {
                        s.create(TABLE_LEADERBOARD, (MigrationBlueprint t) -> t
                                .ifNotExists()
                                .string("id", 36)
                                .primaryKey("id")
                                .string("player_name", 16)
                                .integer("total_purchases"));
                    }
                    @Override public void down(Schema s) throws MigrationException {
                        s.dropIfExists(TABLE_LEADERBOARD);
                    }
                }
        );

        try {
            new MigrationRunner(store, dialect, migrations).run();
        } catch (MigrationException ex) {
            logger.log(Level.SEVERE,
                    "Failed to run storage migrations — plugin may not persist data correctly", ex);
        }
    }
}
