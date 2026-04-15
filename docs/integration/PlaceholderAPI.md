
# PlaceholderAPI integration

This document describes the PlaceholderAPI expansion bundled with EzBoost and the placeholders it exposes.

## Installation

- Ensure PlaceholderAPI is installed on the server (https://www.spigotmc.org/resources/placeholderapi.6245/).
- The EzBoost expansion is registered automatically when both EzBoost and PlaceholderAPI are present — no extra files required.

## Expansion identifier

All placeholders use the `ezboost` expansion identifier. Wrap the identifier in percent signs in PAPI syntax, for example `%ezboost_price_formatted_50000%`.

---

## Available placeholders

### Price & currency

| Placeholder | Returns |
|-------------|---------|
| `%ezboost_price_formatted_<amount\|boostkey>%` | Formatted number or the cost of `<boostkey>` (grouping, separators, decimals from config). Example: `50,000` |
| `%ezboost_price_compact_<amount\|boostkey>%` | Compact cost using K/M suffixes. Example: `50K`, `1.2M` |
| `%ezboost_price_raw_<boostkey>%` | Raw numeric cost with no formatting. Example: `50000` |
| `%ezboost_currency_symbol%` | Configured currency label from `economy.yml`, or empty |

### Boost info (requires player context for world/region overrides)

| Placeholder | Returns |
|-------------|---------|
| `%ezboost_boost_display_<boostkey>%` | Display name of `<boostkey>` |
| `%ezboost_boost_cost_<boostkey>%` | Formatted cost of `<boostkey>` (same as `price_formatted`) |
| `%ezboost_boost_duration_<boostkey>%` | Duration in seconds |
| `%ezboost_boost_status_<boostkey>%` | Player's status: `available`, `locked`, `active`, `insufficient`, or `cooldown` |
| `%ezboost_player_can_afford_<boostkey>%` | `true` / `false` — whether the requesting player can afford the boost |

### Active boost state

| Placeholder | Returns |
|-------------|---------|
| `%ezboost_has_active_boost%` | `true` / `false` — whether the player has a running boost |
| `%ezboost_active_boost%` | Config key of the active boost, or empty |
| `%ezboost_active_boost_display%` | Display name of the active boost, or empty |
| `%ezboost_active_boost_time_remaining%` | Seconds remaining as a plain integer |
| `%ezboost_active_boost_time_remaining_formatted%` | `MM:SS` (under 1 hour) or `HH:MM:SS` (1 hour or more) |
| `%ezboost_is_active_<boostkey>%` | `true` / `false` — plain boolean, no permission or cost check |

### Cooldowns

| Placeholder | Returns |
|-------------|---------|
| `%ezboost_cooldown_remaining_<boostkey>%` | Remaining cooldown in seconds as a plain integer, `0` if not on cooldown |
| `%ezboost_cooldown_remaining_formatted_<boostkey>%` | `MM:SS` or `HH:MM:SS`, `00:00` if not on cooldown |

### XP

| Placeholder | Returns |
|-------------|---------|
| `%ezboost_xp_multiplier%` | Active XP multiplier as an integer (e.g. `2`). Returns `1` if no XP boost is running |

---

## Formatting behaviour

Number formatting honours `economy.format.*` settings from `economy.yml`:
- `grouping` — `true`/`false`, enable thousands grouping
- `grouping-separator` — single character, default `,`
- `decimal-separator` — single character, default `.`
- `decimal-places` — integer, default `2`

Compact formatting (`price_compact`) uses K / M suffixes and ignores the decimal-places setting.

---

## Examples

**Scoreboard line showing active boost and time:**
```
Boost: %ezboost_active_boost_display% (%ezboost_active_boost_time_remaining_formatted%)
```

**Conditional button (e.g. in a GUI plugin) — only visible when no boost is active:**
```
condition: %ezboost_has_active_boost% == false
```

**Shop sign showing cost:**
```
Cost: %ezboost_price_formatted_speed%
```

**XP rate display:**
```
XP rate: %ezboost_xp_multiplier%x
```

**Cooldown display for a specific boost:**
```
Cooldown: %ezboost_cooldown_remaining_formatted_speed%
```

---

## Notes & fallbacks

- All boost key lookups use the requesting player's world/region context, so results may differ depending on active overrides.
- If a boost key cannot be resolved or an argument is invalid, the placeholder returns an empty string (`""`), `false`, or `0` depending on the placeholder type.
- The expansion registers at plugin startup when PlaceholderAPI is present; no manual registration is required.

