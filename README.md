[![Platform: Spigot | Paper | Bukkit](https://img.shields.io/badge/Platform-Spigot%20%7C%20Paper%20%7C%20Bukkit-blue?style=flat-square)](#)
[![Minecraft 1.7-1.21.*](https://img.shields.io/badge/Minecraft-1.7--1.21.*-brightgreen?style=flat-square)](#)
[![MIT License](https://img.shields.io/github/license/ez-plugins/ezboost?style=flat-square)](#)
[![Latest Release](https://img.shields.io/github/v/release/ez-plugins/ezboost?style=flat-square)](#)

# EzBoost

EzBoost is a modern, production-ready Minecraft plugin for Spigot, Paper, and Bukkit servers (Minecraft 1.7–1.21.*). It empowers server owners to offer configurable, time-limited potion boosts to players. Designed for flexibility, maintainability, and performance, EzBoost features a clean multi-file configuration system, a customizable GUI, per-boost cooldowns, world restrictions, and optional Vault economy integration.

---

## Features

- **Highly configurable**: Define custom boosts with effects, durations, cooldowns, permissions, and costs.
- **Multi-file configuration**: Clean separation of settings, GUI, boosts, and more for easy management.
- **Interactive GUI**: Customizable inventory interface for boost activation.
- **Per-boost cooldowns**: Prevents abuse and enables balanced gameplay.
- **World restrictions**: Allow or deny boosts in specific worlds.
- **Vault economy support**: Optionally charge players for activating boosts.
- **Boost token items**: Give, trade, or reward boost tokens.
- **Live reload**: Reload all configuration and messages at runtime.
- **MiniMessage support**: Rich formatting for all messages and GUI text.

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
- `/ezboost give <player> <boostKey> [amount]` — Give boost token items to a player.

### Permissions

- `ezboost.use` — Use boosts (`/boost`).
- `ezboost.admin` — Access admin commands.
- `ezboost.reload` — Reload configuration.
- `ezboost.give` — Give boost tokens.
- `ezboost.cooldown.bypass` — Bypass boost cooldowns.

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



For a complete overview of all configuration options, see the [Configuration Reference](docs/config.md).
For details on default boosts, see the [Boosts Reference](docs/boosts.md). For GUI customization, see the [GUI Reference](docs/gui.md).

#### Example: Adding a New Boost

1. Open `boosts.yml` and copy an existing boost section.
2. Change the key, display name, icon, effects, and other properties as needed.
3. Reload the plugin with `/ezboost reload`.

#### Example: Customizing the GUI

Edit `gui.yml` to change the GUI title, size, filler item, slot mapping, and lore/status text.

---

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines, code style, and module structure. All gameplay features and configuration logic should be organized in dedicated classes and packages under `/ezboost/`.

---

## Support & License

For help, open an issue or discussion on the repository.

EzBoost is licensed under the MIT License. See [LICENSE](LICENSE).
