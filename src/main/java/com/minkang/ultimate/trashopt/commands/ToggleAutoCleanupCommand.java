package com.minkang.ultimate.trashopt.commands;

import com.minkang.ultimate.trashopt.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ToggleAutoCleanupCommand implements CommandExecutor {

    private final Main plugin;

    public ToggleAutoCleanupCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("ultimate.cleanup.admin")) {
            sender.sendMessage(ChatColor.RED + "권한이 없습니다.");
            return true;
        }
        boolean current = plugin.getConfig().getBoolean("cleanup.auto.enabled", false);
        boolean target = current;
        if (args.length >= 1) {
            if ("on".equalsIgnoreCase(args[0])) target = true;
            else if ("off".equalsIgnoreCase(args[0])) target = false;
            else { sender.sendMessage(ChatColor.YELLOW + "사용법: /청소자동 [on|off] (현재: " + (current ? "on" : "off") + ")"); return true; }
        } else {
            target = !current;
        }
        plugin.getConfig().set("cleanup.auto.enabled", target);
        plugin.saveConfig();
        plugin.getAutoCleanupManager().rescheduleFromConfig();
        sender.sendMessage(ChatColor.GREEN + "[청소자동] now " + (target ? "ON" : "OFF"));
        return true;
    }
}
