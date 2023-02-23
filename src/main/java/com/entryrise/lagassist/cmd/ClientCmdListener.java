package com.entryrise.lagassist.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.entryrise.lagassist.client.ClientMain;
import com.entryrise.lagassist.gui.ClientGUI;

public class ClientCmdListener implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {

		if (!s.hasPermission(ClientMain.perm)) {
			s.sendMessage(ClientMain.prefix + "You are not allowed to use the control panel!");
			return true;
		}

		if (!(s instanceof Player)) {
			s.sendMessage(ClientMain.prefix + "You cannot use the client optimizer from the console!");
			return true;
		}

		Player p = (Player) s;

		s.sendMessage(ClientMain.prefix + "Opening your ClientOptimizer. Please Wait.");
		ClientGUI.show(p);

		return false;
	}

}
