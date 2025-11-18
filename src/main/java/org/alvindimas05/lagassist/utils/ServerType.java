package org.alvindimas05.lagassist.utils;


public class ServerType {

    private static final boolean isFolia;

    static {
        boolean foliaDetected = false;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            foliaDetected = true;
        } catch (ClassNotFoundException ignored) {
        }
        isFolia = foliaDetected;
    }

    public static boolean isFolia() {
        return isFolia;
    }
}
