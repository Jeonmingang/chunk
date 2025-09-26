package com.minkang.uto.commands;

import com.minkang.uto.Main;
import com.minkang.uto.integrations.ChunkyIntegration;
import com.minkang.uto.integrations.WorldBorderIntegration;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OptimizeCommand implements CommandExecutor, TabCompleter {
    private final Main plugin;
    public OptimizeCommand(Main plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(plugin.color(plugin.prefix() + "&e/최적화 리로드 &7- 설정 리로드"));
            sender.sendMessage(plugin.color(plugin.prefix() + "&e/최적화 상태 &7- 상태표시"));
            sender.sendMessage(plugin.color(plugin.prefix() + "&e/최적화 청키 시작 [월드] &7- Chunky 경계 내 프리젠 시작"));
            return true;
        }
        String sub = args[0];
        if ("리로드".equals(sub)) {
            plugin.reloadAll();
            sender.sendMessage(plugin.color(plugin.prefix() + "&a리로드 완료."));
            if (plugin.getConfig().getBoolean("integrations.chunky.startOnReload", false)) {
                World w = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
                ChunkyIntegration.startBorderPregen(plugin, w);
            }
            return true;
        }
        if ("상태".equals(sub)) {
            sender.sendMessage(plugin.color(plugin.prefix() + "&7자동 청소/픽셀몬 청소가 동작 중입니다."));
            return true;
        }
        if ("청키".equals(sub) && args.length >= 2 && "시작".equals(args[1])) {
            World w = null;
            if (args.length >= 3) w = Bukkit.getWorld(args[2]);
            if (w == null && !Bukkit.getWorlds().isEmpty()) w = Bukkit.getWorlds().get(0);
            ChunkyIntegration.startBorderPregen(plugin, w);
            sender.sendMessage(plugin.color(plugin.prefix() + "&aChunky 프리젠 시작 요청."));
            return true;
        }
        sender.sendMessage(plugin.color(plugin.prefix() + "&c사용법: /최적화 <리로드|상태|청키 시작 [월드]>"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<String>();
        if (args.length == 1) {
            out.add("리로드"); out.add("상태"); out.add("청키");
        } else if (args.length == 2 && "청키".equals(args[0])) {
            out.add("시작");
        } else if (args.length == 3 && "청키".equals(args[0]) && "시작".equals(args[1])) {
            for (World w : Bukkit.getWorlds()) out.add(w.getName());
        }
        return out;
    }
}
