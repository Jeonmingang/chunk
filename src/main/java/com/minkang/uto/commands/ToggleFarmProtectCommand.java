
package com.minkang.uto.commands;

import com.minkang.uto.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ToggleFarmProtectCommand implements CommandExecutor {
    private final Main plugin;
    public ToggleFarmProtectCommand(Main plugin){ this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ultimate.farm.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.prefix() + plugin.getConfig().getString("messages.no_permission")));
            return true;
        }
        boolean cur = plugin.getConfig().getBoolean("farm.protect.enabled", true);
        boolean next;
        if (args.length >= 1) {
            String a = args[0].toLowerCase();
            if (a.equals("on")) next = true;
            else if (a.equals("off")) next = false;
            else next = !cur;
        } else {
            next = !cur;
        }
        plugin.getConfig().set("farm.protect.enabled", next);
        plugin.saveConfig();
        sender.sendMessage(plugin.prefix() + (next
                ? ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.farm_protect_on","&a농장보호 ON"))
                : ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.farm_protect_off","&c농장보호 OFF"))));
        return true;
    }
}
