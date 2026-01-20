# Permissions

This page documents all permissions used by EzBoost, including their purposes, default assignments, and examples.

## Overview

EzBoost uses a hierarchical permission system to control access to different features. Permissions are checked in combination for certain actions.

## Permission List

| Permission | Description | Default | Examples |
| --- | --- | --- | --- |
| `ezboost.use` | Allows basic use of boost commands and GUI | Players | `/boost`, `/boost speed` |
| `ezboost.admin` | Grants access to admin commands | OPs | `/ezboost create` |
| `ezboost.reload` | Allows reloading the plugin configuration | OPs | `/ezboost reload` |
| `ezboost.give` | Allows giving boost tokens to players | OPs | `/ezboost give player speed` |
| `ezboost.cooldown.bypass` | Bypasses boost cooldowns | OPs | Activate boosts without waiting |
| `ezboost.boost.<key>` | Per-boost permission for specific boosts | Players | `ezboost.boost.speed`, `ezboost.boost.jump` |

## Detailed Descriptions

### `ezboost.use`
- **Purpose**: Grants access to player-facing boost functionality
- **Required for**: Opening the boosts GUI, activating boosts via commands
- **Default**: All players
- **Note**: This is a base permission that must be combined with specific boost permissions

### `ezboost.admin`
- **Purpose**: Allows access to administrative features
- **Required for**: Opening the admin GUI to create/manage boosts
- **Default**: OPs (server operators)
- **Note**: This permission is checked for admin commands only

### `ezboost.reload`
- **Purpose**: Permits reloading of plugin configuration
- **Required for**: Using `/ezboost reload` command
- **Default**: OPs
- **Note**: Useful for administrators who need to reload config without full admin access

### `ezboost.give`
- **Purpose**: Allows distributing boost tokens
- **Required for**: Giving boost token items to players
- **Default**: OPs
- **Note**: Useful for moderators or shop plugins

### `ezboost.cooldown.bypass`
- **Purpose**: Ignores cooldown restrictions
- **Required for**: Activating boosts without waiting for cooldowns
- **Default**: OPs
- **Note**: Should be given sparingly to prevent abuse

### `ezboost.boost.<key>`
- **Purpose**: Controls access to individual boosts
- **Required for**: Activating specific boosts
- **Default**: All players
- **Examples**:
  - `ezboost.boost.speed` - Access to speed boost
  - `ezboost.boost.jump` - Access to jump boost
  - `ezboost.boost.strength` - Access to strength boost
- **Note**: The `<key>` corresponds to the boost key defined in `boosts.yml`

## Permission Combinations

To activate a boost, players need:
- `ezboost.use` (base permission)
- `ezboost.boost.<key>` (specific boost permission)

Example: To allow a player to use the speed boost:
```
ezboost.use
ezboost.boost.speed
```

## Permission Management

### Using LuckPerms (Recommended)
```bash
# Grant base permission
lp user <player> permission set ezboost.use true

# Grant specific boost permission
lp user <player> permission set ezboost.boost.speed true

# Grant admin access
lp user <admin> permission set ezboost.admin true
```

### Using PermissionsEx
```yaml
groups:
  default:
    permissions:
    - ezboost.use
    - ezboost.boost.*
  admin:
    permissions:
    - ezboost.admin
    - ezboost.reload
    - ezboost.give
    - ezboost.cooldown.bypass
```

### Using GroupManager
```yaml
groups:
  Default:
    permissions:
    - ezboost.use
    - 'ezboost.boost.*'
  Admin:
    permissions:
    - ezboost.admin
    - ezboost.reload
    - ezboost.give
    - ezboost.cooldown.bypass
```

## Best Practices

1. **Grant base permissions broadly**: Most players should have `ezboost.use`
2. **Restrict specific boosts**: Use `ezboost.boost.<key>` to control premium boosts
3. **Limit admin permissions**: Only trusted staff should have `ezboost.admin`
4. **Use wildcards carefully**: `ezboost.boost.*` gives access to all boosts
5. **Test permissions**: Use `/boost` to verify GUI shows correct boosts

## Troubleshooting

- **GUI not showing boosts**: Check `ezboost.use` and specific `ezboost.boost.<key>` permissions
- **Command denied**: Verify appropriate admin permissions
- **Cooldowns not working**: Check `ezboost.cooldown.bypass` for staff</content>
<parameter name="filePath">/home/niels/gyvex/EzBoost/docs/permissions.md
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
