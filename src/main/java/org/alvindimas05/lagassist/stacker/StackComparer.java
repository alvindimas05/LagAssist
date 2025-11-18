package org.alvindimas05.lagassist.stacker;

import java.util.ArrayList;
import java.util.List;

import org.alvindimas05.lagassist.Main;
import org.alvindimas05.lagassist.Reflection;
import org.alvindimas05.lagassist.utils.VersionMgr;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;

public class StackComparer {

    private static final List<EntityComparator> comparators = new ArrayList<EntityComparator>();

    public static void Enabler(boolean reload) {
        EntityComparator sheep = (ent1, ent2) -> {
            if (ent1 instanceof Sheep s1 && ent2 instanceof Sheep s2) {

                if (s1.getColor() != s2.getColor()) {
                    return false;
                }


                try {
                    if (VersionMgr.isV_21Plus()) {
                        return !((boolean) Reflection.runMethod(s1, "readyToBeSheared")) == !((boolean) Reflection.runMethod(s2, "readyToBeSheared"));
                    }
                    return (boolean) Reflection.runMethod(s1, "isSheared") == (boolean) Reflection.runMethod(s2, "isSheared");
                } catch (Exception ignored) {
                }
            }
            return true;
        };

        EntityComparator slime = (ent1, ent2) -> {
            if (ent1 instanceof Slime s1 && ent2 instanceof Slime s2) {

                return s1.getSize() == s2.getSize();
            }
            return true;
        };

        EntityComparator pig = (ent1, ent2) -> {
            if (ent1 instanceof Pig s1 && ent2 instanceof Pig s2) {

                // So no mountable pigs stack.
                return !s1.hasSaddle() && !s2.hasSaddle();
            }
            return true;
        };

        EntityComparator villager = (ent1, ent2) -> {
            if (ent1 instanceof Villager s1 && ent2 instanceof Villager s2) {

                return s1.getProfession() == s2.getProfession();
            }
            return true;
        };

        EntityComparator horse = (ent1, ent2) -> {
            if (ent1 instanceof Horse s1 && ent2 instanceof Horse s2) {

                if (s1.getColor() != s2.getColor()) {
                    return false;
                }

                return s2.getStyle() == s2.getStyle();
            }
            return true;
        };

        try {
            EntityComparator abstracthorse = (ent1, ent2) -> {
                if (ent1 instanceof AbstractHorse s1 && ent2 instanceof AbstractHorse s2) {

                    if (s1.getDomestication() != s2.getDomestication()) {
                        return false;
                    }

                    return s2.getJumpStrength() == s2.getJumpStrength();
                }
                return true;
            };
            comparators.add(abstracthorse);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            EntityComparator ageable = (ent1, ent2) -> {
                if (ent1 instanceof Ageable s1 && ent2 instanceof Ageable s2) {

                    return s1.isAdult() == s2.isAdult();
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
            if (ent1 instanceof Tameable s1 && ent2 instanceof Tameable s2) {

                return s1.isTamed() == s2.isTamed();
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
