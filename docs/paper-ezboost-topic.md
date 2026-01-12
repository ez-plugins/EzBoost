# EzBoost

![EzBoost GUI](https://i.ibb.co/1GgSfvWs/image.png)

**EzBoost** is a modern, production-ready boosts plugin for Spigot / Paper / Bukkit 1.7–1.21.*. It delivers configurable potion effects with GUI activation, cooldown management, optional Vault costs, world-based restrictions, boost tokens, and advanced region-based overrides with WorldGuard support. It is a renewed take on [RedBull](https://dev.bukkit.org/projects/redbull).

---

## ✨ Key Features

- **GUI-first activation**: Players can browse boosts with clear status, cooldown, and cost info.
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
- **Command hooks**: Run console commands on enable/disable/toggle per boost.
- **Player-friendly behavior**: Reapply boosts on join, keep on death, and refund on failed activation.

![EzBoost Boost Cooldown](https://i.ibb.co/nsKmgK0H/image.png)

---

## ⚡ Commands

| Command | Description | Permission |
| --- | --- | --- |
| `/boost` | Open the boosts GUI or show usage. | `ezboost.use` |
| `/boost <boostKey>` | Activate a boost directly. | `ezboost.use` + boost permission |
| `/ezboost reload` | Reload configuration and messages. | `ezboost.reload` |
| `/ezboost give <player> <boostKey> [amount]` | Give boost token items. Players redeem by right-clicking. | `ezboost.give` |

---

## 🛡️ Permissions

- `ezboost.use` — Use boosts (`/boost`).
- `ezboost.admin` — Access admin commands.
- `ezboost.reload` — Reload configuration.
- `ezboost.give` — Give boost tokens.
- `ezboost.cooldown.bypass` — Bypass boost cooldowns.
- `ezboost.boost.<key>` — Per-boost permissions (example: `ezboost.boost.speed`).

---

## 🚀 Quick Start

1. Place `EzBoost.jar` in your server’s `plugins/` folder.
2. Start the server to generate `config.yml`, `messages.yml`, and `data.yml`.
3. Configure boosts, cooldowns, costs, and GUI slots in `plugins/EzBoost/boosts.yml`, `gui.yml`, and related config files.
4. Use `/boost` to open the GUI or `/boost <boostKey>` for instant activation.
5. Use `/ezboost give <player> <boostKey> [amount]` to give boost tokens. Players redeem tokens by right-clicking them.
6. Grant per-boost permissions (like `ezboost.boost.speed`) to control access.

---

## ⚙️ Configuration Highlights

- **Boost definitions**: Add or edit boosts in `boosts.yml` with effects, duration, cooldown, cost, and permissions. See [docs/boosts.md](https://github.com/ez-plugins/ezboost/blob/main/docs/boosts.md) for a full reference.
- **Command hooks**: Add `commands.enable`, `commands.disable`, or `commands.toggle` per boost to run console commands when boosts turn on/off (supports `{player}`, `{displayname}`, and `{boost}` placeholders).
- **GUI layout**: Customize title, size, filler, lore templates, and per-boost slot positions in `gui.yml`.
- **Limits**: Clamp amplifier and duration ranges for balance in `limits.yml`.
- **World rules**: Use `worlds.allow-list` / `worlds.deny-list` to control where boosts apply in `worlds.yml`.
- **Region & World Overrides**: Use `boosts.yml` to define per-world or per-region settings. If WorldGuard is installed, region overrides are applied automatically using region IDs.
- **Behavior toggles**: Replace active boosts, reapply on join, keep on death, or refund failed activations in `settings.yml`.
- **Economy**: Enable Vault costs with `economy.enabled` and `economy.vault` in `economy.yml`.

---

## 🌍 WorldGuard Integration & Region Overrides

- EzBoost automatically detects WorldGuard if present and applies region-based overrides for boosts.
- No hard dependency: If WorldGuard is not installed, region overrides are ignored.
- Use region IDs from WorldGuard in your `boosts.yml` to customize boost behavior per region.
- See [docs/overrides.md](https://github.com/ez-plugins/EzBoost/blob/main/docs/overrides.md) for syntax and examples.

---

## ✅ Recommended Use Cases

- **Rank perks**: Grant unique boosts per rank using per-boost permissions.
- **Crates & events**: Distribute boost tokens as rewards.
- **Economy sinks**: Add costs to balance late-game progression.
- **World/region gating**: Enable or disable boosts only in specific worlds or WorldGuard regions.

---

## 📚 More Information & Documentation

- [EzBoost GitHub Repository](https://github.com/ez-plugins/EzBoost) — Source code, issues, and latest updates.
- [Configuration Guide](https://github.com/ez-plugins/EzBoost/blob/main/docs/config.md) — Full details on all config options.
- [Boosts Reference](https://github.com/ez-plugins/EzBoost/blob/main/docs/boosts.md) — YAML format and boost customization.
- [GUI Customization](https://github.com/ez-plugins/EzBoost/blob/main/docs/gui.md) — How to configure the boost GUI.
- [Overrides Documentation](https://github.com/ez-plugins/EzBoost/blob/main/docs/overrides.md) — Region/world override syntax and examples.