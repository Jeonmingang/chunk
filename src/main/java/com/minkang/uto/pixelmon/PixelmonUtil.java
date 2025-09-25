
package com.minkang.uto.pixelmon;

import com.minkang.uto.Main;
import com.minkang.uto.util.NmsUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class PixelmonUtil {

    public static boolean isPixelmon(Main plugin, Entity e) {
        List<String> contains = plugin.getConfig().getStringList("cleanup.pixelmon.detection.classNameContains");
        String lowerName = NmsUtil.tryGetHandleClassName(e).toLowerCase(Locale.ROOT);
        for (String s: contains) {
            if (lowerName.contains(s.toLowerCase(Locale.ROOT))) return true;
        }
        String pat = plugin.getConfig().getString("cleanup.pixelmon.namePattern", "(?i)(Lvl|Lv\\.|\\[\\d+\\])");
        if (e instanceof LivingEntity) {
            String name = ((LivingEntity)e).getCustomName();
            if (name != null && Pattern.compile(pat).matcher(ChatColor.stripColor(name)).find())
                return true;
        }
        return false;
    }

    public static boolean isLegendary(Main plugin, Entity e) {
        if (e instanceof LivingEntity) {
            String name = ((LivingEntity)e).getCustomName();
            if (name != null) {
                String s = ChatColor.stripColor(name);
                for (String lg : plugin.getConfig().getStringList("cleanup.pixelmon.legendarySpeciesWhitelist")) {
                    if (s.toLowerCase(Locale.ROOT).contains(lg.toLowerCase(Locale.ROOT))) {
                        return true;
                    }
                }
                for (String tag: e.getScoreboardTags()) {
                    if (tag.toLowerCase(Locale.ROOT).contains("legendary")) return true;
                }
            }
        }
        return false;
    }
}
