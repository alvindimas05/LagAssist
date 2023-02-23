package com.entryrise.lagassist;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.entryrise.lagassist.api.APIManager;
import com.entryrise.lagassist.chunks.ChkAnalyse;
import com.entryrise.lagassist.chunks.ChkLimiter;
import com.entryrise.lagassist.chunks.DynViewer;
import com.entryrise.lagassist.client.ClientMain;
import com.entryrise.lagassist.cmd.CommandListener;
import com.entryrise.lagassist.cmd.CommandTabListener;
import com.entryrise.lagassist.cmd.StatsAnalyse;
import com.entryrise.lagassist.economy.EconomyManager;
import com.entryrise.lagassist.gui.DataGUI;
import com.entryrise.lagassist.hoppers.HopperManager;
import com.entryrise.lagassist.logpurger.PurgerMain;
import com.entryrise.lagassist.metrics.MetricsManager;
import com.entryrise.lagassist.microfeatures.MicroManager;
import com.entryrise.lagassist.mobs.SmartMob;
import com.entryrise.lagassist.mobs.SpawnerMgr;
import com.entryrise.lagassist.packets.PacketInjector;
import com.entryrise.lagassist.packets.PacketMain;
import com.entryrise.lagassist.packets.Reflection;
import com.entryrise.lagassist.safety.SafetyManager;
import com.entryrise.lagassist.stacker.StackManager;
import com.entryrise.lagassist.updater.SmartUpdater;
import com.entryrise.lagassist.utils.Others;
import com.entryrise.lagassist.utils.VersionMgr;
import com.entryrise.lagassist.utils.WorldMgr;

public class Main extends JavaPlugin implements Listener {

	public static String USER = "%%__USER__%%";

	public static final String PREFIX = "§2§lLag§f§lAssist §e» §f";

	public static JavaPlugin p;
	public static boolean paper = false;

	// Debug mode means people will get verbose info.
	public static int debug = 0;

	private static File file;
	public static FileConfiguration config = new YamlConfiguration();

	@Override
	public void onEnable() {
		p = this;

		file = new File(getDataFolder(), "server.yml");
		config = Others.getConfig(file, 32);

		paper = VersionMgr.isPaper();

		// Start Smart updater to check for updates.
		SmartUpdater.Enabler();

		Bukkit.getLogger().info(Main.PREFIX + "Enabling Systems:");
		EnableClasses(false);

		getServer().getPluginManager().registerEvents(this, this);
		getCommand("lagassist").setExecutor(new CommandListener());
		getCommand("lagassist").setTabCompleter(new CommandTabListener());
	}

	private static void EnableClasses(boolean reload) {

		EconomyManager.Enabler(reload);

		SafetyManager.Enabler(reload);
		Reflection.Enabler();
		Data.Enabler();
		SmartMob.Enabler(reload);
		MicroManager.Enabler(reload);
		HopperManager.Enabler(reload);
		StackManager.Enabler(reload);
		Redstone.Enabler(reload);
		Physics.Enabler(reload);
		Monitor.Enabler(reload);
		MonTools.Enabler(reload);
		WorldMgr.Enabler();
		ChkAnalyse.Enabler();
		ChkLimiter.Enabler(reload);
		StatsAnalyse.Enabler(reload);
		PurgerMain.Enabler();

		SpawnerMgr.Enabler(reload);
		DynViewer.Enabler(reload);
		ClientMain.Enabler();
		DataGUI.Enabler(reload);

		MetricsManager.Enabler(reload);

		PacketMain.Enabler(reload);

		// API ICON
		APIManager.Enabler(reload);
	}

	public static void ReloadPlugin(CommandSender s) {
		config = Others.getConfig(file, 32);

		Bukkit.getLogger().info(Main.PREFIX + "Reloading Systems:");
		EnableClasses(true);

		s.sendMessage(Main.PREFIX + "Reloaded the config successfully.");
	}

	@Override
	public void onDisable() {
		if (PacketMain.isPacketEnabled()) {
			PacketInjector.Disabler();
		}
		StackManager.Disabler();
	}

	public static void sendDebug(String msg, int mindebug) {
		if (mindebug > debug) {
			return;
		}

		for (Player p : Bukkit.getOnlinePlayers()) {
			if (!p.hasPermission("lagassist.debug")) {
				continue;
			}

			p.sendMessage(msg + "(MINDEBUG: " + mindebug + ")");
		}

		if (debug == 3) {
			try {
				throw new Exception(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
	}

}
