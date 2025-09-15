package com.minkang.ultimate.trashopt.util;

import com.minkang.ultimate.trashopt.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AutoCleanupManager {

    private final Main plugin;
    private BukkitTask task;

    public AutoCleanupManager(Main plugin) {
        this.plugin = plugin;
    }

    public void startFromConfig() {
        stop();
        if (!plugin.getConfig().getBoolean("cleanup.auto.enabled", false)) {
            return;
        }
        long intervalMinutes = plugin.getConfig().getLong("cleanup.auto.interval_minutes", 15);
        if (intervalMinutes < 1) intervalMinutes = 1;

        final int seconds = plugin.getConfig().getInt("cleanup.auto.seconds", 30);
        final List<Integer> announce = plugin.getConfig().getIntegerList("cleanup.auto.announce_seconds");
        final boolean checkConditions = plugin.getConfig().getBoolean("cleanup.auto.check_conditions", true);

        List<String> worldList = plugin.getConfig().getStringList("cleanup.auto.worlds");
        if (worldList == null || worldList.isEmpty()) {
            worldList = plugin.getConfig().getStringList("cleanup.worlds");
        }

        final List<String> worldsFinal = new ArrayList<String>(worldList);
        final List<Integer> announceFinal = (announce == null || announce.isEmpty())
                ? java.util.Arrays.asList(30,10,3,2,1)
                : new ArrayList<Integer>(announce);
        final String msgTpl = plugin.getConfig().getString("cleanup.message_template", "&e{world}&7 월드에서 {sec}초 후 바닥 아이템을 청소합니다.");
        final String clearedTpl = plugin.getConfig().getString("cleanup.cleared_message", "&a{world}&7 월드 바닥 아이템 {count}개 제거 완료.");
        final Set<EntityType> targets = loadTargets();

        long periodTicks = intervalMinutes * 60L * 20L;
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                for (String wName : worldsFinal) {
                    World w = Bukkit.getWorld(wName);
                    if (w == null) continue;
                    if (checkConditions) {
                        if (!ConditionGuard.checkAllowed(Bukkit.getConsoleSender(), "자동 월드 청소(" + wName + ")")) {
                            continue;
                        }
                    }
                    CleanupUtil.startCountdownCleanup(plugin, w, seconds, announceFinal, msgTpl, clearedTpl, targets);
                }
            }
        }, 20L, periodTicks);
    }

    private Set<EntityType> loadTargets() {
        Set<EntityType> targets = new HashSet<EntityType>();
        List<String> typeNames = plugin.getConfig().getStringList("cleanup.target_entity_types");
        if (typeNames != null) {
            for (String t : typeNames) {
                try { targets.add(EntityType.valueOf(t)); } catch (IllegalArgumentException ignored) {}
            }
        }
        if (targets.isEmpty()) targets.add(EntityType.DROPPED_ITEM);
        return targets;
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void rescheduleFromConfig() {
        startFromConfig();
    }
}
