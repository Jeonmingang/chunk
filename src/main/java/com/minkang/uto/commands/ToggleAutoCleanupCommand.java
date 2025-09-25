
package com.minkang.uto.commands;

import com.minkang.uto.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ToggleAutoCleanupCommand implements CommandExecutor {
    private final Main plugin;
    public ToggleAutoCleanupCommand(Main plugin){ this.plugin=plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ultimate.cleanup.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix() + plugin.getConfig().getString("messages.no_permission")));
            return true;
        }
        boolean cur = plugin.getConfig().getBoolean("cleanup.ground.enabled", true);
        boolean next = !cur;
        plugin.getConfig().set("cleanup.ground.enabled", next);
        plugin.saveConfig();
        plugin.getAutoCleanupManager().stopAll();
        plugin.getAutoCleanupManager().initFromConfig();
        sender.sendMessage(plugin.prefix() + "자동 바닥청소: " + (next? "ON":"OFF"));
        return true;
    }
}
