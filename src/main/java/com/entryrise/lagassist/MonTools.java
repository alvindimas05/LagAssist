package com.entryrise.lagassist;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapView;

import com.entryrise.lagassist.maps.TpsRender;
import com.entryrise.lagassist.minebench.SpecsGetter;
import com.entryrise.lagassist.packets.Reflection;
import com.entryrise.lagassist.utils.VersionMgr;

public class MonTools implements Listener {

	public static ItemStack mapitem = VersionMgr.getMap();
	public static ItemMeta mapitemmeta = mapitem.getItemMeta();

	public static List<UUID> actionmon = new ArrayList<UUID>();
	public static List<UUID> mapusers = new ArrayList<UUID>();
	private static DecimalFormat format = new DecimalFormat("#0.00");

	private static String stbmsg = Main.config.getString("stats-bar.message");
	private static int stbinterv = Main.config.getInt("stats-bar.tps-interval");
	private static int stbshowdl = Main.config.getInt("stats-bar.show-delay");

	public static void Enabler(boolean reload) {
		if (!reload) {
			Main.p.getServer().getPluginManager().registerEvents(new MonTools(), Main.p);
		}

		Bukkit.getLogger().info("    §e[§a✔§e] §fMapVisualizer.");
		mapitemmeta.setDisplayName("§2§lLag§f§lAssist §e§lMonitor");
		mapitem.setItemMeta(mapitemmeta);

		int mapid = VersionMgr.getMapId(mapitem);
		
		MapView view = Reflection.getMapView(mapid);
		
		if (view != null) {
			view.getRenderers().clear();
			view.addRenderer(new TpsRender());
		}
		

		StatsBar();
	}

	public static void StatsBar() {
		Bukkit.getScheduler().runTaskTimer(Main.p, () -> {
				if (actionmon.size() == 0) {
					return;
				}
				
				boolean found = false;
				
				for (int i = 0; i<actionmon.size();i++) {
					if (Bukkit.getPlayer(actionmon.get(i)) != null) {
						found = true;
						break;
					}
				}
				
				if (!found) {
					return;
				}
			
				double tpsraw = (ExactTPS.getTPS(10) > 20) ? 20 : ExactTPS.getTPS(stbinterv);
				
				String chunks = String.valueOf(getChunkCount());
				String ents =  String.valueOf(getEntCount());
				
				Bukkit.getScheduler().runTaskAsynchronously(Main.p, () -> {
					if (actionmon.isEmpty()) {
						return;
					}
					String tps;
					if (tpsraw > 18) {
						tps = "§a" + format.format(tpsraw);
					} else if (tpsraw > 15) {
						tps = "§e" + format.format(tpsraw);
					} else {
						tps = "§2" + format.format(tpsraw);
					}
					String s = ChatColor.translateAlternateColorCodes('&',
							stbmsg.replaceAll("\\{TPS\\}", tps)
									.replaceAll("\\{MEM\\}", format.format(SpecsGetter.FreeRam() / 1024))
									.replaceAll("\\{CHKS\\}", chunks)
									.replaceAll("\\{ENT\\}", ents));
					for (UUID u : actionmon) {
						Player p = Bukkit.getPlayer(u);
						
						if (p == null) {
							continue;
						}
						
						Reflection.sendAction(p, s);
					}
				});

		}, stbshowdl, stbshowdl);
	}

	public static int getEntCount() {
		int lng = 0;
		for (World w : Bukkit.getWorlds()) {
			lng += w.getEntities().size();
		}
		return lng;
	}

	public static int getChunkCount() {
		int lng = 0;
		for (World w : Bukkit.getWorlds()) {
			lng += w.getLoadedChunks().length;
		}
		return lng;
	}

	@EventHandler
	public void onSlotChange(PlayerItemHeldEvent e) {
		Player p = e.getPlayer();

		ItemStack old = p.getInventory().getItem(e.getPreviousSlot());
		ItemStack nw = p.getInventory().getItem(e.getNewSlot());

		if (runNew(nw, p)) {
			return;
		}
		runOld(old, p);
	}
	
//	@EventHandler(priority = EventPriority.LOWEST)
//	public void onMapLoad(MapInitializeEvent e) {
//		Main.p.getLogger().warning("0");
//		MapView view = e.getMap();
//		
//		if (Reflection.getMapId(e.get) != Data.getMapId()) {
//			return;
//		}
//		
//		Main.p.getLogger().warning("1");
//		
//		view.addRenderer(new TpsRender());
//	}

	public static void giveMap(Player p) {
		PlayerInventory inv = p.getInventory();
		int slot = inv.getHeldItemSlot();

		inv.setItem(slot, MonTools.mapitem);

		UUID UUID = p.getUniqueId();

		if (!mapusers.contains(UUID)) {
			mapusers.add(UUID);
		}
	}

	private static void runOld(ItemStack old, Player p) {
		if (old == null) {
			return;
		}

		if (!old.hasItemMeta()) {
			return;
		}
		ItemMeta ometa = old.getItemMeta();
		if (!ometa.hasDisplayName()) {
			return;
		}
		if (ometa.getDisplayName() != mapitemmeta.getDisplayName()) {
			return;
		}

		UUID UUID = p.getUniqueId();

		if (!mapusers.contains(UUID)) {
			mapusers.add(UUID);
		}
	}

	private static boolean runNew(ItemStack nw, Player p) {

		if (!p.hasPermission("lagassist.use")) {
			return false;
		}

		if (nw == null) {
			return false;
		}

		if (!nw.hasItemMeta()) {
			return false;
		}
		ItemMeta nwmeta = nw.getItemMeta();
		if (!nwmeta.hasDisplayName()) {
			return false;
		}
		if (nwmeta.getDisplayName() != mapitemmeta.getDisplayName()) {
			return false;
		}

		UUID UUID = p.getUniqueId();

		if (!mapusers.contains(UUID)) {
			mapusers.add(UUID);
		}
		return true;
	}
}
