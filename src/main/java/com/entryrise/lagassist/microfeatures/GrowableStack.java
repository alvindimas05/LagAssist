package com.entryrise.lagassist.microfeatures;

import java.util.WeakHashMap;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.inventory.ItemStack;

import com.entryrise.lagassist.Main;

public class GrowableStack implements Listener {
	private static WeakHashMap<Chunk, WeakHashMap<Material, Integer>> counts = new WeakHashMap<>();

	@EventHandler
	public void onCropGrowdth(BlockGrowEvent e) {
		if (!Main.config.getBoolean("microfeatures.stack-growables.enable")) {
			return;
		}

		BlockState b = e.getNewState();
		Chunk chk = b.getChunk();

		Material mat = b.getType();
		
		if (!Main.config.getStringList("microfeatures.stack-growables.blocks").contains(mat.toString())) {
			return;
		}
		
		int current = counts.getOrDefault(chk, new WeakHashMap<>()).getOrDefault(mat, 0) + 1;
		int stacksize = Main.config.getInt("microfeatures.stack-growables.stacksize");
		
		e.setCancelled(true);

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

}
