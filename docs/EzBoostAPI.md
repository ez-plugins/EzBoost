# EzBoost API Overview

EzBoost exposes a professional, extensible API for plugin developers and advanced users. The API allows you to register custom boost effects, manage player boosts, and integrate deeply with the boost system.

## API Components

- [`EzBoostAPI`](api/EzBoostAPI.md): Main static API class for registering effects, querying and managing boosts.
- [`CustomBoostEffect`](api/CustomBoostEffect.md): Interface for defining custom boost effects.

## Getting Started

1. Register your custom effect:
   ```java
   EzBoostAPI.registerCustomEffect(new MyCustomEffect());
   ```
2. Query or manage player boosts:
   ```java
   if (EzBoostAPI.isBoostActive(player)) {
       BoostDefinition boost = EzBoostAPI.getActiveBoost(player);
       // ...
   }
   ```

## Advanced Integration

- Listen for boost lifecycle events ([see events documentation](../events.md)).
- Implement and register new effect types for custom behavior.
- Use the API to create, clear, or modify boosts programmatically.

## Reference

- [EzBoostAPI class reference](api/EzBoostAPI.md)
- [CustomBoostEffect interface reference](api/CustomBoostEffect.md)
- [Events documentation](../events.md)
