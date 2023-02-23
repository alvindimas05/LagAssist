package com.entryrise.lagassist;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.NotePlayEvent;

import com.entryrise.lagassist.utils.WorldMgr;

public class Physics implements Listener {

	public static boolean denyphysics = false;
	private static Set<String> mats = new HashSet<String>(Arrays.asList("REDSTONE_WIRE", "NOTE_BLOCK", "PISTON", "PISTON_HEAD", "DIODE", "REPEATER", "COMPARATOR", "REDSTONE_COMPARATOR", "REDSTONE_COMPARATOR_ON", "REDSTONE_COMPARATOR_OFF"));

	public static void Enabler(boolean reload) {
		if (!reload) {
			Main.p.getServer().getPluginManager().registerEvents(new Physics(), Main.p);
		}
		Bukkit.getLogger().info("    §e[§a✔§e] §fPhysics-Tweaker.");
		denyphysics = Main.config.getBoolean("deny-physics.enabled");
		
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void disablePhysics(BlockPhysicsEvent e) {
		if (Main.config.getBoolean("deny-physics.systems.physics") && denyphysics) {
			if (WorldMgr.blacklist.contains(e.getBlock().getWorld().getName())) {
				return;
			}
			
			if (mats.contains(e.getChangedType().toString().toUpperCase())) {
				return;
			}
			e.setCancelled(true);
		}
	}
	

	@EventHandler(priority = EventPriority.LOWEST)
	public void disableExplosions(BlockExplodeEvent e) {
		if (Main.config.getBoolean("deny-physics.systems.explosions") && denyphysics) {
			if (WorldMgr.blacklist.contains(e.getBlock().getWorld().getName())) {
				return;
			}
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void pistonBrkRetract(BlockPistonRetractEvent e) {
		if (Main.config.getBoolean("deny-physics.systems.pistons") && denyphysics) {
			if (WorldMgr.blacklist.contains(e.getBlock().getWorld().getName())) {
				return;
			}
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void pistonBrkPush(BlockPistonExtendEvent e) {
		if (Main.config.getBoolean("deny-physics.systems.pistons") && denyphysics) {
			if (WorldMgr.blacklist.contains(e.getBlock().getWorld().getName())) {
				return;
			}
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void itemMelt(BlockFadeEvent e) {
		if (Main.config.getBoolean("deny-physics.systems.melting") && denyphysics) {
			if (WorldMgr.blacklist.contains(e.getBlock().getWorld().getName())) {
				return;
			}
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void grassSpread(BlockFormEvent e) {
		if (Main.config.getBoolean("deny-physics.systems.grassspread") && denyphysics) {
			if (WorldMgr.blacklist.contains(e.getBlock().getWorld().getName())) {
				return;
			}
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void grassSpread(BlockSpreadEvent e) {
		if (Main.config.getBoolean("deny-physics.systems.grassspread") && denyphysics) {
			if (WorldMgr.blacklist.contains(e.getBlock().getWorld().getName())) {
				return;
			}
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void fire(BlockIgniteEvent e) {
		if (Main.config.getBoolean("deny-physics.systems.fire") && denyphysics) {
			if (WorldMgr.blacklist.contains(e.getBlock().getWorld().getName())) {
				return;
			}
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void liquid(BlockFromToEvent e) {
		if (Main.config.getBoolean("deny-physics.systems.liquidspread") && denyphysics) {
			if (WorldMgr.blacklist.contains(e.getBlock().getWorld().getName())) {
				return;
			}
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void leaves(LeavesDecayEvent e) {
		if (Main.config.getBoolean("deny-physics.systems.decay") && denyphysics) {
			if (WorldMgr.blacklist.contains(e.getBlock().getWorld().getName())) {
				return;
			}
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onNoteBlocks(NotePlayEvent e) {
		if (Main.config.getBoolean("deny-physics.systems.noteblock") && denyphysics) {
			if (WorldMgr.blacklist.contains(e.getBlock().getWorld().getName())) {
				return;
			}
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onRedstone(BlockRedstoneEvent e) {
		if (Main.config.getBoolean("deny-physics.systems.redstone") && denyphysics) {
			if (WorldMgr.blacklist.contains(e.getBlock().getWorld().getName())) {
				return;
			}
			e.setNewCurrent(0);
		}
	}
	
//	public static ItemStack filterItem(ItemStack itm) {
//		if (itm == null) {
//			return null;
//		}
//		ItemStack unfiltered = itm.clone();
//		
//		Material type = unfiltered.getType();
//		int amount = Math.min(type.getMaxStackSize(), Math.max(unfiltered.getAmount(), 0));
//		
//		
//		ItemStack filtered = new ItemStack(type, amount, unfiltered.getDurability());
//		return filtered;
//	}
//
//	@EventHandler(priority = EventPriority.MONITOR)
//	public void onCreativeItem(InventoryCreativeEvent e) {
//		if (e.isCancelled()) {
//			return;
//		}
//		
//		e.setCurrentItem(filterItem(e.getCurrentItem()));
//		e.setCursor(filterItem(e.getCursor()));
//	}
}
