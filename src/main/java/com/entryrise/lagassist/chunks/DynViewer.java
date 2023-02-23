package com.entryrise.lagassist.chunks;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.entryrise.lagassist.Main;
import com.entryrise.lagassist.utils.PaperOnly;
import com.entryrise.lagassist.utils.VersionMgr;
import com.entryrise.lagassist.utils.WorldMgr;

public class DynViewer implements Listener {

	private static int maxChunks;
	private static int maxView;

	private static int currChunks = 0;

	public static void Enabler(boolean reload) {

		boolean enabled = Main.config.getBoolean("chunk-manager.enabled");

		if (!enabled) {
			return;
		}

		if (!Main.paper) {
			Bukkit.getLogger().info("    §e[§a✖§e] §fChunkManager - No PaperSpigot found");
			return;
		}

		maxChunks = Main.config.getInt("chunk-manager.max-chunks");
		maxView = Main.config.getInt("chunk-manager.max-view");

		if (!reload) {
			Main.p.getServer().getPluginManager().registerEvents(new DynViewer(), Main.p);

			runTask();
		}

		Bukkit.getLogger().info("    §e[§a✔§e] §fChunkManager.");

	}

	private static void runTask() {
		Bukkit.getScheduler().runTaskTimer(Main.p, () -> setViews(), 0, 600);
	}

	public static void setViews() {

		Main.sendDebug("Attempting to set dynamic view distance...", 1);
		
		// Get a value that is at least the minimum view distance and at max the spigot
		// view distance.

		int pl = Bukkit.getOnlinePlayers().size();

		if (pl == 0) {
			return;
		}

		int vd = (int) Math.sqrt(maxChunks / pl / 4);

		int newchunks = Math.max(Math.min(maxView, vd), Bukkit.getViewDistance());

		if (currChunks == newchunks) {
			return;
		}

		currChunks = newchunks;

		for (World w : Bukkit.getWorlds()) {
			if (WorldMgr.blacklist.contains(w.getName())) {
				continue;
			}
			
			PaperOnly.setViewDistance(w, currChunks);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		Main.sendDebug("Join detected for " + p.getName(), 1);
		
		if (VersionMgr.isNewMaterials()) {
			Main.sendDebug("Is new materials in dynviewer for " + p.getName() + ". Returning... ", 1);
			return;
		}
		
		// TODO: FIX Dynamic view
		Bukkit.getScheduler().runTaskLater(Main.p, () -> PaperOnly.setViewDistance(p, currChunks), 25);

	}

}
