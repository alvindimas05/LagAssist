package com.entryrise.lagassist.stacker;

import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import com.entryrise.lagassist.Main;
import com.entryrise.lagassist.stacker.StackMonitor.SplitChangeEvent;
import com.entryrise.lagassist.utils.WorldMgr;

public class StackManager implements Listener {

	protected static boolean smartstacker = false;

	public static void Enabler(boolean reload) {
		smartstacker = Main.config.getBoolean("smart-stacker.enabled");

		if (!smartstacker) {
			return;
		}

		StackChunk.Enabler();
		StackMonitor.Enabler(reload);
		StackComparer.Enabler(reload);

		if (!reload) {
			Main.p.getServer().getPluginManager().registerEvents(new StackManager(), Main.p);
			Main.p.getServer().getPluginManager().registerEvents(new StackManipulator(), Main.p);
			
			if (Main.paper) {
				Main.p.getServer().getPluginManager().registerEvents(new PaperOnly(), Main.p);
			}
		}
		
		StackChunk.runStart();
		Bukkit.getLogger().info("    §e[§a✔§e] §fSmart Mob Stacker.");
	}
	
	public static boolean isStacked(Entity ent) {
		return StackChunk.getStack(ent) > 1;
	}

	public static void Disabler() {
		StackChunk.runShutdown();
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onCreatureSpawn(CreatureSpawnEvent e) {
		if (e.isCancelled()) {
			return;
		}

		if (!smartstacker) {
			return;
		}
		
		if (WorldMgr.isBlacklisted(e.getLocation().getWorld())) {
			return;
		}
		
		e.getEntity().setMetadata("lagassist.spawnreason", new FixedMetadataValue(Main.p, e.getSpawnReason()));
	}
	
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onSpawn(EntitySpawnEvent e) {
		
		if (e.isCancelled()) {
			return;
		}

		if (!smartstacker) {
			return;
		}
		
		if (WorldMgr.isBlacklisted(e.getLocation().getWorld())) {
			return;
		}
		
		if(StackManipulator.isDeadEntity()) {
			return;
		}
		
		Entity ent = e.getEntity();
		
		if (!Main.config.getBoolean("smart-stacker.checks.spawn-check")) {
			return;
		}
		
//		if (!(ent instanceof LivingEntity)) {
//			return;
//		}
		
		if (StackChunk.tryStacking(ent.getLocation(), ent.getType(), ent)) {
//			e.setCancelled(true);
			ent.remove();
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onDeath(EntityDeathEvent e) {
		if (e instanceof Cancellable && ((Cancellable) e).isCancelled()) {
			return;
		}

		if (!smartstacker) {
			return;
		}
		
		if (!Main.config.getBoolean("smart-stacker.technical.drops-fix")) {
			return;
		}

		StackChunk.setDrops(e);
	}
	
	private static class PaperOnly implements Listener {
		// Ugly so it hopefully doesn't break.
		@EventHandler(priority = EventPriority.HIGH)
		public void onPreSpawn(com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent e) {
			if (e.isCancelled()) {
				return;
			}

			if (!smartstacker) {
				return;
			}
			
			if (WorldMgr.isBlacklisted(e.getSpawnLocation().getWorld())) {
				return;
			}
			
			if(StackManipulator.isDeadEntity()) {
				return;
			}

			if (!Main.config.getBoolean("smart-stacker.checks.pre-spawn-check")) {
				return;
			}

			if (StackChunk.tryStacking(e.getSpawnLocation(), e.getType(), null)) {
				e.setCancelled(true);
			}
		}
		
		@EventHandler(priority = EventPriority.HIGH)
		public void onAnvil(PrepareAnvilEvent e) {
			if (!smartstacker) {
				return;
			}

			ItemStack result = e.getResult();

			if (result == null) {
				return;
			}

			if (!result.hasItemMeta()) {
				return;
			}
			
			ItemMeta imeta = result.getItemMeta();

			if (!imeta.hasDisplayName()) {
				return;
			}
			
			Pattern pat = Pattern.compile(StackChunk.regexpat.replace("{type}", "(.*.)"), Pattern.MULTILINE);
			Matcher mtch = pat.matcher(imeta.getDisplayName());
			
			if (!mtch.find()) {
				return;
			}
			
			e.setResult(null);
		}
	}


	private EnumSet<EntityType> illegal = EnumSet.of(EntityType.PLAYER);

	@EventHandler(priority = EventPriority.HIGH)
	public void onSplitChange(SplitChangeEvent e) {
		if (!smartstacker) {
			return;
		}
		
		if (!Main.config.getBoolean("smart-stacker.checks.split-change-check")) {
			return;
		}
		
		if (WorldMgr.isBlacklisted(e.getFrom().getWorld())) {
			return;
		}

		Entity ent = e.getEntity();
		EntityType etype = ent.getType();

		if (illegal.contains(etype)) {
			return;
		}
		
		// Not a stackable entity.
//		if (!(ent instanceof LivingEntity)) {
//			return;
//		}

		StackChunk.cleanSplit(e.getFrom().getChunk(), StackChunk.getSplit(e.getFrom()), ent.getType());

		if (StackChunk.tryStacking(e.getTo(), etype, ent)) {
			ent.remove();
		}
	}

	public void onChunkLoad(ChunkLoadEvent e) {
		if (!smartstacker) {
			return;
		}

		if (Main.config.getBoolean("smart-stacker.technical.shutdown-clean")) {
			return;
		}
		
		if (WorldMgr.isBlacklisted(e.getWorld())) {
			return;
		}

		Chunk chk = e.getChunk();

		StackChunk.loadChunk(chk);
	}
	
	public void onChunkLoad(ChunkUnloadEvent e) {
		if (!smartstacker) {
			return;
		}

		if (WorldMgr.isBlacklisted(e.getWorld())) {
			return;
		}
		
		Chunk chk = e.getChunk();

		StackChunk.unloadChunk(chk);
	}
	
	public static boolean isStackable(Entity ent) {
		if (ent == null) {
			return false;
		}
		
//		if (!(ent instanceof LivingEntity)) {
//			return false;
//		}
		
		if (ent instanceof Player) {
			return false;
		}
		
		if (!Main.config.getStringList("smart-stacker.gameplay.stackable").contains(ent.getType().toString().toUpperCase())) {
			return false;
		}
		
		String spawnreason = ent.hasMetadata("lagassist.spawnreason") ? ent.getMetadata("lagassist.spawnreason").get(0).value().toString().toUpperCase() : "UNKNOWN";
		
		if(!(Main.config.getStringList("smart-stacker.gameplay.spawn-reasons").contains("ALL") || Main.config.getStringList("smart-stacker.gameplay.spawn-reasons").contains(spawnreason))) {
			return false;
		}
		
		int stacksize = StackChunk.getStack(ent);
		
		if (stacksize < 0) {
			return false;
		}

		return true;
	}

}
