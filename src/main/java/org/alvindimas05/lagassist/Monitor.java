package org.alvindimas05.lagassist;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Objects;

import org.alvindimas05.lagassist.mobs.SmartMob;
import org.alvindimas05.lagassist.mobs.SpawnerMgr;
import org.alvindimas05.lagassist.hoppers.ChunkHoppers;
import org.alvindimas05.lagassist.utils.ServerType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Monitor {

    private static final DecimalFormat format = new DecimalFormat("##.##");

    public static Runtime Rtm = Runtime.getRuntime();

    public static byte[][] colors = new byte[129][129];
    public static double exactTPS = 20.0;
    public static int mondelay;

    public static void Enabler(boolean reload) {
        Bukkit.getLogger().info("    §e[§a✔§e] §fLag Monitor.");

        for (byte[] row : colors) {
            Arrays.fill(row, (byte) 34);
        }

        mondelay = Main.config.getInt("lag-measures.timer");

        if (!reload) {
            GetExactTPS();
            LagDetect();
            createGraph();
        }
    }

    public static void LagDetect() {
        long delay = mondelay;
        long period = mondelay;

        if (ServerType.isFolia()) {
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(Main.p, task -> {
                LagMeasures(Double.parseDouble(getTPS(0)));
            }, delay, period);
        } else {
            Bukkit.getScheduler().runTaskTimerAsynchronously(Main.p, () -> {
                Bukkit.getScheduler().runTask(Main.p, () -> LagMeasures(Double.parseDouble(getTPS(0))));
            }, delay, period);
        }
    }

    public static void GetExactTPS() {
        long delay = 60L;
        long period = 1L;

        if (ServerType.isFolia()) {
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(Main.p, task -> {
                double ext = ExactTPS.getTPS();
                if (ext > 20 || ext == 0) {
                    ext = 20;
                }
                exactTPS = ext;
            }, delay, period);
        } else {
            Bukkit.getScheduler().runTaskTimerAsynchronously(Main.p, () -> {
                double ext = ExactTPS.getTPS();
                if (ext > 20 || ext == 0) {
                    ext = 20;
                }
                exactTPS = ext;
            }, delay, period);
        }
    }

    public static void createGraph() {
        long delay = 7L;
        long period = 7L;

        if (ServerType.isFolia()) {
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(Main.p, task -> updateGraph(), delay, period);
        } else {
            Bukkit.getScheduler().runTaskTimerAsynchronously(Main.p, Monitor::updateGraph, delay, period);
        }
    }

    private static void updateGraph() {
        double ctps = ExactTPS.getTPS(20);
        double medtps = Double.parseDouble(getTPS(1));
        if (medtps > 15) {
            medtps = 15.0;
        }
        if (ctps > 20) {
            ctps = 20.0;
        }

        double min = medtps - 5;
        double ntps = 89 - (ctps - min) * 8;

        for (int i = 89; i > 9; i--) {
            if (i == (int) ntps) {
                colors[124][i] = 18;
            } else if (i == (int) ntps + 1) {
                colors[124][i] = -124;
            } else if (i > ntps) {
                colors[124][i] = -122;
            } else {
                colors[124][i] = 32;
            }
        }

        for (int i = 3; i < 124; i++) {
            System.arraycopy(colors[i + 1], 3, colors[i], 3, 87);
        }
    }

    public static long freeMEM() {
        return Rtm.freeMemory() / 1048576;
    }

    public static String getTPS(int time) {
        return format.format(Reflection.getTPS(time)).replace(",", ".");
    }

    private static void LagMeasures(double tps) {
        SmartMob.Spawning = true;
        Physics.denyphysics = false;
        SpawnerMgr.active = false;
        ChunkHoppers.mobhoppers = false;

        String stg = Main.PREFIX + "LagMeasures executed: §e";

        if (tps <= Main.config.getDouble("smart-cleaner.maxtps-cull")) {
            for (World w : Bukkit.getWorlds()) {
                if (!ServerType.isFolia()) {
                    SmartMob.MobCuller();
                } else {
                    Bukkit.getRegionScheduler().execute(Main.p, w, 0, 0, SmartMob::MobCuller);
                }
            }
            stg += "Culled Mobs, ";
        }

        if (tps <= Main.config.getDouble("smart-cleaner.maxtps-disablespawn")) {
            SmartMob.Spawning = false;
            stg += "Disabled MobSpawn, ";
        }

        if (tps <= Main.config.getDouble("redstone-culler.maxtps")) {
            for (World w : Bukkit.getWorlds()) {
                if (ServerType.isFolia()) {
                    Bukkit.getRegionScheduler().execute(Main.p, w, 0, 0, Redstone::CullRedstone);
                } else {
                    Redstone.CullRedstone();
                }
            }
            stg += "Culled Redstone, ";
        }

        if (tps <= Main.config.getDouble("deny-physics.maxtps")) {
            Physics.denyphysics = true;
            stg += "Disabled physics, ";
        }

        if (tps <= Main.config.getDouble("spawner-check.maxtps")) {
            SpawnerMgr.active = true;
            stg += "Optimizing Spawners, ";
        }

        if (tps <= Main.config.getDouble("hopper-check.chunk-hoppers.mob-hopper.maxtps")) {
            ChunkHoppers.mobhoppers = true;
            stg += "Mob Hoppers.";
        }

        if (Main.config.getBoolean("lag-measures.announce.enabled")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (Main.config.getBoolean("lag-measures.announce.staffmsg")) {
                    if (player.hasPermission("lagassist.notify")) {
                        player.sendMessage(stg);
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                Objects.requireNonNull(Main.config.getString("lag-measures.announce.message"))));
                    }
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            Objects.requireNonNull(Main.config.getString("lag-measures.announce.message"))));
                }
            }
        }
    }
}
