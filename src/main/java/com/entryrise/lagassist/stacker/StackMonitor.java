package com.entryrise.lagassist.stacker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.entryrise.lagassist.Main;

public class StackMonitor {

	private static Map<Entity, Location> entities = Collections.synchronizedMap(new WeakHashMap<Entity, Location>());
	private static CopyOnWriteArrayList<SplitChangeEvent> events = new CopyOnWriteArrayList<SplitChangeEvent>();

	public static void Enabler(boolean reload) {

		if (!reload) {
			runTask();
		}
	}

	private static void runTask() {
		// Synchronizer tool.
		Bukkit.getScheduler().runTaskTimer(Main.p, () -> {

			List<Entity> ents = new ArrayList<Entity>();
			for (World w : Bukkit.getWorlds()) {
				ents.addAll(w.getEntities());
			}

//			for (Entity ent : ents) {
//				if (ent == null) {
//					continue;
//				}
//
//				if (!(ent instanceof LivingEntity)) {
//					continue;
//				}
//
//				if (ent instanceof HumanEntity) {
//					continue;
//				}
//
//				Location old = null;
//				if (entities.containsKey(ent)) {
//					old = entities.get(ent);
//				}
//				Location loc = ent.getLocation();
//
//				if (loc.equals(old)) {
//					continue;
//				}
//
//				entities.put(ent, loc);
//
//				if (!runMove(old, loc)) {
//					continue;
//				}
//
//				events.add(new SplitChangeEvent(ent, old, loc));
//
//				for (SplitChangeEvent event : events) {
//					Bukkit.getPluginManager().callEvent(event);
//				}
//				events.clear();
//			}

			Bukkit.getScheduler().runTaskAsynchronously(Main.p, () -> {
				for (Entity ent : ents) {
					if (ent == null) {
						continue;
					}

					if (!(ent instanceof LivingEntity)) {
						continue;
					}

					if (ent instanceof HumanEntity) {
						continue;
					}

					Location old = null;
					if (entities.containsKey(ent)) {
						old = entities.get(ent);
					}
					Location loc = ent.getLocation();

					if (loc.equals(old)) {
						continue;
					}

					entities.put(ent, loc);

					if (!runMove(old, loc)) {
						continue;
					}

					events.add(new SplitChangeEvent(ent, old, loc));
				}
			});

			for (SplitChangeEvent event : events) {
				Bukkit.getPluginManager().callEvent(event);
			}
			events.clear();
		}, 5, 5);

		// Creator tool.
	}

	public static boolean runMove(Location from, Location to) {
		if (!Main.config.getBoolean("smart-stacker.checks.split-change-check")) {
			return false;
		}

		if (from == null || to == null) {
			return false;
		}

		from = from.clone();
		to = to.clone();

//		if (!from.getWorld().getName().equals(to.getWorld().getName())) {
//			return true;
//		}

//		int fromx = from.getBlockX() / 16;
//		int tox = to.getBlockX() / 16;
//		
//		int fromz = from.getBlockZ() / 16;
//		int toz = to.getBlockZ() / 16;
//		
//		
//		if (fromx != tox) {
//			return true;
//		}
//		
//		if (fromz != toz) {
//			return true;
//		}

//		Chunk chk = to.getChunk();
//		
//		if (chk == null) {
//			return false;
//		}
//		
//		if (!chk.isLoaded()) {
//			return false;
//		}

//		Chunk chk1 = from.getChunk();
//		Chunk chk2 = to.getChunk();

//		if (!chk1.equals(chk2)) {
//			return true;
//		}

//		int sp1 = StackChunk.getSplit(from);
//		int sp2 = StackChunk.getSplit(to);

		return new Split(from).equals(new Split(to));
	}

	public static class Split {
		private String world;
		private int x;
		private int z;

		private int split;

		public Split(Location loc) {
			setWorld(loc.getWorld().getName());

			setX(loc.getBlockX() / 16);
			setZ(loc.getBlockZ() / 16);

			setSplit(StackChunk.getSplit(loc));
		}

		public int getSplit() {
			return split;
		}

		public void setSplit(int split) {
			this.split = split;
		}

		public int getZ() {
			return z;
		}

		public void setZ(int z) {
			this.z = z;
		}

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public String getWorld() {
			return world;
		}

		public void setWorld(String world) {
			this.world = world;
		}

		public boolean equals(Split other) {
			if (getX() != other.getX()) {
//				System.out.println("X NOT SAME");
				return false;
			}

			if (getZ() != other.getZ()) {
//				System.out.println("Z NOT SAME");
				return false;
			}

			if (getSplit() != other.getSplit()) {
//				System.out.println("SPLIT NOT SAME");
				return false;
			}

			if (!getWorld().equalsIgnoreCase(other.getWorld())) {
//				System.out.println("WORLD NOT SAME");
				return false;
			}

			return true;
		}

	}

	public static class SplitChangeEvent extends Event {

		private static final HandlerList HANDLERS = new HandlerList();

		private Entity ent;
		private Location from;
		private Location to;

		public SplitChangeEvent(Entity ent, Location from, Location to) {
			this.ent = ent;
			this.from = from;
			this.to = to;
		}

		public HandlerList getHandlers() {
			return HANDLERS;
		}

		public static HandlerList getHandlerList() {
			return HANDLERS;
		}

		public Entity getEntity() {
			return ent;
		}

		public Location getFrom() {
			return from;
		}

		public Location getTo() {
			return to;
		}

	}

}
