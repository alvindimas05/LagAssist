package org.alvindimas05.lagassist.microfeatures;

import java.util.WeakHashMap;

import org.alvindimas05.lagassist.Main;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.inventory.ItemStack;

public class GrowableStack implements Listener {
	private static WeakHashMap<Chunk, WeakHashMap<Material, Integer>> counts = new WeakHashMap<>();

	public <T> void onCropGrow(T e) {
		if (!Main.config.getBoolean("microfeatures.stack-growables.enable")) {
			return;
		}

		BlockState b = null;
		if(e instanceof BlockGrowEvent) {
			b = ((BlockGrowEvent) e).getNewState();
		}
		if(e instanceof BlockSpreadEvent) {
			b = ((BlockSpreadEvent) e).getNewState();
		}

        assert b != null;
        Chunk chk = b.getChunk();

		Material mat = b.getType();

		if (!Main.config.getStringList("microfeatures.stack-growables.blocks").contains(mat.toString())) {
			return;
		}

		int current = counts.getOrDefault(chk, new WeakHashMap<>()).getOrDefault(mat, 0) + 1;
		int stacksize = Main.config.getInt("microfeatures.stack-growables.stacksize");

		if(e instanceof BlockGrowEvent) {
			((BlockGrowEvent) e).setCancelled(true);
		}
		if(e instanceof BlockSpreadEvent) {
			((BlockSpreadEvent) e).setCancelled(true);
		}
		if (current % stacksize == 0) {
			b.getLocation().getWorld().dropItemNaturally(b.getLocation(), new ItemStack(mat, stacksize));
			// Main.sendDebug("Cactus stack in chunk " + chk.getX() + " " + chk.getZ() + " at " + current + " for " + mat.toString(), 2);
		}

		counts.compute(chk, (k, v) -> {
			if (v == null) {
				v = new WeakHashMap<>();
			}

			v.put(mat, current);

			return v;
		});
	}

	@EventHandler
	public void onBlockGrow(BlockGrowEvent e) {
		onCropGrow(e);
	}

	@EventHandler
	public void onBlockSpread(BlockSpreadEvent e) {
		onCropGrow(e);
	}
}
