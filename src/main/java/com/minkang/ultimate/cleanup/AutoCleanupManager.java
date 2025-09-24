
package com.minkang.ultimate.cleanup;
import com.minkang.ultimate.Main;
import com.minkang.ultimate.pixelmon.PixelmonCleaner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class AutoCleanupManager {
    private final Main plugin;
    private BukkitRunnable groundTask;
    private BukkitRunnable pixelmonTask;
    public AutoCleanupManager(Main plugin) { this.plugin = plugin; }
    public void initFromConfig() {
        startGroundCleaner();
        startPixelmonCleaner();
    }
    public void stopAll() {
        if (groundTask != null) groundTask.cancel();
        if (pixelmonTask != null) pixelmonTask.cancel();
    }
    private void startGroundCleaner() {
        if (groundTask != null) groundTask.cancel();
        if (!plugin.getConfig().getBoolean("cleanup.ground.enabled", true)) return;
        int sec = Math.max(30, plugin.getConfig().getInt("cleanup.ground.intervalSeconds", 300));
        List<String> worlds = plugin.getConfig().getStringList("cleanup.ground.worlds");
        Set<String> worldSet = new HashSet<>(worlds);
        groundTask = new BukkitRunnable() {
            @Override public void run() {
                for (World w : Bukkit.getWorlds()) {
                    if (!worldSet.isEmpty() && !worldSet.contains(w.getName())) continue;
                    int removed = 0;
                    for (org.bukkit.entity.Item e : w.getEntitiesByClass(org.bukkit.entity.Item.class)) {
                        try { e.remove(); removed++; } catch (Throwable ignored) {}
                    }
                    if (removed > 0) {
                        String done = plugin.getConfig().getString("messages.cleanup_done");
                        if (done != null) Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                                plugin.prefix() + done.replace("%count%", String.valueOf(removed))));
                    }
                }
            }
        };
        groundTask.runTaskTimer(plugin, sec*20L, sec*20L);
    }
    private void startPixelmonCleaner() {
        if (pixelmonTask != null) pixelmonTask.cancel();
        if (!plugin.getConfig().getBoolean("cleanup.pixelmon.enabled", true)) return;
        int sec = Math.max(60, plugin.getConfig().getInt("cleanup.pixelmon.intervalSeconds", 600));
        List<String> worlds = plugin.getConfig().getStringList("cleanup.pixelmon.worlds");
        Set<String> worldSet = new HashSet<>(worlds);
        pixelmonTask = new BukkitRunnable() {
            @Override public void run() {
                int totalRemoved = 0;
                for (World w : Bukkit.getWorlds()) {
                    if (!worldSet.isEmpty() && !worldSet.contains(w.getName())) continue;
                    totalRemoved += com.minkang.ultimate.pixelmon.PixelmonCleaner.cleanNonLegendary(plugin, w);
                    if (totalRemoved > 0) {
                        String msg = plugin.getConfig().getString("messages.pixelmon_cleanup_done");
                        if (msg != null) {
                            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                                    plugin.prefix() + msg.replace("%count%", String.valueOf(totalRemoved)).replace("%world%", w.getName())));
                        }
                    }
                }
            }
        };
        pixelmonTask.runTaskTimer(plugin, sec*20L, sec*20L);
    }
}
