# EzBoost API Overview

EzBoost exposes a professional, extensible API for plugin developers and advanced users. The API allows you to register custom boost effects, manage player boosts, and integrate deeply with the boost system.

## API Components

- [`EzBoostAPI`](api/EzBoostAPI.md): Main static API class for registering effects, querying and managing boosts.
- [`CustomBoostEffect`](api/CustomBoostEffect.md): Interface for defining custom boost effects.


## Configuring Custom Effects in YAML

To use a custom effect in your boost configuration, reference its unique type string (as returned by `getType()`) in your `boosts.yml`:

```yaml
boosts:
   my_custom_boost:
      name: "My Custom Boost"
      effects:
         - type: mycustom   # This must match your CustomBoostEffect#getType()
            # ... any additional effect parameters your effect supports
      duration: 120
      # ... other boost options
```

Make sure your plugin registers the custom effect with `EzBoostAPI.registerCustomEffect(...)` before any boosts are loaded or activated.

### 1. Add EzBoost to your project

You can use JitPack to include the latest version directly from GitHub:

```xml
<repositories>
   <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
   </repository>
</repositories>

<dependency>
   <groupId>com.github.ez-plugins</groupId>
   <artifactId>EzBoost</artifactId>
   <version>1.4.0</version> <!-- a specific release/tag -->
</dependency>
```

### 2. Register your custom effect:
```java
EzBoostAPI.registerCustomEffect(new MyCustomEffect());
```

### 3. Query or manage player boosts:
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

For more details, see: [EzBoost on GitHub](https://github.com/ez-plugins/EzBoost)