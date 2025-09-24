
package com.minkang.ultimate.tps;
import com.minkang.ultimate.Main;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
public class TpsMonitor {
    private final Main plugin;
    private BukkitRunnable task;
    private final Deque<Double> shortWin = new ArrayDeque<>();
    private final Deque<Double> midWin = new ArrayDeque<>();
    private final Deque<Double> longWin = new ArrayDeque<>();
    private final int sShort, sMid, sLong;
    public TpsMonitor(Main plugin) {
        this.plugin = plugin;
        this.sShort = Math.max(1, plugin.getConfig().getInt("tps.avg_seconds_short", 1));
        this.sMid = Math.max(1, plugin.getConfig().getInt("tps.avg_seconds_mid", 5));
        this.sLong = Math.max(1, plugin.getConfig().getInt("tps.avg_seconds_long", 15));
    }
    public void start() {
        if (task != null) task.cancel();
        task = new BukkitRunnable() {
            @Override public void run() {
                double tps = readTps();
                push(shortWin, tps, sShort);
                push(midWin, tps, sMid);
                push(longWin, tps, sLong);
            }
        };
        task.runTaskTimer(plugin, 20L, 20L);
    }
    public void stop() { if (task != null) task.cancel(); }
    private void push(Deque<Double> dq, double val, int max) { dq.addLast(val); while (dq.size() > max) dq.removeFirst(); }
    private double avg(Deque<Double> dq) { if (dq.isEmpty()) return 20.0; double s=0; for (double v: dq) s+=v; return s/dq.size(); }
    private double readTps() {
        try { Method m = Bukkit.getServer().getClass().getMethod("getTPS");
            double[] t = (double[]) m.invoke(Bukkit.getServer()); return Math.min(20.0, t[0]);
        } catch (Throwable ignored) {}
        return 20.0;
    }
    public double getShortAvg() { return avg(shortWin); }
    public double getMidAvg() { return avg(midWin); }
    public double getLongAvg() { return avg(longWin); }
}
