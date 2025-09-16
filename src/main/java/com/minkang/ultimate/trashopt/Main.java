package com.minkang.ultimate.trashopt;

import com.minkang.ultimate.trashopt.commands.*;
import com.minkang.ultimate.trashopt.listeners.FarmProtectListener;
import com.minkang.ultimate.trashopt.listeners.TrashListener;
import com.minkang.ultimate.trashopt.util.AutoCleanupManager;
import com.minkang.ultimate.trashopt.util.WorldBorderAutomationManager;
import com.minkang.ultimate.trashopt.util.TpsMonitor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private NamespacedKey openerKey;
    private TpsMonitor tpsMonitor;
    private AutoCleanupManager autoCleanupManager;
    private WorldBorderAutomationManager wbAutomation;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        openerKey = new NamespacedKey(this, "trash_opener");

        // TPS monitor
        tpsMonitor = new TpsMonitor(this);
        tpsMonitor.start();

        // Commands (only primary labels; aliases in plugin.yml are handled automatically)
        if (getCommand("trash") != null) getCommand("trash").setExecutor(new TrashCommand(this));
        if (getCommand("쓰레기통") != null) getCommand("쓰레기통").setExecutor(new TrashCommand(this));
        if (getCommand("optimize") != null) getCommand("optimize").setExecutor(new OptimizeCommand(this));
        if (getCommand("cleanup") != null) getCommand("cleanup").setExecutor(new CleanupCommand(this));
        if (getCommand("조건") != null) getCommand("조건").setExecutor(new ToggleConditionsCommand(this));
        if (getCommand("조건상태") != null) getCommand("조건상태").setExecutor(new ToggleConditionsCommand(this));
        if (getCommand("청소자동") != null) getCommand("청소자동").setExecutor(new ToggleAutoCleanupCommand(this));
        if (getCommand("농장보호") != null) getCommand("농장보호").setExecutor(new ToggleFarmProtectCommand(this));
        if (getCommand("자동화") != null) getCommand("자동화").setExecutor(new AutomationCommand(this));

        // Listeners
        Bukkit.getPluginManager().registerEvents(new TrashListener(this), this);
        Bukkit.getPluginManager().registerEvents(new FarmProtectListener(this), this);

        // Managers
        autoCleanupManager = new AutoCleanupManager(this);
        autoCleanupManager.startFromConfig();

        wbAutomation = new WorldBorderAutomationManager(this);
        wbAutomation.start();

        getLogger().info("[UltimateTrashOptimize] Enabled v" + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        if (tpsMonitor != null) tpsMonitor.stop();
        if (autoCleanupManager != null) autoCleanupManager.stop();
        if (wbAutomation != null) wbAutomation.stop();
        getLogger().info("[UltimateTrashOptimize] Disabled.");
    }

    public static Main getInstance() { return instance; }
    public NamespacedKey getOpenerKey() { return openerKey; }
    public TpsMonitor getTpsMonitor() { return tpsMonitor; }
    public AutoCleanupManager getAutoCleanupManager() { return autoCleanupManager; }
    public WorldBorderAutomationManager getWorldBorderAutomationManager() { return wbAutomation; }
}
