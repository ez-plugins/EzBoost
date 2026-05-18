package com.skyblockexp.ezboost;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Runtime Folia/Paper scheduler adapter.
 * Use static factory methods instead of direct BukkitScheduler calls so the
 * plugin works on both threaded-region Folia and standard Paper/Spigot.
 */
public final class FoliaScheduler {

    /** {@code true} when the server is running Folia. */
    public static final boolean FOLIA;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException ignored) {
            folia = false;
        }
        FOLIA = folia;
    }

    private FoliaScheduler() {}

    /**
     * A cancellable task handle that works on both Paper and Folia.
     */
    @FunctionalInterface
    public interface TaskHandle {
        void cancel();

        /** A no-op handle for tasks that were never scheduled (e.g. entity retired on Folia). */
        TaskHandle NOOP = () -> {};
    }

    /**
     * Schedules a one-shot entity-bound task after {@code delayTicks} server ticks.
     * On Folia, the task runs on the entity's owning region thread.
     * If the entity is retired before the task runs, it is silently dropped.
     */
    public static TaskHandle runEntityTaskLater(JavaPlugin plugin, Entity entity, Runnable task, long delayTicks) {
        if (FOLIA) {
            var scheduled = entity.getScheduler().runDelayed(
                    plugin, t -> task.run(), null, Math.max(1L, delayTicks));
            return scheduled != null ? scheduled::cancel : TaskHandle.NOOP;
        }
        BukkitTask bt = plugin.getServer().getScheduler().runTaskLater(plugin, task, delayTicks);
        return bt::cancel;
    }

    /**
     * Schedules a repeating entity-bound task.
     * On Folia, if the entity is retired the task is silently dropped.
     */
    public static TaskHandle runEntityTaskTimer(JavaPlugin plugin, Entity entity, Runnable task,
                                                long initialDelay, long period) {
        if (FOLIA) {
            var scheduled = entity.getScheduler().runAtFixedRate(
                    plugin, t -> task.run(), null, Math.max(1L, initialDelay), Math.max(1L, period));
            return scheduled != null ? scheduled::cancel : TaskHandle.NOOP;
        }
        BukkitTask bt = plugin.getServer().getScheduler().runTaskTimer(plugin, task, initialDelay, period);
        return bt::cancel;
    }

    /**
     * Schedules an immediate entity-bound task (no delay).
     * On Folia, runs on the entity's owning region thread.
     */
    public static void runEntityTask(JavaPlugin plugin, Entity entity, Runnable task) {
        if (FOLIA) {
            entity.getScheduler().run(plugin, t -> task.run(), null);
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, task);
    }

    /**
     * Schedules an asynchronous task.
     */
    public static void runAsync(JavaPlugin plugin, Runnable task) {
        if (FOLIA) {
            plugin.getServer().getAsyncScheduler().runNow(plugin, t -> task.run());
            return;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
    }

    /**
     * Schedules a task on the global region (server-level state, console commands).
     */
    public static void runGlobal(JavaPlugin plugin, Runnable task) {
        if (FOLIA) {
            plugin.getServer().getGlobalRegionScheduler().run(plugin, t -> task.run());
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, task);
    }
}
