package com.minkang.uto.commands;

import com.minkang.uto.Main;
import com.minkang.uto.cleanup.AutoCleanupManager;
import com.minkang.uto.integrations.ChunkyIntegration;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import java.util.ArrayList;
import java.util.List;

public class OptimizeCommand implements CommandExecutor, TabCompleter {
    private final Main plugin;
    public OptimizeCommand(Main plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        AutoCleanupManager mgr = plugin.getCleanupManager();

        if (args.length == 0) {
            sender.sendMessage(plugin.color(plugin.prefix()+\"&e/최적화 리로드 &7- 설정 리로드\"));
            sender.sendMessage(plugin.color(plugin.prefix()+\"&e/최적화 상태 &7- 상태표시\"));
            sender.sendMessage(plugin.color(plugin.prefix()+\"&e/최적화 자동 실행|취소 &7- 새벽/인원 조건 기반 자동 실행 토글\"));
            sender.sendMessage(plugin.color(plugin.prefix()+\"&e/최적화 바닥청소 on|off &7- 자동화 ON/OFF\"));
            sender.sendMessage(plugin.color(plugin.prefix()+\"&e/최적화 바닥청소 <월드> &7- 해당 월드만 1회 실행 예약\"));
            sender.sendMessage(plugin.color(plugin.prefix()+\"&e/최적화 픽셀몬 on|off &7- 자동화 ON/OFF\"));
            sender.sendMessage(plugin.color(plugin.prefix()+\"&e/최적화 픽셀몬 <월드> &7- 해당 월드만 1회 실행 예약\"));
            sender.sendMessage(plugin.color(plugin.prefix()+\"&e/최적화 청키 시작 [월드] &7- WorldBorder 기반 프리젠\"));
            return true;
        }

        String sub = args[0];

        if (\"리로드\".equals(sub)) {
            plugin.reloadAll();
            sender.sendMessage(plugin.color(plugin.prefix()+\"&a리로드 완료.\"));
            if (plugin.getConfig().getBoolean(\"integrations.chunky.startOnReload\", false)) {
                World w = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
                ChunkyIntegration.startBorderPregen(plugin, w);
            }
            return true;
        }

        if (\"상태\".equals(sub)) {
            sender.sendMessage(plugin.color(plugin.prefix()+\"&7자동화 \"
                    + (plugin.getConfig().getBoolean(\"auto.enabled\", true) ? \"활성\" : \"비활성\")
                    + \" / 현재온라인:\" + Bukkit.getOnlinePlayers().size()));
            return true;
        }

        if (\"자동\".equals(sub)) {
            if (args.length >= 2 && \"실행\".equals(args[1])) {
                mgr.setAutoEnabledRuntime(true);
                sender.sendMessage(plugin.color(plugin.prefix()+\"&a자동 최적화 실행 (조건 충족 시 카운트다운 후 동작)\")); return true;
            }
            if (args.length >= 2 && \"취소\".equals(args[1])) {
                mgr.setAutoEnabledRuntime(false);
                sender.sendMessage(plugin.color(plugin.prefix()+\"&e자동 최적화 취소 (다음 스케줄/카운트다운 중지)\")); return true;
            }
            sender.sendMessage(plugin.color(plugin.prefix()+\"&e/최적화 자동 실행 &7| &e/최적화 자동 취소\")); return true;
        }

        if (\"바닥청소\".equals(sub)) {
            if (args.length >= 2) {
                if (\"on\".equalsIgnoreCase(args[1])) { mgr.setGroundEnabledRuntime(true);  sender.sendMessage(plugin.color(plugin.prefix()+\"&a바닥청소 자동화 ON\")); return true; }
                if (\"off\".equalsIgnoreCase(args[1])){ mgr.setGroundEnabledRuntime(false); sender.sendMessage(plugin.color(plugin.prefix()+\"&e바닥청소 자동화 OFF\")); return true; }
                World w = Bukkit.getWorld(args[1]);
                if (w != null) { mgr.triggerGroundOnce(java.util.Arrays.asList(w), plugin.getConfig().getInt(\"auto.countdownSeconds\", 60));
                    sender.sendMessage(plugin.color(plugin.prefix()+\"&a바닥청소 1회 실행 예약(월드: \"+w.getName()+\")\")); return true; }
            }
            sender.sendMessage(plugin.color(plugin.prefix()+\"&e/최적화 바닥청소 on|off &7또는 &e/최적화 바닥청소 <월드>\")); return true;
        }

        if (\"픽셀몬\".equals(sub)) {
            if (args.length >= 2) {
                if (\"on\".equalsIgnoreCase(args[1])) { mgr.setPixelEnabledRuntime(true);  sender.sendMessage(plugin.color(plugin.prefix()+\"&a픽셀몬 청소 자동화 ON\")); return true; }
                if (\"off\".equalsIgnoreCase(args[1])){ mgr.setPixelEnabledRuntime(false); sender.sendMessage(plugin.color(plugin.prefix()+\"&e픽셀몬 청소 자동화 OFF\")); return true; }
                World w = Bukkit.getWorld(args[1]);
                if (w != null) { mgr.triggerPixelOnce(java.util.Arrays.asList(w), plugin.getConfig().getInt(\"auto.countdownSeconds\", 60));
                    sender.sendMessage(plugin.color(plugin.prefix()+\"&a픽셀몬 청소 1회 실행 예약(월드: \"+w.getName()+\")\")); return true; }
            }
            sender.sendMessage(plugin.color(plugin.prefix()+\"&e/최적화 픽셀몬 on|off &7또는 &e/최적화 픽셀몬 <월드>\")); return true;
        }

        if (\"청키\".equals(sub) && args.length >= 2 && \"시작\".equals(args[1])) {
            World w = (args.length >= 3 ? Bukkit.getWorld(args[2]) : (Bukkit.getWorlds().isEmpty()? null : Bukkit.getWorlds().get(0)));
            ChunkyIntegration.startBorderPregen(plugin, w);
            sender.sendMessage(plugin.color(plugin.prefix()+\"&aChunky 프리젠 시작 요청.\")); return true;
        }

        sender.sendMessage(plugin.color(plugin.prefix()+\"&c사용법: /최적화 <리로드|상태|자동 실행|자동 취소|바닥청소 on|off|바닥청소 <월드>|픽셀몬 on|off|픽셀몬 <월드>|청키 시작 [월드]>\"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<String>();
        if (args.length == 1) { out.add(\"리로드\"); out.add(\"상태\"); out.add(\"자동\"); out.add(\"바닥청소\"); out.add(\"픽셀몬\"); out.add(\"청키\"); }
        else if (args.length == 2 && \"자동\".equals(args[0])) { out.add(\"실행\"); out.add(\"취소\"); }
        else if (args.length == 2 && \"바닥청소\".equals(args[0])) { out.add(\"on\"); out.add(\"off\"); for (World w : Bukkit.getWorlds()) out.add(w.getName()); }
        else if (args.length == 2 && \"픽셀몬\".equals(args[0])) { out.add(\"on\"); out.add(\"off\"); for (World w : Bukkit.getWorlds()) out.add(w.getName()); }
        else if (args.length == 2 && \"청키\".equals(args[0])) { out.add(\"시작\"); }
        else if (args.length == 3 && \"청키\".equals(args[0]) && \"시작\".equals(args[1])) { for (World w : Bukkit.getWorlds()) out.add(w.getName()); }
        return out;
    }
}
