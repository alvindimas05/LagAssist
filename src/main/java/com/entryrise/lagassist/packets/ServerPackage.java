package com.entryrise.lagassist.packets;

import org.bukkit.Bukkit;

public enum ServerPackage {

	MINECRAFTSERVER("net.minecraft.server." + getServerVersion()),
	CRAFTBUKKIT("org.bukkit.craftbukkit." + getServerVersion()), MINECRAFT("net.minecraft." + getServerVersion());

	private final String path;

	ServerPackage(String path) {
		this.path = path;
	}

	public static String getServerVersion() {
		return Bukkit.getServer().getClass().getPackage().getName().substring(23);
	}

	@Override
	public String toString() {
		return path;
	}

	public Class<?> getClass(String className) throws ClassNotFoundException {
		return Class.forName(this.toString() + "." + className);
	}

}
