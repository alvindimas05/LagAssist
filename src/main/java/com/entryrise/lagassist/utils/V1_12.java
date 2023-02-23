package com.entryrise.lagassist.utils;

import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.ItemStack;

import com.entryrise.lagassist.Data;

@SuppressWarnings("deprecation")
public class V1_12 {

	public static ItemStack[] getStatics() {

		Material pane = Material.getMaterial("STAINED_GLASS_PANE");

		ItemStack[] pnes = new ItemStack[19];

		pnes[0] = new ItemStack(pane, 1, (short) 5);
		pnes[1] = new ItemStack(pane, 1, (short) 10);
		pnes[2] = new ItemStack(pane, 1, (short) 4);
		pnes[3] = new ItemStack(pane, 1, (short) 14);
		pnes[4] = new ItemStack(Material.getMaterial("COMMAND"), 1);
		pnes[5] = new ItemStack(Material.getMaterial("MAP"), 1);
		pnes[6] = new ItemStack(Material.getMaterial("GRASS"), 1);
		pnes[7] = new ItemStack(Material.getMaterial("SOUL_SAND"), 1);
		pnes[8] = new ItemStack(Material.getMaterial("PISTON_BASE"), 1);
		pnes[9] = new ItemStack(Material.getMaterial("REDSTONE_BLOCK"), 1);
		pnes[10] = new ItemStack(Material.getMaterial("EMERALD_ORE"), 1);
		pnes[11] = new ItemStack(Material.getMaterial("MOB_SPAWNER"), 1);
		pnes[12] = new ItemStack(Material.getMaterial("SEA_LANTERN"), 1);
		pnes[13] = new ItemStack(Material.getMaterial("TNT"), 1);
		pnes[14] = new ItemStack(Material.getMaterial("SAND"), 1);
		pnes[15] = new ItemStack(Material.getMaterial("PUMPKIN_SEEDS"), 1);
		pnes[16] = new ItemStack(Material.getMaterial("PISTON_BASE"), 1);
		pnes[17] = new ItemStack(Material.getMaterial("REDSTONE_BLOCK"), 1);
		pnes[18] = new ItemStack(Material.getMaterial("EMERALD_BLOCK"), 1);

		return pnes;

	}

	public static ItemStack getLagMap() {
		return new ItemStack(Material.getMaterial("MAP"), 1, Data.getMapId());
	}

	public static int getMapId(ItemStack s) {
		return s.getDurability();
	}

	public static boolean modifySpawner(CreatureSpawner cs, int spawncount, int spawnrange, int playerrange) {

		boolean changed = false;

		if (cs.getRequiredPlayerRange() != playerrange) {
			cs.setRequiredPlayerRange(playerrange);
			changed = true;
		}
		if (cs.getSpawnCount() != spawncount) {
			cs.setSpawnCount(spawncount);
			changed = true;
		}
		if (cs.getSpawnRange() != spawnrange) {
			cs.setSpawnRange(spawnrange);
			changed = true;
		}
		return changed;
	}
	
	public static String getHopperName(Hopper h) {
		return h.getCustomName() == null ? "container.hopper" : h.getCustomName();
	}

}
