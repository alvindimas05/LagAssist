package com.entryrise.lagassist;

import java.util.SplittableRandom;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.entryrise.lagassist.utils.V1_11;
import com.entryrise.lagassist.utils.WorldMgr;

public class Redstone implements Listener {

	SplittableRandom sr = new SplittableRandom();

	public static boolean redstoneculler;

	private static boolean destructives;
	private static int chance;
	private static int ticks;

	private static BukkitTask br;

	public static void Enabler(boolean reload) {
		redstoneculler = false;
		destructives = Main.config.getBoolean("redstone-culler.destructive.enabled");
		chance = Main.config.getInt("redstone-culler.chance");
		ticks = Main.config.getInt("redstone-culler.ticks");

		if (!reload) {
			Main.p.getServer().getPluginManager().registerEvents(new Redstone(), Main.p);
		}

		Bukkit.getLogger().info("    §e[§a✔§e] §fRedstone Culler.");
	}

	public static void CullRedstone() {
		if (!redstoneculler) {
			redstoneculler = true;
			setTimer();
		} else if (Bukkit.getScheduler().isCurrentlyRunning(br.getTaskId())) {
			br.cancel();
			setTimer();
		}
	}

	private static void setTimer() {
		br = new BukkitRunnable() {
			@Override
			public void run() {
				redstoneculler = false;
				V1_11.observerBreaker();
			}

		}.runTaskLater(Main.p, ticks);
	}

	@EventHandler
	public void redstoneCuller(BlockRedstoneEvent e) {
		if (!redstoneculler) {
			return;
		}
		if (WorldMgr.isBlacklisted(e.getBlock().getWorld())) {
			return;
		}
		e.setNewCurrent(0);

		if (chance < 0) {
			return;
		}

		Block b = e.getBlock();
		
		if (!Main.config.getStringList("redstone-culler.affected-materials").contains(b.getType().toString().toUpperCase())) {
			return;
		}

		int rand = sr.nextInt(100);
		if (rand > chance) {
			return;
		}

		b.setType(Material.AIR);
	}

	@EventHandler
	public void ObserverCuller(BlockPhysicsEvent e) {
		if (redstoneculler) {
			if (WorldMgr.isBlacklisted(e.getBlock().getWorld())) {
				return;
			}
//			if (VersionMgr.isNewMaterials()) {
				if (destructives) {
					V1_11.ObserverAdd(e.getBlock());
				}
				if (V1_11.isObserver(e.getBlock())) {
					e.setCancelled(true);
				}
//			}
		}
	}

}
