package com.minkang.uto.cleanup;

import com.minkang.uto.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

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

    private void broadcastOnce(String msg) {
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix() + msg));
    }

    private void broadcastCountdown(int seconds, String typeKey) {
        ConfigurationSection map = plugin.getConfig().getConfigurationSection("messages.cleanup_countdown_map");
        String base = plugin.getConfig().getString("messages.cleanup_countdown", "&e모든 월드에서 %type% %seconds%s 후 실행됩니다...");
        String line = (map != null ? map.getString(String.valueOf(seconds), base) : base);
        String msg = line.replace("%seconds%", String.valueOf(seconds)).replace("%type%", typeKey);
        broadcastOnce(msg);
    }

    private void broadcastStarted(String typeKey) {
        String base = plugin.getConfig().getString("messages.cleanup_started", "&6모든 월드에서 %type% 를 시작합니다.");
        broadcastOnce(base.replace("%type%", typeKey));
    }

    private void broadcastDone(String typeKey, int totalRemoved) {
        String base = plugin.getConfig().getString("messages.cleanup_done", "&a%type% 완료: 제거된 엔티티 %count%개");
        broadcastOnce(base.replace("%type%", typeKey).replace("%count%", String.valueOf(totalRemoved)));
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
                    broadcastCountdown(t, "바닥청소");
                }
                if (t <= 0) {
                    broadcastStarted("바닥청소");
                    int total = 0;
                    for (World w : worlds) {
                        total += EntityBatchCleaner.cleanGroundItems(plugin, w);
                    }
                    broadcastDone("바닥청소", total);
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
                    broadcastCountdown(t, "비전설 픽셀몬 청소");
                }
                if (t <= 0) {
                    broadcastStarted("비전설 픽셀몬 청소");
                    int total = 0;
                    for (World w : worlds) {
                        total += PixelmonCleaner.cleanNonLegendary(plugin, w);
                    }
                    String msg = plugin.getConfig().getString("messages.pixelmon_cleanup_done", "&b비전설 픽셀몬 제거 완료: %count%개 (모든 월드)");
                    broadcastOnce(msg.replace("%count%", String.valueOf(total)));
                    t = interval;
                } else {
                    t--;
                }
            }
        };
        pixelTask.runTaskTimer(plugin, 20L, 20L);
    }
}
