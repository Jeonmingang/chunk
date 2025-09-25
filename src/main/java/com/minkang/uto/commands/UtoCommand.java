
package com.minkang.uto.commands;

import com.minkang.uto.Main;
import com.minkang.uto.tps.TpsMonitor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class UtoCommand implements CommandExecutor, TabCompleter {
    private final Main plugin;
    public UtoCommand(Main plugin){ this.plugin=plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ultimate.uto")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix() + plugin.getConfig().getString("messages.no_permission")));
            return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.help_header")));
            sender.sendMessage(line("help", "이 도움말 표시"));
            sender.sendMessage(line("version", "플러그인/서버 버전"));
            sender.sendMessage(line("status", "TPS/조건/자동청소 상태"));
            sender.sendMessage(line("reload", "config.yml 리로드"));
            sender.sendMessage(line("entityauto <on|off> <world>", "일반 엔티티 자동청소 on/off"));
            sender.sendMessage(line("pixelmonauto <on|off> <world>", "Pixelmon 비전설 자동제거 on/off"));
            return true;
        }
        String sub = args[0].toLowerCase();
        if (sub.equals("version")) {
            String msg = plugin.getConfig().getString("messages.version");
            msg = msg.replace("%version%", plugin.getDescription().getVersion())
                    .replace("%server%", Bukkit.getVersion())
                    .replace("%java%", System.getProperty("java.version"));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix() + msg));
            return true;
        }
        if (sub.equals("status")) {
            TpsMonitor t = plugin.getTpsMonitor();
            String a = String.format("%.2f", t.getShortAvg());
            String b = String.format("%.2f", t.getMidAvg());
            String c = String.format("%.2f", t.getLongAvg());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.prefix() + plugin.getConfig().getString("messages.status_tps")
                            .replace("%tps1%", a).replace("%tps5%", b).replace("%tps15%", c)));
            boolean ok = plugin.getConditionGuard().isEnabled();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.prefix() + plugin.getConfig().getString("messages.status_conditions")
                            .replace("%enabled%", String.valueOf(ok))
                            .replace("%reason%", ok? "OK" : "DISABLED")));
            boolean auto = plugin.getConfig().getBoolean("cleanup.ground.enabled", true);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.prefix() + plugin.getConfig().getString("messages.status_auto")
                            .replace("%enabled%", String.valueOf(auto))
                            .replace("%sec%", String.valueOf(plugin.getConfig().getInt("cleanup.ground.intervalSeconds", 300)))));
            return true;
        }
        if (sub.equals("reload")) {
            plugin.reloadConfig();
            plugin.getAutoCleanupManager().stopAll();
            plugin.getAutoCleanupManager().initFromConfig();
            plugin.getWbAutomation().stop();
            plugin.getWbAutomation().initFromConfig();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix() + plugin.getConfig().getString("messages.reloaded")));
            return true;
        }
        if (sub.equals("entityauto")) {
            if (args.length < 3) {
                sender.sendMessage(plugin.prefix() + "/uto entityauto <on|off> <world>");
                return true;
            }
            String onoff = args[1].toLowerCase();
            String w = args[2];
            List<String> worlds = plugin.getConfig().getStringList("cleanup.entity.worlds");
            if (onoff.equals("on")) {
                if (!worlds.contains(w)) worlds.add(w);
            } else {
                worlds.remove(w);
            }
            plugin.getConfig().set("cleanup.entity.worlds", worlds);
            plugin.saveConfig();
            sender.sendMessage(plugin.prefix() + "cleanup.entity.worlds = " + worlds);
            return true;
        }
        if (sub.equals("pixelmonauto")) {
            if (args.length < 3) {
                sender.sendMessage(plugin.prefix() + "/uto pixelmonauto <on|off> <world>");
                return true;
            }
            String onoff = args[1].toLowerCase();
            String w = args[2];
            List<String> worlds = plugin.getConfig().getStringList("cleanup.pixelmon.worlds");
            if (onoff.equals("on")) {
                if (!worlds.contains(w)) worlds.add(w);
            } else {
                worlds.remove(w);
            }
            plugin.getConfig().set("cleanup.pixelmon.worlds", worlds);
            plugin.saveConfig();
            sender.sendMessage(plugin.prefix() + "cleanup.pixelmon.worlds = " + worlds);
            return true;
        }
        sender.sendMessage(plugin.prefix() + "알 수 없는 서브커맨드입니다. /uto help");
        return true;
    }

    private String line(String cmd, String desc){
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.help_line")
                        .replace("%cmd%", cmd).replace("%desc%", desc));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return java.util.Arrays.asList("help","version","status","reload","entityauto","pixelmonauto");
        if (args.length == 2 && (args[0].equalsIgnoreCase("entityauto") || args[0].equalsIgnoreCase("pixelmonauto"))) {
            return java.util.Arrays.asList("on","off");
        }
        if (args.length == 3 && (args[0].equalsIgnoreCase("entityauto") || args[0].equalsIgnoreCase("pixelmonauto"))) {
            List<String> list = new ArrayList<>();
            for (World w: Bukkit.getWorlds()) list.add(w.getName());
            return list;
        }
        return java.util.Collections.emptyList();
    }
}
