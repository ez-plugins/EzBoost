[![CI](https://github.com/ez-plugins/ezboost/actions/workflows/smoke-test.yml/badge.svg)](https://github.com/ez-plugins/ezboost/actions/workflows/smoke-test.yml)
[![GitHub Packages](https://img.shields.io/badge/GitHub_Packages-2.0.0-blue?logo=github)](https://github.com/ez-plugins/ezboost/packages)
[![Coverage](https://img.shields.io/codecov/c/github/ez-plugins/ezboost)](https://codecov.io/github/ez-plugins/ezboost)
[![Docs](https://img.shields.io/badge/Docs-GitHub_Pages-blue?logo=github)](https://ez-plugins.github.io/ezboost)
[![Platform](https://img.shields.io/badge/Platform-Spigot%20%7C%20Paper%20%7C%20Bukkit-blue)](#)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.7--1.21.*-brightgreen)](#)
[![License](https://img.shields.io/github/license/ez-plugins/ezboost)](LICENSE)
[![Release](https://img.shields.io/github/v/release/ez-plugins/ezboost)](https://github.com/ez-plugins/ezboost/releases)

# EzBoost

> Configurable potion boosts for Spigot / Paper / Bukkit 1.7–1.21.* — GUI activation, cooldowns, economy costs, boost tokens, and WorldGuard region overrides.

![EzBoost GUI](https://i.ibb.co/1GgSfvWs/image.png)

EzBoost lets server owners offer time-limited potion boosts through a fully customisable chest GUI or
direct commands. Every boost is independently configurable — potion effects, amplifier, duration, cooldown,
permission, economy cost, and behaviour on death or reconnect. Boosts can be scoped to specific worlds or
WorldGuard regions, and players can receive tradeable **boost tokens** as crate prizes or vote rewards.

---

## Features

### Player experience
- **Chest GUI** — browse boosts with live cooldown timers, cost display, and active-boost indicator
- **Direct activation** — `/boost <key>` for players who prefer commands over the GUI
- **Boost tokens** — physical inventory items redeemed by right-click; tradeable and giftable
- **Rich feedback** — MiniMessage-formatted actionbar and chat messages, fully customisable

### Server management
- **Fully configurable boosts** — any potion effect, any amplifier, per-boost cooldown, permission, and cost
- **In-game admin GUI** — create and edit boosts with `/ezboost create`, no YAML editing required
- **World allow / deny lists** — restrict boosts to specific worlds for gameplay balance
- **Region overrides** — change any boost property (effect, cost, enabled state) per WorldGuard region; no hard dependency
- **Live reload** — `/ezboost reload` applies all config changes without a server restart
- **Persistent storage** — boost states and cooldowns survive restarts; choice of YAML, SQLite, MySQL, MariaDB, or PostgreSQL backend

### Integrations
- **Vault** — optional economy cost per boost; gracefully disabled if Vault is absent
- **PlaceholderAPI** — 18+ placeholders for scoreboards, holograms, and GUI plugins
- **Internal message tags** — `<boost_display>`, `<boost_cost>`, `<boost_duration>`, and more available directly in `messages.yml`

---

## Installation

1. Download `EzBoost-<version>.jar` from the [releases page](https://github.com/ez-plugins/ezboost/releases).
2. Drop the JAR into your server's `plugins/` folder.
3. Start (or restart) your server — EzBoost generates all config files in `plugins/EzBoost/`.
4. Edit `boosts.yml` to define your boosts, then run `/ezboost reload` to apply.

**Optional extras:**
- Enable economy costs in `economy.yml` (requires Vault).
- Switch the storage backend in `storage.yml` (default: YAML; supports SQLite, MySQL, MariaDB, PostgreSQL).

---

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/boost` | Open the boost GUI | `ezboost.use` |
| `/boost <key>` | Activate a boost directly | `ezboost.use` + boost node |
| `/ezboost create` | Open the admin GUI | `ezboost.admin` |
| `/ezboost reload` | Reload all configuration | `ezboost.reload` |
| `/ezboost give <player> <key> [amount]` | Give boost tokens to a player | `ezboost.give` |

→ Full reference: [Commands](https://ez-plugins.github.io/ezboost/commands)

---

## Permissions

| Permission | Description | Default |
|-----------|-------------|---------|
| `ezboost.use` | Use `/boost` and the GUI | `true` |
| `ezboost.admin` | Admin commands and GUI | `op` |
| `ezboost.reload` | Reload configuration | `op` |
| `ezboost.give` | Give boost tokens | `op` |
| `ezboost.cooldown.bypass` | Skip cooldown checks | `op` |
| `ezboost.boost.<key>` | Activate a specific boost | `true` |

→ Full reference: [Permissions](https://ez-plugins.github.io/ezboost/permissions)

---

## Configuration Files

| File | Purpose |
|------|---------|
| `boosts.yml` | Define boosts — effects, duration, cooldown, cost, permissions, command hooks |
| `settings.yml` | General toggles: replace-active-boost, keep-on-death, reapply-on-join |
| `limits.yml` | Clamp amplifier and duration ranges across all boosts |
| `worlds.yml` | World allow / deny lists |
| `economy.yml` | Vault economy enable / disable and cost settings |
| `gui.yml` | GUI title, size, filler items, and slot assignments |
| `messages.yml` | All MiniMessage-formatted feedback strings and actionbar text |
| `storage.yml` | Storage backend selection and connection settings |

→ Full reference: [Configuration](https://ez-plugins.github.io/ezboost/config)

---

## Storage Backends

Player boost states, cooldowns, and leaderboard data are persisted by [Jaloquent](https://github.com/EzFramework/Jaloquent).
Configure the backend in `plugins/EzBoost/storage.yml`:

| Backend | Notes |
|---------|-------|
| `yaml` | **Default.** Zero setup; data stored in flat files inside the plugin folder |
| `sqlite` | Single-file database; good for small-to-medium servers |
| `mysql` | Recommended for high-traffic or multi-server setups |
| `mariadb` | Drop-in MySQL-compatible alternative |
| `postgresql` | Full support; bring your own JDBC driver |

---

## Boost Tokens

Boost tokens are inventory items that activate a specific boost when right-clicked in the main hand.

- **Give tokens:** `/ezboost give <player> <key> [amount]`
- **Redeem:** The player right-clicks the token — it is consumed and the boost activates immediately.
- Tokens work as crate prizes, vote rewards, auction house listings, or shop items.

---

## WorldGuard Integration

EzBoost detects WorldGuard automatically. Use region IDs in `boosts.yml` to change any boost property
on a per-region basis — useful for PvP arenas, spawn zones, or event worlds.
If WorldGuard is not installed, region overrides are silently ignored.

→ Full reference: [Overrides](https://ez-plugins.github.io/ezboost/overrides)

---

## Documentation

Full documentation is at **<https://ez-plugins.github.io/ezboost>**.

| Page | What it covers |
|------|----------------|
| [Commands](https://ez-plugins.github.io/ezboost/commands) | All `/boost` and `/ezboost` commands |
| [Permissions](https://ez-plugins.github.io/ezboost/permissions) | Permissions reference and defaults |
| [Configuration](https://ez-plugins.github.io/ezboost/config) | All config files explained |
| [Boosts](https://ez-plugins.github.io/ezboost/boosts) | `boosts.yml` schema — effects, duration, costs |
| [GUI](https://ez-plugins.github.io/ezboost/gui) | `gui.yml` layout and slot configuration |
| [Overrides](https://ez-plugins.github.io/ezboost/overrides) | World and region override syntax |
| [Events](https://ez-plugins.github.io/ezboost/events) | Plugin lifecycle events |
| [Developer API](https://ez-plugins.github.io/ezboost/api) | Java API reference |
| [PlaceholderAPI](https://ez-plugins.github.io/ezboost/integration/PlaceholderAPI) | Available placeholders |

---

## Developer API

EzBoost exposes a Java API for starting/stopping boosts, querying active boost state, registering custom
effect types, and listening to lifecycle events (`BoostStartEvent`, `BoostEndEvent`).

```xml
<dependency>
  <groupId>com.github.ez-plugins</groupId>
  <artifactId>EzBoost</artifactId>
  <version>2.0.0</version>
</dependency>
```

→ Full reference: [Developer API](https://ez-plugins.github.io/ezboost/api)

---

## License

EzBoost is licensed under the [MIT License](LICENSE).
