package com.minkang.uto;

import com.minkang.uto.cleanup.AutoCleanupManager;
import com.minkang.uto.commands.OptimizeCommand;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private AutoCleanupManager cleanupManager;
    private String prefix;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadPrefix();
        this.cleanupManager = new AutoCleanupManager(this);
        this.cleanupManager.initFromConfig();

        OptimizeCommand cmd = new OptimizeCommand(this);
        getCommand(\"최적화\").setExecutor(cmd);
        getCommand(\"최적화\").setTabCompleter(cmd);

        getLogger().info(\"UltimateTrashOptimize enabled.\");
    }

    @Override
    public void onDisable() {
        if (cleanupManager != null) cleanupManager.stopAll();
    }

    public void reloadAll() {
        reloadConfig();
        reloadPrefix();
        if (cleanupManager != null) cleanupManager.initFromConfig();
    }

    private void reloadPrefix() {
        FileConfiguration cfg = getConfig();
        this.prefix = ChatColor.translateAlternateColorCodes('&', cfg.getString(\"prefix\", \"&7[&b최적화&7]&r \"));
    }

    public String prefix() { return prefix; }
    public String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    public AutoCleanupManager getCleanupManager() { return cleanupManager; }
}
