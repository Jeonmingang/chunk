
package com.minkang.uto.util;

import org.bukkit.Bukkit;

public class VersionUtil {
    public static String getNmsVersion() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }
}
