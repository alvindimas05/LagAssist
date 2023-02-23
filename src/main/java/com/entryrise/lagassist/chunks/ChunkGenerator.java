package com.entryrise.lagassist.chunks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.entryrise.lagassist.Main;
import com.entryrise.lagassist.packets.Reflection;
import com.entryrise.lagassist.utils.Chat;
import com.entryrise.lagassist.utils.MathUtils;
import com.entryrise.lagassist.utils.VersionMgr;

public class ChunkGenerator {

	private static Object chunkProvider;
	private static World world;
	private static BukkitTask gentask;

	// WORLDBORDER VALUES
	private static int maxx;
	private static int maxz;

	private static int minx;
	private static int minz;

	// WORLDBORDER VALUES END

	private static int currenti;

	public static void pregenWorld(CommandSender s, String[] args) {
		if (!(s instanceof Player)) {
			s.sendMessage(Main.PREFIX + "You need to execute this command as a player");
			return;
		}

		if (args.length != 2) {
			s.sendMessage(Main.PREFIX + "Correct usage: §2/lagassist pregench [Max-Millis-Per-Tick]");
			return;
		}

		if (!MathUtils.isInt(args[1])) {
			s.sendMessage(Main.PREFIX + "Please input a correct ammount of milliseconds.");
			return;
		}

		if (gentask != null) {
			s.sendMessage(
					Main.PREFIX + "The world is already pregenerating. Please wait for it to finish or cancel it.");
			return;
		}

		int millis = Integer.valueOf(args[1]);

		Player p = (Player) s;

		World w = p.getWorld();

		if (w.getWorldBorder().getSize() > 50000) {
			s.sendMessage(Main.PREFIX + "The max worldborder size is 50k.");
			return;
		}

		pregenWorld(w, millis);
	}

	public static void stopGen(CommandSender s) {
		if (gentask == null) {
			s.sendMessage(Main.PREFIX + "There is no active world pregeneration.");
			return;
		}
		gentask.cancel();
		gentask = null;
		s.sendMessage(Main.PREFIX + "Pregeneration task cancelled.");

	}

	private static void pregenWorld(World w, int millis) {

		// PREPARING ENVIRONMENT
		if (!VersionMgr.isNewMaterials()) {
			chunkProvider = Reflection.getChunkProvider(Reflection.getWorldServer(Reflection.getCraftWorld(w)));
		}
		world = w;

		// PREPARING WORLDBORDER CHECK
		WorldBorder wb = w.getWorldBorder();
		Location center = wb.getCenter();
		double radius = wb.getSize() / 2;

		maxx = (int) ((center.getX() + radius) / 16);
		maxz = (int) ((center.getZ() + radius) / 16);

		minx = (int) ((center.getX() - radius) / 16);
		minz = (int) ((center.getZ() - radius) / 16);

		Chunk spawn = world.getSpawnLocation().getChunk();
		int xfin = spawn.getX();
		int zfin = spawn.getZ();

		// Prepare list of chunks to pregenerate. It will further be used to pregenerate
		// the world.

		Bukkit.getScheduler().runTaskAsynchronously(Main.p, new Runnable() {
			@Override
			public void run() {
				List<int[]> chunks = new ArrayList<int[]>();

				int x = xfin;
				int z = zfin;
				int i = 1;

				while (isInside(x, z)) {
					for (int temp = x + 1; temp <= x + i; temp++) {
						chunks.add(new int[] { x, z });
					}
					x += i;
					for (int temp = z - 1; temp >= z - i; temp--) {
						chunks.add(new int[] { x, temp });
					}
					z -= i;
					i++;
					for (int temp = x - 1; temp >= x - i; temp--) {
						chunks.add(new int[] { temp, z });
					}
					x -= i;
					for (int temp = z + 1; temp <= z + i; temp++) {
						chunks.add(new int[] { x, temp });
					}
					z += i;
					i++;
				}
				startGen(chunks, millis);
			}
		});
	}

	private static void startGen(List<int[]> chunks, int millis) {
		currenti = 0;
		gentask = Bukkit.getScheduler().runTaskTimer(Main.p, new Runnable() {
			@Override
			public void run() {
				long time = System.currentTimeMillis();
				for (int i = currenti; i < chunks.size(); i++) {
					pregenChunk(chunks.get(i));
					if (i % 300 == 0 && i != 0) {
						int percent = (int) ((double) i / chunks.size() * 100);
						Bukkit.getLogger()
								.info("§e[§a✪§e] §fThe pregeneration of the §2"
										+ Chat.capitalize(world.getName()) + " §fworld is at §a" + percent
										+ "% completion.");
					}
					if (i % 20 == 0 && i != 0) {
						long curr = System.currentTimeMillis();
						if (time + millis < curr) {
							currenti = i;
							return;
						}
					}
				}
				gentask.cancel();
				gentask = null;
				Bukkit.getLogger()
						.info("§e[§a✪§e] §fThe pregeneration of the §2" + Chat.capitalize(world.getName())
								+ " §fworld is finished. Thank you for your patience.");

			}
		}, 1L, 1L);
	}

	private static void pregenChunk(int[] cords) {

		pregenChunk(cords[0], cords[1]);

	}

	private static void pregenChunk(int x, int z) {
		if (VersionMgr.isChunkGenerated(world, chunkProvider, x, z)) {
			return;
		}

		VersionMgr.loadChunk(world, x, z);
	}

	private static boolean isInside(int x, int z) {

		if (x < minx || x > maxx) {
			return false;
		}
		if (z < minz || z > maxz) {
			return false;
		}

		return true;
	}

}
