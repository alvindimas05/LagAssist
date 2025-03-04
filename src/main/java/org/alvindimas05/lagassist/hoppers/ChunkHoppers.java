package org.alvindimas05.lagassist.hoppers;

import java.util.*;

import org.alvindimas05.lagassist.Data;
import org.alvindimas05.lagassist.Main;
import org.alvindimas05.lagassist.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ChunkHoppers implements Listener {

    // The Cachetime represents how often the cache should be removed
    // and the tiles rechecked.

    private static Cache<Chunk, BlockState[]> tilecache = new Cache<Chunk, BlockState[]>(40);

    public static boolean mobhoppers;
    private static double multiplier;
    private static List<String> reasons;

    private static String hoppermode;
    public static String customname;

    private static Item dropplayer;

    private class HopperComparator implements Comparator<Hopper> {

        Location initial;

        public HopperComparator(Location initial) {
            this.initial = initial;
        }

        @Override
        public int compare(Hopper h1, Hopper h2) {
            return (int) h1.getLocation().distance(initial) - (int) h2.getLocation().distance(initial);
        }

    }

    public static void Enabler(boolean reload) {
        hoppermode = Main.config.getString("hopper-check.chunk-hoppers.mode");
        mobhoppers = Main.config.getDouble("hopper-check.chunk-hoppers.mob-hopper.maxtps") >= 20;
        multiplier = Main.config.getDouble("hopper-check.chunk-hoppers.mob-hopper.multiplier");
        reasons = Main.config.getStringList("hopper-check.chunk-hoppers.mob-hopper.spawn-reasons");

        customname = ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(Main.config.getString("hopper-check.chunk-hoppers.define")));

        tilecache.clear();

        if (!reload) {
            Main.p.getServer().getPluginManager().registerEvents(new ChunkHoppers(), Main.p);
            runTask();
        }

        HopperFilter.Enabler(reload);
        SellHoppers.Enabler(reload);
    }

    private static void runTask() {
        if (ServerType.isFolia()) {
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(Main.p, task -> tilecache.tick(), 1L, 1L);
        } else {
            Bukkit.getScheduler().runTaskTimer(Main.p, () -> tilecache.tick(), 1L, 1L);
        }
    }


    public static void giveChunkHopper(Player p, int amount) {
        Inventory inv = p.getInventory();
        ItemStack itm = getCustomHopper(amount);

        int empty = inv.firstEmpty();

        if (empty == -1) {
            p.getLocation().getWorld().dropItem(p.getLocation(), itm);
            return;
        }

        p.getInventory().addItem(itm);
    }

    private static Material getMaterial(EntityType et) {
        String loc = "hopper-check.chunk-hoppers.mob-hopper.filter-items." + et.toString().toLowerCase();
        if (!Main.config.contains(loc)) {
            return null;
        }

        return Material.valueOf(Main.config.getString(loc));
    }

    private List<Hopper> getHoppers(Chunk chk, Material mat) {
        List<Hopper> hoppers = new ArrayList<Hopper>();

        if (!tilecache.isCached(chk)) {
            tilecache.putCached(chk, chk.getTileEntities());
        }

        for (BlockState b : tilecache.getCached(chk)) {
            if (!(b instanceof Hopper)) {
                continue;
            }

            Hopper h = (Hopper) b;

            String stg = V1_12.getHopperName(h);

            // Check if not default; and if it isn't default; check if the string is null or
            // it isn't custom and continue.
            if (!customname.equalsIgnoreCase("DEFAULT") && (stg == null || !stg.equals(customname))) {
                continue;
            }

            if (!HopperFilter.isAllowed(h.getLocation(), mat)) {
                continue;
            }

            hoppers.add((Hopper) b);
        }

        return hoppers;
    }

    private ItemStack spreadItemInHoppers(List<Hopper> hoppers, ItemStack itm, Location loc) {
        ItemStack remainder = itm.clone();

        if (isCustomHopper(remainder)) {
            return remainder;
        }


        if (hoppermode.equalsIgnoreCase("RANDOM")) {
            Collections.shuffle(hoppers);
        } else if (hoppermode.equalsIgnoreCase("CLOSEST")) {
            Collections.sort(hoppers, new HopperComparator(loc));
        }

        for (Hopper hopper : hoppers) {
            Inventory inv = hopper.getInventory();

            if (SellHoppers.attemptSell(hopper, itm)) {
                return null;
            }

            Map<Integer, ItemStack> remainers = inv.addItem(remainder);


            if (remainers.size() < 1) {
                return null;
            }

            remainder = remainers.get(0);

            if (remainder == null) {
                return null;
            }
        }

        return remainder;
    }

    private static ItemStack chopper;

    public static boolean isCustomHopper(ItemStack itm) {
        if (chopper == null) {
            chopper = getCustomHopper(1);
        }

        itm = itm.clone();
        itm.setAmount(1);

        return itm.isSimilar(chopper);
    }

    public static ItemStack getCustomHopper(int amount) {
        ItemStack itm = new ItemStack(Material.valueOf("HOPPER"), amount);
        ItemMeta imeta = itm.getItemMeta();

        if (!customname.equalsIgnoreCase("DEFAULT")) {
            imeta.setDisplayName(customname);
        }

        // Make it work with the unbreakable check.
        VersionMgr.setUnbreakable(imeta, true);
        imeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itm.setItemMeta(imeta);

        return itm;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent e) {
        if (e.isCancelled()) {
            return;
        }

        if (customname.equalsIgnoreCase("DEFAULT")) {
            return;
        }

        ItemStack itm = e.getItemInHand();

        if (itm == null) {
            return;
        }

        if (!itm.hasItemMeta()) {
            return;
        }

        ItemMeta imeta = itm.getItemMeta();

        if (!imeta.hasDisplayName()) {
            return;
        }

        String name = imeta.getDisplayName();

        if (!name.equalsIgnoreCase(customname)) {
            return;
        }

        if (!VersionMgr.isUnbreakable(imeta)) {
            e.setCancelled(true);
            return;
        }

//		System.out.println("SET OWNER");
        // Set owner
        Data.setOwningPlayer(e.getBlockPlaced().getLocation(), e.getPlayer());
        tilecache.remove(e.getBlockPlaced().getChunk());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent e) {
        if (e.isCancelled()) {
            return;
        }

        if (customname.equalsIgnoreCase("DEFAULT")) {
            return;
        }

        Block b = e.getBlock();
        BlockState bstate = b.getState();

        if (!(bstate instanceof Hopper)) {
            return;
        }

        Hopper h = (Hopper) bstate;

        String stg = V1_12.getHopperName(h);

        if (stg == null) {
            return;
        }

        if (!stg.equals(customname)) {
            return;
        }

        Data.deleteHopper(h);
        b.setType(Material.AIR);
//		dropplayer = b.getWorld().dropItemNaturally(b.getLocation(), getCustomHopper(1));
        dropplayer = Others.giveOrDrop(e.getPlayer(), getCustomHopper(1));
//		e.getPlayer().getInventory().addItem(getCustomHopper(1));
        tilecache.remove(b.getChunk());

    }

    @EventHandler
    public void playerDrop(PlayerDropItemEvent e) {

        if (e.isCancelled()) {
            return;
        }

        dropplayer = e.getItemDrop();
    }

    @EventHandler
    public void onItemDrop(ItemSpawnEvent e) {

        if (e.isCancelled()) {
            return;
        }

        Item itm = e.getEntity();

        ItemStack its = itm.getItemStack();
        Material mat = its.getType();

        if (itm.equals(dropplayer)) {
            return;
        }

        Chunk chk = itm.getLocation().getChunk();

        List<Hopper> hoppers = getHoppers(chk, mat);

        if (hoppers.size() < 1) {
            return;
        }

        ItemStack remainder = spreadItemInHoppers(hoppers, itm.getItemStack(), itm.getLocation());
        if (remainder == null) {
            e.setCancelled(true);
            return;
        }

        itm.setItemStack(remainder);

    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (e.isCancelled()) {
            return;
        }

        if (!reasons.contains(e.getSpawnReason().toString())) {
            return;
        }

        if (!mobhoppers) {
            return;
        }

        LivingEntity ent = e.getEntity();

        Material mat = getMaterial(ent.getType());

        if (mat == null) {
            return;
        }

        Chunk chk = ent.getLocation().getChunk();

        List<Hopper> hoppers = getHoppers(chk, mat);

        for (ItemStack itm : V1_13.getLootTable(ent)) {
            ItemStack buffed = itm.clone();
            buffed.setAmount((int) (itm.getAmount() * multiplier));
            spreadItemInHoppers(hoppers, buffed, ent.getLocation());
        }

        e.setCancelled(true);

        // TODO: FINISH CHECK
    }
}
