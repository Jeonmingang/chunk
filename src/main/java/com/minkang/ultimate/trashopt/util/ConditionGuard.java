package com.minkang.ultimate.trashopt.util;

import com.minkang.ultimate.trashopt.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.time.ZonedDateTime;
import java.time.ZoneId;

public class ConditionGuard {

    public static boolean checkAllowed(CommandSender sender, String context) {
        Main plugin = Main.getInstance();
        boolean enabled = plugin.getConfig().getBoolean("conditions.enabled", true);
        if (!enabled) return true;

        // TPS
        double minTps = plugin.getConfig().getDouble("conditions.min_tps", 18.5);
        double currentTps = plugin.getTpsMonitor().getTps();
        if (currentTps < minTps) {
            sender.sendMessage(ChatColor.RED + "[조건] TPS " + String.format("%.2f", currentTps) + " < " + minTps + " 이므로 '" + context + "' 실행 불가");
            return false;
        }

        // Players
        int maxPlayers = plugin.getConfig().getInt("conditions.max_players", 4);
        int online = Bukkit.getOnlinePlayers().size();
        if (online > maxPlayers) {
            sender.sendMessage(ChatColor.RED + "[조건] 온라인 " + online + "명 > 허용 " + maxPlayers + "명 이므로 '" + context + "' 실행 불가");
            return false;
        }

        // Time window
        boolean timeEnabled = plugin.getConfig().getBoolean("conditions.time_window.enabled", true);
        if (timeEnabled) {
            String tz = plugin.getConfig().getString("conditions.time_window.timezone", "Asia/Seoul");
            int start = plugin.getConfig().getInt("conditions.time_window.start_hour", 2);
            int end = plugin.getConfig().getInt("conditions.time_window.end_hour", 9);

            ZonedDateTime now = ZonedDateTime.now(ZoneId.of(tz));
            int hour = now.getHour();
            boolean inWindow;
            if (start <= end) {
                inWindow = (hour >= start && hour < end);
            } else { // wrap
                inWindow = !(hour >= end && hour < start);
            }
            if (!inWindow) {
                sender.sendMessage(ChatColor.RED + "[조건] 현재 시간(" + hour + "시)이 허용 시간대(" + start + ":00~" + end + ":00)에 포함되지 않아 '" + context + "' 실행 불가");
                return false;
            }
        }
        return true;
    }
}
