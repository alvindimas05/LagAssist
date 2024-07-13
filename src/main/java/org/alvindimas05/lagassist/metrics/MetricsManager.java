package org.alvindimas05.lagassist.metrics;

import org.alvindimas05.lagassist.ExactTPS;
import org.alvindimas05.lagassist.Main;
import org.alvindimas05.lagassist.minebench.SpecsGetter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

public class MetricsManager {

	private static BStats stats;
	
	public static void Enabler(boolean reload) {
		
		if (reload) {
			return;
		}
		
		stats = new BStats(Main.p);
		
		stats.addCustomChart(new BStats.SimplePie("tps_base", () -> String.valueOf(Math.round(ExactTPS.getTPS(600)))));
		stats.addCustomChart(new BStats.SimplePie("cpu_used", () -> getProcessor()));
		
		stats.addCustomChart(new BStats.SingleLineChart("tile_entities", () -> getTileEntitiesCount()));
		stats.addCustomChart(new BStats.SingleLineChart("normal_entities", () -> getEntitiesCount()));
		stats.addCustomChart(new BStats.SingleLineChart("chunks_count", () -> getChunksCount()));
	}
	
	private static int getEntitiesCount() {
		int ents = 0;
		for (World w : Bukkit.getWorlds()) {
			ents+=w.getEntities().size();
		}
		
		return ents;
	}
	
	private static int getTileEntitiesCount() {
		int tents = 0;
		for (World w : Bukkit.getWorlds()) {
			for (Chunk chk : w.getLoadedChunks()) {
				tents += chk.getTileEntities().length;
			}
		}
		
		return tents;
	}
	
	private static int getChunksCount() {
		int chks = 0;
		for (World w : Bukkit.getWorlds()) {
			chks+=w.getLoadedChunks().length;
		}
		
		return chks;
	}
	
	private static String getProcessor() {
		return SpecsGetter.getCPU(SpecsGetter.getOS());
	}
	
}
