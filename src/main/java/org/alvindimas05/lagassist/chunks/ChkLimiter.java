package org.alvindimas05.lagassist.chunks;

import java.util.HashMap;
import java.util.Map;

import org.alvindimas05.lagassist.Main;
import org.alvindimas05.lagassist.utils.Cache;
import org.alvindimas05.lagassist.utils.CustomLogger;
import org.alvindimas05.lagassist.utils.ServerType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ChkLimiter implements Listener {

    private static final Cache<Chunk, Entity[]> entcache = new Cache<Chunk, Entity[]>(30);
    private static final Cache<Chunk, BlockState[]> tilecache = new Cache<Chunk, BlockState[]>(100);
    private static int moblimit = -1;
    private static int tilelimit = -1;

    public static void Enabler(boolean reload) {
        if (!reload) {
            Main.p.getServer().getPluginManager().registerEvents(new ChkLimiter(), Main.p);
            runTask();
        }
        CustomLogger.info(ChatColor.YELLOW + "    [" + ChatColor.GREEN + "✔" + ChatColor.YELLOW + "] "
                + ChatColor.WHITE + "Chunk Limiter enabled.");

        moblimit = Main.config.getInt("limiter.mobs.total-limit");
        tilelimit = Main.config.getInt("limiter.tiles.total-limit");
    }

    static int i = 0;

    private static void runTask() {
        long interval = 1L;
        int timerTime = Main.config.getInt("limiter.timer-time");

        if (ServerType.isFolia()) {
            Bukkit.getGlobalRegionScheduler().run(Main.p, task -> tickLimiter(timerTime));
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    tickLimiter(timerTime);
                }
            }.runTaskTimer(Main.p, interval, interval);
        }
    }


    private static void tickLimiter(int timerTime) {
        i++;

        if (i % timerTime == 0) {
            for (World w : Bukkit.getWorlds()) {
                for (Chunk chk : w.getLoadedChunks()) {
                    Map<EntityType, Integer> counts = new HashMap<>();

                    for (Entity ent : chk.getEntities()) {
                        if (Main.config.getBoolean("limiter.ignore-named-mobs") && ent.getCustomName() != null) {
                            continue;
                        }

                        EntityType type = ent.getType();
                        int allowed = getMobMaxHard(type);

                        if (allowed == -1) {
                            continue;
                        }

                        int count = counts.getOrDefault(type, 0);

                        if (count >= allowed) {
                            ent.remove();
                        } else {
                            counts.put(type, count + 1);
                        }
                    }
                }
            }
        }

        tilecache.tick();
        entcache.tick();
    }


    private static int getMobMaxHard(EntityType entype) {
        String location = "limiter.mobs.hard-limit." + entype.toString().toLowerCase();
        if (Main.config.contains(location)) {
            return Main.config.getInt(location);
        }
        return -1;
    }

    private static int getMobMaxSoft(EntityType entype) {
        String location = "limiter.mobs.soft-limit." + entype.toString().toLowerCase();
        if (Main.config.contains(location)) {
            return Main.config.getInt(location);
        }
        return -1;
    }

    private static int getMobMaxSoft(Entity ent) {
        return getMobMaxSoft(ent.getType());
    }

    private static int getTileMax(Class<?> cls) {
        String location = "limiter.tiles.per-limit." + cls.getSimpleName().toLowerCase();
        if (Main.config.contains(location)) {
            return Main.config.getInt(location);
        }
        return -1;
    }

    private static int getTileMax(Block b) {
        return getTileMax(b.getState().getClass());
    }

    private static int getMobCount(Chunk chk, EntityType entype) {
        int ents = 0;

        if (!entcache.isCached(chk)) {
            entcache.putCached(chk, chk.getEntities());
        }

        for (Entity ent : entcache.getCached(chk)) {
            if (ent == null) {
                continue;
            }

            if (ent.getType() != entype) {
                continue;
            }
            ents++;
        }
        return ents;
    }

    private static int getMobCount(Chunk chk) {
        return chk.getEntities().length;
    }

    private static int getTileCount(Chunk chk, Class<?> type) {
        int tls = 0;

        if (!tilecache.isCached(chk)) {
            tilecache.putCached(chk, chk.getTileEntities());
        }

        for (BlockState tle : tilecache.getCached(chk)) {
            if (tle == null) {
                continue;
            }

            if (!tle.getClass().equals(type)) {
                continue;
            }
            tls++;
        }
        return tls;
    }

    private static int getTileCount(Chunk chk) {
        return chk.getTileEntities().length;
    }

    // VERIFIERS (CODE QUALITY IMPROVERS)
    private static boolean isDeniedMob(Entity ent) {

        if (Main.config.getBoolean("limiter.ignore-named-mobs") && ent.getCustomName() != null) {
            return false;
        }

        EntityType etype = ent.getType();

        // Fix issues with destroying player objects.
        if (etype == EntityType.PLAYER) {
            return false;
        }

        Chunk chk = ent.getLocation().getChunk();

        int current = getMobCount(chk, etype);
        int maximum = getMobMaxSoft(ent);

        if (maximum < 0) {
            current = getMobCount(chk);
            maximum = moblimit;
        }

        if (maximum < 0) {
            return false;
        }

        return current >= maximum;

    }

    private static boolean isDeniedTile(Block b) {

        Chunk chk = b.getLocation().getChunk();
        Class<?> type = b.getState().getClass();

        String typename = type.getSimpleName().toLowerCase();

        Main.sendDebug("isDeniedTile: " + typename, 1);

        if (typename.equals("craftblockstate")) {
            return false;
        }

        int current = getTileCount(chk, type);
        int maximum = getTileMax(b);

        if (maximum < 0) {
            current = getTileCount(chk);
            maximum = tilelimit;
        }

        if (maximum < 0) {
            return false;
        }

        return current >= maximum;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleSpawn(VehicleCreateEvent e) {
        Vehicle ent = e.getVehicle();

        // 1.8 doesn't support cancel. Fuk
        if (isDeniedMob(ent)) {
            ent.remove();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSpawn(EntitySpawnEvent e) {
        if (e.isCancelled()) {
            return;
        }

        Entity ent = e.getEntity();

        e.setCancelled(isDeniedMob(ent));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlace(BlockPlaceEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Block b = e.getBlock();
        e.setCancelled(isDeniedTile(b));
    }


}