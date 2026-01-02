# EzBoost – GUI Configuration Reference

This document explains all options available in `gui.yml` for EzBoost. Use this as a guide to customize the in-game boost selection menu.

---

## Structure Overview

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

---

## Option Reference

- **enabled**: `true`/`false` — Enable or disable the GUI entirely.
- **title**: String — The GUI title (MiniMessage formatting supported).
- **size**: Integer — Number of slots (must be a multiple of 9, max 54).
- **close-on-click**: `true`/`false` — Whether the GUI closes after a boost is selected.
- **filler**: Section for the background/filler item.
  - **material**: Item material (e.g., `BLACK_STAINED_GLASS_PANE`).
  - **name**: Display name for the filler item.
  - **lore**: List of lore lines for the filler item.
- **lore**: List — Template for each boost's lore. Supports placeholders:
  - `<duration>`, `<cooldown>`, `<cost>`, `<status>`
- **status**: Section — Custom text for each status type:
  - **available**, **locked**, **cooldown**, **active**
- **slots**: Section — Maps each boost key to a slot index (0-based).

---

## Customization Tips

- Use MiniMessage formatting for colors, gradients, and effects in titles and lore.
- Adjust the `size` to fit your number of boosts (9, 18, 27, 36, 45, or 54).
- Place boosts in specific slots by editing the `slots` section.
- Add or remove status types as needed for your server's style.
- The `filler` item fills all unused slots for a clean look.

---

For more details and advanced examples, see the [EzBoost documentation](https://github.com/ez-plugins/ezboost).
