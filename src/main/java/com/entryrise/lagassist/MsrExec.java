package com.entryrise.lagassist;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.entryrise.lagassist.chunks.ChkAnalyse;
import com.entryrise.lagassist.hoppers.ChunkHoppers;
import com.entryrise.lagassist.mobs.SmartMob;
import com.entryrise.lagassist.mobs.SpawnerMgr;
import com.entryrise.lagassist.utils.MathUtils;

public class MsrExec {

	public static void togglePhysics(CommandSender sender) {
		if (Physics.denyphysics) {
			sender.sendMessage(Main.PREFIX + "Physics were enabled.");
			Physics.denyphysics = false;
		} else {
			sender.sendMessage(Main.PREFIX + "Physics were disabled.");
			Physics.denyphysics = true;
		}
	}

	public static void toggleMobs(CommandSender sender) {
		if (SmartMob.Spawning) {
			sender.sendMessage(Main.PREFIX + "Mob spawning was disabled.");
			SmartMob.Spawning = false;
		} else {
			sender.sendMessage(Main.PREFIX + "Mob spawning was enabled.");
			SmartMob.Spawning = true;
		}
	}

	public static void toggleSpawnerOptimization(CommandSender sender) {
		if (SpawnerMgr.active) {
			sender.sendMessage(Main.PREFIX + "Spawners are no longer optimized.");
			SpawnerMgr.active = false;
		} else {
			sender.sendMessage(Main.PREFIX + "Spawners are now optimized.");
			SpawnerMgr.active = true;
		}
	}

	public static void cullRedstone(CommandSender sender) {
		Redstone.CullRedstone();
		sender.sendMessage(Main.PREFIX + "Starting to cull Redstone Changes...");
	}

	public static void cullMobs(CommandSender sender) {
		SmartMob.MobCuller();
		sender.sendMessage(Main.PREFIX + "Clearing mobs...");
	}

	public static void showVersion(CommandSender sender) {
		sender.sendMessage(Main.PREFIX + "Lagassist version: §e" + Main.p.getDescription().getVersion());
	}

	public static void giveMap(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			sender.sendMessage(Main.PREFIX + "You have received a monitor map.");
			MonTools.giveMap(p);
		} else {
			sender.sendMessage(Main.PREFIX + "You cannot get the map from console.");
		}
	}
	
	public static void giveChunkHopper(CommandSender sender, String[] args) {
		Player p = Bukkit.getPlayer(args[1]);
		
		if (p == null) {
			sender.sendMessage(Main.PREFIX + "There is no player with that name online.");
			return;
		}
		
		if (!MathUtils.isInt(args[2])) {
			sender.sendMessage(Main.PREFIX + "The amount must be a number.");
			return;
		}
		
		ChunkHoppers.giveChunkHopper(p, Integer.valueOf(args[2]));
	}


	public static void analyseChunks(CommandSender sender, String[] args) {
		if (args.length == 2 && args[1].equals("this")) {
			ChkAnalyse.analyseCurrentChunk(sender);
		} else {
			if (sender instanceof Player) {
				Player p = (Player) sender;
				ChkAnalyse.analyseChunks(p);
			} else {
				ChkAnalyse.analyseChunks(sender);
			}
		}
	}

	public static boolean physics() {
		return !Physics.denyphysics;
	}

	public static boolean mobSpawning() {
		return SmartMob.Spawning;
	}

	public static boolean spawnerOptimization() {
		return SpawnerMgr.active;
	}

}
