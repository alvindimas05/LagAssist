package com.entryrise.lagassist.stacker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import com.entryrise.lagassist.Main;
import com.entryrise.lagassist.utils.MathUtils;
import com.entryrise.lagassist.utils.Others;
import com.entryrise.lagassist.utils.WorldMgr;

public class StackChunk {

	private static Map<Chunk, StackChunk> chunks = new HashMap<Chunk, StackChunk>();

	public static String nameformat = "";
	public static String regexpat = "";
	private static int splits = 8;

	private Map<EntityType, HashSet<Entity>>[] ents;

	public static void Enabler() {
		splits = Main.config.getInt("smart-stacker.technical.splits");
		nameformat = ChatColor.translateAlternateColorCodes('&',
				Main.config.getString("smart-stacker.gameplay.tag-format"));
		regexpat = nameformat.replace("{size}", "(.*.)");
	}

	@SuppressWarnings("unchecked")
	public StackChunk(Chunk chk) {

		ents = (Map<EntityType, HashSet<Entity>>[]) new HashMap[256 / splits];

		for (int i = 0; i < 256 / splits; i++) {
			ents[i] = new HashMap<EntityType, HashSet<Entity>>();
		}
	}

	// Returns true if mob is consumed in the process, and false if the mob will be
	// kept alive.
	public static boolean tryStacking(Location loc, EntityType type, Entity optional) {
		Chunk chk = loc.getChunk();

		StackChunk stchk;

		if (chunks.containsKey(chk)) {
			stchk = chunks.get(chk);
		} else {
			stchk = new StackChunk(chk);
			chunks.put(chk, stchk);
		}

		int split = getSplit(loc);

		cleanSplit(chk, split, type);

		boolean consumed = false;
		int size = 0;

		if (optional != null) {
			if (!StackManager.isStackable(optional)) {
				return false;
			}
		} else {
			size++;
		}

		Entity free = getMatch(stchk.ents[split], loc, type, optional);

		size += getStack(free);

		Main.sendDebug("FINAL SIZE: " + size, 2);

		consumed = (free.equals(optional)) ? false : true;

		if (optional != null && consumed) {
			size += getStack(optional);
		}

		Main.sendDebug("FINAL OPT: " + size, 2);
//		if (stchk.ents[split].containsKey(type)) {
//			free = stchk.ents[split].get(type);
//			// Check if they are simmilar to avoid stacks of (for example) different color sheep.
//			if (optional != null && !StackComparer.isSimilar(free, optional)) {
//					return false;
//			}
//			size += getStack(free);
//			consumed = true;
//		} else if (optional != null) {
//			free = optional;
//			stchk.ents[split].put(type, optional);
//			consumed = false;
//		} else {
//			free = loc.getWorld().spawnEntity(loc, type);
//			consumed = true;
//		}

		// increase stack size due to new mob.

		setSize(free, size);

		return consumed;
	}

	private static Entity getMatch(Map<EntityType, HashSet<Entity>> ents, Location loc, EntityType type,
			Entity optional) {

		if (!ents.containsKey(type)) {
			ents.put(type, new HashSet<Entity>());
		}

		Set<Entity> list = ents.get(type);

		Entity ideal = null;

		if (optional == null) {
			if (list.isEmpty() || isUnderMinimum(list)) {
				ideal = loc.getWorld().spawnEntity(loc, type);
				list.add(ideal);
				return ideal;
			} else {
				return list.iterator().next();
			}
		} else if (isUnderMinimum(list)) {
			list.add(optional);
			return optional;
		}
		
		for (Entity ent : list) {
			boolean similar = StackComparer.isSimilar(ent, optional);
			if (similar) {
				return ent;
			}
		}
		
		list.add(optional);
		return optional;

	}

	public static int getStack(Entity ent) {
		if (!StackManager.smartstacker) {
			return 0;
		}

		if (ent == null) {
			return 0;
		}

		if (ent.hasMetadata("lagassist.stacksize")) {
			return ent.getMetadata("lagassist.stacksize").get(0).asInt();
		}

		String name = ent.getCustomName();

		if (name == null) {
			return 1;
		}

		Pattern pat = Pattern.compile(regexpat.replace("{type}", Others.firstHighcase(ent.getType().toString())),
				Pattern.MULTILINE);

		Matcher match = pat.matcher(name);

		if (!match.find()) {
			return -1;
		}

		String count = match.group(1);

		if (!MathUtils.isInt(count)) {
			return 1;
		}

		return Integer.valueOf(count);
	}

