package com.entryrise.lagassist.gui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.entryrise.lagassist.hoppers.HopperFilter;

public class HopperGUI implements Listener {

	private static Map<Inventory, Location> locs = new HashMap<Inventory, Location>();
	
	public static Inventory getInventory(Player p, Location loc) {
		return createInventory(p.getName(), loc);
	}


	private static Inventory createInventory(String name, Location loc) {
		Inventory inv = Bukkit.createInventory(null, HopperFilter.filtersize, HopperFilter.guiname);
		addFilter(inv, loc);

		return inv;
	}

	private static void addFilter(Inventory inv, Location loc) {
		int i = 0;
		for (Material mat : HopperFilter.getFilter(loc)) {
			inv.setItem(i++, new ItemStack(mat));
		}
	}

	public static void show(Player p, Location loc) {
		Inventory inv = getInventory(p, loc);
		locs.put(inv, loc);
		
		p.openInventory(inv);
	}
	
	public static void exit(Location loc) {
		lastclosed.clear();
		if (!locs.containsValue(loc)) {
			return;
		}
		
		Set<Inventory> removables = new HashSet<Inventory>();
		
		for (Inventory inv : locs.keySet()) {
			Location iloc = locs.get(inv);
			
			if (iloc.equals(loc)) {
				removables.add(inv);
			}
			
			for (HumanEntity viewer : inv.getViewers()) {
				viewer.closeInventory();
				lastclosed.add(inv);
			}
		}
		
		locs.keySet().removeAll(removables);
	}
	
	private static Set<Inventory> lastclosed = new HashSet<Inventory>();;
	// Save filter when player closes the inventory.
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		Inventory inv = e.getInventory();
		
		// Closed because of block break.
		if (lastclosed.contains(inv)) {
			return;
		}
		
		HumanEntity hm = e.getPlayer();
		if (!(hm instanceof Player)) {
			return;
		}

		if (locs.containsKey(inv)) {
			HopperFilter.saveFilter(inv, locs.get(inv));
			locs.remove(inv);
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {

		
		HumanEntity hm = e.getWhoClicked();

		if (!(hm instanceof Player)) {
			return;
		}

		if (e.getRawSlot() < 0) {
			return;
		}
		
		Inventory clicked = e.getClickedInventory();
		
		Inventory invent = e.getView().getTopInventory();
		Inventory bottom = e.getView().getBottomInventory();
		
		// Click outside inv.
		if (invent == null) {
			return;
		}
		

		ItemStack hand = e.getCursor();
		ItemStack itm = e.getCurrentItem();
		
		if (!(e.getView().getTitle()).equals(HopperFilter.guiname)) {
			return;
		}
		
		if (!locs.containsKey(invent)) {
			return;
		}
		
		// Attempt to fix dupe.
		if (clicked.equals(bottom) && e.getClick() == ClickType.DOUBLE_CLICK) {
			e.setCancelled(true);
			return;
		}
		
		if (clicked.equals(invent) && !(e.getClick() == ClickType.LEFT || e.getClick() == ClickType.RIGHT)) {
			e.setCancelled(true);
			return;
		}
		
		if (clicked.equals(bottom)) {
			return;
		}
		
		if (hand == null && itm != null) {
			e.setCurrentItem(null);
		} else if (hand != null) {
			e.setCurrentItem(new ItemStack(hand.getType()));
		}

		e.setCursor(hand);
		e.setCancelled(true);
		
		
	}
	
	@EventHandler
	public void onInventoryDrag(InventoryDragEvent e) {

		
		HumanEntity hm = e.getWhoClicked();

		if (!(hm instanceof Player)) {
			return;
		}

		Inventory invent = e.getView().getTopInventory();
		
		// Click outside inv.
		if (invent == null) {
			return;
		}
		
		if (!(e.getView().getTitle()).equals(HopperFilter.guiname)) {
			return;
		}
		
		if (!locs.containsKey(invent)) {
			return;
		}
		
		e.setCancelled(true);
		
		
	}
	
	

}
