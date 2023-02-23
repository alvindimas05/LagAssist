package com.entryrise.lagassist.packets;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.entryrise.lagassist.client.ClientMain;
import com.entryrise.lagassist.safety.SafetyManager;
import com.entryrise.lagassist.utils.VersionMgr;

import io.netty.channel.Channel;

public class PacketInjector {
	private static Field channel;
	private static Field networkManager;
	private static Field playerConnection;

	public static void Enabler() {
		if (!PacketMain.isPacketEnabled()) {
			return;
		}
		try {
			PacketInjector.playerConnection = Reflection.getClass(VersionMgr.isV_17Plus() ? "{nms}.level.EntityPlayer" : "{nms}.EntityPlayer").getField(VersionMgr.isV_17Plus() ? "b" : "playerConnection");
			PacketInjector.networkManager = Reflection.getClass(VersionMgr.isV_17Plus() ? "{nms}.network.PlayerConnection" : "{nms}.PlayerConnection").getField(VersionMgr.isV_17Plus() ? "a" : "networkManager");

			PacketInjector.channel = Reflection.getClass(VersionMgr.isV_17Plus() ? "{nm}.network.NetworkManager" : "{nms}.NetworkManager").getField(VersionMgr.isV_17Plus() ? "k" : "channel");

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
			final Channel channel = PacketInjector
					.getChannel(PacketInjector.getNetworkManager(Reflection.getNmsPlayer(p)));
			if (channel == null) {
				return;
			}
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

	private static Channel getChannel(final Object networkManager) {
		if (networkManager == null) {
			return null;
		}
		Channel channel = null;
		try {
			channel = (Channel) PacketInjector.channel.get(networkManager);
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return channel;
	}

	private static Object getNetworkManager(final Object entityPlayer) {
		if (entityPlayer == null) {
			return null;
		}
		Object networkManager = null;
		try {
			networkManager = PacketInjector.networkManager.get(PacketInjector.playerConnection.get(entityPlayer));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return networkManager;
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

			final Channel channel = PacketInjector
					.getChannel(PacketInjector.getNetworkManager(Reflection.getNmsPlayer(p)));
			if (channel == null) {
				return;
			}
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
