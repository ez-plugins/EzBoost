# BoostEndEvent Class Reference

## Overview
`BoostEndEvent` is fired when a boost is about to end for a player. This event is cancellable, allowing plugins to prevent the boost from ending. It provides full context, including the player and the `BoostDefinition`.

## Package
`com.skyblockexp.ezboost.event`

## Class Declaration
```java
public class BoostEndEvent extends Event implements Cancellable {
    public BoostEndEvent(Player player, String boostKey, BoostDefinition boostDefinition);
    // ...
}
```

## Key Methods & Fields
- `Player getPlayer()` — The player whose boost is ending.
- `String getBoostKey()` — The unique key of the boost.
- `BoostDefinition getBoostDefinition()` — The full boost definition.
- `boolean isCancelled()` — Whether the event is cancelled.
- `void setCancelled(boolean cancel)` — Cancel or allow the boost end.

## Usage Example
```java
@EventHandler
public void onBoostEnd(BoostEndEvent event) {
    if (event.getBoostDefinition().isPermanent()) {
        event.setCancelled(true); // Prevent permanent boosts from ending
    }
}
```

## Notes
- Fired in all scenarios where a boost may end (expiry, death, region/world change, forced removal, etc.).
- Always provides the full `BoostDefinition` for advanced integrations.
