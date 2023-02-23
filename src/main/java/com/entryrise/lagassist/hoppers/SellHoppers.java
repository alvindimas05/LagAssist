package com.entryrise.lagassist.hoppers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

import com.entryrise.lagassist.Data;
import com.entryrise.lagassist.Main;
import com.entryrise.lagassist.economy.EconomyManager;
import com.entryrise.lagassist.utils.MathUtils;

public class SellHoppers {

	private static Map<Material, Double> prices = new HashMap<Material, Double>();
	
	private static boolean enabled;
	
	public static void Enabler(boolean reload) {
		
		enabled = Main.config.getBoolean("hopper-check.chunk-hoppers.sell-hopper.enabled");
		
		if (!enabled) {
			return;
		}
		
		String loc = "hopper-check.chunk-hoppers.sell-hopper.prices";
		
		prices.clear();
		for (String stg : Main.config.getConfigurationSection(loc).getKeys(false)) {
			prices.put(Material.getMaterial(stg), Main.config.getDouble(loc + "." + stg));
		}
		
		Bukkit.getLogger().info("    §e[§a✔§e] §fSellHopper System");
	}
	
	public static double getMultiplierPercentage(OfflinePlayer pl) {
		if (!pl.isOnline()) {
			return 0;
		}
		
		Player p = pl.getPlayer();
		
		int multiplier = 100;
		
		for (PermissionAttachmentInfo permi : p.getEffectivePermissions()) {
			String perm = permi.getPermission();
			
			if (!perm.startsWith("lagassist.sellhopper.")) {
				continue;
			}
			
			perm = perm.replace("lagassist.sellhopper.", "");
			
			if (!MathUtils.isInt(perm)) {
				continue;
			}
			
			multiplier = Math.max(multiplier, Integer.valueOf(perm));
		}
		
		return multiplier;
	}
	
	public static boolean attemptSell(Hopper h, ItemStack itm) { 
		if (!enabled) {
			return false;
		}
		
		Location loc = h.getLocation();
		
		OfflinePlayer owner = Data.getOwningPlayer(loc);
		
		if (owner == null) {
			return false;
		}
		
		if (!Data.isSellHopper(loc)) {
			return false;
		}
		
		if (!prices.containsKey(itm.getType())) {
			return false;
		}
		
		double price = prices.get(itm.getType()) * itm.getAmount() * getMultiplierPercentage(owner) / 100d;
		
		if (price == 0) {
			return false;
		}
		
		EconomyManager.payPlayer(owner, price);
		return true;
	}
	
}
