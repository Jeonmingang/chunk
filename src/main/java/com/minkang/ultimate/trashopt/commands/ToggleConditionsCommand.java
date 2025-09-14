package com.minkang.ultimate.trashopt.commands;

import com.minkang.ultimate.trashopt.Main;
import com.minkang.ultimate.trashopt.util.ConditionGuard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ToggleConditionsCommand implements CommandExecutor {

    private final Main plugin;

    public ToggleConditionsCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("ultimate.conditions.admin")) {
            sender.sendMessage(ChatColor.RED + "권한이 없습니다.");
            return true;
        }

        if ("조건상태".equalsIgnoreCase(cmd.getName())) {
            boolean enabled = plugin.getConfig().getBoolean("conditions.enabled", true);
            double minTps = plugin.getConfig().getDouble("conditions.min_tps", 18.5);
            int maxPlayers = plugin.getConfig().getInt("conditions.max_players", 4);
            boolean timeEnabled = plugin.getConfig().getBoolean("conditions.time_window.enabled", true);
            int start = plugin.getConfig().getInt("conditions.time_window.start_hour", 2);
            int end = plugin.getConfig().getInt("conditions.time_window.end_hour", 9);
            String tz = plugin.getConfig().getString("conditions.time_window.timezone", "Asia/Seoul");

            sender.sendMessage(ChatColor.GOLD + "[조건 상태] enabled=" + enabled +
                    ", min_tps=" + minTps +
                    ", max_players=" + maxPlayers +
                    ", time_window.enabled=" + timeEnabled +
                    ", time=" + start + "~" + end + " (" + tz + ")");
            boolean allowed = com.minkang.ultimate.trashopt.util.ConditionGuard.checkAllowed(sender, "점검");
            sender.sendMessage(ChatColor.AQUA + "현재 조건으로 실행 가능? " + (allowed ? ChatColor.GREEN + "예" : ChatColor.RED + "아니오"));
            return true;
        }

        // /조건 [on|off]  or toggle if no arg
        boolean currently = plugin.getConfig().getBoolean("conditions.enabled", true);
        boolean target = currently;
        if (args.length >= 1) {
            if ("on".equalsIgnoreCase(args[0])) target = true;
            else if ("off".equalsIgnoreCase(args[0])) target = false;
            else {
                sender.sendMessage(ChatColor.YELLOW + "사용법: /조건 [on|off]  (현재: " + (currently ? "on" : "off") + ")");
                return true;
            }
        } else {
            target = !currently;
        }
        plugin.getConfig().set("conditions.enabled", target);
        plugin.saveConfig();
        sender.sendMessage(ChatColor.GREEN + "[조건] now " + (target ? "ON" : "OFF"));
        return true;
    }
}
