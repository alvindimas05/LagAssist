package org.alvindimas05.lagassist.cmd;

import org.alvindimas05.lagassist.gui.ClientGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.alvindimas05.lagassist.client.ClientMain;
import org.jetbrains.annotations.NotNull;

public class ClientCmdListener implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender s, @NotNull Command cmd, @NotNull String label, String @NotNull [] args) {

        if (!s.hasPermission(ClientMain.perm)) {
            s.sendMessage(ClientMain.prefix + "You are not allowed to use the control panel!");
            return true;
        }

        if (!(s instanceof Player p)) {
            s.sendMessage(ClientMain.prefix + "You cannot use the client optimizer from the console!");
            return true;
        }

        s.sendMessage(ClientMain.prefix + "Opening your ClientOptimizer. Please Wait.");
        ClientGUI.show(p);

        return false;
    }

}
