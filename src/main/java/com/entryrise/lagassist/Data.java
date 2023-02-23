package com.entryrise.lagassist;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Hopper;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.map.MapView;

import com.entryrise.lagassist.hoppers.SellHoppers;
import com.entryrise.lagassist.packets.Reflection;
import com.entryrise.lagassist.utils.WorldMgr;

public class Data {

	private static File dataf = new File(Main.p.getDataFolder(), "data.yml");
	private static FileConfiguration data = new YamlConfiguration();

	public static void Enabler() {

		try {
			if (!dataf.exists()) {
				dataf.createNewFile();
			}

			data.load(dataf);
		} catch (Exception e) {
			e.printStackTrace();

		}

		if (!data.contains("version")) {

			if (data.contains("hoppers")) {
				for (String rawh : data.getConfigurationSection("hoppers").getKeys(false)) {
					String loc = "hoppers." + rawh;

					List<String> values = data.getStringList(loc);
					data.set(loc, null);
					data.set(loc + ".materials", values);
				}
			}

			data.set("version", 1);

			saveData();

		}

	}

	private static long last;

	private static void saveData() {

		if (System.currentTimeMillis() - last < 3000) {
			return;
		}

		last = System.currentTimeMillis();

		Bukkit.getScheduler().runTaskAsynchronously(Main.p, () -> {
			try {
				data.save(dataf);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private static short genMapId() {
		MapView newe = Bukkit.createMap(Bukkit.getWorlds().get(0));
		short mapid = (short) Reflection.getId(newe);
		data.set("data.mapid", mapid);
		saveData();
		return mapid;
	}

	public static short getMapId() {
		if (data.contains("data.mapid")) {
			int mapid = data.getInt("data.mapid");
//			if (Reflection.getMapView(mapid) == null) {
//				return genMapId();
//			} else {
//				return mapid;
//			}
			return (short) mapid;
		} else {
			return genMapId();
		}

	}

	public static void deleteHopper(Hopper h) {
		String cloc = "hoppers." + WorldMgr.serializeLocation(h.getLocation());

		data.set(cloc, null);
		saveData();
	}

	public static boolean isSellHopper(Location loc) {
		String cloc = "hoppers." + WorldMgr.serializeLocation(loc);

		return data.getBoolean(cloc + ".sellhopper", false);
	}

	public static boolean toggleSellHopper(Player p, Location loc) {
		String cloc = "hoppers." + WorldMgr.serializeLocation(loc);

		String owner = data.getString(cloc + ".owner", "NONE");

		if (!(p.getUniqueId().toString().equals(owner) || p.hasPermission("lagassist.hopper.bypass"))) {
			p.sendMessage(Main.PREFIX + "You can't toggle selling for a hopper not owned by you");
			return false;
		}

		String percentage = "§e" + SellHoppers.getMultiplierPercentage(Bukkit.getOfflinePlayer(p.getUniqueId())) + "%";

		if (isSellHopper(loc)) {
			data.set(cloc + ".sellhopper", false);
			p.sendMessage(Main.PREFIX + "This sellhopper has been §2disabled§f at " + percentage + "§f.");
			saveData();
			return true;
		} else {
			data.set(cloc + ".sellhopper", true);
			p.sendMessage(Main.PREFIX + "This sellhopper has been §aenabled§f at " + percentage + "§f.");
			saveData();
			return true;
		}
	}

	public static OfflinePlayer getOwningPlayer(Location loc) {
		String cloc = "hoppers." + WorldMgr.serializeLocation(loc);

		if (data.contains(cloc + ".owner")) {
			return Bukkit.getOfflinePlayer(UUID.fromString(data.getString(cloc + ".owner")));
		}

		return null;
	}

	public static void setOwningPlayer(Location loc, OfflinePlayer owner) {
		String cloc = "hoppers." + WorldMgr.serializeLocation(loc);

		data.set(cloc + ".owner", owner.getUniqueId().toString());
		saveData();
	}

	public static Set<Material> getFilterWhitelist(Location loc) {
		String cloc = "hoppers." + WorldMgr.serializeLocation(loc);

		List<String> found = null;

		if (data.contains(cloc + ".materials")) {
			found = data.getStringList(cloc + ".materials");
		}

		Set<Material> allowed = new HashSet<Material>();

		if (found == null) {
			return allowed;
		}

		for (String stg : found) {
			allowed.add(Material.getMaterial(stg));
		}

		return allowed;
	}

	public static void saveFilterWhitelist(Location loc, Set<Material> mats) {
		String cloc = "hoppers." + WorldMgr.serializeLocation(loc) + ".materials";

		if (mats == null) {
			data.set(cloc, null);
			saveData();
			return;
		}

		List<String> allowed = new ArrayList<String>();

		for (Material mat : mats) {
			allowed.add(mat.name());
		}

		data.set(cloc, allowed);
		saveData();

	}
	
	public static void toggleAdvertising(CommandSender sender) {
		boolean advertising = !data.getBoolean("disable-advertising", false);
		data.set("disable-advertising", advertising);
		sender.sendMessage(Main.PREFIX + "Advertising disabled: " + advertising);
		saveData();
	}

	public static boolean isAdvertising() {
		return !data.getBoolean("disable-advertising");
	}
}
