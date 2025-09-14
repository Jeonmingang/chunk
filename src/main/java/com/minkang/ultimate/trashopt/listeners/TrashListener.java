package com.minkang.ultimate.trashopt.listeners;

import com.minkang.ultimate.trashopt.Main;
import com.minkang.ultimate.trashopt.util.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class TrashListener implements Listener {

    private final Main plugin;

    public TrashListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        if (!ItemUtil.isTrashOpener(e.getItem())) return;
        e.getPlayer().performCommand("trash");
        e.setCancelled(true);
    }

    @EventHandler
    public void onTrashClose(InventoryCloseEvent e) {
        String title = ChatColor.stripColor(e.getView().getTitle());
        String confTitle = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("trash.gui-title", "쓰레기통")));
        if (title == null || confTitle == null) return;
        if (!title.equalsIgnoreCase(confTitle)) return;
        Inventory inv = e.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, null);
        }
    }

    @EventHandler
    public void onTrashClick(InventoryClickEvent e) {
        String title = ChatColor.stripColor(e.getView().getTitle());
        String confTitle = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("trash.gui-title", "쓰레기통")));
        if (title == null || confTitle == null) return;
        if (!title.equalsIgnoreCase(confTitle)) return;

        ItemStack cursor = e.getCursor();
        ItemStack current = e.getCurrentItem();
        if (cursor != null && ItemUtil.isTrashOpener(cursor)) {
            e.setCancelled(true);
            return;
        }
        if (current != null && ItemUtil.isTrashOpener(current)) {
            e.setCancelled(true);
        }
    }
}
