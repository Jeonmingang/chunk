package com.minkang.ultimate.trashopt.util;

import com.minkang.ultimate.trashopt.Main;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Set;

public class CleanupUtil {

    public static void startCountdownCleanup(Main plugin,
                                             World world,
                                             int seconds,
                                             List<Integer> announceSeconds,
                                             String messageTemplate,
                                             String clearedTemplate,
                                             Set<EntityType> targets) {
        final World worldRef = world;
        final int totalSec = seconds;
        final java.util.List<Integer> announceFinal = new java.util.ArrayList<Integer>(announceSeconds);
        final String msgTplFinal = messageTemplate;
        final String clearedTplFinal = clearedTemplate;
        final java.util.Set<EntityType> targetsFinal = new java.util.HashSet<EntityType>(targets);

        new BukkitRunnable() {
            int remaining = totalSec;
            @Override
            public void run() {
                if (remaining <= 0) {
                    int removed = 0;
                    for (Entity e : worldRef.getEntities()) {
                        if (targetsFinal.contains(e.getType())) {
                            e.remove();
                            removed++;
                        }
                    }
                    String done = ChatColor.translateAlternateColorCodes('&',
                            clearedTplFinal.replace("{world}", worldRef.getName()).replace("{count}", String.valueOf(removed)));
                    worldRef.getPlayers().forEach(p -> p.sendMessage(done));
                    cancel();
                    return;
                }
                if (announceFinal.contains(remaining)) {
                    String m = ChatColor.translateAlternateColorCodes('&',
                            msgTplFinal.replace("{world}", worldRef.getName()).replace("{sec}", String.valueOf(remaining)));
                    worldRef.getPlayers().forEach(p -> p.sendMessage(m));
                }
                remaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
}
