package com.entryrise.lagassist.economy;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.entryrise.lagassist.Main;

import net.milkbowl.vault.economy.Economy;

public class EconomyManager {

	public static Map<OfflinePlayer, Double> cache = new HashMap<OfflinePlayer, Double>();
	public static Economy econ;

	public static void Enabler(boolean reload) {
		if (setupEconomy() && !reload) {
			runPayTask();
		}
		
		Bukkit.getLogger().info("    §e[§a✔§e] §fEconomy manager");

	}

	private static boolean setupEconomy() {
		if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	public static void payPlayer(Player p, double amount) {
		payPlayer(Bukkit.getOfflinePlayer(p.getUniqueId()), amount);
	}
	
	public static void payPlayer(OfflinePlayer p, double amount) {
		cache.compute(p, (player, adder) -> (adder == null) ? amount : amount + adder);
	}

	private static final String MSGLOC = "hooks.vault.message";
	public static void runPayTask() {
		if (econ == null) {
			return;
		}
    	Bukkit.getScheduler().runTaskTimer(Main.p, () -> {
    		for (Entry<OfflinePlayer, Double> entr : cache.entrySet()) {
    			
    			
    			OfflinePlayer pl =entr.getKey();
    			double amount = entr.getValue();
    			
    			String msg = Main.config.getString(MSGLOC, "");
    			
    			if (pl.isOnline() && !msg.isEmpty()) {
    				pl.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', msg.replace("%amount%", String.format("%.2f", amount))));
    			}
    			
    			econ.depositPlayer(pl, amount);
    		}
    		
    		cache.clear();
    		
    	}, 100, Main.config.getLong("hooks.vault.cache-duration"));
    }
}
