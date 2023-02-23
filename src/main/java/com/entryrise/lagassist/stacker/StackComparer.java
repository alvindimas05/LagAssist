package com.entryrise.lagassist.stacker;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;

import com.entryrise.lagassist.Main;

public class StackComparer {

	private static List<EntityComparator> comparators = new ArrayList<EntityComparator>();

	public static void Enabler(boolean reload) {
		EntityComparator sheep = (ent1, ent2) -> {
			if (ent1 instanceof Sheep && ent2 instanceof Sheep) {
				Sheep s1 = (Sheep) ent1;
				Sheep s2 = (Sheep) ent2;

				if (s1.getColor() != s2.getColor()) {
					return false;
				}

				if (s1.isSheared() != s2.isSheared()) {
					return false;
				}
			}
			return true;
		};

		EntityComparator slime = (ent1, ent2) -> {
			if (ent1 instanceof Slime && ent2 instanceof Slime) {
				Slime s1 = (Slime) ent1;
				Slime s2 = (Slime) ent2;

				if (s1.getSize() != s2.getSize()) {
					return false;
				}
			}
			return true;
		};

		EntityComparator pig = (ent1, ent2) -> {
			if (ent1 instanceof Pig && ent2 instanceof Pig) {
				Pig s1 = (Pig) ent1;
				Pig s2 = (Pig) ent2;

				// So no mountable pigs stack.
				if (s1.hasSaddle() || s2.hasSaddle()) {
					return false;
				}
			}
			return true;
		};

		EntityComparator villager = (ent1, ent2) -> {
			if (ent1 instanceof Villager && ent2 instanceof Villager) {
				Villager s1 = (Villager) ent1;
				Villager s2 = (Villager) ent2;

				if (s1.getProfession() != s2.getProfession()) {
					return false;
				}
			}
			return true;
		};

		EntityComparator horse = (ent1, ent2) -> {
			if (ent1 instanceof Horse && ent2 instanceof Horse) {
				Horse s1 = (Horse) ent1;
				Horse s2 = (Horse) ent2;

				if (s1.getColor() != s2.getColor()) {
					return false;
				}

				if (s2.getStyle() != s2.getStyle()) {
					return false;
				}
			}
			return true;
		};

		try {
			EntityComparator abstracthorse = (ent1, ent2) -> {
				if (ent1 instanceof AbstractHorse && ent2 instanceof AbstractHorse) {
					AbstractHorse s1 = (AbstractHorse) ent1;
					AbstractHorse s2 = (AbstractHorse) ent2;

					if (s1.getDomestication() != s2.getDomestication()) {
						return false;
					}

					if (s2.getJumpStrength() != s2.getJumpStrength()) {
						return false;
					}
				}
				return true;
			};
			comparators.add(abstracthorse);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			EntityComparator ageable = (ent1, ent2) -> {
				if (ent1 instanceof Ageable && ent2 instanceof Ageable) {
					Ageable s1 = (Ageable) ent1;
					Ageable s2 = (Ageable) ent2;

					if (s1.isAdult() != s2.isAdult()) {
						return false;
					}
				}
				return true;
			};
			if (Main.config.getBoolean("smart-stacker.technical.comparison.ageable")) {
				comparators.add(ageable);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		EntityComparator tameable = (ent1, ent2) -> {
			if (ent1 instanceof Tameable && ent2 instanceof Tameable) {
				Tameable s1 = (Tameable) ent1;
				Tameable s2 = (Tameable) ent2;

				if (s1.isTamed() != s2.isTamed()) {
					return false;
				}
			}
			return true;
		};

		comparators.clear();

		if (Main.config.getBoolean("smart-stacker.technical.comparison.sheep")) {
			comparators.add(sheep);
		}
		if (Main.config.getBoolean("smart-stacker.technical.comparison.pig")) {
			comparators.add(pig);
		}
		if (Main.config.getBoolean("smart-stacker.technical.comparison.slime")) {
			comparators.add(slime);
		}
		if (Main.config.getBoolean("smart-stacker.technical.comparison.villager")) {
			comparators.add(villager);
		}
		if (Main.config.getBoolean("smart-stacker.technical.comparison.tameable")) {
			comparators.add(tameable);
		}
		if (Main.config.getBoolean("smart-stacker.technical.comparison.horse")) {
			comparators.add(horse);
		}
	}

	public static boolean isSimilar(Entity ent1, Entity ent2) {
		if (ent1 == null || ent2 == null) {
			return false;
		}

		if (ent1.isDead() || ent2.isDead()) {
			return false;
		}

		if (ent1.getType() != ent2.getType()) {
			return false;
		}

		for (EntityComparator comp : comparators) {
			if (!comp.isSimilar(ent1, ent2)) {
				return false;
			}
		}

		return true;
	}

	@FunctionalInterface
	public interface EntityComparator {

		public boolean isSimilar(Entity ent1, Entity ent2);

	}

}
