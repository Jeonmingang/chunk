package com.minkang.ultimate.trashopt;

import com.minkang.ultimate.trashopt.commands.*;
import com.minkang.ultimate.trashopt.listeners.FarmProtectListener;
import com.minkang.ultimate.trashopt.listeners.TrashListener;
import com.minkang.ultimate.trashopt.util.TpsMonitor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private NamespacedKey openerKey;
    private TpsMonitor tpsMonitor;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        openerKey = new NamespacedKey(this, "trash_opener");

        // Start TPS monitor
        tpsMonitor = new TpsMonitor(this);
        tpsMonitor.start();

        // Commands
        getCommand("trash").setExecutor(new TrashCommand(this));
        getCommand("쓰레기통").setExecutor(new TrashCommand(this));
        getCommand("optimize").setExecutor(new OptimizeCommand(this));
        getCommand("최적화").setExecutor(new OptimizeCommand(this));
        getCommand("cleanup").setExecutor(new CleanupCommand(this));
        getCommand("청소").setExecutor(new CleanupCommand(this));
        getCommand("조건").setExecutor(new ToggleConditionsCommand(this));
        getCommand("조건상태").setExecutor(new ToggleConditionsCommand(this));

        // Listeners
        Bukkit.getPluginManager().registerEvents(new TrashListener(this), this);
        Bukkit.getPluginManager().registerEvents(new FarmProtectListener(), this);

        getLogger().info("[UltimateTrashOptimize] Enabled v" + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        if (tpsMonitor != null) tpsMonitor.stop();
        getLogger().info("[UltimateTrashOptimize] Disabled.");
    }

    public static Main getInstance() {
        return instance;
    }

    public NamespacedKey getOpenerKey() {
        return openerKey;
    }

    public TpsMonitor getTpsMonitor() {
        return tpsMonitor;
    }
}
