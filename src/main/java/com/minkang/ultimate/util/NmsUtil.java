
package com.minkang.ultimate.util;
import org.bukkit.entity.Entity;
import java.lang.reflect.Method;
import java.util.Locale;
public class NmsUtil {
    public static String tryGetHandleClassName(Entity e) {
        try {
            String ver = com.minkang.ultimate.util.VersionUtil.getNmsVersion();
            Class<?> craft = Class.forName("org.bukkit.craftbukkit." + ver + ".entity.CraftEntity");
            if (!craft.isInstance(e)) return "";
            Method getHandle = craft.getMethod("getHandle");
            Object handle = getHandle.invoke(e);
            return handle.getClass().getName();
        } catch (Throwable t) {
            return "";
        }
    }
    public static boolean classNameContains(Entity e, String keyword) {
        String n = tryGetHandleClassName(e);
        if (n == null) return false;
        return n.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }
}
