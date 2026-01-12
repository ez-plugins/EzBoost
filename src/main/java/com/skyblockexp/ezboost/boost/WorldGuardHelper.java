package com.skyblockexp.ezboost.boost;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Helper for WorldGuard region detection using reflection (optional dependency).
 */
public class WorldGuardHelper {
    /**
     * Returns the highest-priority WorldGuard region for a player, or null if none/applicable.
     * Uses reflection to avoid hard dependency on WorldGuard.
     */
    public static String getHighestPriorityRegion(Player player) {
        try {
            org.bukkit.plugin.PluginManager pm = Bukkit.getPluginManager();
            org.bukkit.plugin.Plugin wg = pm.getPlugin("WorldGuard");
            if (wg == null || !wg.isEnabled()) return null;
            Class<?> bukkitAdapter = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            Object wgWorld = bukkitAdapter.getMethod("adapt", org.bukkit.World.class).invoke(null, player.getWorld());
            Object wgLoc = bukkitAdapter.getMethod("adapt", org.bukkit.Location.class).invoke(null, player.getLocation());
            Class<?> worldGuard = Class.forName("com.sk89q.worldguard.WorldGuard");
            Object wgInstance = worldGuard.getMethod("getInstance").invoke(null);
            Object platform = worldGuard.getMethod("getPlatform").invoke(wgInstance);
            Object regionContainer = platform.getClass().getMethod("getRegionContainer").invoke(platform);
            Object regionManager = regionContainer.getClass().getMethod("get", wgWorld.getClass()).invoke(regionContainer, wgWorld);
            if (regionManager == null) return null;
            Object blockPoint = wgLoc.getClass().getMethod("toVector").invoke(wgLoc);
            blockPoint = blockPoint.getClass().getMethod("toBlockPoint").invoke(blockPoint);
            Object applicableRegions = regionManager.getClass().getMethod("getApplicableRegions", blockPoint.getClass()).invoke(regionManager, blockPoint);
            Iterable<?> regions = (Iterable<?>) applicableRegions.getClass().getMethod("iterator").invoke(applicableRegions);
            Object highest = null;
            for (Object region : regions) {
                int priority = (int) region.getClass().getMethod("getPriority").invoke(region);
                if (highest == null || priority > (int) highest.getClass().getMethod("getPriority").invoke(highest)) {
                    highest = region;
                }
            }
            if (highest != null) {
                return (String) highest.getClass().getMethod("getId").invoke(highest);
            }
            return null;
        } catch (Throwable t) {
            // WorldGuard not present or error
            return null;
        }
    }
}