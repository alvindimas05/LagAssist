package com.entryrise.lagassist.chunks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.entryrise.lagassist.Main;

public class ChkAnalyse {

	protected static int ammoshow = Main.config.getInt("chunkanalyse.ammount");
	protected static Map<String, Integer> values = new HashMap<String, Integer>();
	private static List<ChkStats> scores = new ArrayList<ChkStats>();

	public static void Enabler() {
		Bukkit.getLogger().info("    §e[§a✔§e] §fChunk Analyser.");

		Set<String> slist = Main.config.getConfigurationSection("chunkanalyse.values").getKeys(false);

		for (String s : slist) {
			int value = Main.config.getInt("chunkanalyse.values." + s);
			values.put(s.toLowerCase(), value);
		}
	}

	public static void analyseChunks(Player p) {
		scores.clear();
		for (World w : Bukkit.getWorlds()) {
			for (Chunk ch : w.getLoadedChunks()) {
				scores.add(new ChkStats(ch, false));
			}
		}
		Bukkit.getScheduler().runTaskAsynchronously(Main.p, new Runnable() {
			@Override
			public void run() {

				for (ChkStats ch : scores) {
					ch.genScores();
				}
				Collections.sort(scores);
				p.sendMessage("");
				p.sendMessage("§2§l⬛⬛⬛⬛⬛⬛ §f§lCHUNKANALYSER §2§l⬛⬛⬛⬛⬛⬛");
				p.sendMessage("");

				for (int i = 0; i < ammoshow; i++) {
					ChkStats cs = scores.get(i);
					p.spigot().sendMessage(cs.genText());
				}

				p.sendMessage("");
				p.sendMessage("§2§l⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛");
			}
		});

	}

	public static void analyseChunks(CommandSender p) {
		Map<Chunk, Integer> chunkscore = new HashMap<Chunk, Integer>();
		for (World w : Bukkit.getWorlds()) {
			for (Chunk ch : w.getLoadedChunks()) {
				if (!chunkscore.containsKey(ch)) {
					chunkscore.put(ch, 0);
				}
				BlockState[] tiles = ch.getTileEntities();
				for (BlockState blkst : tiles) {
					String m = blkst.getType().toString().toLowerCase();
					if (values.containsKey(m)) {
						chunkscore.put(ch, chunkscore.get(ch) + values.get(m));
					}
				}
				Entity[] ents = ch.getEntities();
				for (Entity e : ents) {
					String et = e.getType().toString().toLowerCase();
					if (values.containsKey(et)) {
						chunkscore.put(ch, chunkscore.get(ch) + values.get(et));
					}
				}

			}
		}
		p.sendMessage("");
		p.sendMessage("⬛⬛⬛⬛⬛⬛ §f§lCHUNKANALYSER ⬛⬛⬛⬛⬛⬛");
		p.sendMessage("");

		Stream<Map.Entry<Chunk, Integer>> sorted = chunkscore.entrySet().stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));

		Map<Chunk, Integer> topTen = sorted.limit(ammoshow)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		for (Chunk ch : topTen.keySet()) {
			int score = chunkscore.get(ch);
			p.sendMessage("  ✸ Chunk (" + String.valueOf(ch.getX()) + " " + String.valueOf(ch.getZ()) + ") - Score "
					+ String.valueOf(score));
		}
		p.sendMessage("");
		p.sendMessage("⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛");

	}

	public static void analyseCurrentChunk(CommandSender s) {
		if (!(s instanceof Player)) {
			s.sendMessage(Main.PREFIX + "You cannot analyse the current chunk from console.");
			return;
		}
		Player p = (Player) s;
		Location l = p.getLocation();
		ChkStats stats = new ChkStats(l.getChunk(), true);

		int coords[] = stats.getCoords();

		String crds = String.valueOf(coords[0]) + " " + String.valueOf(coords[1]);

		String cmd = "lagassist tpchunk " + p.getWorld().getName() + " " + String.valueOf(coords[0]) + " "
				+ String.valueOf(coords[1]);

		p.sendMessage("§2§l⬛⬛⬛⬛⬛⬛ §f§lCHUNK STATS §2§l⬛⬛⬛⬛⬛⬛");
		p.sendMessage("");
		p.sendMessage("  §2✸ §fChunk coordonates: §7" + crds);
		p.sendMessage("");
		p.sendMessage("  §2✸ §fEntity Amount: §7" + stats.getEnts().length);
		p.sendMessage("  §2✸ §fTiles Amount: §7" + stats.getTiles().length);
		p.sendMessage("");
		p.spigot().sendMessage(stats.genMobCount("  §2✸ §fDetailed Info §7(HOVER)", cmd));
		p.sendMessage("");
		p.sendMessage("§2§l⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛");

	}

}
