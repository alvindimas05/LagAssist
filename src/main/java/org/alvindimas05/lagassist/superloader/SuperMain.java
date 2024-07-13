package org.alvindimas05.lagassist.superloader;

import org.alvindimas05.lagassist.Main;
import org.bukkit.Bukkit;

public class SuperMain {

	public static void Enabler(boolean reload) {
		if (!Main.config.getBoolean("super-loader.enabled")) {
			return;
		}
		
		Bukkit.getLogger().info("    §e[§a✔§e] §fSuperLoader.");
		
		
	}
	
}
