
package com.minkang.ultimate.cleanup;

import com.minkang.ultimate.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class EntityBatchCleaner {

    public static int cleanWorldOnce(Main plugin, World world) {
        Set<EntityType> exclude = new HashSet<>();
        for (String s : plugin.getConfig().getStringList("cleanup.entity.excludeEntityTypes")) {
            try { exclude.add(EntityType.valueOf(s)); } catch (Exception ignored) {}
        }
        java.util.function.Predicate<Entity> filter = e -> !exclude.contains(e.getType()) && e.getType() != EntityType.PLAYER;
        int[] removed = {0};
        for (Entity e: world.getEntities()) {
            if (filter.test(e)) {
                try { e.remove(); removed[0]++; } catch (Throwable ignored) {}
            }
        }
        return removed[0];
    }

    public static void cleanWorldBatched(Main plugin, World world, Predicate<Entity> predicate, int batchPerTick, Runnable onFinish) {
        Entity[] all = world.getEntities().toArray(new Entity[0]);
        new BukkitRunnable(){
            int idx = 0, removed = 0;
            @Override public void run() {
                int count = 0;
                while (idx < all.length && count < batchPerTick) {
                    Entity e = all[idx++];
                    if (predicate.test(e)) {
                        try { e.remove(); removed++; } catch (Throwable ignored) {}
                    }
                    count++;
                }
                if (idx >= all.length) {
                    cancel();
                    if (onFinish != null) {
                        Bukkit.getScheduler().runTask(plugin, onFinish);
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }
}
