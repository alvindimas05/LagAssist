package org.alvindimas05.lagassist;

import java.io.File;
import java.util.Objects;

import org.alvindimas05.lagassist.api.APIManager;
import org.alvindimas05.lagassist.cmd.CommandListener;
import org.alvindimas05.lagassist.cmd.CommandTabListener;
import org.alvindimas05.lagassist.cmd.StatsAnalyse;
import org.alvindimas05.lagassist.gui.DataGUI;
import org.alvindimas05.lagassist.mobs.SmartMob;
import org.alvindimas05.lagassist.mobs.SpawnerMgr;
import org.alvindimas05.lagassist.utils.CustomLogger;
import org.alvindimas05.lagassist.utils.WorldMgr;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import org.alvindimas05.lagassist.chunks.ChkAnalyse;
import org.alvindimas05.lagassist.chunks.ChkLimiter;
import org.alvindimas05.lagassist.chunks.DynViewer;
import org.alvindimas05.lagassist.client.ClientMain;
import org.alvindimas05.lagassist.economy.EconomyManager;
import org.alvindimas05.lagassist.hoppers.HopperManager;
import org.alvindimas05.lagassist.logpurger.PurgerMain;
import org.alvindimas05.lagassist.metrics.MetricsManager;
import org.alvindimas05.lagassist.microfeatures.MicroManager;
import org.alvindimas05.lagassist.packets.PacketInjector;
import org.alvindimas05.lagassist.packets.PacketMain;
import org.alvindimas05.lagassist.safety.SafetyManager;
import org.alvindimas05.lagassist.stacker.StackManager;
import org.alvindimas05.lagassist.updater.SmartUpdater;
import org.alvindimas05.lagassist.utils.Others;
import org.alvindimas05.lagassist.utils.VersionMgr;

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

        CustomLogger.info(Main.PREFIX + "Enabling Systems:");
        EnableClasses(false);

        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("lagassist")).setExecutor(new CommandListener());
        Objects.requireNonNull(getCommand("lagassist")).setTabCompleter(new CommandTabListener());
    }

	private static void EnableClasses(boolean reload) {
		    EconomyManager.Enabler(reload);

        SafetyManager.Enabler(reload);
        Reflection.Enabler();
        Data.Enabler();
        SmartMob.Enabler(reload);
        MicroManager.enable(reload);
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
        APIManager.enable(reload);
    }

    public static void ReloadPlugin(CommandSender s) {
        config = Others.getConfig(file, 32);

        CustomLogger.info(Main.PREFIX + "Reloading Systems:");
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
        }
    }

}
