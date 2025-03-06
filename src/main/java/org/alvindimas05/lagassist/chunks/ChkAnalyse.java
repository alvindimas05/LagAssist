package org.alvindimas05.lagassist.chunks;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alvindimas05.lagassist.Main;
import org.alvindimas05.lagassist.utils.CustomLogger;
import org.alvindimas05.lagassist.utils.ServerType;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ChkAnalyse {

    protected static int ammoshow = Main.config.getInt("chunkanalyse.ammount");
    protected static Map<String, Integer> values = new HashMap<>();
    private static List<ChkStats> scores = new ArrayList<>();

    public static void Enabler() {
        CustomLogger.info(ChatColor.YELLOW + "    [" + ChatColor.GREEN + "✔" + ChatColor.YELLOW + "] "
                + ChatColor.WHITE + "Chunk Analyser enabled.");

        Set<String> slist = Objects.requireNonNull(Main.config.getConfigurationSection("chunkanalyse.values")).getKeys(false);

        for (String s : slist) {
            int value = Main.config.getInt("chunkanalyse.values." + s);
            values.put(s.toLowerCase(), value);
        }
    }

    public static void analyseChunks(Player p) {
        if (ServerType.isFolia()) {
            scores = Collections.synchronizedList(new ArrayList<>());
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (World world : Bukkit.getWorlds()) {
                for (Chunk chunk : world.getLoadedChunks()) {
                    Location chunkLoc = chunk.getBlock(0, 0, 0).getLocation();
                    CompletableFuture<Void> future = new CompletableFuture<>();

                    Bukkit.getRegionScheduler().execute(Main.p, chunkLoc, () -> {
                        try {
                            scores.add(new ChkStats(chunk, false));
                        } finally {
                            future.complete(null);
                        }
                    });
                    futures.add(future);
                }
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRunAsync(() -> {
                        for (ChkStats cs : scores) {
                            cs.genScores();
                        }
                        Collections.sort(scores);
                    })
                    .thenRun(() -> {
                        p.getScheduler().execute(Main.p, () -> {
                            p.sendMessage("");
                            p.sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "⬛⬛⬛ CHUNK ANALYSER ⬛⬛⬛");
                            p.sendMessage("");

                            int count = 0;
                            for (ChkStats cs : scores) {
                                if (count >= ammoshow) break;
                                p.spigot().sendMessage(cs.genText());
                                count++;
                            }

                            p.sendMessage("");
                            p.sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛");
                        }, null, 0L);
                    });
        }
    }

    public static void analyseChunks(CommandSender p) {
        Map<Chunk, Integer> chunkscore = new HashMap<>();

        for (World w : Bukkit.getWorlds()) {
            for (Chunk ch : w.getLoadedChunks()) {
                chunkscore.putIfAbsent(ch, 0);

                for (BlockState blkst : ch.getTileEntities()) {
                    String type = blkst.getType().toString().toLowerCase();
                    if (values.containsKey(type)) {
                        chunkscore.put(ch, chunkscore.get(ch) + values.get(type));
                    }
                }

                for (Entity e : ch.getEntities()) {
                    String entityType = e.getType().toString().toLowerCase();
                    if (values.containsKey(entityType)) {
                        chunkscore.put(ch, chunkscore.get(ch) + values.get(entityType));
                    }
                }
            }
        }

        p.sendMessage("");
        p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "⭐ CHUNK ANALYSER ⭐");
        p.sendMessage("");

        Stream<Map.Entry<Chunk, Integer>> sorted = chunkscore.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));

        Map<Chunk, Integer> topChunks = sorted.limit(ammoshow)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        for (Chunk ch : topChunks.keySet()) {
            int score = topChunks.get(ch);
            p.sendMessage(ChatColor.YELLOW + "  ✸ Chunk (" + ch.getX() + ", " + ch.getZ() + ") - Score: "
                    + ChatColor.GREEN + score);
        }

        p.sendMessage("");
        p.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "⭐".repeat(20));
    }

    public static void analyseCurrentChunk(CommandSender s) {
        if (!(s instanceof Player p)) {
            s.sendMessage(ChatColor.RED + "❌ You cannot analyze the current chunk from the console.");
            return;
        }

        Location l = p.getLocation();
        ChkStats stats = new ChkStats(l.getChunk(), true);
        int[] coords = stats.getCoords();

        String chunkCoords = coords[0] + ", " + coords[1];
        String teleportCommand = "/lagassist tpchunk " + p.getWorld().getName() + " " + coords[0] + " " + coords[1];

        p.sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "⬛⬛⬛ CHUNK STATS ⬛⬛⬛");
        p.sendMessage("");
        p.sendMessage(ChatColor.GREEN + "  ✸ " + ChatColor.WHITE + "Chunk Coordinates: " + ChatColor.GRAY + chunkCoords);
        p.sendMessage("");
        p.sendMessage(ChatColor.GREEN + "  ✸ " + ChatColor.WHITE + "Entity Count: " + ChatColor.GRAY + stats.getEnts().length);
        p.sendMessage(ChatColor.GREEN + "  ✸ " + ChatColor.WHITE + "Tile Entities: " + ChatColor.GRAY + stats.getTiles().length);
        p.sendMessage("");
        p.spigot().sendMessage(stats.genMobCount(ChatColor.GREEN + "  ✸ " + ChatColor.WHITE + "Detailed Info " + ChatColor.GRAY + "(HOVER)", teleportCommand));
        p.sendMessage("");
        p.sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛");
    }
}
