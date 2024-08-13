package org.alvindimas05.lagassist.packets;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import org.alvindimas05.lagassist.client.ClientMain;
import org.alvindimas05.lagassist.safety.SafetyManager;

import io.netty.channel.Channel;

public class PacketInjector {

	public static void Enabler() {
		if (!PacketMain.isPacketEnabled()) {
			return;
		}
		try {
			PacketInjector.refreshSessions();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void Disabler() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			removePlayer(p);
		}
	}

	public static void addPlayer(final Player p) {
		if (p == null) {
			return;
		}
		try {
			final Channel channel = ((CraftPlayer) p).getHandle().connection.connection.channel;
			if (channel.pipeline().get("LagAssist_Handler") == null) {
				final PacketHandler packetHandler = new PacketHandler(p);
				final BlacklistHandler blacklistHandler = new BlacklistHandler();
//				channel.pipeline().addLast("LagAssistPck", packetHandler);
				if (ClientMain.enabled) {
					channel.pipeline().addBefore("packet_handler", "LagAssist_Handler", packetHandler);
				}
				if (SafetyManager.enabled) {
					channel.pipeline().addAfter("splitter", "LagAssist_Blacklist", blacklistHandler);
				}
			}

//			System.out.println("PIPES");
//			for(String stg : channel.pipeline().toMap().keySet()) {
//				System.out.println(stg);
//			}
		} catch (final Exception t) {
			t.printStackTrace();
		}
	}

	public static void refreshSessions() {
		for (final Player player : Bukkit.getOnlinePlayers()) {
			PacketInjector.removePlayer(player);
			PacketInjector.addPlayer(player);
		}
	}

	public static void removePlayer(final Player p) {
		if (p == null) {
			return;
		}
		if (!p.isOnline()) {
			return;
		}
		try {

			final Channel channel = ((CraftPlayer) p).getHandle().connection.connection.channel;
			List<String> oldnames = new ArrayList<>(channel.pipeline().names());
			for (String pipe : oldnames) {
				if (!pipe.contains("LagAssist")) {
					continue;
				}
				channel.pipeline().remove(pipe);
			}
//			if (channel.pipeline().get("LagAssist_Handler") != null) {
//				channel.pipeline().remove("LagAssist_Handler");
//			}
//			if (channel.pipeline().get("LagAssist_Blacklist") != null) {
//				channel.pipeline().remove("LagAssist_Blacklist");
//			}
		} catch (Exception e) {
//			e.printStackTrace();
		}
	}
}
