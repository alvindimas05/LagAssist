package com.entryrise.lagassist.cmd;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.entryrise.lagassist.Main;
import com.entryrise.lagassist.Monitor;
import com.entryrise.lagassist.minebench.SpecsGetter;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class StatsAnalyse implements Listener {

	public static void Enabler(boolean reload) {
		if (!reload) {
			Main.p.getServer().getPluginManager().registerEvents(new StatsAnalyse(), Main.p);
		}
		Bukkit.getLogger().info("    §e[§a✔§e] §fStatsAnalyse.");
	}

	public static TextComponent genOverview() {
		String msgstrg = Main.PREFIX + "Hover over this message for a lag overview.";
		TextComponent msg = new TextComponent(msgstrg);

		int ent = 0, chk = 0;

		for (World w : Bukkit.getWorlds()) {
			ent += w.getEntities().size();
			chk += w.getLoadedChunks().length;
		}

		DecimalFormat df = new DecimalFormat("0.000");

		int crs = SpecsGetter.threadCount();
		double load = SpecsGetter.getSystemLoad();

		String crstg = String.valueOf(crs);
		if (crs == -1) {
			crstg = "NOT AVAILABLE";
		}

		String loadstg = df.format(load);
		if (load == -1) {
			crstg = "NOT AVAILABLE";
		}

		String stats = "\n\n  §2✸    §fExact TPS: §2" + df.format(Monitor.exactTPS) + "\n\n  §2✸    §fEntities: §2"
				+ String.valueOf(ent) + "\n  §2✸    §fLoaded Chunks: §2" + String.valueOf(chk)
				+ "\n\n  §2✸    §fFree Memory: §2" + String.valueOf(Monitor.freeMEM()) + "MB"
				+ "\n  §2✸    §fCPU Cores: §2" + crstg + "\n  §2✸    §fLoad Average: §2" + loadstg
				+ "\n  §2✸    §fDisk Space: §2" + String.valueOf((Main.p.getDataFolder().getUsableSpace() / 1073741824))
				+ "GB\n";

		msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(stats).create()));
		return msg;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onTpsCmd(PlayerCommandPreprocessEvent e) {
		Player p = e.getPlayer();
		if (!p.hasPermission("lagassist.use")) {
			return;
		}
		String cmd = e.getMessage();
		if (cmd.startsWith("/tps")) {
			p.spigot().sendMessage(genOverview());
		}
	}

}
