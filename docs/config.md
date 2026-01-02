# EzBoost – Configuration Options Reference

This document provides a complete reference for all configuration files and options available in EzBoost. Use this guide to understand and customize every aspect of the plugin.

---

## Table of Contents
- [settings.yml](#settingsyml)
- [limits.yml](#limitsyml)
- [worlds.yml](#worldsyml)
- [economy.yml](#economyyml)
- [messages.yml](#messagesyml)
- [gui.yml](#guiyml)
- [boosts.yml](#boostsyml)

---

## settings.yml

Controls general plugin behavior.

```yaml
settings:
  replace-active-boost: false   # Replace active boost if a new one is activated
  refund-on-fail: true         # Refund cost if boost activation fails
  keep-boost-on-death: true    # Keep boost effects after player death
  reapply-on-join: true        # Reapply active boosts when player rejoins
  send-expired-message: true   # Notify player when boost expires
  cooldown-per-boost-type: true # Separate cooldowns for each boost type
```

---

## limits.yml

Defines allowed ranges for boost durations and amplifiers.

```yaml
limits:
  duration-min: 5      # Minimum allowed duration (seconds)
  duration-max: 3600   # Maximum allowed duration (seconds)
  amplifier-min: 0     # Minimum allowed amplifier (effect strength)
  amplifier-max: 5     # Maximum allowed amplifier (effect strength)
```

---

## worlds.yml

Controls which worlds allow or deny boost usage.

```yaml
worlds:
  allow-list: []   # List of world names where boosts are allowed (empty = all worlds allowed)
  deny-list: []    # List of world names where boosts are denied (empty = no worlds denied)
```
- If both lists are empty, boosts are allowed in all worlds.
- If a world appears in both lists, deny-list takes priority.

---

## economy.yml

Controls economy integration and Vault support.

```yaml
economy:
  enabled: true   # Enable/disable economy features
  vault: true     # Use Vault for cost handling
```

---

## messages.yml

Customize all plugin messages and prefixes. Supports MiniMessage formatting.

```yaml
prefix: "<gray>[<aqua>EzBoost</aqua>]</gray> "
no-permission: "<red>You don't have permission.</red>"
boost-not-found: "<red>Boost not found.</red>"
boost-disabled: "<red>This boost is currently disabled.</red>"
boost-active: "<yellow>You already have that boost active.</yellow>"
boost-replaced: "<yellow>Your active boost was replaced.</yellow>"
boost-activated: "<green>Boost activated: <white><boost></white>.</green>"
boost-expired: "<gray>Your boost <white><boost></white> has expired.</gray>"
boost-cooldown: "<red>That boost is on cooldown for <white><time></white> seconds.</red>"
insufficient-funds: "<red>You need <white><cost></white> to activate this boost.</red>"
economy-unavailable: "<red>Economy is unavailable. Please contact an admin.</red>"
cost-charged: "<gray>Charged <white><cost></white> for <white><boost></white>.</gray>"
reload: "<green>EzBoost reloaded.</green>"
only-players: "<red>Only players can use this command.</red>"
invalid-target: "<red>Player not found.</red>"
token-given: "<green>Gave <white><amount>x</white> <white><boost></white> boost token(s) to <white><player></white>.</green>"
token-used: "<green>Activated <white><boost></white> from a boost token.</green>"
world-blocked: "<red>You can't use boosts in this world.</red>"
actionbar:
  enabled: false
  format: "<gray>Boost:</gray> <white><boost></white> <gray>(<time>s)</gray>"
```

**Placeholders:** `<boost>`, `<time>`, `<cost>`, `<amount>`, `<player>`

---

## gui.yml

Controls the in-game boost selection menu. See also [docs/gui.md](gui.md) for advanced details.

```yaml
gui:
  enabled: true
  title: "<gradient:#00d2ff:#3a47d5>EzBoost</gradient>"
  size: 45
  close-on-click: true
  filler:
    material: BLACK_STAINED_GLASS_PANE
    name: "<gray> </gray>"
    lore: []
  lore:
    - "<gray>Duration:</gray> <white><duration>s</white>"
    - "<gray>Cooldown:</gray> <white><cooldown>s</white>"
    - "<gray>Cost:</gray> <white><cost></white>"
    - "<gray>Status:</gray> <status>"
  status:
    available: "<green>Available</green>"
    locked: "<red>Locked</red>"
    cooldown: "<gold>Cooldown</gold>"
    active: "<aqua>Active</aqua>"
  slots:
    speed: 10
    jump: 11
    haste: 12
    strength: 13
    regen: 14
    nightvision: 15
    resistance: 16
    fireresist: 17
```

**Placeholders:** `<duration>`, `<cooldown>`, `<cost>`, `<status>`

---

## boosts.yml

Defines all available boosts. See [docs/boosts.md](boosts.md) for a full reference and customization guide.

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

For more details, see the full EzBoost documentation on [GitHub](https://github.com/ez-plugins/ezboost).
