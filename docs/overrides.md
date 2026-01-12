# Boost Overrides: Worlds, Groups, and Regions

EzBoost supports advanced configuration overrides for boosts based on world, world group, and WorldGuard region. This allows you to customize boost behavior for specific worlds or protected regions on your server.

## Table of Contents
- [Overview](#overview)
- [Configuration Structure](#configuration-structure)
- [Override Priority](#override-priority)
- [Examples](#examples)
- [WorldGuard Region Support](#worldguard-region-support)
- [Best Practices](#best-practices)

## Overview
You can override any boost's settings for a specific world, a group of worlds, or a WorldGuard region. The plugin will automatically use the most specific override available for each player.

## Configuration Structure
Overrides are defined in `boosts.yml` under each boost key:

```yaml
my_boost:
  # ...global/default settings...
  overrides:
    worlds:
      world_nether:
        # settings for this world
    worldgroups:
      minigames:
        # settings for this group
    regions:
      spawn:
        # settings for this WorldGuard region
```

- `worlds`: Map of world name → override settings
- `worldgroups`: Map of group name → override settings (define groups in config)
- `regions`: Map of WorldGuard region name → override settings

## Override Priority
When a player is in a region/world/group with overrides, the plugin uses the following priority:

1. **Region** (highest priority, if player is in a WorldGuard region with an override)
2. **World** (if the player's world has an override)
3. **World Group** (if the player's world is in a group with an override)
4. **Global** (default boost settings)

## Examples
### Per-World Override
```yaml
speed_boost:
  duration-seconds: 60
  amplifier: 1
  overrides:
    worlds:
      world_nether:
        duration-seconds: 30
        amplifier: 2
```

### Per-WorldGroup Override
```yaml
speed_boost:
  # ...
  overrides:
    worldgroups:
      minigames:
        enabled: false
```

### Per-Region Override
```yaml
speed_boost:
  # ...
  overrides:
    regions:
      spawn:
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