	public static void setSize(Entity ent, int size) {
		String formatted = nameformat.replace("{type}", Others.firstHighcase(ent.getType().toString()))
				.replace("{size}", "" + Math.min(size, Main.config.getInt("smart-stacker.technical.max-stack")));

		ent.setMetadata("lagassist.stacksize", new FixedMetadataValue(Main.p, size));
		ent.setCustomName(formatted);
		ent.setCustomNameVisible(Main.config.getBoolean("smart-stacker.gameplay.tag-visibility"));
	}

	public static void setDrops(EntityDeathEvent e) {
		Entity ent = e.getEntity();

		// Clean in case it does and is main mob.
//		int split =getSplit(loc);
//		cleanSplit(chk, split, ent.getType(), true);

		int stack = getStack(ent);

		if (stack < 2) {
			return;
		}

		List<ItemStack> drops = new ArrayList<ItemStack>();

		for (ItemStack itm : e.getDrops()) {
			int amount = itm.getAmount() * stack;
			while (amount > 0) {
				ItemStack cl = itm.clone();
				cl.setAmount(Math.min(amount, cl.getMaxStackSize()));
				amount -= cl.getAmount();
				drops.add(cl);
			}
		}

		e.getDrops().clear();
		e.getDrops().addAll(drops);
		e.setDroppedExp(e.getDroppedExp() * stack);
	}

	public static void runShutdown() {
		if (!Main.config.getBoolean("smart-stacker.technical.shutdown-clean")) {
			return;
		}
		for (StackChunk chk : chunks.values()) {
			for (int i = 0; i < splits; i++) {
				for (Set<Entity> elist : chk.ents[i].values()) {
					for (Entity ent : elist) {
						ent.remove();
					}
				}
			}
		}
	}

	public static void runStart() {
		if (Main.config.getBoolean("smarshot-stacker.technical.shutdown-clean")) {
			return;
		}

		for (World w : Bukkit.getWorlds()) {
			if (WorldMgr.isBlacklisted(w)) {
				continue;
			}
			for (Chunk chk : w.getLoadedChunks()) {
				loadChunk(chk);
			}
		}
	}

	public static void loadChunk(Chunk chk) {
		Entity[] ents = chk.getEntities();

		for (Entity ent : ents) {
			if (!(ent instanceof LivingEntity)) {
				continue;
			}

			if (StackChunk.tryStacking(ent.getLocation(), ent.getType(), ent)) {
				ent.remove();
			}
		}
	}

	public static void unloadChunk(Chunk chk) {
		StackChunk stack = chunks.get(chk);

		if (stack == null) {
			return;
		}

		for (Map<EntityType, HashSet<Entity>> m : stack.ents) {
			for (HashSet<Entity> types : m.values()) {
				for (Entity ent : types) {
					ent.remove();
				}
			}
		}

		chunks.remove(chk);

	}

	/*
	 * Implement min stack feature in beta.
	 *
	 * TODO: TEST FUNCTIONALITY
	 */
	protected static boolean isUnderMinimum(Set<Entity> ents) {
		int minstack = Main.config.getInt("smart-stacker.technical.min-stack");

		if (minstack <= 1) {
			return false;
		}

		int stacktotal = getStackTotal(ents);

		return minstack > 0 && stacktotal < minstack;
	}

	private static int getStackTotal(Set<Entity> ents) {
		int total = 0;
		for (Entity ent : ents) {
			total += getStack(ent);
		}

		return total;
	}

	// Setting clean to true makes it force it;
	public static void cleanSplit(Chunk chk, int split, EntityType ent) {
		if (!chunks.containsKey(chk)) {
			return;
		}

		StackChunk stchk = chunks.get(chk);

		if (!stchk.ents[split].containsKey(ent) || isUnderMinimum(stchk.ents[split].get(ent))) {
			return;
		}

		boolean clean;

		for (Entity entity : stchk.ents[split].get(ent)) {
			clean = false;
			if (entity == null || entity.isDead()) {
				clean = true;
			} else if (!entity.getLocation().getChunk().equals(chk)) {
				clean = true;
			} else if (getSplit(entity.getLocation()) != split) {
				clean = true;
			}

			if (!clean) {
				continue;
			}

			stchk.ents[split].remove(ent);
		}

	}

	public static int getSplit(Location loc) {
		return Math.max(0, Math.min(loc.getBlockY(), 255)) / splits;
	}
}
