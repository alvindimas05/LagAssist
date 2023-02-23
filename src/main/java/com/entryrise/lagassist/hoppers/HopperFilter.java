package com.entryrise.lagassist.hoppers;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.entryrise.lagassist.Data;
import com.entryrise.lagassist.Main;
import com.entryrise.lagassist.gui.HopperGUI;
import com.entryrise.lagassist.utils.Cache;
import com.entryrise.lagassist.utils.V1_12;
import com.entryrise.lagassist.utils.VersionMgr;

public class HopperFilter implements Listener {

	private static Cache<Location, Set<Material>> filtercache = new Cache<Location, Set<Material>>(50);
	
	// HopperGUI data
	public static String guiname;
	public static int filtersize;
	
	// Default Filter
	public static Set<Material> defaultfilter;
	
	public static void Enabler(boolean reload) {
		guiname = ChatColor.translateAlternateColorCodes('&', Main.config.getString("hopper-check.chunk-hoppers.filter.gui.name"));
		filtersize = Main.config.getInt("hopper-check.chunk-hoppers.filter.gui.size");
		
		defaultfilter = new HashSet<Material>();
		
		for (String stg : Main.config.getStringList("hopper-check.chunk-hoppers.filter.default")) {
			defaultfilter.add(Material.getMaterial(stg));
		}
		
		if (!reload) {
			Main.p.getServer().getPluginManager().registerEvents(new HopperFilter(), Main.p);
			runTask();
		}
	}
	
	private static void runTask() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.p, new Runnable() {
			@Override
			public void run() {
				filtercache.tick();
			}
		}, 1L, 1L);
	}
	
	public static Set<Material> getFilter(Location loc) {
		Set<Material> cnf = Data.getFilterWhitelist(loc);
		
		if (cnf == null) {
			cnf = defaultfilter;
		}
		
		return cnf;
	}
	
	public static void saveFilter(Inventory inv, Location loc) {
		Set<Material> mats = new HashSet<Material>();
		
		for (ItemStack itm : inv.getContents()) {
			if (itm == null) {
				continue;
			}
			
			Material mat = itm.getType();
			
			mats.add(mat);
		}
		
		Data.saveFilterWhitelist(loc, mats);
	}
	
	public static Set<Material> getAllowedMaterials(Location loc) {
		if (!filtercache.isCached(loc)) {
			filtercache.putCached(loc, getFilter(loc));
		}
		
		return filtercache.getCached(loc);
	}
	
	public static boolean isAllowed(Location loc, Material mat) {
		Set<Material> mats = getAllowedMaterials(loc);
		
		if (mats.isEmpty()) {
			return true;
		}
		
		return mats.contains(mat);
	}
	
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onBreak(BlockBreakEvent e) {
		if (e.isCancelled()) {
			return;
		}
		
		Block b = e.getBlock();
		
		
		if (b.getType() != Material.HOPPER) {
			return;
		}
		
		Hopper h = (Hopper) b.getState();
		
		String stg = V1_12.getHopperName(h);
		
		// Check if not default; and if it isn't default; check if the string is null or it isn't custom and continue.
		if (!ChunkHoppers.customname.equalsIgnoreCase("DEFAULT") && (stg == null || !stg.equals(ChunkHoppers.customname))) {
			return;
		}
		
		if (!HopperManager.chunkhoppers) {
			return;
		}

		Location l = b.getLocation();
		
		HopperGUI.exit(l);
		Data.saveFilterWhitelist(l, null);
		filtercache.remove(l);
		
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.isCancelled()) {
			return;
		}
		
		// Not activated
		if (!HopperManager.chunkhoppers) {
			return;
		}

		
		// Not sneaking.
		Player p = e.getPlayer();
		
		if (!p.isSneaking()) {
			return;
		}
		
		// No permission
//		if (!p.hasPermission("lagassist.customfilter")) {
//			return;
//		}
		
		// Not having an empty hand
		ItemStack itm = e.getItem();
		
		if (itm != null) {
			return;
		}
		
		
		// Not clicking a hopper.
		Block b = e.getClickedBlock();
		
		if (b.getType() != Material.HOPPER) {
			return;
		}
		
		Hopper h = (Hopper) b.getState();
		
		String stg = V1_12.getHopperName(h);
		
		// Check if not default; and if it isn't default; check if the string is null or it isn't custom and continue.
		if (!ChunkHoppers.customname.equalsIgnoreCase("DEFAULT") && (stg == null || !stg.equals(ChunkHoppers.customname))) {
			return;
		}
		
		// Not right clicking a block.
		Action a = e.getAction();
		
		if (a == Action.RIGHT_CLICK_BLOCK && p.hasPermission("lagassist.hoppers.customfilter")) {
			// Opening filter inventory.
			HopperGUI.show(p, b.getLocation());
			
			e.setCancelled(true);
		} else if (a == Action.LEFT_CLICK_BLOCK && p.hasPermission("lagassist.hoppers.togglesell")) {
			Data.toggleSellHopper(p, b.getLocation());
			
			e.setCancelled(true);
		}
		
	}
	
}
