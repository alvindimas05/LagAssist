package com.entryrise.lagassist.safety;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.entryrise.lagassist.Main;
import com.entryrise.lagassist.packets.Reflection;
import com.google.common.collect.Maps;

import io.netty.channel.Channel;

public class SafetyAnticrash {

	private static Set<Channel> dropped = new HashSet<Channel>();

	public static Map<Player, Map<String, PacketData>> packetdata = Maps.newConcurrentMap();

	public static boolean isBlocked(Player p, Object msg, Channel ch) {
		if (!SafetyManager.enabled) {
			return false;
		}
//		if (WorldMgr.isBlacklisted(p.getWorld())) {
//			return false;
//		}

		if (isDropped(ch)) {
			return true;
		}

		try {
			String packet = msg.getClass().getSimpleName().toLowerCase();
			String loc = "safety-manager.anti-crasher.packets." + packet;

			if (!Main.config.contains(loc)) {
				return false;
			}

			String val = Reflection.getObjectSerialized(msg);
			long size = val.length();
			long count = getPacketCount(p, packet);

			String temp;

			if (SafetyManager.crash_debug) {
				System.out.println("===== " + packet.toUpperCase() + " =====");
				System.out.println("PACKET SIZE: " + size);
				System.out.println("PACKET COUNT: " + count);
				System.out.println("PACKET CONT:");
				System.out.println(val);
				System.out.println("======================");
			} else if (count > Main.config.getLong(loc + ".drop-threshold")) {
				dropPlayer(p, ch, packet.toUpperCase() + " TRESHOLD REACHED");
				return true;
			} else if (size > Main.config.getLong(loc + ".size")) {
				dropPlayer(p, ch, packet.toUpperCase() + " SIZE OVERFLOW - ");
				return true;
			} else if ((temp = isDenied(val, loc)) != null) {
				dropPlayer(p, ch, packet.toUpperCase() + " ILLEGAL VALUE - " + temp);
				return true;
			}

			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	private static int getPacketCount(Player p, String packet) {
		if (!packetdata.containsKey(p)) {
			packetdata.put(p, Maps.newConcurrentMap());
		}

		Map<String, PacketData> packets = packetdata.get(p);

		if (!packets.containsKey(packet)) {
			packets.put(packet, new PacketData());
		}
		
		PacketData data = packets.get(packet);
		
		return data.incrementCount(Main.config.getInt("safety-manager.anti-crasher.packets." + packet + ".decay"));
	}

	private static void dropPlayer(Player p, Channel ch, String reason) {
		Main.p.getLogger().warning("Player " + p.getName() + " dropped for malicious packets: " + reason);
		Bukkit.getScheduler().runTask(Main.p, () -> {
			p.kickPlayer("Malicious Packets - " + reason);
		});
		dropped.add(ch);
	}

	public static boolean isDropped(Channel ch) {
		if (!SafetyManager.enabled) {
			return false;
		}
		
		return ch == null || dropped.contains(ch);
	}

	protected static void startTask(Player p) {
//
//		long fix = lastfix.get(p);
//
//		if (fix + 1000 > System.currentTimeMillis()) {
//			return;
//		}
//
//		fix = System.currentTimeMillis();
//		lastfix.put(p, fix);
//
//		Map<String, Integer> vals = packetdata.get(p);
//
//		for (String packet : vals) {
//			vals.put(packet, Math.max(0,
//					vals.get(packet) - Main.config.getInt("safety-manager.anti-crasher." + packet + ".decay")));
//		}
//		Bukkit.getScheduler().runTaskTimerAsynchronously(Main.p, new Runnable() {
//			@Override
//			public void run() {
//				for (Map<String, Integer> vals : packetdata.values()) {
//					for (String packet : vals.keySet()) {
//						vals.put(packet, Math.max(0, vals.get(packet)-Main.config.getInt("safety-manager.anti-crasher." + packet + ".decay")));
//					}
//				}
//			}
//
//		}, 20L, 20L);
	}

	private static String isDenied(String val, String loc) {
		loc = loc + ".illegals";
		for (String illegal : Main.config.getStringList(loc)) {
			if (!val.contains(illegal)) {
				continue;
			}

			return illegal;
		}
		return null;
	}
	
	protected static class PacketData {
		private int count;
		private long lastclean = System.currentTimeMillis();
		
		
		public long getLastclean() {
			return lastclean;
		}
		
		public void setClean() {
			long time = System.currentTimeMillis();
			if (lastclean + 1000 > time) {
				return;
			}
			
			lastclean = time;
		}
		
		
		public int getCount() {
			return count;
		}
		
		
		
		public int incrementCount(int decay) {
			long old = lastclean;
			setClean();
			decay = (int) (((lastclean-old) / 1000) * decay);
			this.count = (int) Math.max(0, count + 1 - decay);
			return count;
		}
		
		
	}
}
