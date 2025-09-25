
package com.minkang.uto;

import com.minkang.uto.automation.AutomationCommand;
import com.minkang.uto.automation.WorldBorderAutomationManager;
import com.minkang.uto.cleanup.AutoCleanupManager;
import com.minkang.uto.cleanup.CleanupCommand;
import com.minkang.uto.farm.FarmProtectListener;
import com.minkang.uto.guard.ConditionGuard;
import com.minkang.uto.tps.TpsMonitor;
import com.minkang.uto.trash.TrashCommand;
import com.minkang.uto.trash.TrashListener;
import com.minkang.uto.optimize.OptimizeCommand;
import com.minkang.uto.commands.UtoCommand;
import com.minkang.uto.commands.ToggleAutoCleanupCommand;
import com.minkang.uto.commands.ToggleConditionsCommand;
import com.minkang.uto.commands.ConditionStatusCommand;
import com.minkang.uto.commands.ToggleFarmProtectCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private TpsMonitor tpsMonitor;
    private ConditionGuard conditionGuard;
    private AutoCleanupManager autoCleanupManager;
    private WorldBorderAutomationManager wbAutomation;

    public static Main getInstance() { return instance; }
    public TpsMonitor getTpsMonitor() { return tpsMonitor; }
    public ConditionGuard getConditionGuard() { return conditionGuard; }
    public AutoCleanupManager getAutoCleanupManager() { return autoCleanupManager; }
    public WorldBorderAutomationManager getWbAutomation() { return wbAutomation; }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Register listeners
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new TrashListener(this), this);
        pm.registerEvents(new FarmProtectListener(this), this);

        // Register commands + tab
        register("trash", new TrashCommand(this));
        register("optimize", new OptimizeCommand(this));
        register("cleanup", new CleanupCommand(this));
        register("automation", new AutomationCommand(this));
        register("farmprotect", new ToggleFarmProtectCommand(this));
        register("conditions", new ToggleConditionsCommand(this));
        register("conditionstatus", new ConditionStatusCommand(this));
        register("uto", new UtoCommand(this));

        // Core services
        this.tpsMonitor = new TpsMonitor(this);
        this.tpsMonitor.start();

        this.conditionGuard = new ConditionGuard(this);

        this.autoCleanupManager = new AutoCleanupManager(this);
        this.autoCleanupManager.initFromConfig();

        this.wbAutomation = new WorldBorderAutomationManager(this);
        this.wbAutomation.initFromConfig();

        getLogger().info("[UTO] Enabled v" + getDescription().getVersion());
        getLogger().info("[UTO] Detected Plugins -> Chunky=" + (pm.getPlugin("Chunky")!=null)
                + ", WorldBorder=" + (pm.getPlugin("WorldBorder")!=null)
                + ", Starlight=" + (pm.getPlugin("Starlight")!=null));
    }

    @Override
    public void onDisable() {
        // Ensure Trash GUI closed
        String title = ChatColor.translateAlternateColorCodes('&',
                getConfig().getString("trash.gui-title", "&8쓰레기통"));
        for (Player p : Bukkit.getOnlinePlayers()) {
            try {
                String openTitle = (p.getOpenInventory()!=null ? p.getOpenInventory().getTitle() : "");
                if (openTitle != null &&
                        ChatColor.stripColor(openTitle)
                                .equals(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', title)))) {
                    p.closeInventory();
                }
            } catch (Throwable ignored) {}
        }

        if (tpsMonitor != null) tpsMonitor.stop();
        if (autoCleanupManager != null) autoCleanupManager.stopAll();
        if (wbAutomation != null) wbAutomation.stop();

        getLogger().info("[UTO] Disabled");
    }

    private void register(String name, Object executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            if (executor instanceof org.bukkit.command.CommandExecutor) {
                cmd.setExecutor((org.bukkit.command.CommandExecutor) executor);
            }
            if (executor instanceof org.bukkit.command.TabCompleter) {
                cmd.setTabCompleter((org.bukkit.command.TabCompleter) executor);
            }
        } else {
            getLogger().warning("[UTO] Command not found in plugin.yml: " + name);
        }
    }

    public String prefix() {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.prefix", "&6[UTO]&r "));
    }
}
