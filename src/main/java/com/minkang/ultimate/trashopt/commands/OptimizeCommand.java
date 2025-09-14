package com.minkang.ultimate.trashopt.commands;

import com.minkang.ultimate.trashopt.Main;
import com.minkang.ultimate.trashopt.util.ConditionGuard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class OptimizeCommand implements CommandExecutor {

    private final Main plugin;

    public OptimizeCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "사용법:");
            sender.sendMessage(ChatColor.GRAY + "/" + label + " chunky 시작 [월드] [반경]");
            sender.sendMessage(ChatColor.GRAY + "/" + label + " chunky 일시중지 | chunky 상태");
            sender.sendMessage(ChatColor.GRAY + "/" + label + " worldborder 시작 [월드] [반경] | 일시중지 | 상태");
            sender.sendMessage(ChatColor.GRAY + "/" + label + " starlight 재조명 [월드]");
            sender.sendMessage(ChatColor.GRAY + "/" + label + " all 시작 [월드] [반경]  §7(Chunky+WorldBorder+Starlight 동시에)");
            return true;
        }

        String tool = args[0].toLowerCase();
        String action = args.length >= 2 ? args[1].toLowerCase() : "";

        String worldName = plugin.getConfig().getString("world-name", "world");
        int radius = 3000;

        if ("시작".equals(action)) {
            if (args.length >= 3) {
                worldName = args[2];
            }
            if (args.length >= 4) {
                try {
                    radius = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "반경은 숫자여야 합니다.");
                    return true;
                }
            }
        } else if ("재조명".equals(action)) {
            if (args.length >= 3) {
                worldName = args[2];
            }
        }

        if ("all".equals(tool)) {
            if (!"시작".equals(action)) {
                sender.sendMessage(ChatColor.RED + "사용법: /" + label + " all 시작 [월드] [반경]");
                return true;
            }
            if (!ConditionGuard.checkAllowed(sender, "최적화(all)")) return true;
            runTemplate(sender, "optimize.chunky.start", worldName, radius);
            runTemplate(sender, "optimize.worldborder.start", worldName, radius);
            runTemplate(sender, "optimize.starlight.relight", worldName, radius);
            sender.sendMessage(ChatColor.GREEN + "모든 최적화 작업을 요청했습니다.");
            return true;
        }

        if ("chunky".equals(tool)) {
            if ("시작".equals(action)) {
                if (!ConditionGuard.checkAllowed(sender, "Chunky")) return true;
                runTemplate(sender, "optimize.chunky.start", worldName, radius);
                return true;
            } else if ("일시중지".equals(action)) {
                runTemplate(sender, "optimize.chunky.pause", worldName, radius);
                return true;
            } else if ("상태".equals(action)) {
                runTemplate(sender, "optimize.chunky.status", worldName, radius);
                return true;
            }
        }

        if ("worldborder".equals(tool)) {
            if ("시작".equals(action)) {
                if (!ConditionGuard.checkAllowed(sender, "WorldBorder")) return true;
                runTemplate(sender, "optimize.worldborder.start", worldName, radius);
                return true;
            } else if ("일시중지".equals(action)) {
                runTemplate(sender, "optimize.worldborder.pause", worldName, radius);
                return true;
            } else if ("상태".equals(action)) {
                runTemplate(sender, "optimize.worldborder.status", worldName, radius);
                return true;
            }
        }

        if ("starlight".equals(tool)) {
            if ("재조명".equals(action)) {
                if (!ConditionGuard.checkAllowed(sender, "Starlight 재조명")) return true;
                runTemplate(sender, "optimize.starlight.relight", worldName, radius);
                return true;
            }
        }

        sender.sendMessage(ChatColor.RED + "알 수 없는 명령입니다. /" + label + " 로 도움말을 확인하세요.");
        return true;
    }

    private void runTemplate(CommandSender sender, String path, String worldName, int radius) {
        List<String> cmds = plugin.getConfig().getStringList(path);
        if (cmds == null || cmds.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "설정에 명령 템플릿이 없습니다: " + path);
            return;
        }
        if (Bukkit.getWorld(worldName) == null) {
            World any = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
            if (any != null) {
                worldName = any.getName();
            }
        }
        for (String c : cmds) {
            String finalCmd = c.replace("{world}", worldName).replace("{radius}", String.valueOf(radius));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
            sender.sendMessage(ChatColor.DARK_AQUA + "> " + ChatColor.GRAY + finalCmd);
        }
    }
}
