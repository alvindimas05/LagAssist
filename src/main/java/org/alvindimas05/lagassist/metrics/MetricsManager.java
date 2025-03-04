package org.alvindimas05.lagassist.metrics;

import org.alvindimas05.lagassist.ExactTPS;
import org.alvindimas05.lagassist.Main;
import org.alvindimas05.lagassist.minebench.SpecsGetter;
import org.alvindimas05.lagassist.utils.ServerType;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.concurrent.atomic.AtomicInteger;

public class MetricsManager {

    public static void Enabler(boolean reload) {
        if (reload) {
            return;
        }

        BStats stats = new BStats(Main.p);

        stats.addCustomChart(new BStats.SimplePie("tps_base", () -> String.valueOf(Math.round(ExactTPS.getTPS(600)))));
        stats.addCustomChart(new BStats.SimplePie("cpu_used", MetricsManager::getProcessor));

        stats.addCustomChart(new BStats.SingleLineChart("tile_entities", MetricsManager::getTileEntitiesCount));
        stats.addCustomChart(new BStats.SingleLineChart("normal_entities", MetricsManager::getEntitiesCount));
        stats.addCustomChart(new BStats.SingleLineChart("chunks_count", MetricsManager::getChunksCount));
    }

    private static int getEntitiesCount() {
        AtomicInteger ents = new AtomicInteger(0);

        if (ServerType.isFolia()) {
            Bukkit.getGlobalRegionScheduler().execute(Main.p, () -> {
                for (World w : Bukkit.getWorlds()) {
                    ents.addAndGet(w.getEntities().size());
                }
            });
        } else {
            for (World w : Bukkit.getWorlds()) {
                ents.addAndGet(w.getEntities().size());
            }
        }

        return ents.get();
    }

    private static int getTileEntitiesCount() {
        AtomicInteger tents = new AtomicInteger(0);

        for (World w : Bukkit.getWorlds()) {
            for (Chunk chk : w.getLoadedChunks()) {
                final int cx = chk.getX();
                final int cz = chk.getZ();

                if (ServerType.isFolia()) {
                    Bukkit.getRegionScheduler().execute(Main.p, w, cx, cz, () -> {
                        tents.addAndGet(chk.getTileEntities().length);
                    });
                } else {
                    tents.addAndGet(chk.getTileEntities().length);
                }
            }
        }

        return tents.get();
    }

    private static int getChunksCount() {
        AtomicInteger chks = new AtomicInteger(0);

        if (ServerType.isFolia()) {
            Bukkit.getGlobalRegionScheduler().execute(Main.p, () -> {
                for (World w : Bukkit.getWorlds()) {
                    chks.addAndGet(w.getLoadedChunks().length);
                }
            });
        } else {
            for (World w : Bukkit.getWorlds()) {
                chks.addAndGet(w.getLoadedChunks().length);
            }
        }

        return chks.get();
    }

    private static String getProcessor() {
        return SpecsGetter.getCPU(SpecsGetter.getOS());
    }
}
