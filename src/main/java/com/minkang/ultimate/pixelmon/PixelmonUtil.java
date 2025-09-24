
package com.minkang.ultimate.pixelmon;

import com.minkang.ultimate.Main;
import com.minkang.ultimate.util.NmsUtil;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PixelmonUtil {

    public static boolean isPixelmon(Main plugin, Entity e) {
        // Try by class name
        List<String> contains = plugin.getConfig().getStringList("cleanup.pixelmon.detection.classNameContains");
        String lowerName = NmsUtil.tryGetHandleClassName(e).toLowerCase(Locale.ROOT);
        for (String s: contains) {
            if (lowerName.contains(s.toLowerCase(Locale.ROOT))) return true;
        }
        // Fallback by custom name pattern
        String pat = plugin.getConfig().getString("cleanup.pixelmon.detection.namePattern", "(?i)(Lvl|Lv\\.|\\[\\d+\\])");
        if (e instanceof LivingEntity) {
            String name = ((LivingEntity)e).getCustomName();
            if (name != null && Pattern.compile(pat).matcher(ChatColor.stripColor(name)).find())
                return true;
        }
        return false;
    }

    public static boolean isLegendary(Main plugin, Entity e) {
        // Heuristic: custom name contains any whitelisted legendary species
        if (e instanceof LivingEntity) {
            String name = ((LivingEntity)e).getCustomName();
            if (name != null) {
                String s = ChatColor.stripColor(name);
                for (String lg : plugin.getConfig().getStringList("cleanup.pixelmon.legendarySpeciesWhitelist")) {
                    if (s.toLowerCase(Locale.ROOT).contains(lg.toLowerCase(Locale.ROOT))) {
                        return true;
                    }
                }
                // scoreboard tags may include 'legendary'
                for (String tag: e.getScoreboardTags()) {
                    if (tag.toLowerCase(Locale.ROOT).contains("legendary")) return true;
                }
            }
        }
        return false;
    }
}
