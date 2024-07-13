package org.alvindimas05.lagassist.api;

import org.bukkit.Bukkit;

import org.alvindimas05.lagassist.Main;

public class APIManager {
	
	
	public static void Enabler(boolean reload) {
		Bukkit.getLogger().info("    §e[§a✔§e] §fAPI Tools.");
		
		if (!reload) {
			Main.p.getServer().getPluginManager().registerEvents(new MotdAPI(), Main.p);
		}
	}
}
