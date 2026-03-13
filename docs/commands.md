# EzBoost Commands

This document details all commands provided by EzBoost, including syntax, arguments, permissions, and usage examples.

## Player Commands

### `/boost`
- **Description**: Opens the boosts GUI where players can browse and activate available boosts. If the GUI is disabled, displays usage information.
- **Permission**: `ezboost.use`
- **Usage**: `/boost`
- **Example**:
  ```
  /boost
  ```
  Opens the interactive boosts menu.

### `/boost <boostKey>`
- **Description**: Directly activates a specific boost by its key, bypassing the GUI.
- **Permission**: `ezboost.use` + `ezboost.boost.<boostKey>` (per-boost permission)
- **Usage**: `/boost <boostKey>`
- **Example**:
  ```
  /boost speed
  ```
  Activates the "speed" boost if available and permitted.

## Admin Commands

### `/ezboost create`
- **Description**: Opens the admin GUI for creating and managing boosts. Allows administrators to define new boosts with effects, durations, cooldowns, and more.
- **Permission**: `ezboost.admin`
- **Usage**: `/ezboost create`
- **Example**:
  ```
  /ezboost create
  ```
  Opens the boost creation interface.

### `/ezboost reload`
- **Description**: Reloads all configuration files and messages at runtime without restarting the server.
- **Permission**: `ezboost.reload`
- **Usage**: `/ezboost reload`
- **Example**:
  ```
  /ezboost reload
  ```
  Reloads boosts.yml, gui.yml, messages.yml, and other config files.

### `/ezboost give <player> <boostKey> [amount]`
- **Description**: Gives boost token items to a player. Tokens can be redeemed by right-clicking them to activate the boost.
- **Permission**: `ezboost.give`
- **Usage**: `/ezboost give <player> <boostKey> [amount]`
- **Parameters**:
  - `<player>`: The target player's name
  - `<boostKey>`: The key of the boost to give tokens for
  - `[amount]`: Optional amount of tokens (default: 1)
- **Example**:
  ```
  /ezboost give Steve speed 5
  ```
  Gives 5 speed boost tokens to player Steve.

For detailed permissions documentation, see [docs/permissions.md](permissions.md).

## Notes

- Boost keys are case-insensitive.
- Players must have both `ezboost.use` and the specific `ezboost.boost.<key>` permission to activate a boost.
- The admin GUI (`/ezboost create`) provides an intuitive interface for creating boosts without editing config files directly.
- Boost tokens are consumable items that players can right-click to activate boosts instantly.

- Boost keys are case-insensitive.
- Players must have both `ezboost.use` and the specific `ezboost.boost.<key>` permission to activate a boost.
- The admin GUI (`/ezboost create`) provides an intuitive interface for creating boosts without editing config files directly.
- Boost tokens are consumable items that players can right-click to activate boosts instantly.</content>
<parameter name="filePath">/home/niels/gyvex/EzBoost/docs/commands.md
