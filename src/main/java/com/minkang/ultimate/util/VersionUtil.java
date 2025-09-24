
package com.minkang.ultimate.util;

import org.bukkit.Bukkit;

public class VersionUtil {
    public static String getNmsVersion() {
        // org.bukkit.craftbukkit.v1_16_R3...
        String name = Bukkit.getServer().getClass().getPackage().getName();
        String version = name.substring(name.lastIndexOf('.') + 1);
        return version;
    }
}
