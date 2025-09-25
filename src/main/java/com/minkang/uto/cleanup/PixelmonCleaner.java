package com.minkang.uto.cleanup;

import com.minkang.uto.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.List;

public class PixelmonCleaner {

    /** Remove non-legendary "pixelmon-like" entities based on class-name heuristics. Returns removed count. */
    public static int cleanNonLegendary(Main plugin, World world) {
        List<String> contains = plugin.getConfig().getStringList("cleanup.pixelmon.detection.classNameContains");
        if (contains == null || contains.isEmpty()) contains = java.util.Arrays.asList("pixelmon");
        List<String> legendaryTokens = plugin.getConfig().getStringList("cleanup.pixelmon.legendaryNameContains");
        if (legendaryTokens == null) legendaryTokens = java.util.Collections.emptyList();

        int removed = 0;
        for (Entity e : world.getEntities()) {
            String cls = e.getClass().getName().toLowerCase();
            boolean looksPixelmon = false;
            for (String t : contains) {
                if (cls.contains(t.toLowerCase())) { looksPixelmon = true; break; }
            }
            if (!looksPixelmon) continue;

            // Legendary skip
            String check = (e.getCustomName() == null ? "" : e.getCustomName()) + "|" + cls + "|" + e.getType().name();
            String low = check.toLowerCase();
            boolean legendary = false;
            for (String t : legendaryTokens) {
                if (low.contains(t.toLowerCase())) { legendary = true; break; }
            }
            if (legendary) continue;

            try { e.remove(); removed++; } catch (Throwable ignored) {}
        }
        return removed;
    }
}
