package org.alvindimas05.lagassist.packets;

import org.alvindimas05.lagassist.Main;
import org.bukkit.Bukkit;

public enum ServerPackage {

	MINECRAFTSERVER("net.minecraft.server." + getServerVersion()),
	CRAFTBUKKIT("org.bukkit.craftbukkit." + getServerVersion()), MINECRAFT("net.minecraft." + getServerVersion());

	private final String path;

	ServerPackage(String path) {
		this.path = path;
	}

    public static String getServerVersion() {
        String craftPkg = Bukkit.getServer().getClass().getPackage().getName();

        // If the package already contains v1_* just return it
        if (craftPkg.contains("v1_")) {
            return craftPkg.substring(craftPkg.lastIndexOf('.') + 1);
        }

        // Example: "1.21.4" → "1_21_"
        String mcVersion = Bukkit.getMinecraftVersion();
        String[] split = mcVersion.split("\\.");
        if (split.length >= 2) {
            mcVersion = split[0] + "_" + split[1] + "_";
        } else {
            mcVersion = mcVersion.replace(".", "_") + "_";
        }

        // Try R1..R15
        for (int i = 1; i <= 15; i++) {
            String version = "v" + mcVersion + "R" + i;
            String nmsPath = "org.bukkit.craftbukkit." + version + ".CraftServer";

            try {
                Class.forName(nmsPath);
                return version;
            } catch (ClassNotFoundException ignored) {}
        }

        Bukkit.getLogger().warning(Main.PREFIX + "Can't get server version from " + Bukkit.getMinecraftVersion());

        return "v" + mcVersion + "R1";
    }


	@Override
	public String toString() {
		return path;
	}

	public Class<?> getClass(String className) throws ClassNotFoundException {
		return Class.forName(this.toString() + "." + className);
	}

}
