package org.alvindimas05.lagassist.client;

import java.io.File;
import java.util.List;
import java.util.Objects;

import org.alvindimas05.lagassist.Main;
import org.alvindimas05.lagassist.cmd.ClientCmdListener;
import org.alvindimas05.lagassist.gui.DataGUI;
import org.alvindimas05.lagassist.Reflection;
import org.alvindimas05.lagassist.utils.CustomLogger;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import org.alvindimas05.lagassist.utils.Others;
import org.alvindimas05.lagassist.utils.VersionMgr;

import net.md_5.bungee.api.ChatColor;

public class ClientMain implements Listener {

    private static final File clf = new File(Main.p.getDataFolder(), "client.yml");
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
        prefix = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(clcnf.getString("settings.prefix")));
        guiname = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(clcnf.getString("settings.gui-name")));

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
            CustomLogger.info("    §e[§a✖§e] §fClient Optimizer - The command is already registered");
            return;
        }
        Reflection.getCommandMap().register(Main.p.getDescription().getName(), cmd);
        Objects.requireNonNull(Main.p.getCommand(ClientMain.command)).setExecutor(new ClientCmdListener());

        CustomLogger.info("    §e[§a✔§e] §fClient Optimizer. " + (VersionMgr.isV_17Plus() ? " EXPERIMENTAL SUPPORT 1.17+" : ""));


    }

}
