---
title: EzBoostAPI
parent: API
nav_order: 1
description: "EzBoostAPI class reference — full public method tables"
---

# EzBoostAPI Class Reference
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

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

### `static boolean registerCustomBoostEffect(CustomBoostEffect effect)`
Register a custom boost effect implementation so other plugins can provide new effect types.

- **effect**: The `CustomBoostEffect` implementation to register.
- **Returns**: `true` if registration succeeded; `false` if the API isn't initialized or the effect name is already registered.

**Usage Example:**

```java
boolean ok = EzBoostAPI.registerCustomBoostEffect(new MyCustomEffect());
```

---

### `static Map<String, CustomBoostEffect> getCustomBoostEffects()`
Returns an unmodifiable map of all registered custom boost effects keyed by their normalized names.

**Usage Example:**

```java
Map<String, CustomBoostEffect> effects = EzBoostAPI.getCustomBoostEffects();
```

---

### `static BoostManager getBoostManager()`
Returns the internal `BoostManager` instance. This exposes advanced integration points but should be used carefully.

**Usage Example:**

```java
BoostManager manager = EzBoostAPI.getBoostManager();
```

---

### `static long getCooldownRemainingForEffect(Player player, com.skyblockexp.ezboost.boost.BoostEffect effect)`
Convenience helper that returns remaining cooldown (in seconds) for a specific `BoostEffect` on a player. Returns `0` if no cooldown is present or the API is not initialized.

**Usage Example:**

```java
long remaining = EzBoostAPI.getCooldownRemainingForEffect(player, myBoostEffect);
```

## Notes

- All methods are static for ease of use.
- Designed for open-source extensibility and professional integrations.
- Register custom effects before any boosts are activated.
- See also: [CustomBoostEffect](CustomBoostEffect.md)
- For event hooks, see [Events Documentation](../events.md)
