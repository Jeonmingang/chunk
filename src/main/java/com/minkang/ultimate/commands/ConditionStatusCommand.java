
package com.minkang.ultimate.commands;

import com.minkang.ultimate.guard.ConditionGuard;
import com.minkang.ultimate.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ConditionStatusCommand implements CommandExecutor {
    private final Main plugin;
    public ConditionStatusCommand(Main plugin){ this.plugin=plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConditionGuard g = plugin.getConditionGuard();
        g.checkAllowed(sender);
        return true;
    }
}
