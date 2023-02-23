package com.entryrise.lagassist.cmd;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.entryrise.lagassist.Data;
import com.entryrise.lagassist.Main;
import com.entryrise.lagassist.MonTools;
import com.entryrise.lagassist.MsrExec;
import com.entryrise.lagassist.chunks.ChunkGenerator;
import com.entryrise.lagassist.gui.AdminGUI;
import com.entryrise.lagassist.minebench.Approximate;
import com.entryrise.lagassist.minebench.SpeedTest;
import com.entryrise.lagassist.packets.Reflection;
import com.entryrise.lagassist.updater.SmartUpdater;
import com.entryrise.lagassist.utils.Chat;
import com.entryrise.lagassist.utils.MathUtils;

public class CommandListener implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if ((cmd.getName().equalsIgnoreCase("lagassist"))) {
			if (!sender.hasPermission("lagassist.use")) {
				if (args.length == 1) {
					sender.sendMessage(Main.PREFIX + "Plugin bought by " + Main.USER);
				} else {
					sender.sendMessage(Main.PREFIX + "You don't have permission to run this command!");
				}
				return true;
			}
			if (args.length == 0) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					AdminGUI.show(p);
				} else {
					sendHelp(sender);
				}
			} else if (args.length <= 2) {
				String arg = args[0];
				if (arg.equalsIgnoreCase("mobculler")) {
				} else if (arg.equalsIgnoreCase("version")) {
					MsrExec.showVersion(sender);
				} else if (arg.equalsIgnoreCase("redstoneculler")) {
					MsrExec.cullRedstone(sender);
				} else if (arg.equalsIgnoreCase("togglespawning")) {
					MsrExec.toggleMobs(sender);
				} else if (arg.equalsIgnoreCase("togglephysics")) {
					MsrExec.togglePhysics(sender);
				} else if (arg.equalsIgnoreCase("optimizespawners")) {
					MsrExec.toggleSpawnerOptimization(sender);
				} else if (arg.equalsIgnoreCase("getmap")) {
					MsrExec.giveMap(sender);
				} else if (arg.equalsIgnoreCase("benchmark")) {
					Approximate.showBenchmark(sender);

				} else if (arg.equalsIgnoreCase("chunkanalyse") && sender.hasPermission("lagassist.chunkanalyse")) {
					MsrExec.analyseChunks(sender, args);
				} else if (arg.equalsIgnoreCase("pregench") && sender.hasPermission("lagassist.generatechunks")) {
					ChunkGenerator.pregenWorld(sender, args);
				} else if (arg.equalsIgnoreCase("stopgen") && sender.hasPermission("lagassist.generatechunks")) {
					ChunkGenerator.stopGen(sender);
				} else if (arg.equalsIgnoreCase("tpchunk")) {
					sender.sendMessage(Main.PREFIX + "Correct usage: §2/lagassist tpchunk [WORLD] [X] [Z]");
				} else if (arg.equalsIgnoreCase("reload") && sender.hasPermission("lagassist.reload")) {
					Main.ReloadPlugin(sender);
				} else if (arg.equalsIgnoreCase("changelog")) {
					SmartUpdater.showChangelog(sender);
				} else if (arg.equalsIgnoreCase("statsbar")) {
					if (sender instanceof Player) {
						UUID p = ((Player) sender).getUniqueId();
						if (MonTools.actionmon.contains(p)) {
							MonTools.actionmon.remove(p);
							sender.sendMessage(Main.PREFIX + "StatsBar §2Disabled.");
							Reflection.sendAction(Bukkit.getPlayer(p), "");
						} else {
							MonTools.actionmon.add(p);
							sender.sendMessage(Main.PREFIX + "StatsBar §aEnabled.");
						}
					} else {
						sender.sendMessage(Main.PREFIX + "You cannot see a statsbar from the console.");
					}

				} else if (arg.equalsIgnoreCase("ping")) {
					SpeedTest.pingBenchmark(sender);
				} else if (arg.equalsIgnoreCase("threadanalyse")) {
					for (Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
						Main.p.getLogger().info(Arrays.stream(entry.getValue()).skip(0).map(StackTraceElement::toString)
								.reduce((s1, s2) -> s1 + "\n" + s2).orElse("NOT FOUND"));
					}
				} else if (arg.equalsIgnoreCase("debugmode")) {
					Main.debug = Main.debug >= 3 ? 0 : Main.debug+1;
					sender.sendMessage(Main.PREFIX + "Debug setting currently at: " + Main.debug);
				} else if (arg.equalsIgnoreCase("advertising")) {
					Data.toggleAdvertising(sender);
				} else {
					sendHelp(sender);
				}
			} else if (args.length == 3) {
				if (args[0].equalsIgnoreCase("chunkhopper")) {
					MsrExec.giveChunkHopper(sender, args);
				} else {
					sendHelp(sender);
				}
			} else if (args.length == 4) {
				if (args[0].equalsIgnoreCase("tpchunk") && sender.hasPermission("lagassist.chunkanalyse")) {
					if (sender instanceof Player) {
						Player p = (Player) sender;
						World w = Bukkit.getWorld(args[1]);

						if ((w != null) && MathUtils.isInt(args[2]) && MathUtils.isInt(args[3])) {
							int x = Integer.parseInt(args[2]) * 16 + 8;
							int z = Integer.parseInt(args[3]) * 16 + 8;
							int y = w.getHighestBlockYAt(x, z) + 10;
							p.teleport(new Location(w, x, y, z));
							p.sendMessage(Main.PREFIX + "You have been teleported to the desired chunk.");
						} else {
							sender.sendMessage(Main.PREFIX + "Correct usage: §2/lagassist tpchunk [WORLD] [X] [Z]");
						}
					} else {
						sender.sendMessage(Main.PREFIX + "You cannot tp to laggy chunks from console.");
					}

				} else {
					sendHelp(sender);
				}
			} else {
				sendHelp(sender);
			}
		}
		return false;

	}

	private static void help18(CommandSender s) {
		if (s instanceof Player) {
			Player p = (Player) s;
			p.spigot().sendMessage(Chat.genHoverAndSuggestTextComponent(
					"     §2✸ §fLagAssist MobCuller §2- §eClear the configured mobs",
					"§fClears the mobs that you have set to remove\nin the configuration file.\n\n§2(!) It won't remove named mobs.",
					"LagAssist MobCuller"));
			p.spigot().sendMessage(Chat.genHoverAndSuggestTextComponent(
					"     §2✸ §fLagAssist RedstoneCuller §2- §eDisables Redstone",
					"§fDisables all the redstone that has been triggered in 30 ticks.\n\nIt can be used to temporarily disable lag-machines\nor to even break them, if you configure it to do so.\nIt also breaks lag-machines that don't use redstone wire!",
					"LagAssist RedstoneCuller"));
			p.spigot().sendMessage(Chat.genHoverAndSuggestTextComponent(
					"     §2✸ §fLagAssist ToggleSpawning §2- §eToggles mob spawning",
					"§fDisables all mob-spawning once it is toggled on.\n\nIt can be used when many players are online,\nand your server is not keeping up to all the mobs.",
					"LagAssist ToggleSpawning"));
			p.spigot().sendMessage(Chat.genHoverAndSuggestTextComponent(
					"     §2✸ §fLagAssist TogglePhyisics §2- §eToggles physics events.",
					"§fIt disables all physics events that are enabled\nin the config.\n\nIt can be used to drastically reduce lag on non-minigame servers.",
					"LagAssist TogglePhysics"));
			p.spigot().sendMessage(Chat.genHoverAndSuggestTextComponent(
					"     §2✸ §fLagAssist ChunkAnalyse §2- §eFind the laggiest chunks",
					"§fOutput the first X laggiest chunks, based on a score table.\n\nIt can be fully configured, from how many chunks to show to\nhow much score each entity or tile-entity has.",
					"LagAssist ChunkAnalyse"));
			p.spigot().sendMessage(Chat.genHoverAndSuggestTextComponent(
					"     §2✸ §fLagAssist GetMap §2- §eGet a graph of server TPS",
					"§fGives you a map that can help find lagspikes.\n§2(!) It exaggerates lag so it is more visible.\n§2(!) do not get alarmed.\n\n§fYou can use it along a cronometer\n§fto find how often the lagspikes happen. Then, you can\nuse your timings report to find a plugin that runs\nTasks at that interval.",
					"LagAssist GetMap"));
			p.spigot().sendMessage(Chat.genHoverAndSuggestTextComponent(
					"     §2✸ §fLagAssist Benchmark §2- §eGenerates a performance report",
					"§fIt can be used to approximate how many players your server\ncan run without lagging, based on collected information.\nIt can also be used to find if hosts are overselling, and how much.",
					"LagAssist Benchmark"));
			p.spigot().sendMessage(Chat.genHoverAndSuggestTextComponent(
					"     §2✸ §fLagAssist StatsBar §2- §eGet a simple action-bar TPS Meter",
					"§fShows a simple action-bar showing data that\nis related to lag.\n\nIf you have rare lagspikes, this can help\npinpoint when they happen without having\nto stop doing something else.",
					"LagAssist StatsBar"));
			p.spigot().sendMessage(Chat.genHoverAndSuggestTextComponent(
					"     §2✸ §fLagAssist TpChunk §2- §eTeleport to a chunk",
					"§f/LagAssist TpChunk [World] [X] [Z].\n\n§2(!) The X and Z variables are not F3 coords.\n\n",
					"LagAssist TpChunk"));
			p.spigot().sendMessage(Chat.genHoverAndSuggestTextComponent(
					"     §2✸ §fLagAssist PreGenCH §2- §ePre-Generate chunks",
					"§f/LagAssist PreGenCH [Radius] [Delay] [Amount]\n\n§2(!) This doesn't have any failsafes.\n§2(!) If you don't know how to use it, ask me first.",
					"LagAssist PreGenCH"));
			p.spigot()
					.sendMessage(Chat.genHoverAndSuggestTextComponent(
							"     §2✸ §fLagAssist ChunkHopper §2- §eGet Chunk Hoppers",
							"§f/LagAssist chunkhopper [Player] [Amount]\n\n§fTGive a player a chunk hopper.",
							"LagAssist ChunkHopper"));
			p.spigot().sendMessage(Chat.genHoverAndSuggestTextComponent(
					"     §2✸ §fLagAssist Changelog §2- §eCheck Version info.",
					"§fThis tool will show you the changelog in an update.\nIt is really useful if you haven't checked the changelog in the updates page already.",
					"LagAssist Changelog"));
			p.spigot().sendMessage(Chat.genHoverAndSuggestTextComponent(
					"     §2✸ §fLagAssist Version §2- §eShows you the plugin version.",
					"§fThis should be used when reporting bugs.\nYou should allways update first when having a bug,\nas I fix bugs quite fast once i find them.",
					"LagAssist Version"));

			p.spigot().sendMessage(
					Chat.genHoverAndSuggestTextComponent("     §2✸ §fLagAssist Reload §2- §eReloads the config",
							"§fUse it when you changed the config", "LagAssist Reload"));
		} else {
			s.sendMessage("     ✸ LagAssist MobCuller - Clear the configured mobs");
			s.sendMessage("     ✸ LagAssist RedstoneCuller - Disables Redstone");
			s.sendMessage("     ✸ LagAssist ToggleSpawning - Toggles mob spawning");
			s.sendMessage("     ✸ LagAssist TogglePhyisics - Toggles physics events.");
			s.sendMessage("     ✸ LagAssist ChunkAnalyse - Find the laggiest chunks");
			s.sendMessage("     ✸ LagAssist GetMap - Get a graph of server TPS");
			s.sendMessage("     ✸ LagAssist Benchmark - Generates a performance report");
			s.sendMessage("     ✸ LagAssist Ping - Shows a ping benchmark");
			s.sendMessage("     ✸ LagAssist StatsBar - Get a simple action-bar TPS Meter");
			s.sendMessage("     ✸ LagAssist TpChunk - Teleport to a chunk");
			s.sendMessage("     ✸ LagAssist PreGenCH - Pre-Generate chunks");
			s.sendMessage("     ✸ LagAssist ChunkHopper - Get Chunk Hoppers");
			s.sendMessage("     ✸ LagAssist Version - Shows you the plugin version.");
			s.sendMessage("     ✸ LagAssist Reload - Reloads the config");
		}
	}

	private static void helpnew(CommandSender p) {
		p.spigot().sendMessage(Chat.genHoverAndSuggestTextComponent(
				"     §2✸ §fLagAssist MobCuller §2- §eClear the configured mobs",
				"§fClears the mobs that you have set to remove\nin the configuration file.\n\n§2(!) It won't remove named mobs.",
				"LagAssist MobCuller"));
		p.spigot().sendMessage(Chat.genHoverAndSuggestTextComponent(
				"     §2✸ §fLagAssist RedstoneCuller §2- §eDisables Redstone",
				"§fDisables all the redstone that has been triggered in 30 ticks.\n\nIt can be used to temporarily disable lag-machines\nor to even break them, if you configure it to do so.\nIt also breaks lag-machines that don't use redstone wire!",
				"LagAssist RedstoneCuller"));
		p.spigot().sendMessage(Chat.genHoverAndSuggestTextComponent(
				"     §2✸ §fLagAssist ToggleSpawning §2- §eToggles mob spawning",
				"§fDisables all mob-spawning once it is toggled on.\n\nIt can be used when many players are online,\nand your server is not keeping up to all the mobs.",
				"LagAssist ToggleSpawning"));
		p.spigot().sendMessage(Chat.genHoverAndSuggestTextComponent(
				"     §2✸ §fLagAssist TogglePhyisics §2- §eToggles physics events.",
				"§fIt disables all physics events that are enabled\nin the config.\n\nIt can be used to drastically reduce lag on non-minigame servers.",
				"LagAssist TogglePhysics"));
		p.spigot().sendMessage(Chat.genHoverAndSuggestTextComponent(
				"     §2✸ §fLagAssist ChunkAnalyse §2- §eFind the laggiest chunks",
				"§fOutput the first X laggiest chunks, based on a score table.\n\nIt can be fully configured, from how many chunks to show to\nhow much score each entity or tile-entity has.",
				"LagAssist ChunkAnalyse"));
		p.spigot().sendMessage(Chat.genHoverAndSuggestTextComponent(
				"     §2✸ §fLagAssist GetMap §2- §eGet a graph of server TPS",
				"§fGives you a map that can help find lagspikes.\n§2(!) It exaggerates lag so it is more visible.\n§2(!) do not get alarmed.\n\n§fYou can use it along a cronometer\n§fto find how often the lagspikes happen. Then, you can\nuse your timings report to find a plugin that runs\nTasks at that interval.",
				"LagAssist GetMap"));
		p.spigot().sendMessage(Chat.genHoverAndSuggestTextComponent(
				"     §2✸ §fLagAssist Benchmark §2- §eGenerates a performance report",
				"§fIt can be used to approximate how many players your server\ncan run without lagging, based on collected information.\nIt can also be used to find if hosts are overselling, and how much.",
				"LagAssist Benchmark"));
		p.spigot().sendMessage(Chat.genHoverAndSuggestTextComponent(
				"     §2✸ §fLagAssist Ping §2- §eShows a ping benchmark",
				"§fIt can be used to find the best location to host your server\nbased on what the player ping is.\nThe recommended ping value is under 90ms.",
				"LagAssist Ping"));
		p.spigot().sendMessage(Chat.genHoverAndSuggestTextComponent(
				"     §2✸ §fLagAssist StatsBar §2- §eGet a simple action-bar TPS Meter",
				"§fShows a simple action-bar showing data that\nis related to lag.\n\nIf you have rare lagspikes, this can help\npinpoint when they happen without having\nto stop doing something else.",
				"LagAssist StatsBar"));
		p.spigot()
				.sendMessage(Chat.genHoverAndSuggestTextComponent(
						"     §2✸ §fLagAssist TpChunk §2- §eTeleport to a chunk",
						"§f/LagAssist TpChunk [World] [X] [Z].\n\n§2(!) The X and Z variables are not F3 coords.\n\n",
						"LagAssist TpChunk"));
		p.spigot().sendMessage(Chat.genHoverAndSuggestTextComponent(
				"     §2✸ §fLagAssist PreGenCH §2- §ePre-Generate chunks",
				"§f/LagAssist PreGenCH [Radius] [Delay] [Amount]\n\n§2(!) This doesn't have any failsafes.\n§2(!) If you don't know how to use it, ask me first.",
				"LagAssist PreGenCH"));
		p.spigot().sendMessage(Chat.genHoverAndSuggestTextComponent(
				"     §2✸ §fLagAssist Changelog §2- §eCheck Version info.",
				"§fThis tool will show you the changelog in an update.\nIt is really useful if you haven't checked the changelog in the updates page already.",
				"LagAssist Changelog"));
		p.spigot().sendMessage(Chat.genHoverAndSuggestTextComponent(
				"     §2✸ §fLagAssist Version §2- §eShows you the plugin version.",
				"§fThis should be used when reporting bugs.\nYou should allways update first when having a bug,\nas I fix bugs quite fast once i find them.",
				"LagAssist Version"));
		p.spigot().sendMessage(
				Chat.genHoverAndSuggestTextComponent("     §2✸ §fLagAssist Reload §2- §eReloads the config",
						"§fUse it when you changed the config", "LagAssist Reload"));

	}

	private static void sendHelp(CommandSender p) {

		boolean oldvers = Bukkit.getVersion().contains("1.8");

		p.sendMessage("§2§l⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛§f§l LAG ASSIST §2§l⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛");
		p.sendMessage("");
		if (oldvers) {
			help18(p);
		} else {
			helpnew(p);
		}
		p.sendMessage("");
		p.sendMessage("§2§l⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛");
	}

}
