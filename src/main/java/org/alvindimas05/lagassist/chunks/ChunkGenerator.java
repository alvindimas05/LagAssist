package org.alvindimas05.lagassist.chunks;

import java.util.ArrayList;
import java.util.List;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.alvindimas05.lagassist.Main;
import org.alvindimas05.lagassist.utils.CustomLogger;
import org.alvindimas05.lagassist.utils.MathUtils;
import org.alvindimas05.lagassist.utils.ServerType;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class ChunkGenerator {

    private static World world;
    private static BukkitTask bukkitTask;
    private static ScheduledTask foliaTask;

    private static int maxx, maxz, minx, minz;
    private static int currentIndex;

    public static void pregenWorld(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "You need to execute this command as a player.");
            return;
        }

        if (args.length != 2 || !MathUtils.isInt(args[1])) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: " + ChatColor.GREEN + "/lagassist pregench [Max-Millis-Per-Tick]");
            return;
        }

        int millis = Integer.parseInt(args[1]);
        World w = player.getWorld();

        if (w.getWorldBorder().getSize() > 50000) {
            sender.sendMessage(ChatColor.RED + "The max world border size is 50,000 blocks.");
            return;
        }

        sender.sendMessage(ChatColor.AQUA + "⏳ Chunk pre-generation started! This may take some time.");
        pregenWorld(w, millis);
    }

    public static void stopGen(CommandSender sender) {
        boolean cancelled = false;
        if (bukkitTask != null) {
            bukkitTask.cancel();
            bukkitTask = null;
            cancelled = true;
        }
        if (foliaTask != null) {
            foliaTask.cancel();
            foliaTask = null;
            cancelled = true;
        }
        if (cancelled) {
            sender.sendMessage(ChatColor.RED + "❌ Chunk pre-generation stopped.");
        }
    }

    private static void pregenWorld(World w, int millis) {
        world = w;

        WorldBorder wb = w.getWorldBorder();
        Location center = wb.getCenter();
        double radius = wb.getSize() / 2;

        maxx = (int) ((center.getX() + radius) / 16);
        maxz = (int) ((center.getZ() + radius) / 16);
        minx = (int) ((center.getX() - radius) / 16);
        minz = (int) ((center.getZ() - radius) / 16);

        Chunk spawnChunk = world.getSpawnLocation().getChunk();
        int spawnX = spawnChunk.getX();
        int spawnZ = spawnChunk.getZ();

        List<int[]> chunks = generateChunkList(spawnX, spawnZ);

        if (ServerType.isFolia()) {
            startFoliaGeneration(chunks, millis);
        } else {
            startBukkitGeneration(chunks, millis);
        }
    }

    private static List<int[]> generateChunkList(int startX, int startZ) {
        List<int[]> chunks = new ArrayList<>();
        int x = startX, z = startZ, i = 1;

        while (isInside(x, z)) {
            for (int tempX = x + 1; tempX <= x + i; tempX++) chunks.add(new int[]{tempX, z});
            x += i;
            for (int tempZ = z - 1; tempZ >= z - i; tempZ--) chunks.add(new int[]{x, tempZ});
            z -= i;
            i++;
            for (int tempX = x - 1; tempX >= x - i; tempX--) chunks.add(new int[]{tempX, z});
            x -= i;
            for (int tempZ = z + 1; tempZ <= z + i; tempZ++) chunks.add(new int[]{x, tempZ});
            z += i;
            i++;
        }
        return chunks;
    }

    private static void startFoliaGeneration(List<int[]> chunks, int millis) {
        foliaTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(Main.p, task -> {
            final long startTime = System.currentTimeMillis();
            final int batchSize = Math.min(5, chunks.size() / 100);

            for (int i = 0; i < batchSize && currentIndex < chunks.size(); i++) {
                final int[] coords = chunks.get(currentIndex);
                scheduleChunkLoad(coords[0], coords[1]);
                currentIndex++;

                if (currentIndex % 300 == 0) {
                    reportProgress(currentIndex, chunks.size());
                }

                if (System.currentTimeMillis() - startTime >= millis) {
                    break;
                }
            }

            if (currentIndex >= chunks.size()) {
                task.cancel();
                foliaTask = null;
                Bukkit.broadcastMessage(ChatColor.GREEN + "✅ Chunk pre-generation complete!");
            }
        }, 1L, 1L);
    }

    private static void scheduleChunkLoad(int x, int z) {
        Bukkit.getRegionScheduler().execute(Main.p, world, x, z, () -> {
            if (!world.isChunkGenerated(x, z)) {
                world.loadChunk(x, z, true);
            }
        });
    }

    private static void startBukkitGeneration(List<int[]> chunks, int millis) {
        bukkitTask = Bukkit.getScheduler().runTaskTimer(Main.p, () -> {
            long startTime = System.currentTimeMillis();
            for (int i = currentIndex; i < chunks.size(); i++) {
                int[] coords = chunks.get(i);
                if (!world.isChunkGenerated(coords[0], coords[1])) {
                    world.loadChunk(coords[0], coords[1], true);
                }

                if (i % 300 == 0) {
                    reportProgress(i, chunks.size());
                }

                if (System.currentTimeMillis() - startTime >= millis) {
                    currentIndex = i + 1;
                    return;
                }
            }
            bukkitTask.cancel();
            bukkitTask = null;
        }, 1L, 1L);
    }

    private static boolean isInside(int x, int z) {
        return x >= minx && x <= maxx && z >= minz && z <= maxz;
    }

    private static void reportProgress(int current, int total) {
        int percent = (int) ((current / (double) total) * 100);
        String progressBar = ChatColor.YELLOW + "[" + ChatColor.GREEN
                + "■".repeat(percent / 10) + ChatColor.RED
                + "■".repeat(10 - percent / 10) + ChatColor.YELLOW + "]";
        Bukkit.broadcastMessage(ChatColor.GOLD + "⏳ Progress: " + ChatColor.BOLD + percent + "%" + " " + progressBar + ChatColor.GRAY + " (" + current + "/" + total + ")");
    }
}
