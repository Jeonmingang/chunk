
package com.minkang.ultimate.automation;
import com.minkang.ultimate.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import java.util.ArrayList;
import java.util.List;
public class AutomationCommand implements CommandExecutor, TabCompleter {
    private final Main plugin;
    public AutomationCommand(Main plugin){ this.plugin=plugin; }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ultimate.automation.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix() + plugin.getConfig().getString("messages.no_permission")));
            return true;
        }
        if (args.length == 0) { sender.sendMessage(plugin.prefix() + "/automation <start|stop|status> [world]"); return true; }
        String sub = args[0].toLowerCase();
        if (sub.equals("start")) {
            plugin.getConfig().set("automation.worldborder.enabled", TrueBool(true));
            plugin.saveConfig();
            plugin.getWbAutomation().initFromConfig();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix() + plugin.getConfig().getString("messages.automation_started")));
            return true;
        } else if (sub.equals("stop")) {
            plugin.getConfig().set("automation.worldborder.enabled", FalseBool(false));
            plugin.saveConfig();
            plugin.getWbAutomation().stop();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix() + plugin.getConfig().getString("messages.automation_stopped")));
            return true;
        } else if (sub.equals("status")) {
            if (args.length >= 1 && Bukkit.getWorlds().size() > 0) {
                if (args.length >= 2) {
                    String w = args[1];
                    sender.sendMessage(plugin.getWbAutomation().statusLine(w));
                } else {
                    for (World w: Bukkit.getWorlds()) { sender.sendMessage(plugin.getWbAutomation().statusLine(w.getName())); }
                }
            }
            return true;
        }
        sender.sendMessage(plugin.prefix() + "/automation <start|stop|status> [world]");
        return true;
    }
    private Boolean TrueBool(boolean v){ return Boolean.valueOf(v); }
    private Boolean FalseBool(boolean v){ return Boolean.valueOf(v); }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return java.util.Arrays.asList("start","stop","status");
        if (args.length == 2) { List<String> l = new ArrayList<>(); for (World w: Bukkit.getWorlds()) l.add(w.getName()); return l; }
        return java.util.Collections.emptyList();
    }
}
