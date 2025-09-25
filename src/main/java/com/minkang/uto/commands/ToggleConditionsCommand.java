
package com.minkang.uto.commands;

import com.minkang.uto.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ToggleConditionsCommand implements CommandExecutor, org.bukkit.command.TabCompleter {
    private final Main plugin;
    public ToggleConditionsCommand(Main plugin){ this.plugin=plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ultimate.conditions.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix() + plugin.getConfig().getString("messages.no_permission")));
            return true;
        }
        if (args.length == 0) {
            boolean cur = plugin.getConfig().getBoolean("conditions.enabled", true);
            plugin.getConfig().set("conditions.enabled", !cur);
            plugin.saveConfig();
            sender.sendMessage(plugin.prefix() + "조건가드: " + (!cur? "ON":"OFF"));
            return true;
        }
        String a = args[0].toLowerCase();
        if (a.equals("on") || a.equals("off")) {
            plugin.getConfig().set("conditions.enabled", a.equals("on"));
            plugin.saveConfig();
            sender.sendMessage(plugin.prefix() + "조건가드: " + (a.equals("on")? "ON":"OFF"));
            return true;
        }
        sender.sendMessage(plugin.prefix() + "/conditions [on|off]");
        return true;
    }

    @Override
    public java.util.List<String> onTabComplete(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        if (args.length == 1) return java.util.Arrays.asList("on","off");
        return java.util.Collections.emptyList();
    }
}
