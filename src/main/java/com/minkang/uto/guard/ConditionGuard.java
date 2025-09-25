
package com.minkang.uto.guard;

import com.minkang.uto.Main;
import com.minkang.uto.tps.TpsMonitor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ConditionGuard {
    private final Main plugin;
    private final AtomicLong lastFail = new AtomicLong(0);

    public ConditionGuard(Main plugin) { this.plugin = plugin; }

    public boolean isEnabled() {
        try {
            return plugin.getConfig().getBoolean("conditions.enabled", true);
        } catch (Throwable t) {
            return true;
        }
    }

    public boolean checkAllowed(CommandSender feedbackTarget) {
        if (!isEnabled()) return true;

        String reason = null;

        // TPS
        double minTps = 18.5;
        try { minTps = plugin.getConfig().getDouble("conditions.min_tps", 18.5); } catch (Throwable ignored) {}
        TpsMonitor m = plugin.getTpsMonitor();
        if (m != null && m.getShortAvg() < minTps) {
            reason = "TPS " + String.format("%.2f", m.getShortAvg()) + " < " + minTps;
        }

        // Players
        if (reason == null) {
            int maxPlayers = 50;
            try { maxPlayers = plugin.getConfig().getInt("conditions.max_players", 50); } catch (Throwable ignored) {}
            int online = Bukkit.getOnlinePlayers().size();
            if (online > maxPlayers) {
                reason = "Players " + online + " > " + maxPlayers;
            }
        }

        // Time window
        try {
            if (reason == null && plugin.getConfig().getBoolean("conditions.time_window.enabled", false)) {
                String tz = plugin.getConfig().getString("conditions.time_window.timezone", "Asia/Seoul");
                int start = plugin.getConfig().getInt("conditions.time_window.start_hour", 2);
                int end = plugin.getConfig().getInt("conditions.time_window.end_hour", 9);
                ZonedDateTime now = ZonedDateTime.now(ZoneId.of(tz));
                int h = now.getHour();
                boolean in;
                if (start <= end) in = (h >= start && h < end);
                else in = (h >= start || h < end);
                if (!in) reason = "Time window disallow (" + h + "h not in " + start + "-" + end + ")";
            }
        } catch (Throwable ignored) {}

        // Allowed days
        try {
            if (reason == null) {
                List<String> days = plugin.getConfig().getStringList("conditions.allowed_days");
                if (days != null && !days.isEmpty()) {
                    String today = ZonedDateTime.now().getDayOfWeek().name();
                    boolean ok = false;
                    for (String d: days) if (d.equalsIgnoreCase(today)) { ok = true; break; }
                    if (!ok) reason = "Day disallow (" + today + " not in " + days + ")";
                }
            }
        } catch (Throwable ignored) {}

        // Cooldown
        int cd = 20;
        try { cd = plugin.getConfig().getInt("conditions.cooldown_sec", 20); } catch (Throwable ignored) {}
        long nowMs = System.currentTimeMillis();
        if (reason == null) {
            long last = lastFail.get();
            if (last > 0 && (nowMs - last) < cd*1000L) {
                reason = "Cooldown " + ((cd*1000L - (nowMs - last))/1000) + "s left";
            }
        }

        if (reason != null) {
            lastFail.set(System.currentTimeMillis());
            if (feedbackTarget != null) {
                String msg = "&c[조건] 현재 실행 불가: %reason%";
                try { msg = plugin.getConfig().getString("messages.conditions_denied", msg); } catch (Throwable ignored) {}
                feedbackTarget.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.prefix() + msg.replace("%reason%", reason)));
            }
            return false;
        } else {
            if (feedbackTarget != null) {
                String msg = "&a[조건] 현재 실행 허용";
                try { msg = plugin.getConfig().getString("messages.conditions_allowed", msg); } catch (Throwable ignored) {}
                feedbackTarget.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix() + msg));
            }
            return true;
        }
    }
}
