package org.alvindimas05.lagassist;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.alvindimas05.lagassist.maps.TpsRender;
import org.alvindimas05.lagassist.minebench.SpecsGetter;
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

public class MonTools implements Listener {

    public static ItemStack mapitem;
    public static ItemMeta mapitemmeta;

    public static List<UUID> actionmon = new ArrayList<>();
    public static List<UUID> mapusers = new ArrayList<>();
    private static DecimalFormat format = new DecimalFormat("#0.00");

    private static String stbmsg = Main.config.getString("stats-bar.message");
    private static int stbinterv = Main.config.getInt("stats-bar.tps-interval");
    private static int stbshowdl = Main.config.getInt("stats-bar.show-delay");

    public static void Enabler(boolean reload) {
        if (!reload) {
            Main.p.getServer().getPluginManager().registerEvents(new MonTools(), Main.p);
        }

        Bukkit.getLogger().info("    §e[§a✔§e] §fMapVisualizer.");

        if (VersionMgr.isNewMaterials()) {
            mapitem =  createMapItem();
            mapitemmeta = mapitem.getItemMeta();

            mapitemmeta.setDisplayName("§2§lLag§f§lAssist §e§lMonitor");
            mapitem.setItemMeta(mapitemmeta);

            int mapid = getMapId(mapitem);
            if(mapid != -1){
                MapView view = Reflection.getMapView(mapid);
                if (view != null) {
                    view.getRenderers().clear();
                    view.addRenderer(new TpsRender());
                }
            }
        } else {
            MapView map = Bukkit.createMap(Bukkit.getWorlds().get(0));
            mapitem = createMapItem(map);
            mapitemmeta = mapitem.getItemMeta();

            map.getRenderers().clear();
            map.addRenderer(new TpsRender());
        }

        StatsBar();
    }

    // For older version (1.13 and below)
    private static ItemStack createMapItem(MapView map) {
        ItemStack mapItem = new ItemStack(Material.MAP, 1, getMapId(map));

        MapMeta meta = (MapMeta) mapItem.getItemMeta();
        meta.setDisplayName("§2§lLag§f§lAssist §e§lMonitor");
        mapItem.setItemMeta(meta);
        return mapItem;
    }

    private static ItemStack createMapItem() {
        ItemStack mapItem = new ItemStack(Material.FILLED_MAP);

        MapMeta meta = (MapMeta) mapItem.getItemMeta();
        meta.setDisplayName("§2§lLag§f§lAssist §e§lMonitor");
        mapItem.setItemMeta(meta);
        return mapItem;
    }

    // For older version (1.13 and below)
    private static short getMapId(MapView map){
        try {
            return (short) Class.forName("org.bukkit.map.MapView").getMethod("getId").invoke(map);
        } catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }

    private static int getMapId(ItemStack mapItem) {
        if (mapItem == null) {
            return -1;
        }
        ItemMeta meta = mapItem.getItemMeta();
        if (meta instanceof MapMeta) {
            MapMeta mapMeta = (MapMeta) meta;
            if (mapMeta.hasMapView()) {
                return mapMeta.getMapView().getId();
            }
        }
        return -1;
    }

    public static void StatsBar() {
        Bukkit.getScheduler().runTaskTimer(Main.p, () -> {
            if (actionmon.isEmpty()) {
                return;
            }

            List<Player> onlinePlayers = actionmon.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            if (onlinePlayers.isEmpty()) {
                return;
            }

            double tpsraw = (ExactTPS.getTPS(10) > 20) ? 20 : ExactTPS.getTPS(stbinterv);
            String chunks = String.valueOf(getChunkCount());
            String ents = String.valueOf(getEntCount());

            Bukkit.getScheduler().runTaskAsynchronously(Main.p, () -> {
                String tps;
                if (tpsraw > 18) {
                    tps = "§a" + format.format(tpsraw);
                } else if (tpsraw > 15) {
                    tps = "§e" + format.format(tpsraw);
                } else {
                    tps = "§2" + format.format(tpsraw);
                }

                String message = ChatColor.translateAlternateColorCodes('&',
                    stbmsg.replaceAll("\\{TPS\\}", tps)
                        .replaceAll("\\{MEM\\}", format.format(SpecsGetter.FreeRam() / 1024))
                        .replaceAll("\\{CHKS\\}", chunks)
                        .replaceAll("\\{ENT\\}", ents));

                for (Player p : onlinePlayers) {
                    Reflection.sendAction(p, message);
                }
            });
        }, stbshowdl, stbshowdl);
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

        if (runNew(nw, p)) {
            return;
        }
        runOld(old, p);
    }

    public static void giveMap(Player p) {
        PlayerInventory inv = p.getInventory();
        int slot = inv.getHeldItemSlot();

        inv.setItem(slot, MonTools.mapitem);

        UUID UUID = p.getUniqueId();

        if (!mapusers.contains(UUID)) {
            mapusers.add(UUID);
        }
    }

    private static void runOld(ItemStack old, Player p) {
        if (old == null) {
            return;
        }

        if (!old.hasItemMeta()) {
            return;
        }
        ItemMeta ometa = old.getItemMeta();
        if (!ometa.hasDisplayName()) {
            return;
        }
        if (!ometa.getDisplayName().equals(mapitemmeta.getDisplayName())) {
            return;
        }

        UUID UUID = p.getUniqueId();

        if (!mapusers.contains(UUID)) {
            mapusers.add(UUID);
        }
    }

    private static boolean runNew(ItemStack nw, Player p) {
        if (!p.hasPermission("lagassist.use")) {
            return false;
        }

        if (nw == null) {
            return false;
        }

        if (!nw.hasItemMeta()) {
            return false;
        }
        ItemMeta nwmeta = nw.getItemMeta();
        if (!nwmeta.hasDisplayName()) {
            return false;
        }
        if (!nwmeta.getDisplayName().equals(mapitemmeta.getDisplayName())) {
            return false;
        }

        UUID UUID = p.getUniqueId();

        if (!mapusers.contains(UUID)) {
            mapusers.add(UUID);
        }
        return true;
    }
}
