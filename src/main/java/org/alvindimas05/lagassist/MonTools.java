package org.alvindimas05.lagassist;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.alvindimas05.lagassist.maps.TpsRender;
import org.alvindimas05.lagassist.minebench.SpecsGetter;
import org.alvindimas05.lagassist.utils.CustomLogger;
import org.alvindimas05.lagassist.utils.ServerType;
import org.alvindimas05.lagassist.utils.VersionMgr;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class MonTools implements Listener {

    public static ItemStack mapitem;
    public static ItemMeta mapitemmeta;

    public static final List<UUID> actionmon = new ArrayList<>();
    public static final List<UUID> mapusers = new ArrayList<>();
    private static final DecimalFormat format = new DecimalFormat("#0.00");

    private static final String stbmsg = Main.config.getString("stats-bar.message");
    private static final int stbinterv = Main.config.getInt("stats-bar.tps-interval");
    private static final int stbshowdl = Main.config.getInt("stats-bar.show-delay");

    public static void Enabler(boolean reload) {
        if (!reload) {
            Main.p.getServer().getPluginManager().registerEvents(new MonTools(), Main.p);
        }

        CustomLogger.info("    §e[§a✔§e] §fMapVisualizer.");
        initializeMap();
        StatsBar();
    }

    private static void initializeMap() {
        mapitem = createMapItem();
        mapitemmeta = mapitem.getItemMeta();

        mapitemmeta.setDisplayName("§2§lLag§f§lAssist §e§lMonitor");
        mapitem.setItemMeta(mapitemmeta);

        MapView mapView = Bukkit.createMap(Bukkit.getWorlds().getFirst());
        mapView.getRenderers().clear();
        mapView.addRenderer(new TpsRender());

        MapMeta mapMeta = (MapMeta) mapitem.getItemMeta();
        mapMeta.setMapView(mapView);
        mapitem.setItemMeta(mapMeta);
    }

    private static ItemStack createMapItem() {
        ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) mapItem.getItemMeta();
        meta.setDisplayName("§2§lLag§f§lAssist §e§lMonitor");
        mapItem.setItemMeta(meta);
        return mapItem;
    }

    public static void StatsBar() {
        long delay = stbshowdl;
        long period = stbshowdl;

        if (ServerType.isFolia()) {
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(Main.p, task -> updateStatsBar(), delay, period);
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    updateStatsBar();
                }
            }.runTaskTimer(Main.p, delay, period);
        }
    }

    private static void updateStatsBar() {
        if (actionmon.isEmpty()) return;

        List<Player> onlinePlayers = actionmon.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toList();

        if (onlinePlayers.isEmpty()) return;

        double tpsraw = (ExactTPS.getTPS(10) > 20) ? 20 : ExactTPS.getTPS(stbinterv);
        Runnable task = getRunnable(tpsraw, onlinePlayers);

        if (ServerType.isFolia()) {
            Bukkit.getAsyncScheduler().runNow(Main.p, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(Main.p, task);
        }
    }

    private static @NotNull Runnable getRunnable(double tpsraw, List<Player> onlinePlayers) {
        String chunks = String.valueOf(getChunkCount());
        String ents = String.valueOf(getEntCount());

        return () -> {
            String tps = formatTPS(tpsraw);
            String message = ChatColor.translateAlternateColorCodes('&',
                    stbmsg.replace("{TPS}", tps)
                            .replace("{MEM}", format.format(SpecsGetter.FreeRam() / 1024))
                            .replace("{CHKS}", chunks)
                            .replace("{ENT}", ents));

            for (Player p : onlinePlayers) {
                Reflection.sendAction(p, message);
            }
        };
    }


    private static String formatTPS(double tpsraw) {
        if (tpsraw > 18) return "§a" + format.format(tpsraw);
        if (tpsraw > 15) return "§e" + format.format(tpsraw);
        return "§2" + format.format(tpsraw);
    }

    public static int getEntCount() {
        return Bukkit.getWorlds().stream().mapToInt(world -> world.getEntities().size()).sum();
    }

    public static int getChunkCount() {
        return Bukkit.getWorlds().stream().mapToInt(world -> world.getLoadedChunks().length).sum();
    }

    @EventHandler
    public void onSlotChange(PlayerItemHeldEvent e) {
        Player p = e.getPlayer();

        ItemStack old = p.getInventory().getItem(e.getPreviousSlot());
        ItemStack nw = p.getInventory().getItem(e.getNewSlot());

        if (runNew(nw, p)) return;
        runOld(old, p);
    }

    public static void giveMap(Player p) {
        PlayerInventory inv = p.getInventory();
        int slot = inv.getHeldItemSlot();
        inv.setItem(slot, MonTools.mapitem);

        UUID uuid = p.getUniqueId();
        if (!mapusers.contains(uuid)) mapusers.add(uuid);
    }

    private static void runOld(ItemStack old, Player p) {
        if (old == null || !old.hasItemMeta()) return;
        ItemMeta ometa = old.getItemMeta();
        if (!ometa.hasDisplayName() || !ometa.getDisplayName().equals(mapitemmeta.getDisplayName())) return;

        UUID uuid = p.getUniqueId();
        if (!mapusers.contains(uuid)) mapusers.add(uuid);
    }

    private static boolean runNew(ItemStack nw, Player p) {
        if (!p.hasPermission("lagassist.use") || nw == null || !nw.hasItemMeta()) return false;
        ItemMeta nwmeta = nw.getItemMeta();
        if (!nwmeta.hasDisplayName() || !nwmeta.getDisplayName().equals(mapitemmeta.getDisplayName())) return false;

        UUID uuid = p.getUniqueId();
        if (!mapusers.contains(uuid)) mapusers.add(uuid);
        return true;
    }
}
