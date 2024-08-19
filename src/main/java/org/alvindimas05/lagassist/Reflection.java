package org.alvindimas05.lagassist;

import java.lang.reflect.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alvindimas05.lagassist.packets.ServerPackage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.json.simple.JSONObject;
import org.bukkit.inventory.Inventory;

import org.alvindimas05.lagassist.utils.VersionMgr;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class Reflection {

	private static Map<Class<?>, Field[]> cached = new HashMap<Class<?>, Field[]>();
	private static String version = ServerPackage.getServerVersion();

	public static enum Classes {

		CraftWorld(), CraftBlock(), CraftPlayer(), Material(), MapMeta(), WorldServer(), ChunkProviderServer(),
		PacketPlayOutTitle(), IChatBaseComponent(), World(), MinecraftServer();

		private Class<?> type;

		public Class<?> getType() {
			return type;
		}
	}

	public static enum Methods {

		setMapId(), getMapId(), getPlayerHandle(), getBlockType(), getChunkProviderServer(), chunkExists(),
		getIChatBaseComponent(), setViewDistance(), getServer();

		private Method mthd;

		public Method getMethod() {
			return mthd;
		}
	}

	public static void Enabler() {
		// PUTTING CLASSES IN ENUM.
		Classes.CraftWorld.type = getClass("{cb}.CraftWorld");
		Classes.World.type = getClass("{b}.World");
		Classes.CraftBlock.type = getClass("{cb}.block.CraftBlock");
		Classes.CraftPlayer.type = getClass("{cb}.entity.CraftPlayer");
		Classes.Material.type = getClass("{b}.Material");
		Classes.MapMeta.type = getClass("{b}.inventory.meta.MapMeta");
		Classes.WorldServer.type = getClass(VersionMgr.isV_17Plus() ? "{nms}.level.WorldServer" : "{nms}.WorldServer");
		Classes.MinecraftServer.type = getClass("{nms}.MinecraftServer");
		Classes.ChunkProviderServer.type = getClass(VersionMgr.isV_17Plus() ? "{nms}.level.ChunkProviderServer" : "{nms}.ChunkProviderServer");
		Classes.IChatBaseComponent.type = getClass(VersionMgr.isV_17Plus() ? "{nm}.network.chat.IChatBaseComponent" : "{nms}.IChatBaseComponent");
//		Classes.PacketPlayOutTitle.type = getClass(VersionMgr.isV_17Plus()? "{nm}.network.protocol.game.PacketPlayOutTitle" : "{nms}.PacketPlayOutTitle");

		// PUTTING METHODS IN ENUM.
		Methods.setMapId.mthd = getMethod(Classes.MapMeta.getType(), "setMapId", int.class);
		Methods.getMapId.mthd = getMethod(Classes.MapMeta.getType(), "getMapId");
		Methods.getPlayerHandle.mthd = getMethod(Classes.CraftPlayer.getType(), "getHandle");
		Methods.getBlockType.mthd = getMethod(Classes.CraftBlock.getType(), "getType");
		Methods.getChunkProviderServer.mthd = getMethod(Classes.WorldServer.getType(), "getChunkProviderServer");
		Methods.getIChatBaseComponent.mthd = getMethod(Classes.IChatBaseComponent.getType(), "a", String.class);
		Methods.chunkExists.mthd = getMethod(Classes.ChunkProviderServer.getType(), VersionMgr.ChunkExistsName(),
				int.class, int.class);
		Methods.setViewDistance.mthd = getMethod(Classes.World.getType(), "setViewDistance", int.class);
		Methods.getServer.mthd = getMethod(Classes.MinecraftServer.getType(), "getServer");
	}

	public static Inventory getTopInventory(InventoryEvent event) {
		try {
			Object view = event.getView();
			Method m = view.getClass().getMethod("getTopInventory");
			m.setAccessible(true);
			return (Inventory) m.invoke(view);
		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static Inventory getBottomInventory(InventoryEvent event) {
		try {
			Object view = event.getView();
			Method m = view.getClass().getMethod("getBottomInventory");
			m.setAccessible(true);
			return (Inventory) m.invoke(view);
		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getInventoryViewTitle(InventoryEvent event){
		try {
			Object view = event.getView();
			Method m = view.getClass().getMethod("getTitle");
			m.setAccessible(true);
			return (String) m.invoke(view);
		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("deprecation")
	public static void sendAction(Player player, String s) {
		if(VersionMgr.isV1_8()){
			try {
				Object chat = Reflection.getClass("{nmsv}.ChatComponentText")
						.getConstructor(String.class).newInstance(s);
				Object packet = getClass("{nmsv}.PacketPlayOutChat")
						.getConstructor(
								getClass("{nmsv}.IChatBaseComponent"), byte.class
						).newInstance(chat, (byte) 2);
				sendPlayerPacket(player, packet);
			} catch (Exception e){
				e.printStackTrace();
			}
		} else {
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(s).create());
		}
	}

//	public void sendTitle(Player p, int fadein, int stay, int fadeout, String title, String subtitle) {
//		try {
//			Object enumTitle = Classes.PacketPlayOutTitle.getType().getDeclaredClasses()[0].getField("TITLE").get(null);
//			Object enumSubtitle = Classes.PacketPlayOutTitle.getType().getDeclaredClasses()[0].getField("SUBTITLE")
//					.get(null);
//
//			Object titlebase = runMethod(null, Methods.getIChatBaseComponent.getMethod(),
//					"{\"text\": \"" + title + "\"}");
//			Object subtitlebase = runMethod(null, Methods.getIChatBaseComponent.getMethod(),
//					"{\"text\": \"" + subtitle + "\"}");
//
//			Class<?> packetcls = Classes.PacketPlayOutTitle.getType();
//			Constructor<?> constr = packetcls.getConstructor(
//					Classes.PacketPlayOutTitle.getType().getDeclaredClasses()[0], Classes.IChatBaseComponent.getType(),
//					int.class, int.class, int.class);
//
//			Object packetTitle = constr.newInstance(enumTitle, titlebase, fadein, stay, fadeout);
//			Object packetSubtitle = constr.newInstance(enumSubtitle, subtitlebase, fadein, stay, fadeout);
//
//			sendPlayerPacket(p, packetTitle);
//			sendPlayerPacket(p, packetSubtitle);
//		}
//
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	public static MapView getMapView(int i) {
		Class<?> buk = Bukkit.class;

		try {
			Method getMap = buk.getDeclaredMethod("getMap", VersionMgr.isNewMaterials() ? int.class : short.class);
			return (MapView) (VersionMgr.isNewMaterials() ? getMap.invoke(null, i) : getMap.invoke(null, (short) i));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	
	private static Object minecraftserver = null;
	public static double getTPS(int number) {
		
		try {
		if (minecraftserver == null) {
			minecraftserver = Methods.getServer.mthd.invoke(null);
		}
		
		Field f = Reflection.getField(Classes.MinecraftServer.type, "recentTps");
		f.setAccessible(true);
		
		return ((double[])f.get(minecraftserver))[number];
		} catch(Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static int getId(MapView view) {
		if (view == null) {
			return 0;
		}

		Class<?> mv = MapView.class;

		try {
			Method getMap = mv.getDeclaredMethod("getId");
			Object obj = getMap.invoke(view);
			if (obj instanceof Short) {
				return (short) obj;
			} else if (obj instanceof Integer) {
				return (int) obj;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	@SuppressWarnings("unchecked")
	static JSONObject convert(String text) {
		JSONObject json = new JSONObject();
		json.put("text", text);
		return json;
	}

	public static Class<?> getClass(String classname) {
		try {
			
			String path = classname.replace("{nms}", "net.minecraft.server" + (VersionMgr.isV_17Plus() ? "" : "." + version))
					.replace("{nmsv}", "net.minecraft.server." + version)
					.replace("{nm}", "net.minecraft" + (VersionMgr.isV_17Plus() ? "" : "." + version))
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
		System.out.println(crwclass.getName());
		Object craftworld = crwclass.cast(w);
		return craftworld;
	}

	public static Object getWorldServer(Object craftWorld) {
		try {
			return getFieldValue(craftWorld, "world");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Object getChunkProvider(Object worldServer) {
		try {
			return runMethod(worldServer, Methods.getChunkProviderServer.getMethod());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean isChunkExistent(Object chunkProvider, int x, int z) {
		try {
			return (boolean) runMethod(chunkProvider, Methods.chunkExists.getMethod(), x, z);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// NOTE: ASSUMING TRUE IN ORDER TO IGNORE IF EXCEPTION POPS UP.
		return true;
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
		Method getHandle;
		try {
			getHandle = p.getClass().getMethod("getHandle");
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

//	public static long getObjectSize(Object obj, int recursiveness) {
//		if (obj == null) {
//			return 0;
//		}
//		
//		
//		if (obj instanceof Serializable) {
//			return getSerializedSize((Serializable) obj);
//		}
//		
//		Class<?> cls = obj.getClass();
//		
//		long size = 0;
//		
//		if (!cached.containsKey(cls)) {
//			cached.put(cls, cls.getDeclaredFields());
//		}
//		
//		for (Field fl : cached.get(cls)) {
//			fl.setAccessible(true);
//			Object nobj = getFieldValue(fl, obj);
//			size+=getObjectSize(nobj, recursiveness+1);
//		}
//		
//		return size;
//	}
//	
//	private static long getSerializedSize(Serializable obj) {
//		
//		try {
//			ByteArrayOutputStream bos = new ByteArrayOutputStream();
//			ObjectOutputStream oos = new ObjectOutputStream(bos);
//			oos.writeObject(obj);
//			oos.flush();
//			byte[] data = bos.toByteArray();
//
//			return data.length;
//		} catch (Exception e) {
//			return 0;
//		}
//	}

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
		Method method;
		try {
			method = clazz.getDeclaredMethod(methodName, resl);
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
		if(VersionMgr.isV1_8()){
			connection.getClass().getMethod("sendPacket", getClass("{nmsv}.Packet")).invoke(connection, packet);
		} else {
			connection.getClass().getMethod("sendPacket", getClass("{nms}.Packet")).invoke(connection, packet);
		}
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
		return Bukkit.getCommandMap();
	}

	private static String convertToString(Object obj) {
		Class<?> cls = obj.getClass();

		if (cls.isArray()) {
			String stg = "";
			int length = Array.getLength(obj);
			for (int i = 0; i < length; i++) {
				stg += convertToString(Array.get(obj, i));
			}
			return stg;

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
			runMethod(w, Methods.setViewDistance.mthd, amount);
			Main.sendDebug("Succesfully set view distance at " + amount + " in world " + w.getName(), 1);
		} catch (Exception e) {
			Main.sendDebug("Exception at setViewDistance (" + w.getName() + ", " + amount + ")", 1);
		}
		
		
	}
}
