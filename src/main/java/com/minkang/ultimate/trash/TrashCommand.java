
package com.minkang.ultimate.trash;

import com.minkang.ultimate.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class TrashCommand implements CommandExecutor, org.bukkit.command.TabCompleter {
    private final Main plugin;
    public TrashCommand(Main plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix() + plugin.getConfig().getString("messages.not_player")));
            return true;
        }
        Player p = (Player) sender;
        if (!p.hasPermission("ultimate.trash.use")) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.prefix() + plugin.getConfig().getString("messages.no_permission")));
            return true;
        }

        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("trash.gui-title", "&8쓰레기통"));
        int rows = Math.max(1, Math.min(6, plugin.getConfig().getInt("trash.rows", 3)));
        Inventory inv = Bukkit.createInventory(p, rows*9, title);

        // put a 'key' item (won't be deleted)
        ItemStack key = new ItemStack(Material.BARRIER);
        ItemMeta meta = key.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "TRASH-KEY");
        NamespacedKey k = NamespacedKey.fromString(plugin.getConfig().getString("trash.pdc-key", "uto:trash_key"));
        if (k != null) {
            PersistentDataContainer c = meta.getPersistentDataContainer();
            c.set(k, PersistentDataType.INTEGER, 1);
        }
        key.setItemMeta(meta);
        inv.setItem(0, key);

        p.openInventory(inv);
        return true;
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return java.util.Collections.emptyList();
    }
}
