---
title: CustomBoostEffect
parent: API
nav_order: 2
description: "CustomBoostEffect interface — implementing custom plugin-driven boost effects"
---

# CustomBoostEffect Interface Reference
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## Overview
`CustomBoostEffect` is an interface for defining custom boost effects that can be registered with EzBoost. Implement this interface in your plugin to add new effect types that will be executed when a boost is activated or deactivated.

## Package
`com.skyblockexp.ezboost.boost`

## Interface Declaration
```java
public interface CustomBoostEffect {
    /**
     * Apply the effect to the player.
     * @param player the player to apply the effect to
     * @param amplifier effect strength as configured in the boost
     */
    void apply(org.bukkit.entity.Player player, int amplifier);

    /**
     * Remove the effect from the player.
     * @param player the player to remove the effect from
     */
    void remove(org.bukkit.entity.Player player);

    /**
     * @return unique effect name used in configuration
     */
    String getName();

    /**
     * @return cooldown duration in seconds (default 0 = no cooldown)
     */
    default int getCooldownSeconds() {
        return 0;
    }
}
```

## Methods

### `void apply(Player player, int amplifier)`
Called when the boost's custom effect should be applied to a player.
- **player**: The player receiving the effect.
- **amplifier**: The configured amplifier for this effect in the boost.

**Usage Example:**
```java
@Override
public void apply(Player player, int amplifier) {
    player.sendMessage("Your custom boost is now active!");
}
```

---

### `void remove(Player player)`
Called when the boost's custom effect should be removed from a player.
- **player**: The player losing the effect.

**Usage Example:**
```java
@Override
public void remove(Player player) {
    player.sendMessage("Your custom boost has ended.");
}
```

---

### `String getName()`
Returns the unique effect name used in boost configuration to identify the custom effect.
- **Returns**: The effect name (e.g., "mycustom").

### `int getCooldownSeconds()`
Returns the cooldown duration (in seconds) associated with this custom effect. When `settings.cooldown-per-effect` is enabled, this value is used to set per-effect cooldown timestamps after activation. Returning `0` means no cooldown.

**Usage Example:**
```java
@Override
public String getName() {
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
    public void apply(Player player, int amplifier) {
        player.sendMessage("You received a special custom effect!");
        // Add your custom logic here
    }

    @Override
    public void remove(Player player) {
        player.sendMessage("Your special custom effect has ended.");
        // Cleanup logic here
    }

    @Override
    public String getName() {
        return "mycustom";
    }
}

// Register the effect in your plugin's onEnable method:
@Override
public void onEnable() {
    EzBoostAPI.registerCustomBoostEffect(new MyCustomEffect());
}
```

## Notes
- Register your implementation with `EzBoostAPI.registerCustomEffect()` (see example above).
- The effect type returned by `getType()` must be unique across all registered effects.
- See also: [EzBoostAPI](../EzBoostAPI.md)
