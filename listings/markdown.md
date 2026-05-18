
**EzBoost** is a feature-rich potion boost plugin for Spigot, Paper, Bukkit, and **Folia** (Minecraft 1.21+). It provides an inventory GUI for boost selection, per-boost cooldowns, Vault economy integration, WorldGuard region overrides, boost token items, PlaceholderAPI support, persistent storage (YAML, SQLite, MySQL, MariaDB, PostgreSQL), and a leaderboard for top boost buyers. Inspired by [RedBull](https://dev.bukkit.org/projects/redbull).

![EzBoost GUI](https://i.ibb.co/1GgSfvWs/image.png)

![EzBoost Admin GUI](https://i.ibb.co/cXTcS3LT/image.png)

---

## ✨ Key Features

- **GUI-first activation**: Players can browse boosts with clear status, cooldown, and cost info.
- **Admin GUI**: Create and manage boosts through an intuitive admin interface.
- **Folia support**: Fully compatible with Folia servers – task scheduling routes through Folia's region schedulers automatically.
- **Boost top leaderboard**: `/boosttop` displays the all-time top boost buyers, backed by persistent storage.
- **Fully configurable boosts**: Define custom potion effects, amplifiers, durations, and permissions per boost.
- **Multi-file configuration**: Clean separation of settings, GUI, boosts, and more for easy management.
- **Interactive GUI**: Customizable inventory interface for boost activation.
- **Per-boost cooldowns**: Prevents abuse and enables balanced gameplay.
- **World restrictions**: Allow/deny boosts in specific worlds for tight gameplay tuning.
- **Region-based overrides (WorldGuard)**: Apply different boost settings or disable boosts in specific WorldGuard regions using the override system. WorldGuard is detected automatically if present.
- **Vault economy support**: Optionally charge players for activating boosts.
- **Boost token items**: Give, trade, or reward boost tokens with `/ezboost give`. Players redeem tokens by right-clicking them to activate the boost.
- **Live reload**: Reload all configuration and messages at runtime with `/ezboost reload`.
- **MiniMessage support**: Rich formatting for all messages and GUI text.
- **Internal message tags**: Boost-specific tags (`<boost_display>`, `<boost_cost>`, `<boost_duration>`, etc.) are available directly in `messages.yml` – no PlaceholderAPI required.
- **PlaceholderAPI expansion**: 18+ placeholders covering boost status, active boost, cooldowns, time remaining, XP multiplier, and economy formatting, usable in scoreboards, GUI plugins, and any PAPI-compatible plugin. See the [PlaceholderAPI integration guide](https://ez-plugins.github.io/EzBoost/integration/PlaceholderAPI).
- **Command hooks**: Run console commands on enable/disable/toggle per boost.
- **Player-friendly behavior**: Reapply boosts on join, keep on death, and refund on failed activation.

![EzBoost Boost Cooldown](https://i.ibb.co/nsKmgK0H/image.png)

---

## ⚡ Commands

| Command | Description | Permission |
| --- | --- | --- |
| `/boost` | Open the boosts GUI or show usage. | `ezboost.use` |
| `/boost <boostKey>` | Activate a boost directly. | `ezboost.use` + boost permission |
| `/boosttop` | View the top boost buyers leaderboard. | `ezboost.top` |
| `/ezboost create` | Open the admin GUI to create boosts. | `ezboost.admin` |
| `/ezboost reload` | Reload configuration and messages. | `ezboost.reload` |
| `/ezboost give <player> <boostKey> [amount]` | Give boost token items. Players redeem by right-clicking. | `ezboost.give` |

For detailed command and permission documentation, see the [Commands](https://ez-plugins.github.io/EzBoost/commands) and [Permissions](https://ez-plugins.github.io/EzBoost/permissions) pages.

---

## 🛡️ Permissions

- `ezboost.use` – Use boosts (`/boost`).
- `ezboost.top` – View the boost leaderboard (`/boosttop`).
- `ezboost.admin` – Access admin commands.
- `ezboost.reload` – Reload configuration.
- `ezboost.give` – Give boost tokens.
- `ezboost.cooldown.bypass` – Bypass boost cooldowns.
- `ezboost.boost.<key>` – Per-boost permissions (example: `ezboost.boost.speed`).

---

## 🚀 Quick Start

1. Place `EzBoost.jar` in your server’s `plugins/` folder.
2. Start the server to generate all config files in `plugins/EzBoost/` (`settings.yml`, `boosts.yml`, `gui.yml`, `messages.yml`, etc.).
3. Use `/ezboost create` to open the admin GUI and create boosts.
4. Configure boosts, cooldowns, costs, and GUI slots in `plugins/EzBoost/boosts.yml`, `gui.yml`, and related config files.
5. Use `/boost` to open the GUI or `/boost <boostKey>` for instant activation.
6. Use `/ezboost give <player> <boostKey> [amount]` to give boost tokens. Players redeem tokens by right-clicking them.
7. Grant per-boost permissions (like `ezboost.boost.speed`) to control access.

---

## ⚙️ Configuration Highlights

- **Boost definitions**: Add or edit boosts in `boosts.yml` with effects, duration, cooldown, cost, and permissions. See the [Boosts reference](https://ez-plugins.github.io/EzBoost/boosts) for a full reference.
- **Command hooks**: Add `commands.enable`, `commands.disable`, or `commands.toggle` per boost to run console commands when boosts turn on/off (supports `{player}`, `{displayname}`, and `{boost}` placeholders).
- **GUI layout**: Customize title, size, filler, lore templates, and per-boost slot positions in `gui.yml`.
- **Limits**: Clamp amplifier and duration ranges for balance in `limits.yml`.
- **World rules**: Use `worlds.allow-list` / `worlds.deny-list` to control where boosts apply in `worlds.yml`.
- **Storage backend**: Choose YAML (default), SQLite, MySQL, MariaDB, or PostgreSQL in `storage.yml`.
- **Region & World Overrides**: Use `boosts.yml` to define per-world or per-region settings. If WorldGuard is installed, region overrides are applied automatically using region IDs.
- **Behavior toggles**: Replace active boosts, reapply on join, keep on death, or refund failed activations in `settings.yml`.
- **Economy**: Enable Vault costs with `economy.enabled` and `economy.vault` in `economy.yml`.

---

## 🌍 WorldGuard Integration & Region Overrides

- EzBoost automatically detects WorldGuard if present and applies region-based overrides for boosts.
- No hard dependency: If WorldGuard is not installed, region overrides are ignored.
- Use region IDs from WorldGuard in your `boosts.yml` to customize boost behavior per region.
- See the [Overrides documentation](https://ez-plugins.github.io/EzBoost/overrides) for syntax and examples.

---

## ✅ Recommended Use Cases

- **Rank perks**: Grant unique boosts per rank using per-boost permissions.
- **Crates & events**: Distribute boost tokens as rewards.
- **Economy sinks**: Add costs to balance late-game progression.
- **World/region gating**: Enable or disable boosts only in specific worlds or WorldGuard regions.

---

## 📚 Documentation & Support

Full documentation is available at **[ez-plugins.github.io/EzBoost](https://ez-plugins.github.io/EzBoost)**.

| Page | What it covers |
|------|----------------|
| [Commands](https://ez-plugins.github.io/EzBoost/commands) | All `/boost` and `/ezboost` commands |
| [Permissions](https://ez-plugins.github.io/EzBoost/permissions) | Permissions reference and defaults |
| [Configuration](https://ez-plugins.github.io/EzBoost/config) | All config files explained |
| [Boosts](https://ez-plugins.github.io/EzBoost/boosts) | `boosts.yml` schema – effects, duration, costs |
| [GUI](https://ez-plugins.github.io/EzBoost/gui) | `gui.yml` layout and slot configuration |
| [Overrides](https://ez-plugins.github.io/EzBoost/overrides) | World and region override syntax |
| [PlaceholderAPI](https://ez-plugins.github.io/EzBoost/integration/PlaceholderAPI) | 18+ available placeholders |

**Need help or want to chat? Join our Discord:**
[https://discord.gg/yWP95XfmBS](https://discord.gg/yWP95XfmBS)

[![Try the other Minecraft plugins in the EzPlugins series](https://i.ibb.co/PzfjNjh0/ezplugins-try-other-plugins.png)](https://modrinth.com/collection/Q98Ov6dA)
