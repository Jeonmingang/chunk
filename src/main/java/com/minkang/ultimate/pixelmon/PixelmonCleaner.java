
package com.minkang.ultimate.pixelmon;

import com.minkang.ultimate.Main;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.function.Predicate;

public class PixelmonCleaner {

    public static int cleanNonLegendary(Main plugin, World w) {
        if (!plugin.getConfig().getBoolean("cleanup.pixelmon.enabled", true)) return 0;
        boolean skipLeg = plugin.getConfig().getBoolean("cleanup.pixelmon.skipLegendaries", true);

        int removed = 0;
        for (Entity e : w.getEntities()) {
            if (!(e instanceof LivingEntity)) continue;
            if (!PixelmonUtil.isPixelmon(plugin, e)) continue;
            if (skipLeg && PixelmonUtil.isLegendary(plugin, e)) continue;
            try { e.remove(); removed++; } catch (Throwable ignored) {}
        }
        return removed;
    }
}
