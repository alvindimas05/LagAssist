package com.entryrise.lagassist.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.entryrise.lagassist.Main;

public class WorldMgr {

	public static List<String> blacklist = new ArrayList<String>();

	public static void Enabler() {
		if (Main.config.getBoolean("blacklisted-worlds.enabled")) {
			Bukkit.getLogger().info("    §e[§a✔§e] §fWorldManager.");
			blacklist = Main.config.getStringList("blacklisted-worlds.list");

		}
	}

	public static boolean isBlacklisted(World w) {
		return blacklist.contains(w.getName());
	}
	
	public static String serializeLocation(Location loc) {
		return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
	}
	
	public static Location deserializeLocation(String stg) {
		String[] raw = stg.split(",");
		
		return new Location(Bukkit.getWorld(raw[0]), Double.valueOf(raw[1]), Double.valueOf(raw[2]), Double.valueOf(raw[3]));
	}

}
