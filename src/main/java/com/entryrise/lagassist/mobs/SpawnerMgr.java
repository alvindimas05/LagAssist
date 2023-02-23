package com.entryrise.lagassist.mobs;

import java.util.SplittableRandom;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;

import com.entryrise.lagassist.Main;
import com.entryrise.lagassist.utils.V1_12;
import com.entryrise.lagassist.utils.VersionMgr;
import com.entryrise.lagassist.utils.WorldMgr;

public class SpawnerMgr implements Listener {

	private static boolean enabled;
	public static boolean active = false;

	private static int chance;
	private static boolean breaker;

	private static int delay;
	private static int countmin;
	private static int countmax;
	private static int spawnrange;
	private static int playerrange;

	private static SplittableRandom rand = new SplittableRandom();

	public static void Enabler(boolean reload) {
		enabled = Main.config.getBoolean("spawner-check.enabled");

		if (!enabled) {
			return;
		}

		if (!reload) {
			Main.p.getServer().getPluginManager().registerEvents(new SpawnerMgr(), Main.p);
		}

		breaker = Main.config.getBoolean("spawner-check.breaker");
		chance = Main.config.getInt("spawner-check.chance");

		delay = Main.config.getInt("spawner-check.custom-settings.delay");
		countmin = Main.config.getInt("spawner-check.custom-settings.amount.min");
		countmax = Main.config.getInt("spawner-check.custom-settings.amount.max");
		spawnrange = Main.config.getInt("spawner-check.custom-settings.spawnrange");

		int ntsqrt = Main.config.getInt("spawner-check.custom-settings.player-range");
		playerrange = ntsqrt * ntsqrt;

		Bukkit.getLogger().info("    §e[§a✔§e] §fSpawner Manager.");
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onMSpawn(SpawnerSpawnEvent e) {
		CreatureSpawner cs = e.getSpawner();

		if (!enabled) {
			return;
		}

		if (!active) {
			return;
		}

		if (cs == null) {
			return;
		}

		if (WorldMgr.isBlacklisted(cs.getWorld())) {
			return;
		}
		
		if (breaker) {
			Block b = cs.getBlock();
			int rando = rand.nextInt(0, 1000);
			if (rando < chance) {
				b.setType(Material.AIR);
			}
		}

		boolean updated = false;

		if (cs.getDelay() != delay) {
			cs.setDelay(delay);
			updated = true;
		}

		if (VersionMgr.isV1_12()) {
			updated = V1_12.modifySpawner(cs, rand.nextInt(countmin, countmax + 1), spawnrange, playerrange);
		}

		if (updated) {
			cs.update();
		}
	}

}
