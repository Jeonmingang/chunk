
package com.minkang.ultimate.trash;

import com.minkang.ultimate.Main;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class TrashListener implements Listener {
    private final Main plugin;
    public TrashListener(Main plugin) { this.plugin = plugin; }

    private boolean isTrashInv(Inventory inv) {
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("trash.gui-title", "&8쓰레기통"));
        try {
            String t = inv.getViewers().isEmpty()? "" : inv.getViewers().get(0).getOpenInventory().getTitle();
            if (t == null) return false;
            return ChatColor.stripColor(t).equals(ChatColor.stripColor(title));
        } catch (Throwable t) {
            return false;
        }
    }

    private boolean isKey(ItemStack it) {
        if (it == null) return false;
        if (!it.hasItemMeta()) return false;
        ItemMeta meta = it.getItemMeta();
        String raw = plugin.getConfig().getString("trash.pdc-key", "uto:trash_key");
        NamespacedKey k = NamespacedKey.fromString(raw);
        if (k == null) return false;
        Integer v = meta.getPersistentDataContainer().get(k, PersistentDataType.INTEGER);
        return v != null && v == 1;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Inventory inv = e.getInventory();
        if (!isTrashInv(inv)) return;

        if (plugin.getConfig().getBoolean("trash.block-shift-click", true)) {
            if (e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) {
                e.setCancelled(true);
                return;
            }
        }
        if (plugin.getConfig().getBoolean("trash.block-number-key", true)) {
            if (e.getClick() == ClickType.NUMBER_KEY) {
                e.setCancelled(true);
                return;
            }
        }
        ItemStack cur = e.getCurrentItem();
        if (isKey(cur)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Inventory inv = e.getInventory();
        if (!isTrashInv(inv)) return;
        if (!plugin.getConfig().getBoolean("trash.destroy-on-close", true)) return;

        for (int i=0;i<inv.getSize();i++) {
            ItemStack it = inv.getItem(i);
            if (it == null) continue;
            if (isKey(it)) continue; // keep key (auto-removed by inventory close anyway)
            inv.setItem(i, null); // delete
        }
        HumanEntity p = e.getPlayer();
        if (p instanceof Player) {
            ((Player)p).sendMessage(plugin.prefix() + ChatColor.GREEN + "쓰레기통 비우기 완료");
        }
    }
}
