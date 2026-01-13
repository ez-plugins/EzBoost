# EzBoost Events Overview

EzBoost provides a robust event system to allow other plugins and advanced users to hook into boost lifecycle changes. This enables custom logic, integrations, and advanced control over boost activation and deactivation.

## Available Events

- [**BoostStartEvent**](events/BoostStartEvent.md): Fired when a boost is about to start for a player. This event is cancellable, allowing plugins to prevent the boost from activating. Provides full context, including the player and the `BoostDefinition`.
- [**BoostEndEvent**](events/BoostEndEvent.md): Fired when a boost is about to end for a player. This event is cancellable, allowing plugins to prevent the boost from ending (e.g., for custom expiry logic). Provides full context, including the player and the `BoostDefinition`.

## Event Firing Scenarios

Events are fired in all scenarios where a boost may start or end, including:
- Manual activation
- Expiry (time runs out)
- Player join (re-application)
- Player death (if configured to remove boost)
- Region/world change (if boost is no longer valid)
- Forced removal (admin or system action)
- Replacement (when a new boost overrides an existing one)

## Usage Example

To listen for boost events, register an event listener in your plugin:

```java
@EventHandler
public void onBoostStart(BoostStartEvent event) {
    Player player = event.getPlayer();
    BoostDefinition boost = event.getBoostDefinition();
    // Custom logic here
}
```

See the advanced class reference for each event:
- [BoostStartEvent](events/BoostStartEvent.md)
- [BoostEndEvent](events/BoostEndEvent.md)
