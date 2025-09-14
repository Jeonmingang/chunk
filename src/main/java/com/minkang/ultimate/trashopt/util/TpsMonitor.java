package com.minkang.ultimate.trashopt.util;

import com.minkang.ultimate.trashopt.Main;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class TpsMonitor {

    private final Main plugin;
    private BukkitTask task;
    private long lastTime = System.currentTimeMillis();
    private int ticks = 0;
    private double tps = 20.0;

    public TpsMonitor(Main plugin) {
        this.plugin = plugin;
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                ticks++;
                long now = System.currentTimeMillis();
                long diff = now - lastTime;
                if (diff >= 1000) {
                    // Calculate TPS in last second window
                    tps = Math.min(20.0, ticks * 1000.0 / diff);
                    ticks = 0;
                    lastTime = now;
                }
            }
        }, 1L, 1L);
    }

    public void stop() {
        if (task != null) task.cancel();
    }

    public double getTps() {
        return tps;
    }
}
