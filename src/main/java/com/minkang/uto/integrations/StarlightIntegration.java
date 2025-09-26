package com.minkang.uto.integrations;

import com.minkang.uto.Main;
import org.bukkit.Bukkit;

public class StarlightIntegration {
    public static boolean isPresent() { return Bukkit.getPluginManager().getPlugin("Starlight") != null; }
    public static void log(Main plugin) { if (isPresent()) plugin.getLogger().info("Starlight 감지됨 - 서버 조명 최적화 활성."); }
}