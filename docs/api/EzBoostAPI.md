# EzBoostAPI Class Reference

## Overview
`EzBoostAPI` is the main static API class for interacting with the EzBoost plugin. It provides methods for registering custom boost effects, querying and managing player boosts, and integrating with the boost system from other plugins.

## Package
`com.skyblockexp.ezboost.api`

## Class Declaration
```java
public final class EzBoostAPI {
    // Static utility class
}
```

## Methods

### `static void registerCustomEffect(CustomBoostEffect effect)`
Registers a custom boost effect implementation. This allows other plugins to add new effect types that will be executed when a boost is activated or deactivated.
- **effect**: The custom effect implementation to register. Must implement `CustomBoostEffect`.

**Usage Example:**
```java
EzBoostAPI.registerCustomEffect(new MyCustomEffect());
```

---

### `static boolean isBoostActive(Player player)`
Checks if a player currently has an active boost.
- **player**: The player to check.
- **Returns**: `true` if the player has an active boost, `false` otherwise.

**Usage Example:**
```java
if (EzBoostAPI.isBoostActive(player)) {
    // Player has a boost
}
```

---

### `static BoostDefinition getActiveBoost(Player player)`
Gets the active `BoostDefinition` for a player, or `null` if none.
- **player**: The player to query.
- **Returns**: The active `BoostDefinition`, or `null` if no boost is active.

**Usage Example:**
```java
BoostDefinition boost = EzBoostAPI.getActiveBoost(player);
if (boost != null) {
    // Do something with the boost
}
```

---

### `static void clearBoost(Player player)`
Removes any active boost from a player.
- **player**: The player whose boost should be cleared.

**Usage Example:**
```java
EzBoostAPI.clearBoost(player);
```

---

### `static void setBoost(Player player, BoostDefinition definition, int durationSeconds)`
Forces a boost to start for a player with a specific definition and duration.
- **player**: The player to receive the boost.
- **definition**: The `BoostDefinition` to apply.
- **durationSeconds**: The duration of the boost in seconds.

**Usage Example:**
```java
EzBoostAPI.setBoost(player, myBoostDefinition, 120);
```

## Notes
- All methods are static for ease of use.
- Designed for open-source extensibility and professional integrations.
- Register custom effects before any boosts are activated.
- See also: [CustomBoostEffect](CustomBoostEffect.md)
- For event hooks, see [Events Documentation](../events.md)
