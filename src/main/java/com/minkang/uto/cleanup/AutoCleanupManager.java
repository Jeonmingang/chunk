
package com.minkang.uto.cleanup;

import com.minkang.uto.Main;
import com.minkang.uto.pixelmon.PixelmonCleaner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class AutoCleanupManager {
    private final Main plugin;
    private BukkitRunnable groundTask;
    private BukkitRunnable pixelTask;

    public AutoCleanupManager(Main plugin) {
        this.plugin = plugin;
    }

    public void stopAll() {
        if (groundTask != null) { try { groundTask.cancel(); } catch (Throwable ignored) {} groundTask = null; }
        if (pixelTask != null) { try { pixelTask.cancel(); } catch (Throwable ignored) {} pixelTask = null; }
    }

    public void initFromConfig() {
        stopAll();
        setupGroundCleanup();
        setupPixelmonCleanup();
    }

    private Set<Integer> loadSchedule(String path) {
        List<Integer> list = plugin.getConfig().getIntegerList(path);
        if (list == null || list.isEmpty()) {
            return new HashSet<Integer>(Arrays.asList(60,30,10,5,4,3,2,1));
        }
        return new HashSet<Integer>(list);
    }

    private List<World> resolveWorlds(List<String> names) {
        List<World> ws = new ArrayList<World>();
        if (names == null || names.isEmpty()) {
            ws.addAll(Bukkit.getWorlds());
            return ws;
        }
        for (String n : names) {
            World w = Bukkit.getWorld(n);
            if (w != null) ws.add(w);
        }
        if (ws.isEmpty()) ws.addAll(Bukkit.getWorlds());
        return ws;
    }

    private void broadcastCountdown(int seconds, List<World> worlds, boolean pixelmon) {
        ConfigurationSection map = plugin.getConfig().getConfigurationSection("messages.cleanup_countdown_map");
        String base = plugin.getConfig().getString("messages.cleanup_countdown", "&e%world% 월드 청소 %seconds%s 후 실행됩니다...");
        String line = (map != null ? map.getString(String.valueOf(seconds), base) : base);
        for (World w : worlds) {
            String msg = line.replace("%seconds%", String.valueOf(seconds)).replace("%world%", w.getName());
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix() + msg));
        }
    }

    private void setupGroundCleanup() {
        if (!plugin.getConfig().getBoolean("cleanup.ground.enabled", true)) return;
        final int interval = plugin.getConfig().getInt("cleanup.ground.intervalSeconds", 600);
        final Set<Integer> schedule = loadSchedule("cleanup.ground.countdownSchedule");
        final List<World> worlds = resolveWorlds(plugin.getConfig().getStringList("cleanup.ground.worlds"));

        groundTask = new BukkitRunnable() {
            int t = interval;
            @Override public void run() {
                if (!plugin.getConfig().getBoolean("cleanup.ground.enabled", true)) return;
                if (schedule.contains(t)) {
                    broadcastCountdown(t, worlds, false);
                }
                if (t <= 0) {
                    // start cleanup
                    String started = plugin.getConfig().getString("messages.cleanup_started", "&6%world% 월드 청소를 시작합니다.");
                    for (World w : worlds) {
                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix() + started.replace("%world%", w.getName())));
                        final int removed = EntityBatchCleaner.cleanWorldOnce(plugin, w);
                        String done = plugin.getConfig().getString("messages.cleanup_done", "&a청소 완료: 제거된 엔티티 %count%개");
                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix() + done.replace("%count%", String.valueOf(removed)).replace("%world%", w.getName())));
                    }
                    t = interval;
                } else {
                    t--;
                }
            }
        };
        groundTask.runTaskTimer(plugin, 20L, 20L);
    }

    private void setupPixelmonCleanup() {
        if (!plugin.getConfig().getBoolean("cleanup.pixelmon.enabled", true)) return;
        final int interval = plugin.getConfig().getInt("cleanup.pixelmon.intervalSeconds", 600);
        final Set<Integer> schedule = loadSchedule("cleanup.pixelmon.countdownSchedule");
        final List<World> worlds = resolveWorlds(plugin.getConfig().getStringList("cleanup.pixelmon.worlds"));

        pixelTask = new BukkitRunnable() {
            int t = interval;
            @Override public void run() {
                if (!plugin.getConfig().getBoolean("cleanup.pixelmon.enabled", true)) return;
                if (schedule.contains(t)) {
                    broadcastCountdown(t, worlds, true);
                }
                if (t <= 0) {
                    int total = 0;
                    for (World w : worlds) {
                        total += PixelmonCleaner.cleanNonLegendary(plugin, w);
                    }
                    if (total > 0) {
                        String msg = plugin.getConfig().getString("messages.pixelmon_cleanup_done", "&bPixelmon 비전설 제거 완료: %count%개 (월드: %world%)");
                        // If multiple worlds, just show 'ALL' to avoid spam
                        String worldName = (worlds.size() == 1 ? worlds.get(0).getName() : "ALL");
                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                                plugin.prefix() + msg.replace("%count%", String.valueOf(total)).replace("%world%", worldName)));
                    }
                    t = interval;
                } else {
                    t--;
                }
            }
        };
        pixelTask.runTaskTimer(plugin, 20L, 20L);
    }
}
