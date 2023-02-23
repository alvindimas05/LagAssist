package com.entryrise.lagassist.utils;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Observer;

import com.entryrise.lagassist.Main;

public class V1_11 {

	public static Map<Block, Integer> removable = new HashMap<Block, Integer>();

	public static void ObserverAdd(Block b) {
		
		Material mat = Material.getMaterial("OBSERVER");
		
		if (mat == null) {
			return;
		}
		
		if (b.getType().equals(mat)) {
			int val = removable.compute(b, (k, v) -> v == null ? 1 : v+1);
			Main.sendDebug("Incremented value for observer: " + val, 2);
		}
	}

	public static void observerBreaker() {
		int min = Main.config.getInt("redstone-culler.destructive.value");
		for (Block bs : removable.keySet()) {
			if (removable.get(bs) > min) {
				bs.setType(Material.AIR);
			}
		}
		removable.clear();
	}

	public static BlockFace getFace(Block b) {
		Observer obs = (Observer) b.getState().getData();
		return obs.getFacing();
	}

	// public static void ObserverFix(Block b) {
	// if (b.getType().equals(Material.OBSERVER)) {
	// MaterialData md = b.getState().getData();
	// if (!(md instanceof Observer)) {
	// return;
	// }
	// Observer orw = (Observer) md;
	// BlockFace bf = orw.getFacing();
	// b.setType(Material.OBSERVER);
	// BlockState bstate = b.getState();
	// Observer bs = (Observer) bstate.getData();
	// bs.setFacingDirection(bf);
	// bstate.update();
	// System.out.println(bs.isPowered());
	// if (!Main.config.getBoolean("redstone-culler.destructive")) {
	// return;
	// }
	// Block b1 = b.getLocation().add(0, -1, 0).getBlock();
	// Block b2 = b.getLocation().add(0, -2, 0).getBlock();
	// Block b3 = b.getLocation().add(0, -3, 0).getBlock();
	// Block b4 = b.getLocation().add(0, 1, 0).getBlock();
	// Block b5 = b.getLocation().add(0, 2, 0).getBlock();
	// Block b6 = b.getLocation().add(0, 3, 0).getBlock();
	// if (b1.getType().equals(Material.OBSERVER) &&
	// b2.getType().equals(Material.OBSERVER)) {
	// b1.setType(Material.STONE);
	// b2.setType(Material.STONE);
	// b3.setType(Material.STONE);
	// }
	// if (b4.getType().equals(Material.OBSERVER) &&
	// b5.getType().equals(Material.OBSERVER)) {
	// b4.setType(Material.STONE);
	// b5.setType(Material.STONE);
	// b6.setType(Material.STONE);
	// }
	// }
	// }

	public static boolean isObserver(Block b) {
		if (b == null) {
			return false;
		}
		
		if (b.getType().equals(Material.getMaterial("OBSERVER"))) {
			return true;
		}
		return false;
	}

}
