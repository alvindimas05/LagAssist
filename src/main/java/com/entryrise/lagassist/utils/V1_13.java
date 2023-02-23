package com.entryrise.lagassist.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTables;

import com.entryrise.lagassist.Data;
import com.entryrise.lagassist.packets.Reflection;

public class V1_13 {

	private static Random r = new Random();
	
	public static ItemStack getLagMap() {
		ItemStack map = new ItemStack(Material.getMaterial("FILLED_MAP"), 1);
		Reflection.setmapId(map, Data.getMapId());
		return map;
	}

	public static ItemStack[] getStatics() {

		ItemStack[] pnes = new ItemStack[19];
		pnes[0] = new ItemStack(Material.getMaterial("LIME_STAINED_GLASS_PANE"));
		pnes[1] = new ItemStack(Material.getMaterial("MAGENTA_STAINED_GLASS_PANE"));
		pnes[2] = new ItemStack(Material.getMaterial("YELLOW_STAINED_GLASS_PANE"));
		pnes[3] = new ItemStack(Material.getMaterial("RED_STAINED_GLASS_PANE"));

		pnes[4] = new ItemStack(Material.getMaterial("COMMAND_BLOCK"), 1);
		pnes[5] = new ItemStack(Material.getMaterial("FILLED_MAP"), 1);
		pnes[6] = new ItemStack(Material.getMaterial("GRASS"), 1);
		pnes[7] = new ItemStack(Material.getMaterial("SOUL_SAND"), 1);
		pnes[8] = new ItemStack(Material.getMaterial("PISTON"), 1);
		pnes[9] = new ItemStack(Material.getMaterial("REDSTONE_BLOCK"), 1);
		pnes[10] = new ItemStack(Material.getMaterial("EMERALD_BLOCK"), 1);
		pnes[11] = new ItemStack(Material.getMaterial("SPAWNER"), 1);
		pnes[12] = new ItemStack(Material.getMaterial("SEA_LANTERN"), 1);
		pnes[13] = new ItemStack(Material.getMaterial("TNT"), 1);
		pnes[14] = new ItemStack(Material.getMaterial("SAND"), 1);
		pnes[15] = new ItemStack(Material.getMaterial("PUMPKIN_SEEDS"), 1);
		pnes[16] = new ItemStack(Material.getMaterial("PISTON"), 1);
		pnes[17] = new ItemStack(Material.getMaterial("REDSTONE_BLOCK"), 1);
		pnes[18] = new ItemStack(Material.getMaterial("EMERALD_BLOCK"), 1);

		return pnes;

	}

	public static int getMapId(ItemStack s) {
		return Reflection.getMapId(s);
	}

	public static boolean isChunkGenerated(World w, int x, int z) {
		return w.isChunkGenerated(x, z);
	}
	
	public static List<ItemStack> getLootTable(Entity ent) {
		LootTables lt = LootTables.valueOf(ent.getType().toString());
		
		// TODO: FIX in 1.16
		// java.lang.IllegalArgumentException:
		// Missing required parameters:
		// [<parameter minecraft:this_entity>, <parameter minecraft:damage_source>]
			
		List<ItemStack> itms = new ArrayList<ItemStack>();
		
		if (lt == null) {
			return itms;
		}
		
		itms.addAll(lt.getLootTable().populateLoot(r, new LootContext.Builder(ent.getLocation()).lootedEntity(ent).build()));
		
		return itms;
	}
	
	public static Object setUnbreakable(ItemMeta imeta, boolean unbreakable) {
		imeta.setUnbreakable(unbreakable);
		return null;
	}

	public static boolean isUnbreakable(ItemMeta imeta) {
		return imeta.isUnbreakable();
	}

	public static void setViewDistance(World w, int amount) {
		Reflection.setViewDistance(w, amount);
	}
}
