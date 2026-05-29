# Changelog

All notable changes to EzBoost are documented here.

Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).
Versions follow [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
Release tags use the `v` prefix (e.g. `v2.0.1`).

---

## [Unreleased]

---

## [2.2.0] - 2026-05-29

### Added

- **Guardian preset boost** (`guardian`): built-in totem-style defensive boost with default effects (`DAMAGE_RESISTANCE`, `HEALTH_BOOST`) and preconfigured GUI slot.
- **Per-boost revive options** in `boosts.yml`:
  - `revive-enabled` (default `false`)
  - `revive-hearts` (default `4.0`, clamped to safe bounds)
- **Lethal-hit interception** for revive-enabled boosts: when incoming damage would be fatal, death is prevented, configured health is restored, and the active boost is consumed immediately (single-use per activation).

---

## [2.1.0] - 2026-05-18

### Added

- **Folia compatibility**: EzBoost now supports Folia servers via a `FoliaScheduler` abstraction that routes task scheduling through Folia's `GlobalRegionScheduler` / entity schedulers when the Folia runtime is detected.
- **Boost top leaderboard** (`/boosttop`): tracks and displays the top boost buyers using a Jaloquent-backed persistent storage layer.
- **Jaloquent storage backend**: replaced the previous `BoostStorage` abstraction with a fully Jaloquent-backed `EzBoostRepository` for consistent flat-file persistence across all storage operations.
- **`storage.debug-logging` option** (`storage.yml`, default `false`): suppresses verbose Jaloquent console output (`Queried X rows`, `Saved model…`). Set to `true` to re-enable for debugging.
- **Paper 1.21 smoke-test**: CI matrix now includes Paper 1.21.11 alongside Paper 26.1.2, Folia, Spigot, and Bukkit.

### Changed

- `api-version` lowered from `26.1.2` to `1.13` so the plugin loads on any Spigot/Paper 1.13+ server without an api-version warning or rejection.
- Java compiler target lowered from 25 to 17 for wider JDK compatibility.
- `AsyncChatEvent` (Paper-only) replaced with `AsyncPlayerChatEvent` for Spigot compatibility.
- Plugin version lookup changed from `getPluginMeta().getVersion()` to `getDescription().getVersion()` for broader server compatibility.
- CI smoke-test pass condition now also verifies `Enabling EzBoost` appears in the server log, preventing a false pass when the server starts but rejects the plugin due to an incompatible api-version.

### Fixed

- `YamlDataStore.query()` corrected to handle Jaloquent's flat key format.
- `storage.yml` added to `pom.xml` resource includes so it is correctly packaged.
- Vault is now fully optional: economy class access and listener registration are guarded so the plugin loads cleanly without Vault present.

---

## [2.0.0] - 2026-04-15

### Added

- **Minecraft 26.1.2 support**: updated `api-version` and build toolchain to target Paper MC 26.1.2 / Java 25.
- **9 new PlaceholderAPI placeholders** via an internal PAPI expansion (`EzBoostPlaceholder`): expose active boost info, remaining duration, cooldowns, and more.
- **MiniMessage tag resolvers** (`BoostTagResolvers`): native MiniMessage tags for boost context (name, duration, etc.) wired into all `BoostManager` messages for rich formatting without external placeholders.

### Changed

- CI updated to Java 25.

---

## [1.6.0] - 2026-04-14

### Added

- **11 preset boosts**: waterbreathing, saturation, luck, absorption, slow-falling, miner, warrior, farmer, explorer, xpboost, diver — all available out of the box with sensible defaults in `boosts.yml`.
- **XP boost custom effect** (`XpBoostEffect`): configurable experience multiplier applied for the duration of the boost.
- GUI slots pre-configured for the new preset boosts.

---

## [1.5.6] - 2026-04-01

### Fixed

- PlaceholderAPI registration timing corrected so placeholders are reliably available after server startup.
- `/ezboost about` subcommand output and tab-completion fixed.

---

## [1.5.5] - 2026-03-28

### Added

- **PlaceholderAPI integration**: `EzBoostPlaceholder` expansion exposes boost data to any PAPI-compatible plugin.
- **`/ezboost about` subcommand**: displays plugin version, authors, and resource links.
- **Formatted price placeholder** (`%ezboost_price_formatted%`): presents boost costs in a human-readable currency format.

---

## [1.5.4] - 2026-03-13

### Added

- **`show-effects` GUI option**: configurable toggle to show or hide active potion effects in the boost GUI.
- **Vault economy boost effects**: boosts can now charge players via Vault when activated (`XpBoostEffect` and economy hooks).
- **Cooldown improvements**: per-boost cooldown configuration expanded with additional options.
- CI pipeline to validate configuration and run tests.

---

## [1.5.3] - 2026-01-22

### Fixed

- Removed debug console messages that were printed when no effects were applied to a custom boost.

---

## [1.5.2] - 2026-01-21

### Fixed

- Custom boost definitions now load correctly from `boosts.yml`; a regression in 1.5.1 prevented custom boosts from being registered.

---

## [1.5.1] - 2026-01-21

### Changed

- Tab-completion for `/boost` and `/ezboost` improved: suggestions are now context-aware and include boost names.
- `BoostEffect` records now include a `name` field used in completions and display.

---

## [1.5.0] - 2026-01-20

### Added

- **Admin GUI** (`/ezboost create`): in-game inventory interface for creating and configuring boosts without editing config files directly.

---

## [1.4.0] - 2026-01-13

### Added

- **EzBoost API** (`EzBoostAPI`): public API surface for third-party plugins to query and trigger boosts programmatically.
- **Custom boost events**: `BoostStartEvent` and `BoostEndEvent` fired on the Bukkit event bus so other plugins can react to boost lifecycle changes.

---

## [1.3.0] - 2026-01-12

### Added

- **WorldGuard regional overrides**: define per-region boost behaviour (allow, deny, or override settings) using the built-in override system. WorldGuard is detected automatically as a soft dependency.

### Fixed

- Build version warning in `pom.xml` resolved.

---

## [1.2.0] - 2026-01-02

### Added

- Initial public release of EzBoost.
- Configurable potion boosts with durations, cooldowns, world restrictions, and GUI activation.
- Multi-file configuration (`settings.yml`, `boosts.yml`, `gui.yml`, `messages.yml`, `limits.yml`).
- Vault economy integration for charging players on boost activation.
