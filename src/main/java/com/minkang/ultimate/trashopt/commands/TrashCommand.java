package com.minkang.ultimate.trashopt.commands;

import com.minkang.ultimate.trashopt.Main;
import com.minkang.ultimate.trashopt.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class TrashCommand implements CommandExecutor {

    private final Main plugin;

    public TrashCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("플레이어만 사용 가능합니다.");
            return true;
        }
        Player p = (Player) sender;

        if (args.length == 0) {
            openTrash(p);
            return true;
        }

        String sub = args[0];

        if ("설정".equalsIgnoreCase(sub)) {
            if (!p.hasPermission("ultimate.trash.admin")) {
                p.sendMessage(ChatColor.RED + "권한이 없습니다.");
                return true;
            }
            ItemStack inHand = p.getInventory().getItemInMainHand();
            if (inHand == null || inHand.getType() == Material.AIR) {
                p.sendMessage(ChatColor.RED + "손에 아이템을 들고 입력하세요.");
                return true;
            }
            ItemStack opener = ItemUtil.markAsTrashOpener(inHand);
            if (opener == null) {
                p.sendMessage(ChatColor.RED + "아이템 설정에 실패했습니다.");
                return true;
            }
            p.getInventory().setItemInMainHand(opener);
            p.sendMessage(ChatColor.GOLD + "이 아이템을 우클릭하면 쓰레기통 GUI가 열립니다.");
            return true;
        }

        if ("지급".equalsIgnoreCase(sub)) {
            if (!p.hasPermission("ultimate.trash.admin")) {
                p.sendMessage(ChatColor.RED + "권한이 없습니다.");
                return true;
            }
            if (args.length < 2) {
                p.sendMessage(ChatColor.YELLOW + "사용법: /" + label + " 지급 <닉네임>");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                p.sendMessage(ChatColor.RED + "해당 플레이어를 찾을 수 없습니다.");
                return true;
            }
            ItemStack base = new ItemStack(Material.PAPER, 1);
            ItemStack opener = ItemUtil.markAsTrashOpener(base);
            if (opener == null) {
                p.sendMessage(ChatColor.RED + "지급 실패.");
                return true;
            }
            target.getInventory().addItem(opener);
            p.sendMessage(ChatColor.GREEN + target.getName() + " 님에게 쓰레기통을 지급했습니다.");
            return true;
        }

        p.sendMessage(ChatColor.YELLOW + "사용법: /" + label + " [설정|지급]");
        return true;
    }

    private void openTrash(Player p) {
        int size = plugin.getConfig().getInt("trash.size", 54);
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("trash.gui-title", "쓰레기통"));
        if (size < 9) size = 9;
        if (size > 54) size = 54;
        if (size % 9 != 0) size = 54;

        Inventory inv = Bukkit.createInventory(p, size, title);
        p.openInventory(inv);
    }
}
