package com.minkang.ultimate.trashopt.commands;

import com.minkang.ultimate.trashopt.Main;
import com.minkang.ultimate.trashopt.util.ConditionGuard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CleanupCommand implements CommandExecutor {

    private final Main plugin;

    public CleanupCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!sender.hasPermission("ultimate.cleanup.admin")) {
            sender.sendMessage(ChatColor.RED + "권한이 없습니다.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.YELLOW + "사용법: /" + label + " <world> [seconds]");
            return true;
        }

        if (!ConditionGuard.checkAllowed(sender, "월드 청소")) return true;

        String worldName = args[0];
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            sender.sendMessage(ChatColor.RED + "월드를 찾을 수 없습니다: " + worldName);
            return true;
        }

        int totalSeconds = plugin.getConfig().getInt("cleanup.default_seconds", 30);
        if (args.length >= 2) {
            try { totalSeconds = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
        }
        if (totalSeconds < 1) totalSeconds = 1;

        List<Integer> announce = plugin.getConfig().getIntegerList("cleanup.announce_seconds");
        if (announce == null || announce.isEmpty()) announce = java.util.Arrays.asList(30,10,3,2,1);

        String msgTpl = plugin.getConfig().getString("cleanup.message_template", "&e{world}&7 월드에서 {sec}초 후 바닥 아이템을 청소합니다.");
        String clearedTpl = plugin.getConfig().getString("cleanup.cleared_message", "&a{world}&7 월드 바닥 아이템 {count}개 제거 완료.");

        Set<EntityType> targets = new HashSet<EntityType>();
        List<String> typeNames = plugin.getConfig().getStringList("cleanup.target_entity_types");
        if (typeNames != null) {
            for (String t : typeNames) {
                try { targets.add(EntityType.valueOf(t)); } catch (IllegalArgumentException ignored) {}
            }
        }
        if (targets.isEmpty()) targets.add(EntityType.DROPPED_ITEM);

        // Make effectively final copies for inner class
        final World worldRef = world;
        final int seconds = totalSeconds;
        final List<Integer> announceFinal = new ArrayList<Integer>(announce);
        final String msgTplFinal = msgTpl;
        final String clearedTplFinal = clearedTpl;
        final Set<EntityType> targetsFinal = new HashSet<EntityType>(targets);

        new BukkitRunnable() {
            int remaining = seconds;
            @Override
            public void run() {
                if (remaining <= 0) {
                    int removed = 0;
                    for (Entity e : worldRef.getEntities()) {
                        if (targetsFinal.contains(e.getType())) {
                            e.remove();
                            removed++;
                        }
                    }
                    String done = ChatColor.translateAlternateColorCodes('&',
                            clearedTplFinal.replace("{world}", worldRef.getName()).replace("{count}", String.valueOf(removed)));
                    worldRef.getPlayers().forEach(p -> p.sendMessage(done));
                    cancel();
                    return;
                }

                if (announceFinal.contains(remaining)) {
                    String m = ChatColor.translateAlternateColorCodes('&',
                            msgTplFinal.replace("{world}", worldRef.getName()).replace("{sec}", String.valueOf(remaining)));
                    worldRef.getPlayers().forEach(p -> p.sendMessage(m));
                }
                remaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        sender.sendMessage(ChatColor.GREEN + "청소 카운트다운 시작: " + world.getName() + " (" + totalSeconds + "초)");
        return true;
    }
}
