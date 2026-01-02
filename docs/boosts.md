# EzBoost – Default Boosts Reference

This document describes the default boosts provided in `boosts.yml` for EzBoost. Each boost can be customized or extended in your configuration.

---

## Boosts Overview

| Key          | Display Name           | Icon         | Effects                        | Duration (s) | Cooldown (s) | Cost | Permission                | Enabled |
|--------------|-----------------------|--------------|-------------------------------|--------------|--------------|------|---------------------------|---------|
| speed        | Speed Boost           | SUGAR        | SPEED (1)                     | 300          | 600          | 0.0  | ezboost.boost.speed       | true    |
| jump         | Jump Boost            | RABBIT_FOOT  | JUMP (2)                      | 300          | 600          | 0.0  | ezboost.boost.jump        | true    |
| haste        | Haste Boost           | GOLDEN_PICKAXE| FAST_DIGGING (1)              | 300          | 600          | 0.0  | ezboost.boost.haste       | true    |
| strength     | Strength Boost        | BLAZE_POWDER | INCREASE_DAMAGE (1)           | 300          | 600          | 0.0  | ezboost.boost.strength    | true    |
| regen        | Regen Boost           | GHAST_TEAR   | REGENERATION (1)              | 300          | 600          | 0.0  | ezboost.boost.regen       | true    |
| nightvision  | Night Vision Boost    | LANTERN      | NIGHT_VISION (0), SPEED (0)   | 300          | 600          | 0.0  | ezboost.boost.nightvision | true    |
| resistance   | Resistance Boost      | SHIELD       | DAMAGE_RESISTANCE (0), REGENERATION (0) | 300 | 600 | 0.0 | ezboost.boost.resistance  | true    |
| fireresist   | Fire Resist Boost     | MAGMA_CREAM  | FIRE_RESISTANCE (0), DAMAGE_RESISTANCE (0) | 300 | 600 | 0.0 | ezboost.boost.fireresist  | true    |

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

For more details, see the [EzBoost documentation](https://github.com/ez-plugins/ezboost).
