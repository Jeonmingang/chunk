package com.minkang.uto.cleanup;

import com.minkang.uto.Main;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class EntityBatchCleaner {

    public static int cleanGroundItems(Main plugin, World world) {
        int removed = 0;
        for (Entity e : world.getEntities()) {
            if (e.getType() == EntityType.DROPPED_ITEM) {
                try { e.remove(); removed++; } catch (Throwable ignored) {}
            }
        }
        return removed;
    }
}