
package com.minkang.uto.optimize;

import com.minkang.uto.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class OptimizeCommand implements CommandExecutor, TabCompleter {
    private final Main plugin;
    public OptimizeCommand(Main plugin){ this.plugin=plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ultimate.optimize.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix() + plugin.getConfig().getString("messages.no_permission")));
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(plugin.prefix() + "/optimize <chunky|worldborder|starlight|all|start> [world]");
            return true;
        }
        String sub = args[0].toLowerCase();
        String world = args.length >= 2 ? args[1] : (Bukkit.getWorlds().isEmpty() ? "world" : Bukkit.getWorlds().get(0).getName());

        if (sub.equals("chunky")) {
            if (Bukkit.getPluginManager().getPlugin("Chunky") == null) {
                sender.sendMessage(plugin.prefix() + ChatColor.RED + "Chunky 플러그인이 없습니다.");
                return true;
            }
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky world " + world);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky radius 5000");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky start");
            sender.sendMessage(plugin.prefix() + "Chunky 프리필을 시작했습니다: " + world);
            return true;
        }
        if (sub.equals("worldborder")) {
            if (Bukkit.getPluginManager().getPlugin("WorldBorder") == null) {
                sender.sendMessage(plugin.prefix() + ChatColor.RED + "WorldBorder 플러그인이 없습니다.");
                return true;
            }
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb " + world + " set 5000 spawn");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb " + world + " fill");
            sender.sendMessage(plugin.prefix() + "WorldBorder fill을 시작했습니다: " + world);
            return true;
        }
        if (sub.equals("starlight")) {
            if (Bukkit.getPluginManager().getPlugin("Starlight") == null) {
                sender.sendMessage(plugin.prefix() + ChatColor.RED + "Starlight 플러그인이 없습니다.");
                return true;
            }
            sender.sendMessage(plugin.prefix() + "Starlight 최적화는 서버 자동 처리(Chunks relight)입니다.");
            return true;
        }
        if (sub.equals("all") || sub.equals("start")) {
            onCommand(sender, command, label, new String[]{"worldborder", world});
            onCommand(sender, command, label, new String[]{"chunky", world});
            return true;
        }
        sender.sendMessage(plugin.prefix() + "/optimize <chunky|worldborder|starlight|all|start> [world]");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return java.util.Arrays.asList("chunky","worldborder","starlight","all","start");
        }
        if (args.length == 2) {
            List<String> list = new ArrayList<>();
            for (World w : Bukkit.getWorlds()) list.add(w.getName());
            return list;
        }
        return java.util.Collections.emptyList();
    }
}
