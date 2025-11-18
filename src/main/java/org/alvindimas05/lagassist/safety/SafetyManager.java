package org.alvindimas05.lagassist.safety;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.alvindimas05.lagassist.Main;
import org.alvindimas05.lagassist.utils.CustomLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import static org.alvindimas05.lagassist.utils.ServerType.isFolia;

public class SafetyManager {

    public static boolean enabled = false;
    public static boolean crash_debug = false;

    public static void Enabler(boolean reload) {
        enabled = Main.config.getBoolean("safety-manager.enabled");
        crash_debug = Main.config.getBoolean("safety-manager.anti-crasher.settings.debug");
        if (!enabled) {
            return;
        }

        if (!reload) {

            try {
                long bytes = new File(".").getCanonicalFile().getUsableSpace();

                if (Main.config.getLong("safety-manager.no-space.startup-space") > bytes) {
                    CustomLogger.warning("NOT ENOUGH MEMORY TO START SERVER. SHUTTING DOWN.");
                    Bukkit.getServer().shutdown();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

//			SafetyAnticrash.startTask();
            startTask();
        }


        CustomLogger.info("    §e[§a✔§e] §fSafety Manager.");
    }

    public static void startTask() {
        Plugin plugin = Main.p;
        long initialDelayTicks = 60L;
        long periodTicks = 1L;

        if (isFolia()) {
            long initialDelayMs = initialDelayTicks * 50L;
            long periodMs = periodTicks * 50L;

            Bukkit.getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> {
                checkDiskSpaceAndShutdown();
            }, initialDelayMs, periodMs, TimeUnit.MILLISECONDS);
        } else {
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, SafetyManager::checkDiskSpaceAndShutdown, initialDelayTicks, periodTicks);
        }
    }

    private static void checkDiskSpaceAndShutdown() {
        try {
            long bytes = new File(".").getCanonicalFile().getUsableSpace();
            if (Main.config.getLong("safety-manager.no-space.shutdown-space") < bytes) {
                return;
            }

            if (isFolia()) {
                Bukkit.getGlobalRegionScheduler().run(Main.p, scheduledTask -> performShutdown());
            } else {
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.p, SafetyManager::performShutdown);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void performShutdown() {
        CustomLogger.warning("Server doesn't have enough memory to keep running. Shutting down server!");
        Bukkit.getServer().shutdown();
    }

}