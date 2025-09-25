
package com.minkang.uto.automation;

import com.minkang.uto.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class WorldBorderAutomationManager {
    private final Main plugin;
    private boolean enabled;
    private boolean dryRun;
    private int playerThreshold;
    private BukkitRunnable task;
    private final Map<String, Integer> radiusState = new HashMap<>();

    public WorldBorderAutomationManager(Main plugin) { this.plugin = plugin; }

    public void initFromConfig() {
        stop();
        try {
            enabled = plugin.getConfig().getBoolean("automation.worldborder.enabled", false);
            dryRun = plugin.getConfig().getBoolean("automation.worldborder.dryRun", false);
            playerThreshold = plugin.getConfig().getInt("automation.worldborder.player_threshold", 0);
        } catch (Throwable t) {
            enabled = false; dryRun = false; playerThreshold = 0;
        }
        if (!enabled) return;
        task = new BukkitRunnable() {
            @Override public void run() {
                if (playerThreshold > 0 && Bukkit.getOnlinePlayers().size() < playerThreshold) return;
                ConfigurationSection sec = plugin.getConfig().getConfigurationSection("automation.worldborder.worlds");
                if (sec == null) return;
                for (String wname : sec.getKeys(false)) {
                    World world = Bukkit.getWorld(wname);
                    if (world == null) continue;
                    int inc = sec.getInt(wname + ".increment", 100);
                    int max = sec.getInt(wname + ".max_radius", 10000);
                    int cur = radiusState.getOrDefault(wname, 0);
                    if (cur == 0) cur = Math.min(1000, inc);
                    if (cur >= max) continue;

                    int next = Math.min(cur + inc, max);
                    radiusState.put(wname, next);

                    if (!dryRun) {
                        if (Bukkit.getPluginManager().getPlugin("WorldBorder") != null) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb " + wname + " set " + next + " spawn");
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb " + wname + " fill");
                        }
                        if (Bukkit.getPluginManager().getPlugin("Chunky") != null) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky world " + wname);
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky radius " + next);
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky start");
                        }
                    } else {
                        plugin.getLogger().info("[WB][DRYRUN] " + wname + " -> radius " + next + " (max " + max + ")");
                    }
                }
            }
        };
        task.runTaskTimer(plugin, 20L, 20L * 60);
    }

    public void stop() { if (task != null) task.cancel(); }

    public void stopNow() { stop(); }

    public String statusLine(String world) {
        int r = radiusState.getOrDefault(world, 0);
        int max = plugin.getConfig().getInt("automation.worldborder.worlds." + world + ".max_radius", 0);
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.automation_status",
                        "&e[WB] %world% 반경 %radius% / 최대 %max% (드라이런: %dryrun%)")
                        .replace("%world%", world)
                        .replace("%radius%", String.valueOf(r))
                        .replace("%max%", String.valueOf(max))
                        .replace("%dryrun%", String.valueOf(dryRun)));
    }
}
