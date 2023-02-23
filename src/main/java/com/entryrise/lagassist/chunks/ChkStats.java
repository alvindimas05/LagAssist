package com.entryrise.lagassist.chunks;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Chunk;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;

import com.entryrise.lagassist.utils.Chat;

import net.md_5.bungee.api.chat.TextComponent;

public class ChkStats implements Comparable<ChkStats> {

	private int score = 0;
	private int coords[] = new int[2];

	private BlockState[] tiles;
	private Entity[] ents;
	private String world;

	private Map<String, Integer> amount = new HashMap<String, Integer>();

	public ChkStats(Chunk ch, boolean genscores) {
		tiles = ch.getTileEntities();
		ents = ch.getEntities();
		world = ch.getWorld().getName();

		coords[0] = ch.getX();
		coords[1] = ch.getZ();

		if (genscores) {
			genScores();
		}
	}

	public void genScores() {
		for (BlockState blkst : tiles) {
			String m = blkst.getType().toString().toLowerCase();
			if (ChkAnalyse.values.containsKey(m)) {
				score += ChkAnalyse.values.get(m);

				if (!amount.containsKey(m)) {
					amount.put(m, 0);
				}

				amount.put(m, amount.get(m) + 1);
			}
		}

		for (Entity ent : ents) {
			String m = ent.getType().toString().toLowerCase();
			if (ChkAnalyse.values.containsKey(m)) {
				score += ChkAnalyse.values.get(m);

				if (!amount.containsKey(m)) {
					amount.put(m, 0);
				}

				amount.put(m, amount.get(m) + 1);
			}
		}
	}

	public int getScore() {
		return score;
	}

	public int getX() {
		return coords[0];
	}

	public int getZ() {
		return coords[1];
	}

	public int[] getCoords() {
		return coords;
	}

	public Entity[] getEnts() {
		return ents;
	}

	public BlockState[] getTiles() {
		return tiles;
	}

	public TextComponent genMobCount(String s, String click) {

		Stream<Map.Entry<String, Integer>> sorted = amount.entrySet().stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));

		Map<String, Integer> tops = sorted.limit(ChkAnalyse.ammoshow)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		String regionfile = "r." + String.valueOf(coords[0] >> 5) + "." + String.valueOf(coords[1] >> 5) + ".mca";

		String empt = "\n§2✸ §fClick to teleport!\n\n§fChunk Information:\n  §2✸ §fWorld:§e "
				+ Chat.capitalize(world.toLowerCase()) + "\n  §2✸ §fRegion File:§e " + regionfile
				+ "\n\n§fMost often appearances:";
		for (String stg : tops.keySet()) {
			empt = empt + "\n" + "  §2✸ §f" + Chat.capitalize(stg.replace('_', ' ')) + ": §e"
					+ String.valueOf(tops.get(stg));
		}
		empt = empt + "\n";

		return Chat.genHoverAndRunCommandTextComponent(s, empt, click);
	}

	public TextComponent genText() {
		String name = "  §2✸ §fChunk §2(" + String.valueOf(coords[0]) + " " + String.valueOf(coords[1])
				+ ")§f Score: §e" + String.valueOf(score);
		String cmd = "lagassist tpchunk " + world + " " + String.valueOf(coords[0]) + " " + String.valueOf(coords[1]);
		return genMobCount(name, cmd);
	}

	@Override
	public int compareTo(ChkStats from) {
		int compareQuantity = from.getScore();

		return compareQuantity - score;
	}
}
