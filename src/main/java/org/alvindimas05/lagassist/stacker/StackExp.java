package org.alvindimas05.lagassist.stacker;


import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

// Sorry, the exp is hard coded here
// Based on https://minecraft.wiki/w/Experience
public class StackExp {
    static String[] commonFarmMobs = {
            "CREEPER", "DROWNED", "ENDERMEN", "HOGLIN", "HUSK", "PHANTOM", "PILLAGER",
            "SILVERFISH", "SKELETON", "STRAY", "VEX", "VINDICATOR", "WITCH",
            "WITHER_SKELETON", "ZOMBIE", "ZOMBIE_VILLAGER", "ZOGLIN", "ZOMBIFIED_PIGLIN",
    };
    static int commonFarmExp = 5;

    static String[] uncommonFarmMobs = {
            "BLAZE", "ELDER_GUARDIAN", "EVOKER", "GUARDIAN"
    };
    static int uncommonFarmExp = 10;

    static String[] rareFarmMobs = {
            "RAVAGER", "PIGLIN_BRUTE"
    };
    static int rareFarmExp = 20;

    static String[] commonFarmAnimals = {
            "CHICKEN", "COD", "COW", "MOOSHROOM", "PIG", "RABBIT", "SALMON", "SHEEP"
    };
    static int minCommonFarmAnimalExp = 1;
    static int maxCommonFarmAnimalExp = 3;

    public static int getExp(Entity ent) {
        String type = ent.getType().toString().toUpperCase();

        if (Arrays.asList(commonFarmMobs).contains(type)) {
            return commonFarmExp;
        } else if (Arrays.asList(uncommonFarmMobs).contains(type)) {
            return uncommonFarmExp;
        } else if (Arrays.asList(rareFarmMobs).contains(type)) {
            return rareFarmExp;
        } else if (Arrays.asList(commonFarmAnimals).contains(type)) {
            return ThreadLocalRandom.current().nextInt(minCommonFarmAnimalExp, maxCommonFarmAnimalExp);
        }

        return 0;
    }
}
