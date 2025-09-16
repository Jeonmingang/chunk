package com.minkang.ultimate.trashopt.commands;

import com.minkang.ultimate.trashopt.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ToggleFarmProtectCommand implements CommandExecutor {

    private final Main plugin;

    public ToggleFarmProtectCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("ultimate.farm.admin")) {
            sender.sendMessage(ChatColor.RED + "권한이 없습니다.");
            return true;
        }
        boolean current = plugin.getConfig().getBoolean("farm_protect.enabled", true);
        boolean target = current;
        if (args.length >= 1) {
            if ("on".equalsIgnoreCase(args[0])) target = true;
            else if ("off".equalsIgnoreCase(args[0])) target = false;
            else { sender.sendMessage(ChatColor.YELLOW + "사용법: /농장보호 [on|off] (현재: " + (current ? "on" : "off") + ")"); return true; }
        } else {
            target = !current;
        }
        plugin.getConfig().set("farm_protect.enabled", target);
        plugin.saveConfig();
        sender.sendMessage(ChatColor.GREEN + "[농장보호] now " + (target ? "ON" : "OFF"));
        return true;
    }
}
