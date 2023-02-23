package com.entryrise.lagassist.gui;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.entryrise.lagassist.client.ClientMain;

public class ClientGUI implements Listener {

	private static Map<String, Inventory> invs = new HashMap<String, Inventory>();

	public static Inventory getInventory(Player p) {
		return getInventory(p.getName());
	}

	public static boolean existsAlready(Player p) {
		return existsAlready(p.getName());
	}

	public static boolean existsAlready(String name) {

		return invs.containsKey(name);
	}

	public static Inventory getInventory(String name) {
		if (existsAlready(name)) {
			return invs.get(name);
		} else {
			return createInventory(name);
		}
	}

	private static Inventory createInventory(String name) {
		Inventory inv = Bukkit.createInventory(null, 54, ClientMain.guiname);

		DataGUI.setBorders(inv);
		setToggles(inv);

		invs.put(name, inv);

		return inv;
	}

	private static void setToggles(Inventory inv) {
		boolean[] def = ClientMain.defaults;

		inv.setItem(ToggleState.TNT.nr, DataGUI.tnt);
		inv.setItem(ToggleState.SAND.nr, DataGUI.sand);
		inv.setItem(ToggleState.PARTICLES.nr, DataGUI.particle);
		inv.setItem(ToggleState.PISTONS.nr, DataGUI.piston);

		inv.setItem(ToggleState.TNT.nr + 9, DataGUI.getToggler(def[0]));
		inv.setItem(ToggleState.SAND.nr + 9, DataGUI.getToggler(def[1]));
		inv.setItem(ToggleState.PARTICLES.nr + 9, DataGUI.getToggler(def[2]));
		inv.setItem(ToggleState.PISTONS.nr + 9, DataGUI.getToggler(def[3]));
	}

	private static boolean getDefault(ToggleState t) {
		boolean[] def = ClientMain.defaults;
		if (t == ToggleState.TNT) {
			return def[0];
		}
		if (t == ToggleState.SAND) {
			return def[1];
		}
		if (t == ToggleState.PARTICLES) {
			return def[2];
		}
		if (t == ToggleState.PISTONS) {
			return def[3];
		}
		return false;
	}

	public static void show(Player p) {
		p.openInventory(getInventory(p));
	}

	public static boolean isOn(ToggleState t, Player p) {
		if (!existsAlready(p)) {
			return getDefault(t);
		}
		Inventory inv = getInventory(p);
		ItemStack s = inv.getItem(t.getNr() + 9);

		if (s.equals(DataGUI.toggleoff)) {
			return true;
		} else {
			return false;
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {

		HumanEntity hm = e.getWhoClicked();

		if (!(hm instanceof Player)) {
			return;
		}

		Inventory invent = e.getInventory();

		ItemStack itm = e.getCurrentItem();

		if (itm == null) {
			return;
		}

		
		if (!(ChatColor.stripColor(e.getView().getTitle())).equals(ChatColor.stripColor(ClientMain.guiname))) {
			return;
		}

		e.setCancelled(true);

		Material i = itm.getType();
		int slot = e.getSlot();

		if (i == DataGUI.toggleon.getType()) {
			invent.setItem(slot, DataGUI.toggleoff);
		} else if (i == DataGUI.toggleoff.getType()) {
			invent.setItem(slot, DataGUI.toggleon);
		}
	}

	public enum ToggleState {

		TNT(20), SAND(21), PARTICLES(23), PISTONS(24);

		private final int nr;

		private ToggleState(int nr) {
			this.nr = nr;
		}

		public int getNr() {
			return nr;
		}
	}

}
