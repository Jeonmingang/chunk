package com.minkang.uto.integrations;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;

public class WorldBorderIntegration {
    public static boolean hasPlugin() { return Bukkit.getPluginManager().getPlugin("WorldBorder") != null; }
    public static double getRadius(World world) { WorldBorder wb = world.getWorldBorder(); return wb.getSize() / 2.0; }
}