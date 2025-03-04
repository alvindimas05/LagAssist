package org.alvindimas05.lagassist.minebench;

import java.util.ArrayList;
import java.util.List;
import org.alvindimas05.lagassist.Main;
import org.alvindimas05.lagassist.utils.ServerType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class Approximate {

    private static boolean inuse = false;
    private static final List<CommandSender> receivers = new ArrayList<>();

    public static void showBenchmark(CommandSender sender) {
        if (!receivers.contains(sender)) {
            receivers.add(sender);
        }
        if (inuse) {
            sender.sendMessage(Main.PREFIX + "You have been added to the receivers list. Please wait for the benchmark to finish.");
            return;
        }

        sender.sendMessage(Main.PREFIX + "We are getting the benchmark results. This may take a while depending on your download speed. Please wait...");

        if (ServerType.isFolia()) {
            Bukkit.getAsyncScheduler().runNow(Main.p, task -> executeBenchmark());
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    executeBenchmark();
                }
            }.runTaskAsynchronously(Main.p);
        }
    }

    private static void executeBenchmark() {
        inuse = true;

        BenchResponse br = SpecsGetter.getBenchmark();
        int perthread = approximatePlayers(br);
        int multithread = perthread * SpecsGetter.threadCount();

        String cpuname = SpecsGetter.getCPU(SpecsGetter.getOS());
        int cores = SpecsGetter.getCores();
        double percentage = (double) cores / br.getCores();

        String singleapprox = (int) (percentage * ((double) (perthread * 4) / 5)) + "-" + (int) (percentage *
                ((double) (perthread * 6) / 5));
        String multiapprox = (int) (percentage * ((double) (multithread * 4) / 5)) + "-" + (int) (percentage *
                ((double) (multithread * 6) / 5));

        float dspeed = SpeedTest.getDownSpeed();
        float upspeed = SpeedTest.getUpSpeed();

        String MBDL = SpeedTest.df.format(dspeed);
        String MIBDL = SpeedTest.df.format(dspeed * 8.0f);
        String MBUP = SpeedTest.df.format(upspeed);
        String MIBUP = SpeedTest.df.format(upspeed * 8.0f);

        Bukkit.getGlobalRegionScheduler().run(Main.p, task -> {
            for (CommandSender cs : receivers) {
                cs.sendMessage("");
                cs.sendMessage("§2§l⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛§f§l BENCHMARK RESULTS §2§l⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛");
                cs.sendMessage("");
                cs.sendMessage("  §2✸ §fCPU Name: §e" + cpuname);
                cs.sendMessage("  §2✸ §fTotal Cores: §e" + br.getCores());
                cs.sendMessage("  §2✸ §fAvailable Cores: §e" + cores);
                cs.sendMessage("");
                if (br.getOk()) {
                    cs.sendMessage("  §2✸ §fCPU Score: §e" + br.getStringifiedSth(true));
                    cs.sendMessage("  §2✸ §fThread Score: §e" + br.getStringifiedTh(true));
                } else {
                    cs.sendMessage("  §cThere was an error getting the full benchmark results.");
                    cs.sendMessage("  §cYour CPU might be unsupported.");
                }
                cs.sendMessage("");
                cs.sendMessage("  §2✸ §fDownload Speed: §e" + MIBDL + " Mib/s  (" + MBDL + "MB/s)");
                cs.sendMessage("  §2✸ §fUpload Speed: §e" + MIBUP + " Mib/s  (" + MBUP + " MB/s)");
                cs.sendMessage("");
                if (br.getOk()) {
                    cs.sendMessage("  §2✸ §fMax Players (SINGLE): §e" + singleapprox);
                    cs.sendMessage("  §2✸ §fMax Players (GLOBAL): §e" + multiapprox);
                }
                cs.sendMessage("§2§l⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛");
            }
            receivers.clear();
            inuse = false;
        });
    }

    private static int approximatePlayers(BenchResponse br) {
        int mem = SpecsGetter.MaxRam();
        int singlescore = br.getSinglethread();
        int multiscore = br.getMultithread();

        if (singlescore == -1 || multiscore == -1) {
            return -1;
        }

        return Math.min(getMaxSTH(singlescore), getMaxMemo(mem));
    }

    private static int getMaxMemo(int mem) {
        int plgmnt = Bukkit.getPluginManager().getPlugins().length;
        int remmem = (int) (mem - plgmnt * 3.2f);
        return remmem / 50;
    }

    private static int getMaxSTH(int sthread) {
        String vers = Bukkit.getVersion();
        int max;

        if (vers.contains("1.8")) {
            max = sthread / 10;
        } else if (vers.contains("1.9")) {
            max = sthread / 13;
        } else if (vers.contains("1.10")) {
            max = sthread / 14;
        } else if (vers.contains("1.11")) {
            max = sthread / 16;
        } else if (vers.contains("1.12")) {
            max = sthread / 18;
        } else if (vers.contains("1.13")) {
            max = sthread / 25;
        } else if (vers.contains("1.14")) {
            max = sthread / 30;
        } else if (vers.contains("1.15")) {
            max = sthread / 28;
        } else if (vers.contains("1.16")) {
            max = sthread / 23;
        } else if (vers.contains("1.17")) {
            max = sthread / 22;
        } else if (vers.contains("1.18")) {
            max = sthread / 20;
        } else {
            max = sthread / 35;
        }

        return max;
    }
}
