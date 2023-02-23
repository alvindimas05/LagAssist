package com.entryrise.lagassist.utils;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.World.ChunkLoadCallback;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import com.entryrise.lagassist.Main;

@SuppressWarnings("deprecation")
public class PaperOnly {

	public static void freezeArmorstand(ArmorStand arm) {
		arm.setCanMove(false);
	}

	public static void setViewDistance(Player p, int view) {
		ViewDistance.setPerViewDistance(p, view);
	}

	public static void setViewDistance(World w, int view) {
		if (VersionMgr.isNewMaterials()) {
			V1_13.setViewDistance(w, view);
		} else {
			ViewDistance.setViewDistance(w, view);
		}
	}

	public static void loadChunkAsync(World world, int x, int z) {
		world.getChunkAtAsync(x, z, new ChunkLoadCallback() {

			@Override
			public void onLoad(Chunk chk) {
				chk.unload();
			}
		});
	}

	public static class ViewDistance {
		public static void setPerViewDistance(Player p, int amount) {
			p.setViewDistance(amount);
			Main.sendDebug("Set viewdistance to " + p.getName() + " to " + amount, 1);
		}

		public static void setViewDistance(World w, int amount) {

			Main.sendDebug("Attempting to set view distance on players in world " + w.getName(), 1);

			for (Player p : w.getPlayers()) {
				if (p.getViewDistance() == amount) {
					continue;
				}

				p.setViewDistance(amount);
				setPerViewDistance(p, amount);
			}
		}
	}

}
