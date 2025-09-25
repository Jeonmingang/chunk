
package com.minkang.uto.cleanup;

import com.minkang.uto.Main;
import com.minkang.uto.guard.ConditionGuard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class CleanupCommand implements CommandExecutor, TabCompleter {
    private final Main plugin;
    public CleanupCommand(Main plugin){ this.plugin=plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ultimate.cleanup.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix() + plugin.getConfig().getString("messages.no_permission")));
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(plugin.prefix() + "/cleanup <world> [seconds]");
            return true;
        }
        World w = Bukkit.getWorld(args[0]);
        if (w == null) {
            sender.sendMessage(plugin.prefix() + ChatColor.RED + "월드를 찾을 수 없습니다: " + args[0]);
            return true;
        }
        int seconds = 10;
        if (args.length >= 2) {
            try { seconds = Math.max(0, Integer.parseInt(args[1])); } catch (Exception ignored) {}
        }

        ConditionGuard guard = plugin.getConditionGuard();
        if (!guard.checkAllowed(sender)) return true;

        String msg = plugin.getConfig().getString("messages.cleanup_countdown");
        if (msg != null && seconds > 0) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.prefix() + msg.replace("%world%", w.getName()).replace("%seconds%", String.valueOf(seconds))));
        }

        final int runAfter = seconds;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            String st = plugin.getConfig().getString("messages.cleanup_started");
            if (st != null) Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.prefix() + st.replace("%world%", w.getName())));

            int removed = EntityBatchCleaner.cleanWorldOnce(plugin, w);
            String done = plugin.getConfig().getString("messages.cleanup_done");
            if (done != null) Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.prefix() + done.replace("%count%", String.valueOf(removed))));
        }, runAfter * 20L);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            for (World w : Bukkit.getWorlds()) list.add(w.getName());
            return list;
        }
        if (args.length == 2) {
            return java.util.Arrays.asList("10","30","60","120","300");
        }
        return java.util.Collections.emptyList();
    }
}
