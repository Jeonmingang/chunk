
package com.minkang.uto.commands;

import com.minkang.uto.Main;
import com.minkang.uto.guard.ConditionGuard;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ConditionStatusCommand implements CommandExecutor {
    private final Main plugin;
    public ConditionStatusCommand(Main plugin){ this.plugin=plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConditionGuard g = plugin.getConditionGuard();
        g.checkAllowed(sender); // prints reason or allowed
        return true;
    }
}
