package com.minkang.uto.integrations;

import com.minkang.uto.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.ConsoleCommandSender;

public class ChunkyIntegration {

    public static void startBorderPregen(Main plugin, World world) {
        if (world == null) return;
        if (!plugin.getConfig().getBoolean("integrations.chunky.enabled", true)) return;
        if (Bukkit.getPluginManager().getPlugin("Chunky") == null) {
            plugin.getLogger().warning("Chunky 플러그인을 찾지 못했습니다.");
            return;
        }
        WorldBorder wb = world.getWorldBorder();
        double cx = wb.getCenter().getX();
        double cz = wb.getCenter().getZ();
        long radius = Math.round(wb.getSize() / 2.0); // size는 지름, radius는 반지름 추정

        ConsoleCommandSender console = Bukkit.getConsoleSender();
        // Chunky 명령 프로토콜 (1.2.x 기준 일반적인 시퀀스)
        Bukkit.dispatchCommand(console, "chunky world " + world.getName());
        Bukkit.dispatchCommand(console, "chunky center " + (int)Math.round(cx) + " " + (int)Math.round(cz));
        Bukkit.dispatchCommand(console, "chunky radius " + radius);
        Bukkit.dispatchCommand(console, "chunky shape circle");
        Bukkit.dispatchCommand(console, "chunky start");
        plugin.getLogger().info("Chunky pregen started for world " + world.getName() + " with radius " + radius);
    }
}
