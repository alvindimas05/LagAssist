package com.entryrise.lagassist.gui;

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

import com.entryrise.lagassist.MsrExec;
import com.entryrise.lagassist.chunks.ChkAnalyse;
import com.entryrise.lagassist.minebench.Approximate;
import com.entryrise.lagassist.minebench.SpeedTest;

public class AdminGUI implements Listener {

	private static Inventory inv = Bukkit.createInventory(null, 54, "§0§lLagAssist Tools");

	public static void Enabler() {

		// Make Square borders
		DataGUI.setBorders(inv);

		// Add Category panes
		inv.setItem(2, DataGUI.anp);
		inv.setItem(4, DataGUI.optp);
		inv.setItem(6, DataGUI.agp);

		// Add Measures
		inv.setItem(11, DataGUI.bnchie);
		inv.setItem(20, DataGUI.lagmap);
		inv.setItem(29, DataGUI.chkanalyse);
		inv.setItem(38, DataGUI.ping);

		inv.setItem(13, DataGUI.physics);
		inv.setItem(22, DataGUI.mobspawning);
		inv.setItem(31, DataGUI.spawners);

		inv.setItem(15, DataGUI.rcull);
		inv.setItem(24, DataGUI.mobcull);

	}

	public static void show(Player p) {
		p.openInventory(inv);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {

		HumanEntity hm = e.getWhoClicked();

		if (!(hm instanceof Player)) {
			return;
		}

		Player p = (Player) e.getWhoClicked();

		ItemStack itm = e.getCurrentItem();

		if (itm == null) {
			return;
		}

		if (!ChatColor.stripColor(e.getView().getTitle()).equals("LagAssist Tools")) {
			return;
		}

		e.setCancelled(true);

		Material i = itm.getType();

		if (i == DataGUI.bnchie.getType()) {
			Approximate.showBenchmark(p);
		} else if (i == DataGUI.lagmap.getType()) {
			MsrExec.giveMap(p);
		} else if (i == DataGUI.chkanalyse.getType()) {
			ChkAnalyse.analyseChunks(p);
		} else if (i == DataGUI.ping.getType()) {
			SpeedTest.pingBenchmark(p);
		} else if (i == DataGUI.physics.getType()) {
			MsrExec.togglePhysics(p);
		} else if (i == DataGUI.mobspawning.getType()) {
			MsrExec.toggleMobs(p);
		} else if (i == DataGUI.spawners.getType()) {
			MsrExec.toggleSpawnerOptimization(p);
		} else if (i == DataGUI.rcull.getType()) {
			MsrExec.cullRedstone(p);
		} else if (i == DataGUI.mobcull.getType()) {
			MsrExec.cullMobs(p);
		}
	}
}
