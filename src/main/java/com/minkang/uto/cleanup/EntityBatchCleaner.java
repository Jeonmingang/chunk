package com.minkang.uto.cleanup;

import com.minkang.uto.Main;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class EntityBatchCleaner {

    /** Remove dropped items only in the given world. Returns removed count. */
    public static int cleanGroundItems(Main plugin, World world) {
        int removed = 0;
        for (Entity e : world.getEntities()) {
            // Spigot 1.16.5: dropped item is DROPPED_ITEM
            if (e.getType() == EntityType.DROPPED_ITEM) {
                e.remove();
                removed++;
            }
        }
        return removed;
    }
}
