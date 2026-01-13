# EzBoost Permissions Reference

This document provides a comprehensive overview of all permissions used by EzBoost, including their purpose, default values, and recommended assignment.

## Permission Nodes

| Permission                      | Default   | Description                                                      |
|----------------------------------|-----------|------------------------------------------------------------------|
| `ezboost.use`                   | `true`    | Allows players to use boosts and open the boost GUI.             |
| `ezboost.admin`                 | `op`      | Grants access to all admin commands.                             |
| `ezboost.reload`                | `op`      | Allows reloading of all configuration and messages.              |
| `ezboost.give`                  | `op`      | Allows giving boost tokens to players.                           |
| `ezboost.cooldown.bypass`       | `op`      | Bypasses all boost cooldowns.                                    |
| `ezboost.boost.<key>`           | `true`    | Grants access to a specific boost (replace `<key>` with boost).  |

## Usage Examples

- Grant all players access to boosts:
  ```yaml
  - ezboost.use
  - ezboost.boost.*
  ```
- Restrict a special boost to VIPs:
  ```yaml
  - ezboost.boost.vip
  ```
- Give staff full admin access:
  ```yaml
  - ezboost.admin
  - ezboost.reload
  - ezboost.give
  - ezboost.cooldown.bypass
  ```

## Notes
- Per-boost permissions (`ezboost.boost.<key>`) allow fine-grained control over which boosts players can use.
- Use permission plugins (LuckPerms, PermissionsEx, etc.) to manage group assignments.
- By default, most permissions are granted to all players except admin actions, which require operator status.
