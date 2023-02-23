package com.entryrise.lagassist.stacker;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.entryrise.lagassist.Main;
import com.entryrise.lagassist.utils.VersionMgr;

public class StackManipulator implements Listener {

	private static boolean deadentity = false;
	
	protected static boolean isDeadEntity() {
		return deadentity;
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onDamage(EntityDamageEvent e) {
		if (e.isCancelled()) {
			return;
		}
		
		Entity eraw = e.getEntity();
		
		if (!(eraw instanceof LivingEntity)) {
			return;
		}
		
		LivingEntity ent = (LivingEntity) eraw;
		
		int dmg = (int) getModifierDamage(e);
		int maxhealth = (int) VersionMgr.getMaxHealth(ent);
		
		int stack = StackChunk.getStack(ent);
		
		// Not a stacked mob. We don't do anything on it.
		if (stack < 2) {
			return;
		}
		
		// Add damage dealt from before (in case it's not enough to kill).
		dmg+=maxhealth-ent.getHealth();
		
		int nextdamage = dmg % maxhealth;
		int killed = dmg / maxhealth;
		
		// Handled differently, so cancel damage event.
		// Plugins shouldn't oof since it's on HIGHEST.
		
		e.setDamage(0);
		
		// Set health for this or next subject to account for dmg dealt.
		ent.setHealth(maxhealth-nextdamage);
		
		if (killed >= stack) {
			killed = stack;
			ent.setHealth(0);
			return;
		}
		
		Entity damager = (e instanceof EntityDamageByEntityEvent) ? ((EntityDamageByEntityEvent) e).getDamager() : null;
		
		if (killed > 0) {
			spawnDeadEntity(ent.getLocation(), ent.getClass(), killed, damager);
		}
		
		StackChunk.setSize(ent, stack-killed);
	}
	
	private double getModifierDamage(EntityDamageEvent e) {
		Entity eraw = e.getEntity();
		int stack = StackChunk.getStack(eraw);
		
		double initial = e.getFinalDamage();
		
		DamageCause dc = e.getCause();
		
		if (Main.config.getStringList("smart-stacker.technical.damage.multiply").contains(dc.toString().toUpperCase())) {
			initial*=stack;
		}
		
		return initial;
	}
	
	private void spawnDeadEntity(Location loc, Class<? extends Entity> ent, int amount, Entity cause) {
		deadentity = true;	
		LivingEntity spawned = (LivingEntity) loc.getWorld().spawn(loc, ent);
		StackChunk.setSize(spawned, amount);
		deadentity = false;
		
		
//		if (spawned instanceof Damageable) {
//			damage = spawned.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()+5;
//		}
		
		if (cause == null) {
			spawned.damage(5000);
			return;
		}
		
		spawned.damage(5000, cause);
	}









//	@EventHandler(priority=EventPriority.HIGHEST)
//	public void onItemSpawn(ItemSpawnEvent e) {
//		if (e.isCancelled()) {
//			return;
//		}
//		
//		Item ent = e.getEntity();
//		ItemStack item = ent.getItemStack();
//		
//		runItemStacker(ent, item);
//	}
//	
//	@EventHandler(priority=EventPriority.HIGHEST)
//	
//	public void onPlayerPickUp(PlayerPickupItemEvent e) {
//		if (e.isCancelled()) {
//			return;
//		}
//		
//		if(runPickupAttempt(e.getItem(), e.getPlayer().getInventory())) {
//			e.setCancelled(true);
//		}
//	}
//	
//	@EventHandler(priority=EventPriority.HIGHEST)
//	public void onInventoryPickup(InventoryPickupItemEvent e) {
//		if (e.isCancelled()) {
//			return;
//		}
//		
//		if(runPickupAttempt(e.getItem(), e.getInventory())) {
//			e.setCancelled(true);
//		}
//	}
//	private boolean runPickupAttempt(Item ent, Inventory inv) {
//		ItemStack item = ent.getItemStack();
//		
//		int stack = StackChunk.getStack(ent);
//		
//		
//		if (stack < 2) {
//			return false;
//		}
//		
//		int remaining = stack - item.getMaxStackSize();
//		
//		ItemStack adder = item.clone();
//		adder.setAmount(remaining);
//		
//		Map<Integer, ItemStack> result = inv.addItem(adder);
//		
//		if (result.isEmpty()) {
//			
//			return false;
//		}
//		
//		ItemStack fin = result.get(0);
//		ent.setItemStack(fin);
//		runItemStacker(ent, item);
//		return true;
//		
//		
//	}
	
//	private static void runItemStacker(Item ent, ItemStack item) {
//		if (item.getAmount() <= item.getMaxStackSize()) {
//			return;
//		}
//		
//		StackChunk.setSize(ent, item.getAmount());
//		item.setAmount(item.getMaxStackSize());
//		
//		ent.setItemStack(item);
//	}
	
}
