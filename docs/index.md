---
layout: home
title: EzBoost
nav_order: 1
description: "Modern Minecraft boost plugin for Spigot, Paper, and Bukkit"
permalink: /
---

# EzBoost

[![CI](https://github.com/ez-plugins/ezboost/actions/workflows/smoke-test.yml/badge.svg)](https://github.com/ez-plugins/ezboost/actions/workflows/smoke-test.yml)
[![GitHub Packages](https://img.shields.io/badge/GitHub_Packages-2.0.0-blue?logo=github)](https://github.com/ez-plugins/ezboost/packages)
[![Coverage](https://img.shields.io/codecov/c/github/ez-plugins/ezboost)](https://codecov.io/github/ez-plugins/ezboost)

**EzBoost** is a modern, production-ready Minecraft plugin for Spigot, Paper, and Bukkit servers (1.7–1.21.*).
It gives server owners full control over configurable, time-limited potion boosts — complete with GUI,
cooldowns, economy integration, WorldGuard region support, and a developer API.

---

## Features

- **Flexible boost definitions** — configure any potion effect with duration, amplifier, cooldowns, limits, and particle effects via `boosts.yml`
- **Interactive GUI** — fully-customisable chest-based boost menu driven by `gui.yml`; fully disableable
- **Economy integration** — optional Vault economy support with per-boost pricing; gracefully skipped when Vault is absent
- **WorldGuard support** — restrict boost activation to specific WorldGuard regions
- **Per-player and global limits** — cap how many active boosts a player or the server can run simultaneously
- **Overrides** — server-wide event-driven multipliers layered on top of individual boosts
- **PlaceholderAPI** — exposes boost state and duration as placeholders for scoreboards, holograms, and more
- **MiniMessage formatting** — all messages use the Adventure MiniMessage format for rich, hex-colour text
- **Developer API** — clean Java API to start/stop boosts and listen to lifecycle events from other plugins

---

## Quick start

**1. Download the plugin:**

Grab the latest `EzBoost-x.y.z.jar` from [Releases](https://github.com/ez-plugins/ezboost/releases)
and drop it into your server's `plugins/` directory.

**2. Start your server once** to generate default configuration files in `plugins/EzBoost/`.

**3. Edit `boosts.yml`** to define your server's boost types, then **edit `settings.yml`** to enable economy or WorldGuard if needed.

**4. Reload with `/ezboost reload`** (requires `ezboost.admin`).

---

## Documentation

| Page | What it covers |
|------|----------------|
| [Commands](commands) | All `/boost` and `/ezboost` commands with syntax and permissions |
| [Permissions](permissions) | Full permissions reference and default values |
| [Configuration](config) | `settings.yml`, `limits.yml`, `worlds.yml`, `economy.yml` |
| [Boosts](boosts) | `boosts.yml` schema — effects, duration, cooldowns, costs |
| [GUI](gui) | `gui.yml` schema — slots, items, actions |
| [Overrides](overrides) | Server-wide boost multiplier overrides |
| [Events](events) | Plugin events overview |
| &nbsp;&nbsp;[BoostStartEvent](events/BoostStartEvent) | Fired when a boost activates |
| &nbsp;&nbsp;[BoostEndEvent](events/BoostEndEvent) | Fired when a boost expires or is cancelled |
| [API](api) | EzBoost developer API overview |
| &nbsp;&nbsp;[EzBoostAPI](api/EzBoostAPI) | Full public-method reference |
| &nbsp;&nbsp;[CustomBoostEffect](api/CustomBoostEffect) | Implementing custom boost effects |
| [PlaceholderAPI](integration/PlaceholderAPI) | Available placeholders and usage |

---

## Developer API

EzBoost exposes a Java API for starting/stopping boosts, querying active state, registering custom
effect types, and listening to lifecycle events. See the [API reference](api) for setup instructions,
method tables, and code examples.

```xml
<dependency>
  <groupId>com.github.ez-plugins</groupId>
  <artifactId>EzBoost</artifactId>
  <version>2.0.0</version>
</dependency>
```
