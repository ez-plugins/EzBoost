
# PlaceholderAPI integration

This document describes the PlaceholderAPI expansion bundled with EzBoost and the placeholders it exposes.

## Installation

- Ensure PlaceholderAPI is installed on the server (https://www.spigotmc.org/resources/placeholderapi.6245/).
- The EzBoost expansion is registered automatically when both EzBoost and PlaceholderAPI are present — no extra files required.

## Expansion identifier

All placeholders use the `ezboost` expansion identifier. In PAPI syntax wrap the identifier in percent signs, for example `%ezboost_price_formatted_50000%`.

## Available placeholders

The expansion supports parameterized placeholders. Replace `<arg>` with either a numeric amount or a boost key, depending on the placeholder.

- `ezboost_price_formatted_<amount|boostkey>` — Formats a numeric amount or the configured cost of `<boostkey>` using EzBoost's number-format settings (grouping, separators, decimals). Examples:
	- `%ezboost_price_formatted_50000%` → `50,000` (or `50.000` depending on config)
	- `%ezboost_price_formatted_superboost%` → formatted cost of boost `superboost` for the requesting player
- `ezboost_price_raw_<boostkey>` — Raw numeric cost for `<boostkey>` (no formatting). Example: `%ezboost_price_raw_superboost%` → `50000`
 - `ezboost_price_raw_<boostkey>` — Raw numeric cost for `<boostkey>` (no formatting). Example: `%ezboost_price_raw_superboost%` → `50000`
- `ezboost_boost_display_<boostkey>` — Display name for boost (from `boosts.yml`).
- `ezboost_boost_cost_<boostkey>` — Formatted cost for `<boostkey>` (convenience wrapper for `price_formatted`).
- `ezboost_boost_duration_<boostkey>` — Duration in seconds for `<boostkey>`.
- `ezboost_boost_status_<boostkey>` — Status for the requesting player: one of `available`, `locked`, `active`, `insufficient`, `cooldown`.
- `ezboost_player_can_afford_<boostkey>` — `true`/`false` whether the requesting player can afford the boost.
- `ezboost_currency_symbol` — Returns the configured currency label from `economy.yml` (if set), otherwise empty string.
 - `ezboost_price_compact_<amount|boostkey>` — Compact formatted amount using K/M suffixes (e.g. `50K`, `1.2M`). Examples:
	 - `%ezboost_price_compact_50000%` → `50K`
	 - `%ezboost_price_compact_superboost%` → compact cost for `superboost`

## Formatting behaviour

- Number formatting honours `economy.format.*` settings from `economy.yml`:
	- `grouping` (true/false)
	- `grouping-separator` (single character, default `,`)
	- `decimal-separator` (single character, default `.`)
	- `decimal-places` (integer, default `2`)
- If a configured boost cost is an integer (e.g. `50_000.0`), the expansion will omit decimals by default; non-integer amounts use `decimal-places`.

## Examples

- In another plugin's message or scoreboard: `Needed: %ezboost_price_formatted_superboost%`
- On a sign via PAPI-supporting sign plugin: line: `Cost: %ezboost_price_formatted_superboost%`

## Notes & fallbacks

- The expansion resolves boost keys using the requesting player's world/region context — a boost key might return a different cost depending on overrides.
- If a boost key cannot be resolved or an argument is invalid, the placeholder returns an empty string.
- The expansion registers at plugin startup when PlaceholderAPI is present; no manual registration is required.

## Future ideas

- Add compact formatting (`50K`, `1.2M`) as `%ezboost_price_compact_<arg>%`.
- Add localization-aware formatting based on server locale.

If you'd like, I can add the compact-format placeholder and a short example section in `docs/` showing common PAPI use-cases.

