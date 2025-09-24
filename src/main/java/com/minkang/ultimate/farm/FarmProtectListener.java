
package com.minkang.ultimate.farm;

import com.minkang.ultimate.Main;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.Player;
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
        Material m = e.getClickedBlock().getType();
        if (m == Material.FARMLAND) {
            e.setCancelled(true);
        }
    }
}
