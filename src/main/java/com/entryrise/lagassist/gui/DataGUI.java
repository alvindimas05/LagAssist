package com.entryrise.lagassist.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.entryrise.lagassist.Main;
import com.entryrise.lagassist.client.ClientMain;
import com.entryrise.lagassist.utils.VersionMgr;

public class DataGUI {

	// Glass Items
	protected static ItemStack gp;
	protected static ItemStack anp;
	protected static ItemStack optp;
	protected static ItemStack agp;

	// Analyse Items

	protected static ItemStack bnchie;
	protected static ItemStack lagmap;
	protected static ItemStack chkanalyse;
	protected static ItemStack ping;

	// Optimise Items

	protected static ItemStack physics;
	protected static ItemStack mobspawning;
	protected static ItemStack spawners;

	// Energise Items

	protected static ItemStack rcull;
	protected static ItemStack mobcull;

	// ClientGUI Items

	protected static ItemStack tnt;
	protected static ItemStack sand;
	protected static ItemStack particle;
	protected static ItemStack piston;
	// ClientGUI Toggles

	protected static ItemStack toggleon;
	protected static ItemStack toggleoff;

	public static void Enabler(boolean reload) {

		setItems();

		if (!reload) {
			Main.p.getServer().getPluginManager().registerEvents(new AdminGUI(), Main.p);
			Main.p.getServer().getPluginManager().registerEvents(new ClientGUI(), Main.p);
			Main.p.getServer().getPluginManager().registerEvents(new HopperGUI(), Main.p);
		}

		AdminGUI.Enabler();

		Bukkit.getLogger().info("    §e[§a✔§e] §fGUI.");
	}

	private static void setItems() {
		ItemStack[] pnes = VersionMgr.getStatics();

		gp = pnes[0];
		anp = pnes[1];
		optp = pnes[2];
		agp = pnes[3];

		bnchie = pnes[4];
		chkanalyse = pnes[5];
		lagmap = pnes[6];
		ping = pnes[7];

		physics = pnes[8];
		rcull = pnes[9];
		mobspawning = pnes[10];
		spawners = pnes[11];
		mobcull = pnes[12];
		tnt = pnes[13];
		sand = pnes[14];
		particle = pnes[15];
		piston = pnes[16];
		toggleon = pnes[17];
		toggleoff = pnes[18];

		customizeItem(gp, "&f", false);
		customizeItem(anp, "&5&lAnalyse", false, Arrays.asList("&f", "&dAnalysis tools provide a good way",
				"&dof finding lag-sources and checking performance.", "&f"));
		customizeItem(optp, "&6&lOptimise", false,
				Arrays.asList("&f", "&eOptimisation tools offer ways of improving performance by",
						"&echanging how the game works and behaves.", "&f"));
		customizeItem(agp, "&4&lEnergise", false,
				Arrays.asList("&f", "&cEnergyser tools are made to jolt performance, and will aggresively",
						"&cget rid of lag-sources.", "&f"));

		customizeItem(bnchie, "&dBenchmark System", false, Arrays.asList("&f", "&fGet a benchmark of your system",
				"&fthat can highly help you approximate", "&fthe capabilities of your system,", "&f"));
		customizeItem(lagmap, "&dLag Map", false, Arrays.asList("&f", "&fGet a higly accurate map",
				"&fthat graphs the amount of TPS", "&fyour server has.", "&f"));
		customizeItem(chkanalyse, "&dAnalyse chunks", false, Arrays.asList("&f", "&fGet a higly accurate map",
				"&fthat graphs the amount of TPS", "&fyour server has.", "&f"));
		customizeItem(ping, "&dPing Statistics", false,
				Arrays.asList("&f", "&fGet statistics of the Player's ping values.",
						"&fThis is highly helpful for finding the optimal location", "&fwhere to host your server",
						"&f"));

		customizeItem(physics, "&eToggle Physics", false,
				Arrays.asList("&f", "&fToggle Physics that have been enabled in the config.",
						"&fPhysics might be a large cause of lag, even though",
						"&fthey are not essential for the server.", "&f"));
		customizeItem(mobspawning, "&eToggle Mob Spawning", false,
				Arrays.asList("&f", "&fToggle the creation of new mobs.",
						"&fThis may help in some situations, but should only be", "&fused as a last resort.", "&f"));
		customizeItem(spawners, "&eOptimise Spawners", false,
				Arrays.asList("&f", "&fToggle the Spawner Optimizations.",
						"&fThis may highly help in situations where mobs from.",
						"&fMob-Spawners cause lag on the server.", "&f"));

		customizeItem(rcull, "&cCull Redstone", false,
				Arrays.asList("&f", "&fCull active redstone machines.",
						"&fThis will highly help with redstone lag-machines, and can even",
						"&fhelp prevent lag-machines from starting.", "&f"));
		customizeItem(mobcull, "&cCull Mobs", false,
				Arrays.asList("&f", "&fClear Mobs.", "&fThis may help when you have a lot of issues with mobs,",
						"&fbut if you need this it usually means", "&fthat the server has been misconfigured", "&f"));

		// Do the Clientside stuff.

		ClientMain.configureIcon(tnt, "tnt");
		ClientMain.configureIcon(sand, "sand");
		ClientMain.configureIcon(particle, "particle");
		ClientMain.configureIcon(piston, "piston");

		ClientMain.configureIcon(toggleon, "toggleon");
		ClientMain.configureIcon(toggleoff, "toggleoff");
	}

	public static void setBorders(Inventory inv) {
		for (int i = 4; i <= 49; i += 9) {
			inv.setItem(i - 4, gp);
			inv.setItem(i + 4, gp);
		}

		for (int i = 1; i <= 7; i++) {
			inv.setItem(i, gp);
			inv.setItem(i + 45, gp);
		}
	}

	public static void customizeItem(ItemStack s, String raw, boolean shining) {
		customizeItem(s, raw, shining, null);
		return;
	}

	public static void customizeItem(ItemStack s, String raw, boolean shining, List<String> rlore) {

		setShine(s, shining);

		String name = ChatColor.translateAlternateColorCodes('&', raw);

		ItemMeta smeta = s.getItemMeta();
		smeta.setDisplayName(name);

		if (rlore != null) {

			List<String> lore = new ArrayList<String>();

			for (String stg : rlore) {
				lore.add(ChatColor.translateAlternateColorCodes('&', stg));
			}

			smeta.setLore(lore);
		}

		s.setItemMeta(smeta);
	}

	public static void setShine(ItemStack s, boolean shining) {
		ItemMeta smeta = s.getItemMeta();

		if (shining) {
			smeta.addEnchant(Enchantment.DURABILITY, 1, true);
			smeta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
		}

		s.setItemMeta(smeta);

	}

	public static ItemStack getToggler(boolean bl) {
		return (bl) ? toggleoff : toggleon;
	}
}
