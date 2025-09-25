package com.minkang.uto;

import com.minkang.uto.cleanup.AutoCleanupManager;
import com.minkang.uto.commands.UtoCommand;
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
        getCommand("uto").setExecutor(new UtoCommand(this));
        getLogger().info("UltimateTrashOptimize enabled.");
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
        this.prefix = ChatColor.translateAlternateColorCodes('&', cfg.getString("prefix", "&7[&bUTO&7]&r "));
    }

    public String prefix() { return prefix; }

    public String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
