package com.minkang.ultimate.trashopt.listeners;

import com.minkang.ultimate.trashopt.Main;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class FarmProtectListener implements Listener {

    private final Main plugin;

    public FarmProtectListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTrample(EntityChangeBlockEvent e) {
        if (!plugin.getConfig().getBoolean("farm_protect.enabled", true)) return;
        if (e.getBlock() == null) return;
        if (e.getBlock().getType() == Material.FARMLAND) {
            e.setCancelled(true);
        }
    }
}
