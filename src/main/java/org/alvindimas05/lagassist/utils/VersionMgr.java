package org.alvindimas05.lagassist.utils;

import org.alvindimas05.lagassist.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public class VersionMgr {

	public static ItemStack getMap() {
		return (isNewMaterials() ? V1_13.getLagMap() : V1_12.getLagMap());
	}

	public static int getMapId(ItemStack s) {
		return (isNewMaterials()) ? (int) V1_13.getMapId(s) : V1_12.getMapId(s);
	}

	public static ItemStack[] getStatics() {
		if (VersionMgr.isNewMaterials()) {
			return V1_13.getStatics();
		} else {
			return V1_12.getStatics();
		}
	}

	public static boolean isV1_8() {
		return Bukkit.getVersion().contains("1.8");
	}

	public static boolean isV1_9() {
		return Bukkit.getVersion().contains("1.9");
	}

	public static boolean isV1_10() {
		return Bukkit.getVersion().contains("1.10");
	}

	public static boolean isV1_11() {
		return Bukkit.getVersion().contains("1.11");
	}

	public static boolean isV1_12() {
		return Bukkit.getVersion().contains("1.12");
	}

	public static boolean isV1_13() {
		return Bukkit.getVersion().contains("1.13");
	}

	public static boolean isV1_14() {
		return Bukkit.getVersion().contains("1.14");
	}

	public static boolean isV1_17() {
		return Bukkit.getVersion().contains("1.17");
	}

	public static boolean isV1_18() {
		return Bukkit.getVersion().contains("1.18");
	}

	public static boolean isV1_19() {
		return Bukkit.getVersion().contains("1.19");
	}

	public static boolean isV1_20() {
		return Bukkit.getVersion().contains("1.20");
	}

	public static boolean isV1_21() {
		return Bukkit.getVersion().contains("1.21");
	}

	public static boolean isV_17Plus() {
		return isV1_17() || isV1_18() || isV1_19() || isV1_20() || isV1_21();
	}

    public static boolean isV_20Plus() {
        return isV1_20() || isV1_21();
    }

	public static boolean isNewMaterials() {
		if (isV1_8()) {
			return false;
		}

		if (isV1_9()) {
			return false;
		}

		if (isV1_10()) {
			return false;
		}

		if (isV1_11()) {
			return false;
		}

		if (isV1_12()) {
			return false;
		}

		return true;

	}

	public static boolean isPaper() {
		try {
			Class.forName("com.destroystokyo.paper.PaperConfig");
		} catch (ClassNotFoundException e) {
			return false;
		}
		return true;
	}

	public static String ChunkExistsName() {
		return isNewMaterials() ? "f" : "e";
	}

//	private static HashSet<EntityType> exceptions = Sets.newHashSet(EntityType.ARMOR_STAND, EntityType.VILLAGER, EntityType.ENDER_DRAGON, EntityType.ITEM_FRAME, EntityType.PAINTING);
//	private static SplittableRandom sr = new SplittableRandom();

	public static void loadChunk(World world, int x, int z) {

		Chunk chk = world.getChunkAt(x, z);

//		for (Entity e : chk.getEntities()) {
//			if (e.getCustomName() != null) {
//				continue;
//			}
//			if (exceptions.contains(e.getType())) {
//				continue;
//			}
//			if (sr.nextInt(100) > 75) {
//				continue;
//			}
//			e.remove();
//		}
//
		chk.unload();
	}

	public static Object setUnbreakable(ItemMeta imeta, boolean unbreakable) {
		return V1_13.setUnbreakable(imeta, unbreakable);
	}

	public static boolean isUnbreakable(ItemMeta imeta) {
		return V1_13.isUnbreakable(imeta);
	}

	public static boolean isChunkGenerated(World world, Object provider, int x, int z) {
		return isNewMaterials() ? V1_13.isChunkGenerated(world, x, z) : Reflection.isChunkExistent(provider, x, z);
	}


	public static boolean hasPassengers(Entity ent) {
		if (isV1_8()) {
			return ent.getPassenger() != null;
		} else {
			return !ent.getPassengers().isEmpty();
		}
	}

	public static double getMaxHealth(LivingEntity ent) {
		if (isV1_8()) {
			return ent.getMaxHealth();
		} else {
			try {
				return ent.getAttribute((Attribute) Objects.requireNonNull(Reflection.getMethod(Reflection.Classes.Attribute.getType(),
					"getAttribute", String.class)).invoke("max_health")).getBaseValue();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

}
