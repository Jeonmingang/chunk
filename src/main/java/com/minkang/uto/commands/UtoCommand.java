package com.minkang.uto.commands;

import com.minkang.uto.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UtoCommand implements CommandExecutor {
    private final Main plugin;
    public UtoCommand(Main plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(plugin.color(plugin.prefix() + "&e/uto reload &7- 설정 리로드"));
            sender.sendMessage(plugin.color(plugin.prefix() + "&e/uto status &7- 상태 표시"));
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadAll();
            sender.sendMessage(plugin.color(plugin.prefix() + "&a리로드 완료."));
            return true;
        }
        if (args[0].equalsIgnoreCase("status")) {
            sender.sendMessage(plugin.color(plugin.prefix() + "&7자동 청소 상태는 콘솔 로그를 참고하세요."));
            return true;
        }
        sender.sendMessage(plugin.color(plugin.prefix() + "&c사용법: /uto <reload|status>"));
        return true;
    }
}
