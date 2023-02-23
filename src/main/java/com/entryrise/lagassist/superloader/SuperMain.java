package com.entryrise.lagassist.superloader;

import org.bukkit.Bukkit;

import com.entryrise.lagassist.Main;

public class SuperMain {

	public static void Enabler(boolean reload) {
		if (!Main.config.getBoolean("super-loader.enabled")) {
			return;
		}
		
		Bukkit.getLogger().info("    §e[§a✔§e] §fSuperLoader.");
		
		
	}
	
}
