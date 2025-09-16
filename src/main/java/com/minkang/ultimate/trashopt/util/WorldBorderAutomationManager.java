package com.minkang.ultimate.trashopt.util;

import com.minkang.ultimate.trashopt.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.scheduler.BukkitTask;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class WorldBorderAutomationManager {

    private final Main plugin;
    private BukkitTask task;
    private final Set<String> firedKeys = new HashSet<String>(); // day+time keys to avoid duplicate in same day

    public WorldBorderAutomationManager(Main plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        if (!plugin.getConfig().getBoolean("automation.enabled", false)) return;
        long periodTicks = 20L * 30; // check every 30s
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() { tick(); }
        }, 40L, periodTicks);
    }

    public void stop() {
        if (task != null) { task.cancel(); task = null; }
    }

    private void tick() {
        if (!plugin.getConfig().getBoolean("automation.enabled", false)) return;

        String tz = plugin.getConfig().getString("automation.timezone", "Asia/Seoul");
        ZoneId zone = ZoneId.of(tz);
        ZonedDateTime now = ZonedDateTime.now(zone);
        String dayKey = now.toLocalDate().toString();

        List<String> times = plugin.getConfig().getStringList("automation.times");
        if (times == null || times.isEmpty()) {
            times = java.util.Arrays.asList("02:00","08:00");
        }

        for (String t : times) {
            String key = dayKey + " " + t;
            if (firedKeys.contains(key)) continue;
            LocalTime lt;
            try { lt = LocalTime.parse(t); } catch (Exception e) { continue; }
            if (now.getHour() == lt.getHour() && now.getMinute() == lt.getMinute()) {
                if (canRun()) {
                    runExpansion();
                }
                firedKeys.add(key);
            }
        }

        if (firedKeys.size() > 32) {
            Iterator<String> it = firedKeys.iterator();
            while (it.hasNext()) {
                String k = it.next();
                if (!k.startsWith(dayKey)) it.remove();
            }
        }
    }

    private boolean canRun() {
        int threshold = plugin.getConfig().getInt("automation.player_threshold", 30);
        int online = Bukkit.getOnlinePlayers().size();
        return online < threshold;
    }

    private void runExpansion() {
        List<String> worlds = plugin.getConfig().getStringList("automation.worldborder.worlds");
        if (worlds == null || worlds.isEmpty()) {
            worlds = java.util.Arrays.asList("world");
        }

        int inc = plugin.getConfig().getInt("automation.worldborder.increment", 5000);
        int max = plugin.getConfig().getInt("automation.worldborder.max_radius", 200000);
        int start = plugin.getConfig().getInt("automation.worldborder.start_radius", 5000);
        boolean useApi = plugin.getConfig().getBoolean("automation.worldborder.use_api", true);
        boolean triggerChunky = plugin.getConfig().getBoolean("automation.worldborder.trigger_chunky_after_expand", false);

        org.bukkit.configuration.file.FileConfiguration cfg = plugin.getConfig();
        boolean reachedMaxAll = true;

        for (String wName : worlds) {
            World w = Bukkit.getWorld(wName);
            if (w == null) continue;

            int current = cfg.getInt("automation.progress."+wName, -1);
            if (current <= 0) current = start;

            int next = current + inc;
            if (next > max) next = max;

            if (useApi) {
                WorldBorder border = w.getWorldBorder();
                // center to world spawn if unset (we assume 0,0 acceptable otherwise)
                border.setCenter(w.getSpawnLocation().getX(), w.getSpawnLocation().getZ());
                border.setSize((double) (next * 2)); // Spigot WorldBorder size is DIAMETER
            } else {
                // fallback to console commands from optimize.worldborder.start, replacing {diameter}
                java.util.List<String> cmds = plugin.getConfig().getStringList("optimize.worldborder.start");
                for (String c : cmds) {
                    String finalCmd = c.replace("{world}", wName)
                                       .replace("{radius}", String.valueOf(next))
                                       .replace("{diameter}", String.valueOf(next * 2));
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
                }
                if (plugin.getConfig().getBoolean("automation.worldborder.fill_after_set", false)) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "worldborder fill");
                }
            }

            // optional: trigger Chunky start to (re)generate up to new radius
            if (triggerChunky) {
                java.util.List<String> cmds = plugin.getConfig().getStringList("automation.worldborder.chunky.start");
                if (cmds == null || cmds.isEmpty()) {
                    cmds = java.util.Arrays.asList("chunky world {world}", "chunky center 0 0", "chunky radius {radius}", "chunky pattern square", "chunky start");
                }
                for (String c : cmds) {
                    String finalCmd = c.replace("{world}", wName).replace("{radius}", String.valueOf(next));
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
                }
            }

            cfg.set("automation.progress."+wName, next);
            if (next < max) reachedMaxAll = false;
        }

        plugin.saveConfig();

        if (reachedMaxAll) {
            cfg.set("automation.enabled", false);
            plugin.saveConfig();
            stop();
            Bukkit.getConsoleSender().sendMessage("[UTO][자동화] 모든 월드가 최대 반경에 도달하여 자동화를 종료했습니다.");
        } else {
            Bukkit.getConsoleSender().sendMessage("[UTO][자동화] 월드 경계를 확장했습니다. (증가량+" + inc + ", 최대 " + max + ")");
        }
    }
}
