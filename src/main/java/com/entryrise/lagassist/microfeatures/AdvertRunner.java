package com.entryrise.lagassist.microfeatures;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.entryrise.lagassist.Data;
import com.entryrise.lagassist.Main;
import com.entryrise.lagassist.utils.Chat;

public class AdvertRunner implements Listener, PluginMessageListener {

	public AdvertRunner() {
		Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(Main.p, "BungeeCord");
		Bukkit.getServer().getMessenger().registerIncomingPluginChannel(Main.p, "BungeeCord", this);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		if (!Data.isAdvertising()) {
			return;
		}

		if (!p.isOp()) {
			return;
		}

		if (Bukkit.spigot().getSpigotConfig().getBoolean("bungeecord")) {
			p.sendPluginMessage(Main.p, "BungeeCord", new byte[1]);
		} else {
			// TODO: FINSIH non-lousy advertising
			sendAdvertising(null, 1);
		}

	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		// TODO Auto-generated method stub

	}

	private static void sendAdvertising(Player p, int players) {
		if (players < 75) {
			return;
		}

		String plan = players > 300 ? "§f§lPLATINUM" : players > 100 ? "§6§lGOLD" : "§7§lSILVER";

		String ad = "§2§l⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛§f§l ENTRYRISE §2§l⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛"
				+ "\n\n"
				+ "     §a✸ §7We have noticed that your server is popular. Hooray!\n"
				+ "     §a✸ §7Entryrise, the team behind LagAssist, provides system\n"
				+ "     §a✸ §7administration and server management services for\n"
				+ "     §a✸ §7servers, including enterprise networks with thousands of concurrent players.\n\n"
				+ "     §a✸ §7The " + plan + "§7 plan should be ideal for your usecase.\n\n";
		
		
		ad = ad + "     §a✸ §7Click here for more info or to disable the ad."
				+ "§2§l⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛";
				
		final String adf = ad;
		
		Bukkit.getScheduler().runTaskLaterAsynchronously(Main.p, () -> {
			p.spigot().sendMessage(Chat.genHoverAndLinkComponent(adf + "", "https://www.entryrise.com", "Use /lagassist advertising to disable this message from showing"));
		}, 60);
	}

}
