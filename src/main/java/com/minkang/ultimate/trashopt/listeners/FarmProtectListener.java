package com.minkang.ultimate.trashopt.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class FarmProtectListener implements Listener {

    @EventHandler
    public void onTrample(EntityChangeBlockEvent e) {
        if (e.getBlock() == null) return;
        if (e.getBlock().getType() == Material.FARMLAND) {
            e.setCancelled(true);
        }
    }
}
