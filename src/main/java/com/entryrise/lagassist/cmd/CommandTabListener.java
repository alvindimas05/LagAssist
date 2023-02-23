package com.entryrise.lagassist.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

public class CommandTabListener implements TabCompleter {

	private static List<String> argl1 = Arrays.asList("mobculler", "redstoneculler", "togglespawning", "benchmark",
			"togglephysics", "chunkanalyse", "ping", "getmap", "statsbar", "tpchunk", "pregench", "stopgen", "version",
			"reload", "optimizespawners", "chunkhopper");

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		List<String> response = new ArrayList<String>();
		if (cmd.getName().equalsIgnoreCase("lagassist")) {
			if (args.length == 1) {

				StringUtil.copyPartialMatches(args[0], argl1, response);
				return response;

			} else if (args.length == 2) {
				if (args[0].equalsIgnoreCase("pregench")) {
					response.add("10");
					response.add("20");
					response.add("35");
					return response;
				} else if (args[0].equalsIgnoreCase("tpchunk")) {
					for (World w : Bukkit.getWorlds()) {
						response.add(w.getName());
					}
					return response;
				} else if (args[0].equalsIgnoreCase("chunkanalyse")) {
					response.add("this");
				}
			}

		}
		return response;
	}

}
