package org.alvindimas05.lagassist.stacker;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alvindimas05.lagassist.Main;
import org.alvindimas05.lagassist.utils.MathUtils;
import org.alvindimas05.lagassist.utils.WorldMgr;
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
import org.alvindimas05.lagassist.utils.Others;

public class StackChunk {

    private static final Map<Chunk, StackChunk> chunks = new HashMap<>();
    public static String nameformat = "";
    public static String regexpat = "";
    private static int splits = 8;

    private final Map<EntityType, HashSet<Entity>>[] ents;

    public static void Enabler() {
        splits = Main.config.getInt("smart-stacker.technical.splits");
        nameformat = ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(Main.config.getString("smart-stacker.gameplay.tag-format")));
        regexpat = nameformat.replace("{size}", "(.*)");
    }

    @SuppressWarnings("unchecked")
    public StackChunk(Chunk chk) {
        ents = (Map<EntityType, HashSet<Entity>>[]) new HashMap[256 / splits];
        for (int i = 0; i < 256 / splits; i++) {
            ents[i] = new HashMap<>();
        }
    }

    /**
     * Attempts to stack an entity at the given location.
     *
     * @param loc      the location where stacking is attempted
     * @param type     the type of entity to stack
     * @param optional an optional entity reference to consider in the stack
     * @return true if the entity was consumed in stacking, false otherwise
     */
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
        boolean consumed;
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
        consumed = optional != null && !free.equals(optional);
        if (optional != null && consumed) {
            size += getStack(optional);
        }
        Main.sendDebug("FINAL OPT: " + size, 2);
        setSize(free, size);
        return consumed;
    }

    private static Entity getMatch(Map<EntityType, HashSet<Entity>> ents, Location loc, EntityType type, Entity optional) {
        if (!ents.containsKey(type)) {
            ents.put(type, new HashSet<>());
        }
        Set<Entity> list = ents.get(type);
        Entity ideal;
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
            if (StackComparer.isSimilar(ent, optional)) {
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
            return ent.getMetadata("lagassist.stacksize").getFirst().asInt();
        }
        String name = ent.getCustomName();
        if (name == null) {
            return 1;
        }
        Pattern pat = Pattern.compile(regexpat.replace("{type}", Others.firstHighcase(ent.getType().toString())),
                Pattern.MULTILINE);
        Matcher match = pat.matcher(name);
        if (!match.find()) {
            return 1;
        }
        String count = match.group(1);
        if (!MathUtils.isInt(count)) {
            return 1;
        }
        return Integer.parseInt(count);
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
        int stack = getStack(ent);
        if (stack < 2) {
            return;
        }
        List<ItemStack> drops = new ArrayList<>();
        for (ItemStack itm : e.getDrops()) {
            int amount = itm.getAmount() * stack;
            while (amount > 0) {
                ItemStack cl = itm.clone();
                cl.setAmount(Math.min(amount, cl.getMaxStackSize()));
                amount -= cl.getAmount();
                drops.add(cl);
            }
        }
        int droppedExp = e.getDroppedExp();
        if (droppedExp < 1) {
            droppedExp = StackExp.getExp(e.getEntity());
        }
        e.getDrops().clear();
        e.getDrops().addAll(drops);
        e.setDroppedExp(droppedExp * stack);
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
        if (Main.config.getBoolean("smart-stacker.technical.shutdown-clean")) {
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
        for (Entity ent : chk.getEntities()) {
            if (!(ent instanceof LivingEntity)) {
                continue;
            }
            if (tryStacking(ent.getLocation(), ent.getType(), ent)) {
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

    /**
     * Checks if the total stack size in the given set of entities is under the configured minimum.
     *
     * @param ents the set of entities to check
     * @return true if the total stack size is below the minimum, false otherwise
     */
    protected static boolean isUnderMinimum(Set<Entity> ents) {
        int minstack = Main.config.getInt("smart-stacker.technical.min-stack");
        if (minstack <= 1) {
            return false;
        }
        int stacktotal = getStackTotal(ents);
        return stacktotal < minstack;
    }

    private static int getStackTotal(Set<Entity> ents) {
        int total = 0;
        for (Entity ent : ents) {
            total += getStack(ent);
        }
        return total;
    }

    /**
     * Cleans the specified split in the chunk by removing invalid or outdated entities.
     *
     * @param chk   the chunk to clean
     * @param split the split index based on Y-coordinate
     * @param ent   the entity type to clean from the split
     */
    public static void cleanSplit(Chunk chk, int split, EntityType ent) {
        if (!chunks.containsKey(chk)) {
            return;
        }
        StackChunk stchk = chunks.get(chk);
        if (!stchk.ents[split].containsKey(ent)) {
            return;
        }
        Set<Entity> entitySet = stchk.ents[split].get(ent);
        entitySet.removeIf(entity -> entity == null || entity.isDead() ||
                !entity.getLocation().getChunk().equals(chk) ||
                getSplit(entity.getLocation()) != split);
    }

    public static int getSplit(Location loc) {
        return Math.max(0, Math.min(loc.getBlockY(), 255)) / splits;
    }
}
