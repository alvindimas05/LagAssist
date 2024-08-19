package org.alvindimas05.lagassist;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import net.kyori.adventure.text.Component;
import org.alvindimas05.lagassist.maps.TpsRender;
import org.alvindimas05.lagassist.minebench.SpecsGetter;
import org.alvindimas05.lagassist.packets.ServerPackage;
import org.alvindimas05.lagassist.utils.VersionMgr;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scoreboard.Scoreboard;
import org.json.simple.JSONObject;

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
//        if(!VersionMgr.isNewMaterials()) return;

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
            mapitem = createMapItemLegacy(map);
            map.getRenderers().clear();
            map.addRenderer(new TpsRender());
        }

        StatsBar();
    }

    private static ItemStack createMapItemLegacy(MapView map) {
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

    // Inner class Reflection
    static class Reflection {

        private static Map<Class<?>, Field[]> cached = new HashMap<>();
        private static String version = ServerPackage.getServerVersion();

        public enum Classes {
            CraftWorld(), CraftBlock(), CraftPlayer(), Material(), MapMeta(), IChatBaseComponent(), World(), MinecraftServer();

            private Class<?> type;

            public Class<?> getType() {
                return type;
            }

            public void setType(Class<?> type) {
                this.type = type;
            }
        }

        public enum Methods {
            setMapId(), getMapId(), getPlayerHandle(), getBlockType(), getIChatBaseComponent(), setViewDistance(), getServer();

            private Method mthd;

            public Method getMethod() {
                return mthd;
            }

            public void setMethod(Method mthd) {
                this.mthd = mthd;
            }
        }

        public static void Enabler() {
            // PUTTING CLASSES IN ENUM.
            Classes.CraftWorld.setType(getClass("{cb}.CraftWorld"));
            Classes.World.setType(getClass("{b}.World"));
            Classes.CraftBlock.setType(getClass("{cb}.block.CraftBlock"));
            Classes.CraftPlayer.setType(getClass("{cb}.entity.CraftPlayer"));
            Classes.Material.setType(getClass("{b}.Material"));
            Classes.MapMeta.setType(getClass("{b}.inventory.meta.MapMeta"));
            Classes.IChatBaseComponent.setType(getClass("{nm}.network.chat.IChatBaseComponent"));

            // PUTTING METHODS IN ENUM.
            Methods.setMapId.setMethod(getMethod(Classes.MapMeta.getType(), "setMapId", int.class));
            Methods.getMapId.setMethod(getMethod(Classes.MapMeta.getType(), "getMapId"));
            Methods.getPlayerHandle.setMethod(getMethod(Classes.CraftPlayer.getType(), "getHandle"));
            Methods.getBlockType.setMethod(getMethod(Classes.CraftBlock.getType(), "getType"));
            Methods.getIChatBaseComponent.setMethod(getMethod(Classes.IChatBaseComponent.getType(), "a", String.class));
            Methods.setViewDistance.setMethod(getMethod(Classes.World.getType(), "setViewDistance", int.class));
            Methods.getServer.setMethod(getMethod(Classes.MinecraftServer.getType(), "getServer"));
        }

        public static void sendAction(Player player, String s) {
            Component message = Component.text(s);
            player.sendActionBar(message);
        }

        public static MapView getMapView(int i) {
            try {
                Method getMap = Bukkit.class.getDeclaredMethod("getMap", int.class);
                return (MapView) getMap.invoke(null, i);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public static double getTPS(int number) {
            return Bukkit.getServer().getTPS()[number];
        }

        public static int getId(MapView view) {
            if (view == null) {
                return 0;
            }
            return view.getId();
        }

        @SuppressWarnings("unchecked")
        static JSONObject convert(String text) {
            JSONObject json = new JSONObject();
            json.put("text", text);
            return json;
        }

        public static Class<?> getClass(String classname) {
            try {
                String path = classname.replace("{nms}", "net.minecraft.server")
                    .replace("{nm}", "net.minecraft")
                    .replace("{cb}", "org.bukkit.craftbukkit." + version)
                    .replace("{b}", "org.bukkit");
                return Class.forName(path);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public static Object getCraftWorld(World w) {
            Class<?> crwclass = Classes.CraftWorld.getType();
            Object craftworld = crwclass.cast(w);
            return craftworld;
        }

        public static boolean isTile(Block b) {
            return !b.getState().getClass().getSimpleName().toLowerCase().contains("craftblockstate");
        }

        public static Entity getEntity(Location l) {
            try {
                Collection<Entity> ents = l.getWorld().getNearbyEntities(l, 1, 1, 1);
                for (Entity ent : ents) {
                    return ent;
                }
            } catch (Exception e) {
                return null;
            }
            return null;
        }

        public static void setmapId(ItemStack s, int id) {
            MapMeta mapm = (MapMeta) s.getItemMeta();
            try {
                runMethod(mapm, Methods.setMapId.getMethod(), id);
            } catch (Exception e) {
                e.printStackTrace();
            }
            s.setItemMeta(mapm);
        }

        public static int getMapId(ItemStack s) {
            MapMeta mapm = (MapMeta) s.getItemMeta();
            try {
                return (int) runMethod(mapm, Methods.getMapId.getMethod());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }

        public static Object getNmsPlayer(Player p) {
            if (p == null) {
                return null;
            }
            try {
                Method getHandle = p.getClass().getMethod("getHandle");
                return getHandle.invoke(p);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public static Object getNmsScoreboard(Scoreboard s) throws Exception {
            Method getHandle = s.getClass().getMethod("getHandle");
            return getHandle.invoke(s);
        }

        public static String getObjectSerialized(Object obj) {
            YamlConfiguration conf = new YamlConfiguration();
            Class<?> cls = obj.getClass();
            String loc = cls.getSimpleName();

            createObjectSerialized(conf, loc, obj, 0);

            return conf.saveToString();
        }

        public static void createObjectSerialized(YamlConfiguration conf, String loc, Object obj, int recursiveness) {
            Class<?> cls = obj.getClass();

            if (!cached.containsKey(cls)) {
                cached.put(cls, cls.getDeclaredFields());
            }

            for (Field fl : cached.get(cls)) {
                fl.setAccessible(true);

                Object newobj = getFieldValue(fl, obj);

                if (newobj == null) {
                    continue;
                }

                String name = fl.getName();
                String type = newobj.getClass().getSimpleName();

                String newloc = loc + "." + type + "." + name;

                if (isSimple(newobj) || recursiveness > 1) {
                    conf.set(newloc, convertToString(newobj));
                    continue;
                }

                createObjectSerialized(conf, newloc, newobj, recursiveness + 1);
            }
        }

        public static Object getFieldValue(Object instance, String fieldName) throws Exception {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(instance);
        }

        @SuppressWarnings("unchecked")
        public static <T> T getFieldValue(Field field, Object obj) {
            try {
                return (T) field.get(obj);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public static Field getField(Class<?> clazz, String fieldName) throws Exception {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        }

        public static Method getMethod(Class<?> clazz, String methodName, Class<?>... resl) {
            try {
                Method method = clazz.getDeclaredMethod(methodName, resl);
                method.setAccessible(true);
                return method;
            } catch (Exception e) {
                return null;
            }
        }

        public static Object runMethod(Object obj, Method m, Object... resl) throws Exception {
            return m.invoke(obj, resl);
        }

        public static Object runMethod(Object obj, String name, Object... resl) throws Exception {
            Class<?>[] classes = new Class<?>[resl.length];
            for (int i = 0; i < resl.length; i++) {
                classes[i] = resl[i].getClass();
            }
            return getMethod(obj.getClass(), name, classes).invoke(obj, resl);
        }

        public static void setValue(Object instance, String field, Object value) {
            try {
                Field f = instance.getClass().getDeclaredField(field);
                f.setAccessible(true);
                f.set(instance, value);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        public static void sendAllPacket(Object packet) throws Exception {
            for (Player p : Bukkit.getOnlinePlayers()) {
                Object nmsPlayer = getNmsPlayer(p);
                Object connection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
                connection.getClass().getMethod("sendPacket", getClass("{nms}.Packet")).invoke(connection, packet);
            }
        }

        public static int getPing(Player p) {
            try {
                Object entityPlayer = Methods.getPlayerHandle.getMethod().invoke(p);
                return (int) getFieldValue(entityPlayer, "ping");
            } catch (Exception e) {
                return -1;
            }
        }

        public static void sendListPacket(List<String> players, Object packet) {
            try {
                for (String name : players) {
                    Object nmsPlayer = getNmsPlayer(Bukkit.getPlayer(name));
                    Object connection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
                    connection.getClass().getMethod("sendPacket", getClass("{nms}.Packet")).invoke(connection, packet);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void sendPlayerPacket(Player p, Object packet) throws Exception {
            Object nmsPlayer = getNmsPlayer(p);
            Object connection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
            connection.getClass().getMethod("sendPacket", getClass("{nms}.Packet")).invoke(connection, packet);
        }

        public static PluginCommand getCommand(String name, Plugin plugin) {
            PluginCommand command = null;

            try {
                Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
                c.setAccessible(true);

                command = c.newInstance(name, plugin);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return command;
        }

        public static CommandMap getCommandMap() {
            CommandMap commandMap = null;

            try {
                PluginManager pluginManager = Bukkit.getPluginManager();
                Field f = pluginManager.getClass().getDeclaredField("commandMap");
                f.setAccessible(true);

                commandMap = (CommandMap) f.get(pluginManager);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return commandMap;
        }

        private static String convertToString(Object obj) {
            Class<?> cls = obj.getClass();

            if (cls.isArray()) {
                StringBuilder stg = new StringBuilder();
                int length = Array.getLength(obj);
                for (int i = 0; i < length; i++) {
                    stg.append(convertToString(Array.get(obj, i)));
                }
                return stg.toString();
            }

            return obj.toString();
        }

        private static boolean isSimple(Object obj) {
            Class<?> cls = obj.getClass();

            if (cls.isPrimitive()) {
                return true;
            }

            if (cls.isEnum()) {
                return true;
            }

            if (cls == Integer.class) {
                return true;
            }

            if (cls == Boolean.class) {
                return true;
            }

            if (cls == String.class) {
                return true;
            }

            if (cls == Character.class) {
                return true;
            }

            if (cls == Byte.class) {
                return true;
            }

            if (cls == Short.class) {
                return true;
            }

            if (cls == Float.class) {
                return true;
            }

            if (cls == Double.class) {
                return true;
            }

            if (cls == Long.class) {
                return true;
            }

            return false;
        }

        public static void setViewDistance(World w, int amount) {
            try {
                runMethod(w, Methods.setViewDistance.getMethod(), amount);
                Bukkit.getLogger().info("Successfully set view distance at " + amount + " in world " + w.getName());
            } catch (Exception e) {
                Bukkit.getLogger().warning("Exception at setViewDistance (" + w.getName() + ", " + amount + ")");
            }
        }
    }
}
