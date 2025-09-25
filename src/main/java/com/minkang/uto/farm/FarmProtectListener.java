
package com.minkang.uto.farm;

import com.minkang.uto.Main;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class FarmProtectListener implements Listener {
    private final Main plugin;
    public FarmProtectListener(Main plugin){ this.plugin=plugin; }

    @EventHandler
    public void onTrample(PlayerInteractEvent e) {
        if (!plugin.getConfig().getBoolean("farm.protect.enabled", true)) return;
        if (e.getAction() != Action.PHYSICAL) return;
        if (e.getClickedBlock() == null) return;
        if (e.getClickedBlock().getType() == Material.FARMLAND) {
            e.setCancelled(true);
        }
    }
}
