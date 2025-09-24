
package com.minkang.ultimate.commands;

import com.minkang.ultimate.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ToggleFarmProtectCommand implements CommandExecutor {
    private final Main plugin;
    public ToggleFarmProtectCommand(Main plugin){ this.plugin=plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ultimate.farm.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix() + plugin.getConfig().getString("messages.no_permission")));
            return true;
        }
        boolean cur = plugin.getConfig().getBoolean("farm.protect.enabled", true);
        boolean next = !cur;
        plugin.getConfig().set("farm.protect.enabled", next);
        plugin.saveConfig();
        sender.sendMessage(plugin.prefix() + (next ? plugin.getConfig().getString("messages.farm_protect_on","농장보호 ON")
                                                  : plugin.getConfig().getString("messages.farm_protect_off","농장보호 OFF")));
        return true;
    }
}
