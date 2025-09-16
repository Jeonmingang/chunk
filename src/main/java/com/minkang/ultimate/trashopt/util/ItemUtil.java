package com.minkang.ultimate.trashopt.util;

import com.minkang.ultimate.trashopt.Main;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ItemUtil {

    public static ItemStack markAsTrashOpener(ItemStack base) {
        if (base == null || base.getType() == Material.AIR) return null;

        ItemStack clone = base.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta == null) return null;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(Main.getInstance().getOpenerKey(), PersistentDataType.BYTE, (byte)1);

        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<String>();
        lore.add("§7이 아이템을 §e우클릭§7하면 §c쓰레기통§7이 열립니다.");
        meta.setLore(lore);
        meta.setDisplayName("§c쓰레기통 열쇠");
        clone.setItemMeta(meta);
        return clone;
    }

    public static boolean isTrashOpener(ItemStack item) {
        if (item == null) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Byte b = pdc.get(Main.getInstance().getOpenerKey(), PersistentDataType.BYTE);
        return b != null && b.byteValue() == (byte)1;
    }
}
