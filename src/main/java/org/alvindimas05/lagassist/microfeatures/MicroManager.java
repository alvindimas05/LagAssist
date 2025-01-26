package org.alvindimas05.lagassist.microfeatures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.alvindimas05.lagassist.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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
				b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getBlockData());
			}

			breakables.clear();
			
			for(Player p : intervals.keySet()) {
				intervals.compute(p, (key, value) -> value - 5 < 0 ? null : value-5);
			}

		}, 5, 5);
	}

	private static List<BlockState> breakables = new ArrayList<>();
	private static final List<BlockFace> growablefaces = Arrays.asList(BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH,
			BlockFace.SOUTH, BlockFace.DOWN, BlockFace.UP);

	public <T> void onGrowableGrow(T e) {
		if (!Main.config.getBoolean("microfeatures.optimize-growable-farms.enable")) {
			return;
		}

		BlockState b = null;
		if(e instanceof BlockGrowEvent){
			if (((BlockGrowEvent) e).isCancelled()) {
				return;
			}

			b = ((BlockGrowEvent) e).getNewState();
		}
		if(e instanceof BlockSpreadEvent){
			if (((BlockSpreadEvent) e).isCancelled()) {
				return;
			}

			b = ((BlockSpreadEvent) e).getNewState();
		}
		assert b != null;

		Material mat = b.getType();

		if (!Main.config.getStringList("microfeatures.optimize-growable-farms.blocks").contains(mat.toString())) {
			return;
		}

		for (BlockFace face : growablefaces) {
			Block piston = b.getBlock().getRelative(face);
			if(piston.getType() != Material.PISTON) continue;

			//Block frontBlock = piston.getRelative(piston.getFace(b.getBlock()).getOppositeFace());
			BlockData blockData = piston.getBlockData();
			if (blockData instanceof Directional) {
				Directional directional = (Directional) blockData;
				BlockFace facing = directional.getFacing();
				if (facing != face.getOppositeFace()) {
					continue;
				}
			}

			breakables.add(b);

			return;
		}

	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockGrow(BlockGrowEvent e) {
		onGrowableGrow(e);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockSpread(BlockSpreadEvent e) {
		onGrowableGrow(e);
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
