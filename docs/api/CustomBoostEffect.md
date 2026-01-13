# CustomBoostEffect Interface Reference

## Overview
`CustomBoostEffect` is an interface for defining custom boost effects that can be registered with EzBoost. Implement this interface in your plugin to add new effect types that will be executed when a boost is activated or deactivated.

## Package
`com.skyblockexp.ezboost.boost`

## Interface Declaration
```java
public interface CustomBoostEffect {
    /**
     * Called when the boost is activated for a player.
     * @param player The player receiving the boost.
     * @param boost The boost definition.
     */
    void onEnable(Player player, BoostDefinition boost);

    /**
     * Called when the boost is deactivated for a player.
     * @param player The player losing the boost.
     * @param boost The boost definition.
     */
    void onDisable(Player player, BoostDefinition boost);

    /**
     * @return The unique effect type key (e.g., "mycustom").
     */
    String getType();
}
```

## Methods

### `void onEnable(Player player, BoostDefinition boost)`
Called when the boost is activated for a player.
- **player**: The player receiving the boost.
- **boost**: The boost definition being applied.

**Usage Example:**
```java
@Override
public void onEnable(Player player, BoostDefinition boost) {
    player.sendMessage("Your custom boost is now active!");
}
```

---

### `void onDisable(Player player, BoostDefinition boost)`
Called when the boost is deactivated for a player.
- **player**: The player losing the boost.
- **boost**: The boost definition being removed.

**Usage Example:**
```java
@Override
public void onDisable(Player player, BoostDefinition boost) {
    player.sendMessage("Your custom boost has ended.");
}
```

---

### `String getType()`
Returns the unique effect type key. This is used in boost configuration to identify the effect.
- **Returns**: The effect type key (e.g., "mycustom").

**Usage Example:**
```java
@Override
public String getType() {
    return "mycustom";
}
```

## Full Example: Creating and Registering a Custom Effect

Below is a complete example of how to implement a custom boost effect and register it with EzBoost:

```java
import com.skyblockexp.ezboost.api.EzBoostAPI;
import com.skyblockexp.ezboost.boost.CustomBoostEffect;
import com.skyblockexp.ezboost.boost.BoostDefinition;
import org.bukkit.entity.Player;

public class MyCustomEffect implements CustomBoostEffect {
    @Override
    public void onEnable(Player player, BoostDefinition boost) {
        player.sendMessage("You received a special custom effect!");
        // Add your custom logic here
    }

    @Override
    public void onDisable(Player player, BoostDefinition boost) {
        player.sendMessage("Your special custom effect has ended.");
        // Cleanup logic here
    }

    @Override
    public String getType() {
        return "mycustom";
    }
}

// Register the effect in your plugin's onEnable method:
@Override
public void onEnable() {
    EzBoostAPI.registerCustomEffect(new MyCustomEffect());
}
```

## Notes
- Register your implementation with `EzBoostAPI.registerCustomEffect()` (see example above).
- The effect type returned by `getType()` must be unique across all registered effects.
- See also: [EzBoostAPI](../EzBoostAPI.md)
