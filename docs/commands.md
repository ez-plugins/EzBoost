# EzBoost Commands Reference

This document details all commands provided by EzBoost, including syntax, arguments, permissions, and usage examples.

## Player Commands

### `/boost`
- **Description:** Opens the boosts GUI (if enabled) or displays usage information.
- **Permission:** `ezboost.use`
- **Usage:**
  ```
  /boost
  ```

### `/boost <boostKey>`
- **Description:** Activates a specific boost directly by key.
- **Permission:** `ezboost.use` and `ezboost.boost.<key>`
- **Usage:**
  ```
  /boost speed
  ```

## Admin Commands

### `/ezboost reload`
- **Description:** Reloads all configuration and messages without restarting the server.
- **Permission:** `ezboost.reload`
- **Usage:**
  ```
  /ezboost reload
  ```

### `/ezboost give <player> <boostKey> [amount]`
- **Description:** Gives boost token items to a player. Tokens can be redeemed by right-clicking.
- **Permission:** `ezboost.give`
- **Usage:**
  ```
  /ezboost give Steve speed 3
  ```

## Command Summary Table

| Command                                 | Permission(s)                | Description                                  |
|-----------------------------------------|------------------------------|----------------------------------------------|
| `/boost`                                | `ezboost.use`                | Open the boosts GUI                          |
| `/boost <boostKey>`                     | `ezboost.use`, `ezboost.boost.<key>` | Activate a specific boost           |
| `/ezboost reload`                       | `ezboost.reload`             | Reload all configuration and messages         |
| `/ezboost give <player> <boostKey> [amount]` | `ezboost.give`           | Give boost tokens to a player                |

## Notes
- All commands support tab completion for available boost keys and player names.
- Use `/ezboost` for additional admin subcommands and help.
