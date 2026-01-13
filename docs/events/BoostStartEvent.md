# BoostStartEvent Class Reference

## Overview
`BoostStartEvent` is fired when a boost is about to start for a player. This event is cancellable, allowing plugins to prevent the boost from activating. It provides full context, including the player and the `BoostDefinition`.

## Package
`com.skyblockexp.ezboost.event`

## Class Declaration
```java
public class BoostStartEvent extends Event implements Cancellable {
    public BoostStartEvent(Player player, String boostKey, BoostDefinition boostDefinition);
    // ...
}
```

## Key Methods & Fields
- `Player getPlayer()` — The player receiving the boost.
- `String getBoostKey()` — The unique key of the boost.
- `BoostDefinition getBoostDefinition()` — The full boost definition.
- `boolean isCancelled()` — Whether the event is cancelled.
- `void setCancelled(boolean cancel)` — Cancel or allow the boost start.

## Usage Example
```java
@EventHandler
public void onBoostStart(BoostStartEvent event) {
    if (event.getBoostDefinition().isSpecial()) {
        event.setCancelled(true); // Prevent special boosts
    }
}
```

## Notes
- Fired in all scenarios where a boost may start (activation, join, replacement, etc.).
- Always provides the full `BoostDefinition` for advanced integrations.
