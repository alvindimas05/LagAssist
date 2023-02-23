package com.entryrise.lagassist;

import java.text.DecimalFormat;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.entryrise.lagassist.hoppers.ChunkHoppers;
import com.entryrise.lagassist.mobs.SmartMob;
import com.entryrise.lagassist.mobs.SpawnerMgr;
import com.entryrise.lagassist.packets.Reflection;

public class Monitor {

	private static DecimalFormat format = new DecimalFormat("##.##");


	public static Runtime Rtm = Runtime.getRuntime();

	public static byte[][] colors = new byte[129][129];
	public static double exactTPS = 20.0;

	public static int mondelay;

	private static BukkitTask btk;

	public static void Enabler(boolean reload) {
		Bukkit.getLogger().info("    §e[§a✔§e] §fLag Monitor.");

		for (byte[] row : colors)
			Arrays.fill(row, (byte) 34);

		mondelay = Main.config.getInt("lag-measures.timer");
		
		if (!reload) {
			Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.p, new ExactTPS(), 100L, 1L);
			
			LagDetect();
			GetExactTPS();
			createGraph();
		}
	}

	private static void LagMeasures(double d) {
		float tps = (float) (Math.floor(d * 100) / 100);

		SmartMob.Spawning = true;
		Physics.denyphysics = false;
		SpawnerMgr.active = false;
		ChunkHoppers.mobhoppers = false;

		String stg = Main.PREFIX + "LagMeasures executed: §e";

		if (tps <= Main.config.getDouble("smart-cleaner.maxtps-cull")) {
			SmartMob.MobCuller();
			stg += "Culled Mobs, ";
		}
		if (tps <= Main.config.getDouble("smart-cleaner.maxtps-disablespawn")) {
			SmartMob.Spawning = false;
			stg += "Disabled MobSpawn, ";
		}
		if (tps <= Main.config.getDouble("redstone-culler.maxtps")) {
			Redstone.CullRedstone();
			stg += "Culled Redstone, ";
		}
		if (tps <= Main.config.getDouble("deny-physics.maxtps")) {
			Physics.denyphysics = true;
			stg += "Disabled physics, ";
		}

		if (tps <= Main.config.getDouble("spawner-check.maxtps")) {
			SpawnerMgr.active = true;
			stg += "Optimizing Spawners, ";
		}
		
		if (tps <= Main.config.getDouble("hopper-check.chunk-hoppers.mob-hopper.maxtps")) {
			ChunkHoppers.mobhoppers = true;
			stg += "Mob Hoppers.";
		}

		if (Main.config.getBoolean("lag-measures.announce.enabled")) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (Main.config.getBoolean("lag-measures.announce.staffmsg")) {
					if (player.hasPermission("lagassist.notify")) {
						if (stg.equals(Main.PREFIX + "LagMeasures executed: §e")) {
							player.sendMessage(Main.PREFIX + "No LagMeasures Executed");
						} else {
							player.sendMessage(stg);
						}
					} else {
						player.sendMessage(ChatColor.translateAlternateColorCodes('&',
								Main.config.getString("lag-measures.announce.message")));
					}
				} else {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&',
							Main.config.getString("lag-measures.announce.message")));
				}
			}
		}

		// if (tps < 19.5) {
		// Bukkit.getLogger().info(Main.PREFIX + "TPS under 19.5!
		// Culling redstone.");
		// Redstone.CullRedstone();
		// SmartMob.Spawning = true;
		// if (!Main.config.getBoolean("deny-physics.enabled")) {
		// Physics.denyphysics = false;
		// }
		// } else if (tps < 18.5) {
		// Bukkit.getLogger().info(Main.PREFIX + "TPS under 18.5!
		// Culling redstone & disabling Physics");
		// SmartMob.Spawning = true;
		// if (!Main.config.getBoolean("deny-physics.enabled")) {
		// Physics.denyphysics = true;
		// }
		// Redstone.CullRedstone();
		// } else if (tps < 15) {
		// Bukkit.getLogger().info(Main.PREFIX + "TPS under 15.0!
		// Aggresive systems has been activated");
		// if (!Main.config.getBoolean("deny-physics.enabled")) {
		// Physics.denyphysics = true;
		// }
		// Redstone.CullRedstone();
		// SmartMob.MobCuller();
		// SmartMob.Spawning = false;
		// } else {
		// SmartMob.Spawning = true;
		// if (!Main.config.getBoolean("deny-physics.enabled")) {
		// Physics.denyphysics = false;
		// }
		// }
	}

	public static void LagDetect() {
		Bukkit.getScheduler().runTaskTimerAsynchronously(Main.p, new Runnable() {
			@Override
			public void run() {
				Bukkit.getScheduler().scheduleSyncDelayedTask(Main.p, new Runnable() {
					@Override
					public void run() {
						if (Main.config.getBoolean("lag-measures.announce.enabled")) {
							Bukkit.getLogger().info("    §e[§a☯§e] §fRunning lag check task.");
						}

						LagMeasures(Double.valueOf(getTPS(0)));
					}
				}, 0L);
			}

		}, mondelay, mondelay);
	}

	public static void GetExactTPS() {
		btk = Bukkit.getScheduler().runTaskTimerAsynchronously(Main.p, new Runnable() {
			@Override
			public void run() {
				try {
					Bukkit.getScheduler().scheduleSyncDelayedTask(Main.p, new Runnable() {
						@Override
						public void run() {
							double ext = ExactTPS.getTPS();
							if (ext > 20 || ext == 0) {
								ext = 20;
							}
							exactTPS = ext;
						}
					}, 0L);
				} catch (IllegalStateException e) {
					btk.cancel();
				}
			}

		}, 60L, 1L);
	}

	public static long freeMEM() {
		return Rtm.freeMemory() / 1048576;
	}

	public static String getTPS(int time) {
		return format.format(Reflection.getTPS(time)).replace(",", ".");
	}

	public static void createGraph() {
		Bukkit.getScheduler().runTaskTimerAsynchronously(Main.p, new Runnable() {
			@Override
			public void run() {

				double ctps = ExactTPS.getTPS(20);
				double medtps = Double.valueOf(getTPS(1));
				if (medtps > 15) {
					medtps = 15.0;
				}
				if (ctps > 20) {
					ctps = 20.0;
				}

				double min = medtps - 5;

				double ntps = 89 - (ctps - min) * 8;
				// Bukkit.getLogger().info(String.valueOf(ntps));
				for (int i = 89; i > 9; i--) {
					if (i == (int) ntps) {
						colors[124][i] = 18;

					} else if (i == (int) ntps + 1) {
						colors[124][i] = -124;
					} else if (i > ntps) {
						colors[124][i] = -122;
					} else {
						colors[124][i] = 32;
					}
				}

				// BEGIN GRAPHING
				for (int i = 3; i < 124; i++) {
					for (int j = 3; j < 90; j++) {
						colors[i][j] = colors[i + 1][j];
					}
				}

			}
		}, 7L, 7L);
	}

}
