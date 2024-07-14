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

		// Return latest version if name has no version
		if(!name.contains("v1_")) return "v1_21_R1";

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
