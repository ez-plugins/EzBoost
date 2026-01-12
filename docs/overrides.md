# Boost Overrides: Worlds, Groups, and Regions

EzBoost supports advanced configuration overrides for boosts based on world, world group, and WorldGuard region. This allows you to customize boost behavior for specific worlds or protected regions on your server.

## Table of Contents
- [Boost Overrides: Worlds, Groups, and Regions](#boost-overrides-worlds-groups-and-regions)
  - [Table of Contents](#table-of-contents)
  - [Overview](#overview)
  - [Configuration Structure](#configuration-structure)
  - [Override Priority](#override-priority)
  - [Examples](#examples)
    - [Per-World Override](#per-world-override)
    - [Per-WorldGroup Override](#per-worldgroup-override)
    - [Per-Region Override](#per-region-override)
  - [WorldGuard Region Support](#worldguard-region-support)
  - [Best Practices](#best-practices)

## Overview
You can override any boost's settings for a specific world, a group of worlds, or a WorldGuard region. The plugin will automatically use the most specific override available for each player.

## Configuration Structure
Overrides are defined at the TOP LEVEL of `boosts.yml` (not in a separate file, and not under each boost key):

```yaml
boosts:
  speed:
    duration-seconds: 60
    amplifier: 1
    # ...
  jump:
    # ...

overrides:
  worlds:
    world_nether:
      speed:
        duration-seconds: 30
        amplifier: 2
  worldgroups:
    minigames:
      speed:
        enabled: false
  regions:
    spawn:
      jump:
        enabled: false
```

- `worlds`: Map of world name → per-boost override settings
- `worldgroups`: Map of group name → per-boost override settings (define groups in config)
- `regions`: Map of WorldGuard region name → per-boost override settings

## Override Priority
When a player is in a region/world/group with overrides, the plugin uses the following priority:

1. **Region** (highest priority, if player is in a WorldGuard region with an override)
2. **World** (if the player's world has an override)
3. **World Group** (if the player's world is in a group with an override)
4. **Global** (default boost settings)

## Examples
### Per-World Override
```yaml
overrides:
  worlds:
    world_nether:
      speed:
        duration-seconds: 30
        amplifier: 2
```

### Per-WorldGroup Override
```yaml
overrides:
  worldgroups:
    minigames:
      speed:
        enabled: false
```

### Per-Region Override
```yaml
overrides:
  regions:
    spawn:
      jump:
        enabled: false
```

## WorldGuard Region Support
- Region overrides require WorldGuard to be installed.
- The plugin uses the highest-priority region the player is in.
- If WorldGuard is not present, region overrides are ignored.

## Best Practices
- Only specify override fields you want to change; all other settings inherit from the global boost definition.
- Use region overrides for spawn, PvP arenas, or special protected areas.
- Use worldgroups to manage multiple worlds with the same rules.

---
For more details, see the main configuration and example files.
