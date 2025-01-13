package org.alvindimas05.lagassist.packets;

import org.bukkit.Bukkit;

public enum ServerPackage {

	MINECRAFTSERVER("net.minecraft.server." + getServerVersion()),
	CRAFTBUKKIT("org.bukkit.craftbukkit." + getServerVersion()), MINECRAFT("net.minecraft." + getServerVersion());

	private final String path;

	ServerPackage(String path) {
		this.path = path;
	}

	public static String getServerVersion() {
		String name = Bukkit.getServer().getClass().getPackage().getName();

		// CraftBukkit doesn't use server version on the package name after version 1.20.4
		// So we need to add it manually instead
		if(!name.contains("v1_")){
			String version = Bukkit.getBukkitVersion().split("-")[0];
            return switch (version) {
                case "1.20.5", "1.20.6" -> "v1_20_R4";
				case "1.21", "1.21.1" -> "v1_21_R1";
				case "1.21.3" -> "v1_21_R2";
                // Set default to latest
                default -> "v1_21_R3";
            };
		}

		return name.substring(name.lastIndexOf('.') + 1);
	}

	@Override
	public String toString() {
		return path;
	}

	public Class<?> getClass(String className) throws ClassNotFoundException {
		return Class.forName(this.toString() + "." + className);
	}

}
