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
    private BukkitRunnable autoTask;
    private boolean groundEnabledRuntime = true;
    private boolean pixelEnabledRuntime = true;
    private boolean autoEnabledRuntime = true;

    public AutoCleanupManager(Main plugin) { this.plugin = plugin; }

    public void stopAll() {
        if (groundTask != null) { try { groundTask.cancel(); } catch (Throwable ignored) {} groundTask = null; }
        if (pixelTask != null) { try { pixelTask.cancel(); } catch (Throwable ignored) {} pixelTask = null; }
        if (autoTask != null)  { try { autoTask.cancel(); }  catch (Throwable ignored) {} autoTask  = null; }
    }

    public void initFromConfig() {
        stopAll();
        setupGroundCleanup();
        setupPixelmonCleanup();
        if (plugin.getConfig().getBoolean("auto.enabled", true)) {
            autoEnabledRuntime = true;
            setupAutoOrchestrator();
        }
    }

    private Set<Integer> loadSchedule(String path) {
        List<Integer> list = plugin.getConfig().getIntegerList(path);
        if (list == null || list.isEmpty()) return new HashSet<Integer>(Arrays.asList(60,30,10,5,4,3,2,1));
        return new HashSet<Integer>(list);
    }

    private List<World> resolveWorlds(List<String> names) {
        List<World> ws = new ArrayList<World>();
        if (names == null || names.isEmpty()) { ws.addAll(Bukkit.getWorlds()); return ws; }
        for (String n : names) { World w = Bukkit.getWorld(n); if (w != null) ws.add(w); }
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

    private void broadcastCountdownWorlds(int seconds, String typeKey, List<World> worlds) {
        boolean once = plugin.getConfig().getBoolean("cleanup.broadcastOnce", true);
        if (once) { broadcastCountdown(seconds, typeKey); return; }
        ConfigurationSection map = plugin.getConfig().getConfigurationSection("messages.cleanup_countdown_map");
        String base = plugin.getConfig().getString("messages.cleanup_countdown", "&e%world% 월드에서 %type% %seconds%s 후 실행됩니다...");
        String line = (map != null ? map.getString(String.valueOf(seconds), base) : base);
        for (World w : worlds) {
            String msg = line.replace("%seconds%", String.valueOf(seconds)).replace("%type%", typeKey).replace("%world%", w.getName());
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix() + msg));
        }
    }

    private void broadcastStartedWorlds(String typeKey, List<World> worlds) {
        boolean once = plugin.getConfig().getBoolean("cleanup.broadcastOnce", true);
        if (once) {
            String base = plugin.getConfig().getString("messages.cleanup_started", "&6모든 월드에서 %type% 를 시작합니다.");
            broadcastOnce(base.replace("%type%", typeKey));
        } else {
            String base = plugin.getConfig().getString("messages.cleanup_started", "&6%world% 월드에서 %type% 를 시작합니다.");
            for (World w : worlds) {
                String msg = base.replace("%type%", typeKey).replace("%world%", w.getName());
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix() + msg));
            }
        }
    }

    private void broadcastDoneWorlds(String typeKey, int totalRemoved, List<World> worlds) {
        boolean once = plugin.getConfig().getBoolean("cleanup.broadcastOnce", true);
        if (once) {
            String base = plugin.getConfig().getString("messages.cleanup_done", "&a%type% 완료: 제거된 엔티티 %count%개");
            broadcastOnce(base.replace("%type%", typeKey).replace("%count%", String.valueOf(totalRemoved)));
        } else {
            String base = plugin.getConfig().getString("messages.cleanup_done", "&a%world%: %type% 완료 - 제거 %count%개");
            for (World w : worlds) {
                String msg = base.replace("%type%", typeKey).replace("%count%", String.valueOf(totalRemoved)).replace("%world%", w.getName());
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix() + msg));
            }
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
                if (!plugin.getConfig().getBoolean("cleanup.ground.enabled", true) || !groundEnabledRuntime) return;
                if (schedule.contains(t)) broadcastCountdownWorlds(t, "바닥청소", worlds);
                if (t <= 0) {
                    broadcastStartedWorlds("바닥청소", worlds);
                    int total = 0; for (World w : worlds) total += EntityBatchCleaner.cleanGroundItems(plugin, w);
                    broadcastDoneWorlds("바닥청소", total, worlds);
                    t = interval;
                } else t--;
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
                if (!plugin.getConfig().getBoolean("cleanup.pixelmon.enabled", true) || !pixelEnabledRuntime) return;
                if (schedule.contains(t)) broadcastCountdownWorlds(t, "비전설 픽셀몬 청소", worlds);
                if (t <= 0) {
                    broadcastStartedWorlds("비전설 픽셀몬 청소", worlds);
                    int total = 0; for (World w : worlds) total += PixelmonCleaner.cleanNonLegendary(plugin, w);
                    String msg = plugin.getConfig().getString("messages.pixelmon_cleanup_done", "&b비전설 픽셀몬 제거 완료: %count%개 (모든 월드)");
                    broadcastOnce(msg.replace("%count%", String.valueOf(total)));
                    t = interval;
                } else t--;
            }
        };
        pixelTask.runTaskTimer(plugin, 20L, 20L);
    }

    private int[] parseHM(String s) {
        try { String[] p = s.split(":"); int h=Integer.parseInt(p[0]); int m=Integer.parseInt(p[1]); if (h<0||h>23||m<0||m>59) return new int[]{4,0}; return new int[]{h,m}; }
        catch (Exception e){ return new int[]{4,0}; }
    }

    private void setupAutoOrchestrator() {
        final int playerThreshold = plugin.getConfig().getInt("auto.playerThreshold", 10);
        final boolean timeEnabled    = plugin.getConfig().getBoolean("auto.timeWindow.enabled", true);
        final int[] st = parseHM(plugin.getConfig().getString("auto.timeWindow.start", "04:00"));
        final int[] en = parseHM(plugin.getConfig().getString("auto.timeWindow.end", "07:00"));
        final boolean actGround = plugin.getConfig().getBoolean("auto.actions.ground", true);
        final boolean actPixel  = plugin.getConfig().getBoolean("auto.actions.pixelmon", true);
        final int cdSec = plugin.getConfig().getInt("auto.countdownSeconds", 60);

        autoTask = new BukkitRunnable() {
            boolean firedThisWindow = false;
            @Override public void run() {
                if (!autoEnabledRuntime) return;
                java.util.Calendar now = java.util.Calendar.getInstance();
                int cur = now.get(java.util.Calendar.HOUR_OF_DAY)*60 + now.get(java.util.Calendar.MINUTE);
                int smin = st[0]*60+st[1], emin = en[0]*60+en[1];
                boolean inWin = !timeEnabled || ((smin <= emin) ? (cur>=smin && cur<=emin) : (cur>=smin || cur<=emin));
                if (!inWin) { firedThisWindow = false; return; }
                if (Bukkit.getOnlinePlayers().size() > playerThreshold) return;
                if (firedThisWindow) return;
                firedThisWindow = true;
                if (actGround) triggerGroundOnce(null, cdSec);
                if (actPixel)  triggerPixelOnce(null, cdSec);
            }
        };
        autoTask.runTaskTimer(plugin, 20L, 20L*60);
    }

    public void setGroundEnabledRuntime(boolean on) { groundEnabledRuntime = on; }
    public void setPixelEnabledRuntime(boolean on)  { pixelEnabledRuntime  = on; }
    public void setAutoEnabledRuntime(boolean on) {
        autoEnabledRuntime = on;
        if (!on && autoTask != null) { try { autoTask.cancel(); } catch (Throwable ignored) {} autoTask = null; }
        if (on && autoTask == null) setupAutoOrchestrator();
    }

    public void triggerGroundOnce(List<World> onlyWorlds, int countdownSeconds) {
        final List<World> worlds = (onlyWorlds!=null && !onlyWorlds.isEmpty())
                ? onlyWorlds : resolveWorlds(plugin.getConfig().getStringList("cleanup.ground.worlds"));
        scheduleOneOff("바닥청소", worlds, countdownSeconds, new Runnable() {
            public void run() {
                int total = 0; for (World w : worlds) total += EntityBatchCleaner.cleanGroundItems(plugin, w);
                String base = plugin.getConfig().getString("messages.ground_cleanup_done",
                        plugin.getConfig().getString("messages.cleanup_done", "&a%type% 완료: 제거된 엔티티 %count%개"));
                broadcastOnce(base.replace("%type%","바닥청소").replace("%count%", String.valueOf(total)));
            }
        });
    }

    public void triggerPixelOnce(List<World> onlyWorlds, int countdownSeconds) {
        final List<World> worlds = (onlyWorlds!=null && !onlyWorlds.isEmpty())
                ? onlyWorlds : resolveWorlds(plugin.getConfig().getStringList("cleanup.pixelmon.worlds"));
        scheduleOneOff("비전설 픽셀몬 청소", worlds, countdownSeconds, new Runnable() {
            public void run() {
                int total = 0; for (World w : worlds) total += PixelmonCleaner.cleanNonLegendary(plugin, w);
                String msg = plugin.getConfig().getString("messages.pixelmon_cleanup_done",
                        "&b비전설 픽셀몬 제거 완료: %count%개 (모든 월드)");
                broadcastOnce(msg.replace("%count%", String.valueOf(total)));
            }
        });
    }

    private void scheduleOneOff(final String type, final List<World> worlds, int countdownSeconds, final Runnable job) {
        final Set<Integer> schedule = new HashSet<Integer>(
                plugin.getConfig().getIntegerList(type.contains("픽셀") ? "cleanup.pixelmon.countdownSchedule" : "cleanup.ground.countdownSchedule"));
        final int total = (countdownSeconds <= 0 ? 60 : countdownSeconds);
        int[] ss = new int[]{60,30,10,5,4,3,2,1};
        for (int s : ss) {
            if (schedule.contains(s) && s <= total) {
                long delay = (total - s) * 20L;
                new BukkitRunnable() { @Override public void run() { broadcastCountdownWorlds(s, type, worlds); } }.runTaskLater(plugin, delay);
            }
        }
        new BukkitRunnable() { @Override public void run() {
            broadcastStartedWorlds(type, worlds);
            try { job.run(); } catch (Throwable ignored) {}
        }}.runTaskLater(plugin, total * 20L);
    }
}