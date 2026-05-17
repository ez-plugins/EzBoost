---
title: Boosts
nav_order: 5
description: "boosts.yml schema — defining effects, durations, cooldowns, costs, and particles"
---

# EzBoost – Default Boosts Reference
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

This document describes the default boosts provided in `boosts.yml` for EzBoost. Each boost can be customized or extended in your configuration.

---

## Boosts Overview

| Key            | Display Name           | Icon               | Effects                                                    | Duration (s) | Cooldown (s) | Cost | Permission                   | Enabled |
|----------------|------------------------|--------------------|------------------------------------------------------------|--------------|--------------|------|------------------------------|---------|
| speed          | Speed Boost           | SUGAR              | SPEED (1)                                          | 300 | 600 | 0.0 | ezboost.boost.speed          | true |
| jump           | Jump Boost            | RABBIT_FOOT        | JUMP (2)                                           | 300 | 600 | 0.0 | ezboost.boost.jump           | true |
| haste          | Haste Boost           | GOLDEN_PICKAXE     | FAST_DIGGING (1)                                   | 300 | 600 | 0.0 | ezboost.boost.haste          | true |
| strength       | Strength Boost        | BLAZE_POWDER       | INCREASE_DAMAGE (1)                                | 300 | 600 | 0.0 | ezboost.boost.strength       | true |
| regen          | Regen Boost           | GHAST_TEAR         | REGENERATION (1)                                   | 300 | 600 | 0.0 | ezboost.boost.regen          | true |
| nightvision    | Night Vision Boost    | LANTERN            | NIGHT_VISION (0), SPEED (0)                        | 300 | 600 | 0.0 | ezboost.boost.nightvision    | true |
| resistance     | Resistance Boost      | SHIELD             | DAMAGE_RESISTANCE (0), REGENERATION (0)            | 300 | 600 | 0.0 | ezboost.boost.resistance     | true |
| fireresist     | Fire Resist Boost     | MAGMA_CREAM        | FIRE_RESISTANCE (0), DAMAGE_RESISTANCE (0)         | 300 | 600 | 0.0 | ezboost.boost.fireresist     | true |
| waterbreathing | Water Breathing Boost | PUFFERFISH         | WATER_BREATHING (0)                                | 300 | 600 | 0.0 | ezboost.boost.waterbreathing | true |
| saturation     | Saturation Boost      | COOKED_BEEF        | SATURATION (0)                                     | 300 | 600 | 0.0 | ezboost.boost.saturation     | true |
| luck           | Luck Boost            | EMERALD            | LUCK (0)                                           | 300 | 600 | 0.0 | ezboost.boost.luck           | true |
| absorption     | Absorption Boost      | GOLDEN_APPLE       | ABSORPTION (1)                                     | 300 | 600 | 0.0 | ezboost.boost.absorption     | true |
| slowfall       | Slow Falling Boost    | PHANTOM_MEMBRANE   | SLOW_FALLING (0)                                   | 300 | 600 | 0.0 | ezboost.boost.slowfall       | true |
| miner          | Miner Boost           | DIAMOND_PICKAXE    | FAST_DIGGING (2), NIGHT_VISION (0)                 | 300 | 600 | 0.0 | ezboost.boost.miner          | true |
| warrior        | Warrior Boost         | DIAMOND_SWORD      | INCREASE_DAMAGE (1), ABSORPTION (1)                | 300 | 600 | 0.0 | ezboost.boost.warrior        | true |
| farmer         | Farmer Boost          | WHEAT              | SATURATION (0), LUCK (0)                           | 300 | 600 | 0.0 | ezboost.boost.farmer         | true |
| explorer       | Explorer Boost        | COMPASS            | SPEED (1), JUMP (1)                                | 300 | 600 | 0.0 | ezboost.boost.explorer       | true |
| xpboost        | XP Boost              | EXPERIENCE_BOTTLE  | xpboost (0) *(2× XP multiplier)*                  | 300 | 600 | 0.0 | ezboost.boost.xpboost        | true |
| diver          | Diver Boost           | HEART_OF_THE_SEA   | WATER_BREATHING (0), DOLPHINS_GRACE (0), CONDUIT_POWER (0) | 300 | 600 | 0.0 | ezboost.boost.diver | true |

---

## Example: Boost Definition

```yaml
speed:
  display-name: "<aqua>Speed Boost</aqua>"
  icon: SUGAR
  effects:
    - type: SPEED
      amplifier: 1
  commands:
    enable: []
    disable: []
    toggle: []
  duration: 300
  cooldown: 600
  cost: 0.0
  permission: "ezboost.boost.speed"
  enabled: true
```

---

## Customization Tips

- **Display Name**: Supports MiniMessage formatting for colors and gradients.
- **Icon**: Use any valid Minecraft material name.
- **Effects**: List one or more potion effects with type and amplifier.
- **Commands**: Run console commands on enable/disable/toggle (supports `{player}`, `{displayname}`, `{boost}` placeholders).
- **Duration/Cooldown**: Set in seconds.
- **Cost**: Requires Vault if nonzero.
- **Permission**: Controls who can use each boost.
- **Enabled**: Set to `false` to hide a boost from the GUI.

### Per-effect cooldowns and custom effects

- EzBoost supports optional per-effect cooldown tracking. When `settings.cooldown-per-effect` is enabled (see `settings.yml`), each effect in a boost may have its own cooldown tracked independently. This is useful when a boost contains multiple effects and you want separate cooldowns for each.
- For built-in potion effects the boost uses the boost-level `cooldown` unless a custom effect provides its own cooldown value.
- For custom effects, implement `CustomBoostEffect.getCooldownSeconds()` to return the desired cooldown (default 0 = no cooldown). See the API docs for details.

For more details, see the [EzBoost documentation](https://github.com/ez-plugins/ezboost).
