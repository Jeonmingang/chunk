package com.minkang.ultimate.trashopt.commands;

import com.minkang.ultimate.trashopt.Main;
import com.minkang.ultimate.trashopt.util.CleanupUtil;
import com.minkang.ultimate.trashopt.util.ConditionGuard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

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
        if (announce == null || announce.isEmpty()) announce = java.util.Arrays.asList(30,10,5,4,3,2,1);

        String msgTpl = plugin.getConfig().getString("cleanup.message_template", "&e{world}&7 월드에서 {sec}초 후 바닥 아이템을 청소합니다.");
        String clearedTpl = plugin.getConfig().getString("cleanup.cleared_message", "&a{world}&7 월드 바닥 아이템 {count}개 제거 완료.");

        Set<EntityType> targets = new HashSet<EntityType>();
        java.util.List<String> typeNames = plugin.getConfig().getStringList("cleanup.target_entity_types");
        if (typeNames != null) {
            for (String t : typeNames) {
                try { targets.add(org.bukkit.entity.EntityType.valueOf(t)); } catch (IllegalArgumentException ignored) {}
            }
        }
        if (targets.isEmpty()) targets.add(EntityType.DROPPED_ITEM);

        CleanupUtil.startCountdownCleanup(plugin, world, totalSeconds, announce, msgTpl, clearedTpl, targets);
        sender.sendMessage(ChatColor.GREEN + "청소 카운트다운 시작: " + world.getName() + " (" + totalSeconds + "초)");
        return true;
    }
}
