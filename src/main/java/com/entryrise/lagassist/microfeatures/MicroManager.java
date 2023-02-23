package com.entryrise.lagassist.microfeatures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.material.PistonBaseMaterial;

import com.entryrise.lagassist.Main;

@SuppressWarnings("deprecation")
public class MicroManager implements Listener {

	public static void Enabler(boolean reload) {
		Bukkit.getLogger().info("    §e[§a✔§e] §fMicroFeatures.");

		if (!reload) {
			runTask();
		}

		Main.p.getServer().getPluginManager().registerEvents(new MicroManager(), Main.p);
		Main.p.getServer().getPluginManager().registerEvents(new GrowableStack(), Main.p);
	}

	private static void runTask() {
		Bukkit.getScheduler().runTaskTimer(Main.p, () -> {
			for (BlockState b : breakables) {
				b.getBlock().breakNaturally();
			}

			breakables.clear();
			
			for(Player p : intervals.keySet()) {
				intervals.compute(p, (key, value) -> value - 5 < 0 ? null : value-5);
			}

		}, 5, 5);
	}

	private static List<BlockState> breakables = new ArrayList<>();
	private static final List<BlockFace> growablefaces = Arrays.asList(BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH,
			BlockFace.SOUTH);

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onGrowableGrow(BlockGrowEvent e) {
		if (!Main.config.getBoolean("microfeatures.optimize-growable-farms.enable")) {
			return;
		}

		if (e.isCancelled()) {
			return;
		}

		BlockState b = e.getNewState();

		Material mat = b.getType();

		if (!Main.config.getStringList("microfeatures.optimize-growable-farms.blocks").contains(mat.toString())) {
			return;
		}

		for (BlockFace face : growablefaces) {
			Block piston = b.getBlock().getRelative(face);
			if (piston == null) {
				continue;
			}

			MaterialData mdata = piston.getState().getData();

			if (!(mdata instanceof PistonBaseMaterial)) {
				continue;
			}

			PistonBaseMaterial pdata = (PistonBaseMaterial) mdata;

			if (pdata.getFacing().getOppositeFace() != face) {
				continue;
			}

			breakables.add(b);

			return;
		}

	}

	private static Map<Player, Integer> intervals = new WeakHashMap<>();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRightClick(PlayerInteractEvent e) {
		if (!Main.config.getBoolean("microfeatures.click-spam-fix.enable")) {
			return;
		}

		if (e.isCancelled()) {
			return;
		}

		Block b = e.getClickedBlock();

		if (b == null) {
			return;
		}

		Material mat = b.getType();

		if (!Main.config.getStringList("microfeatures.click-spam-fix.blocks").contains(mat.toString())) {
			return;
		}

		Player p = e.getPlayer();
		
		int increment =  Main.config.getInt("microfeatures.click-spam-fix.counter.increment");
		
		int result = intervals.getOrDefault(p, 0);
		
		if (result > Main.config.getInt("microfeatures.click-spam-fix.counter.max")) {
			e.setCancelled(true);
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.config.getString("microfeatures.click-spam-fix.counter.message")));
			return;
		}
		
		intervals.compute(p, (key, counter) -> (counter == null ? increment : counter+increment));
//
//		System.out.println(increment + " " + result);
	}

}
