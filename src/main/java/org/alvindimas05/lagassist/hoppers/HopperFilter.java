package org.alvindimas05.lagassist.hoppers;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.alvindimas05.lagassist.Data;
import org.alvindimas05.lagassist.Main;
import org.alvindimas05.lagassist.gui.HopperGUI;
import org.alvindimas05.lagassist.utils.Cache;
import org.alvindimas05.lagassist.utils.ServerType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import org.alvindimas05.lagassist.utils.V1_12;

public class HopperFilter implements Listener {

	private static final Cache<Location, Set<Material>> filtercache = new Cache<>(50);

	public static String guiname;
	public static int filtersize;
	public static Set<Material> defaultfilter;

	public static void Enabler(boolean reload) {
		guiname = ChatColor.translateAlternateColorCodes('&',
				Objects.requireNonNull(Main.config.getString("hopper-check.chunk-hoppers.filter.gui.name")));
		filtersize = Main.config.getInt("hopper-check.chunk-hoppers.filter.gui.size");

		defaultfilter = new HashSet<>();

		for (String stg : Main.config.getStringList("hopper-check.chunk-hoppers.filter.default")) {
			defaultfilter.add(Material.getMaterial(stg));
		}

		if (!reload) {
			Main.p.getServer().getPluginManager().registerEvents(new HopperFilter(), Main.p);
			runTask();
		}
	}

	private static void runTask() {
		long delay = 1L;
		long period = 1L;

		if (ServerType.isFolia()) {
			Bukkit.getGlobalRegionScheduler().runAtFixedRate(Main.p, task -> filtercache.tick(), delay, period);
		} else {
			new BukkitRunnable() {
				@Override
				public void run() {
					filtercache.tick();
				}
			}.runTaskTimer(Main.p, delay, period);
		}
	}

	public static Set<Material> getFilter(Location loc) {
        return Data.getFilterWhitelist(loc);
	}

	public static void saveFilter(Inventory inv, Location loc) {
		Set<Material> mats = new HashSet<>();
		for (ItemStack itm : inv.getContents()) {
			if (itm != null) {
				mats.add(itm.getType());
			}
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
		return mats.isEmpty() || mats.contains(mat);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBreak(BlockBreakEvent e) {
		if (e.isCancelled()) return;

		Block b = e.getBlock();
		if (b.getType() != Material.HOPPER) return;

		Hopper h = (Hopper) b.getState();
		String stg = V1_12.getHopperName(h);

		if (!ChunkHoppers.customname.equalsIgnoreCase("DEFAULT") &&
				(stg == null || !stg.equals(ChunkHoppers.customname))) return;

		if (!HopperManager.chunkhoppers) return;

		Location l = b.getLocation();
		HopperGUI.exit(l);
		Data.saveFilterWhitelist(l, null);
		filtercache.remove(l);
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.isCancelled()) return;
		if (!HopperManager.chunkhoppers) return;

		Player p = e.getPlayer();
		if (!p.isSneaking()) return;

		ItemStack itm = e.getItem();
		if (itm != null) return;

		Block b = e.getClickedBlock();
		if (b == null || b.getType() != Material.HOPPER) return;

		Hopper h = (Hopper) b.getState();
		String stg = V1_12.getHopperName(h);

		if (!ChunkHoppers.customname.equalsIgnoreCase("DEFAULT") &&
				(stg == null || !stg.equals(ChunkHoppers.customname))) return;

		Action a = e.getAction();

		if (a == Action.RIGHT_CLICK_BLOCK && p.hasPermission("lagassist.hoppers.customfilter")) {
			HopperGUI.show(p, b.getLocation());
			e.setCancelled(true);
		} else if (a == Action.LEFT_CLICK_BLOCK && p.hasPermission("lagassist.hoppers.togglesell")) {
			Data.toggleSellHopper(p, b.getLocation());
			e.setCancelled(true);
		}
	}
}
