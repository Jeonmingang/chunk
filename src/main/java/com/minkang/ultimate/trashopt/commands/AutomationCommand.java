package com.minkang.ultimate.trashopt.commands;

import com.minkang.ultimate.trashopt.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AutomationCommand implements CommandExecutor {

    private final Main plugin;

    public AutomationCommand(Main plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("ultimate.automation.admin")) {
            sender.sendMessage(ChatColor.RED + "권한이 없습니다.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.YELLOW + "사용법: /자동화 <시작|종료>");
            return true;
        }
        String sub = args[0];
        if ("시작".equalsIgnoreCase(sub)) {
            plugin.getConfig().set("automation.enabled", true);
            plugin.saveConfig();
            plugin.getWorldBorderAutomationManager().start();
            sender.sendMessage(ChatColor.GREEN + "[자동화] 시작했습니다.");
            return true;
        } else if ("종료".equalsIgnoreCase(sub)) {
            plugin.getConfig().set("automation.enabled", false);
            plugin.saveConfig();
            plugin.getWorldBorderAutomationManager().stop();
            sender.sendMessage(ChatColor.RED + "[자동화] 종료했습니다.");
            return true;
        } else {
            sender.sendMessage(ChatColor.YELLOW + "사용법: /자동화 <시작|종료>");
            return true;
        }
    }
}
