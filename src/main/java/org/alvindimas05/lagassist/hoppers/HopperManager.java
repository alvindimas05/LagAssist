package org.alvindimas05.lagassist.hoppers;

import java.util.Objects;
import java.util.SplittableRandom;

import org.alvindimas05.lagassist.Main;
import org.alvindimas05.lagassist.utils.CustomLogger;
import org.alvindimas05.lagassist.utils.WorldMgr;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Hopper;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.InventoryHolder;

import org.alvindimas05.lagassist.utils.V1_12;

public class HopperManager implements Listener {

    private static SplittableRandom r = new SplittableRandom();

    public static boolean chunkhoppers;
    public static boolean denyhoppers = false;

    public static void Enabler(boolean reload) {
        denyhoppers = Main.config.getBoolean("hopper-check.enabled");
        chunkhoppers = Main.config.getBoolean("hopper-check.chunk-hoppers.enabled");

        if (!reload) {
            Main.p.getServer().getPluginManager().registerEvents(new HopperManager(), Main.p);
        }

        if (chunkhoppers) {
            ChunkHoppers.Enabler(reload);
        }

        CustomLogger.info("    §e[§a✔§e] §fHopper Manager.");
    }

    @EventHandler
    public void disableCraft(CraftItemEvent e) {
        if (e.getRecipe().getResult().getType().equals(Material.HOPPER) && denyhoppers) {
            e.setCancelled(true);
            for (HumanEntity human : e.getViewers())
                if (human instanceof Player) {
                    Player p = Bukkit.getPlayer(human.getName());
                    p.sendMessage(
                            ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Main.config.getString("hopper-check.reason"))));
                }

        }
    }

    @EventHandler
    public void hopperBoom(InventoryMoveItemEvent e) {
        InventoryHolder holder = e.getInitiator().getHolder();

        if (!(holder instanceof Hopper h)) {
            return;
        }

        if (WorldMgr.blacklist.contains(h.getWorld().getName())) {
            return;
        }

        if (Main.config.getInt("hopper-check.chance") <= 0) {
            return;
        }

        String stg = V1_12.getHopperName(h);

        if (!(stg.equalsIgnoreCase("container.hopper"))) {
            return;
        }

        int rand = r.nextInt(10000 - 1) + 1;
        int chance = Main.config.getInt("hopper-check.chance");
        Hopper hopp = (Hopper) e.getInitiator().getHolder();
        if (rand <= chance) {
            hopp.getBlock().setType(Material.STONE);
        }

    }

}
