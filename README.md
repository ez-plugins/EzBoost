[![Platform: Spigot | Paper | Bukkit](https://img.shields.io/badge/Platform-Spigot%20%7C%20Paper%20%7C%20Bukkit-blue?style=flat-square)](#)
[![Minecraft 1.7-1.21.*](https://img.shields.io/badge/Minecraft-1.7--1.21.*-brightgreen?style=flat-square)](#)
[![MIT License](https://img.shields.io/github/license/ez-plugins/ezboost?style=flat-square)](#)
[![Latest Release](https://img.shields.io/github/v/release/ez-plugins/ezboost?style=flat-square)](#)

# EzBoost

EzBoost is a modern, production-ready Minecraft plugin for Spigot, Paper, and Bukkit servers (Minecraft 1.7–1.21.*). It empowers server owners to offer configurable, time-limited potion boosts to players. Designed for flexibility, maintainability, and performance, EzBoost features a clean multi-file configuration system, a customizable GUI, per-boost cooldowns, world restrictions, optional Vault economy integration, boost token items, and advanced region-based overrides with WorldGuard support.

---

## Features

- **Highly configurable**: Define custom boosts with effects, durations, cooldowns, permissions, and costs.
- **Multi-file configuration**: Clean separation of settings, GUI, boosts, and more for easy management.
- **Interactive GUI**: Customizable inventory interface for boost activation.
- **Per-boost cooldowns**: Prevents abuse and enables balanced gameplay.
- **World restrictions**: Allow or deny boosts in specific worlds.
- **Region-based overrides (WorldGuard)**: Apply different boost settings or disable boosts in specific WorldGuard regions using the built-in override system. No hard dependency—WorldGuard is detected automatically if present.
- **Vault economy support**: Optionally charge players for activating boosts.
- **Boost token items**: Give, trade, or reward boost tokens. Players redeem tokens by right-clicking them to activate the corresponding boost.
- **Live reload**: Reload all configuration and messages at runtime.
- **MiniMessage support**: Rich formatting for all messages and GUI text.

---


## Documentation

- [Commands Reference](docs/commands.md)
- [Permissions Reference](docs/permissions.md)
- [Overrides Guide](docs/overrides.md)
- [GUI Guide](docs/gui.md)
- [Configuration Guide](docs/config.md)
- [Boosts Guide](docs/boosts.md)
- [API Overview](docs/api.md)
  - [EzBoostAPI Reference](docs/api/EzBoostAPI.md)
  - [CustomBoostEffect Reference](docs/api/CustomBoostEffect.md)
  - [Events Overview](docs/events.md)
    - [BoostStartEvent Reference](docs/events/BoostStartEvent.md)
    - [BoostEndEvent Reference](docs/events/BoostEndEvent.md)

---

## Installation

1. Build the plugin JAR from this repository or download a release from the [releases page](https://github.com/ez-plugins/ezboost/releases).
2. Place `EzBoost-<version>.jar` in your server's `plugins/` directory.
3. Start your Spigot, Paper, or Bukkit server (Minecraft 1.7–1.21.*). EzBoost will generate all required configuration files in the plugin data folder.

---

## Usage

### Commands

- `/boost` — Open the boosts GUI (if enabled) or display usage.
- `/boost <boostKey>` — Activate a boost directly.
- `/ezboost reload` — Reload all configuration and messages.
- `/ezboost give <player> <boostKey> [amount]` — Give boost token items to a player. Tokens can be redeemed by right-clicking them.

### Permissions

- `ezboost.use` — Use boosts (`/boost`).
- `ezboost.admin` — Access admin commands.
- `ezboost.reload` — Reload configuration.
- `ezboost.give` — Give boost tokens.
- `ezboost.cooldown.bypass` — Bypass boost cooldowns.
- `ezboost.boost.<key>` — Per-boost permissions (example: `ezboost.boost.speed`).

---

## Boost Tokens

Boost tokens are special items that can be given to players as rewards, crate prizes, or shop items.  
- **Giving tokens:** Use `/ezboost give <player> <boostKey> [amount]` to give boost tokens.
- **Redeeming tokens:** Players right-click a boost token in their main hand to instantly activate the corresponding boost. The token is consumed on use.

---

## Configuration

EzBoost uses a multi-file configuration system for clarity and maintainability. All configuration files are located in the plugin's data folder:

- `settings.yml` — General plugin toggles (e.g., replace-active-boost, keep-boost-on-death).
- `limits.yml` — Minimum/maximum duration and amplifier values for boosts.
- `worlds.yml` — World allow/deny lists for boost usage.
- `economy.yml` — Economy integration settings (Vault, enable/disable).
- `gui.yml` — GUI layout, appearance, and slot mapping.
- `boosts.yml` — All boost definitions (effects, duration, cooldown, cost, permissions).
- `messages.yml` — MiniMessage-formatted strings for feedback and actionbar text.
- `data.yml` — Persisted player boost states and cooldowns (auto-managed).

### Region & World Overrides

EzBoost supports advanced overrides for boosts based on world or WorldGuard region. You can:
- Change boost effects, duration, cost, or permissions for a specific world or region.
- Disable certain boosts in specific regions (e.g., PvP arenas, spawn zones).
- Use `boosts.yml` to define per-world or per-region settings. If WorldGuard is installed, region overrides are applied automatically using reflection (no hard dependency).

See [docs/overrides.md](docs/overrides.md) for syntax and examples.

---

## WorldGuard Integration

- EzBoost automatically detects WorldGuard if present and applies region-based overrides for boosts.
- No hard dependency: If WorldGuard is not installed, region overrides are ignored.
- Use region IDs from WorldGuard in your `overrides.yml` to customize boost behavior per region.

---

## Support & License

For help, open an issue or discussion on the repository.

EzBoost is licensed under the MIT License. See [LICENSE](LICENSE).
