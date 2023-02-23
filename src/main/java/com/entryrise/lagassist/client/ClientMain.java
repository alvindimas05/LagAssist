package com.entryrise.lagassist.client;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.entryrise.lagassist.Main;
import com.entryrise.lagassist.cmd.ClientCmdListener;
import com.entryrise.lagassist.gui.DataGUI;
import com.entryrise.lagassist.packets.Reflection;
import com.entryrise.lagassist.utils.Others;
import com.entryrise.lagassist.utils.VersionMgr;

import net.md_5.bungee.api.ChatColor;

public class ClientMain implements Listener {

	private static File clf = new File(Main.p.getDataFolder(), "client.yml");
	public static YamlConfiguration clcnf = new YamlConfiguration();

	private static String command;
	public static boolean enabled;
	public static String prefix;
	public static String perm;
	public static String guiname;

	public static boolean[] defaults = new boolean[4];

	protected static Entity last;

	public static void Enabler() {

		clcnf = Others.getConfig(clf, 4);

		enabled = clcnf.getBoolean("settings.enabled");
		command = clcnf.getString("settings.command");
		perm = clcnf.getString("settings.permission");

		// Color Translated Special Messages
		prefix = ChatColor.translateAlternateColorCodes('&', clcnf.getString("settings.prefix"));
		guiname = ChatColor.translateAlternateColorCodes('&', clcnf.getString("settings.gui-name"));

		defaults[0] = clcnf.getBoolean("defaults.tnt");
		defaults[1] = clcnf.getBoolean("defaults.sand");
		defaults[2] = clcnf.getBoolean("defaults.particles");
		defaults[3] = clcnf.getBoolean("defaults.pistons");

	}

	public static void configureIcon(ItemStack s, String type) {
		String loc = "language." + type;

		String raw = clcnf.getString(loc + ".name");
		List<String> rawlore = clcnf.getStringList(loc + ".lore");

		DataGUI.customizeItem(s, raw, false, rawlore);
	}

	public static void secondaryEnabler(boolean reload) {
		if (!enabled) {
			return;
		}

		if (reload) {
			return;
		}
		Main.p.getServer().getPluginManager().registerEvents(new ClientMain(), Main.p);

		PluginCommand cmd = Reflection.getCommand(command, Main.p);

		if (Reflection.getCommandMap().getCommand(ClientMain.command) != null) {
			Bukkit.getLogger().info("    §e[§a✖§e] §fClient Optimizer - The command is already registered");
			return;
		}
		Reflection.getCommandMap().register(Main.p.getDescription().getName(), cmd);
		Main.p.getCommand(ClientMain.command).setExecutor(new ClientCmdListener());

		Bukkit.getLogger().info("    §e[§a✔§e] §fClient Optimizer. " + (VersionMgr.isV_17Plus() ? " EXPERIMENTAL SUPPORT 1.17+" : ""));
		

	}

}
