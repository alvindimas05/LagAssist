package com.entryrise.lagassist.minebench;

import org.bukkit.command.CommandSender;

import com.entryrise.lagassist.Main;

public class SysInfo {

	public static void sendInfo(CommandSender s) {
		String os = System.getProperty("os.name").toLowerCase();
		String arch = System.getProperty("os.arch").toLowerCase();
		String java = System.getProperty("java.version");
		String CPU = SpecsGetter.getCPU(SpecsGetter.getOS());
		String cores = String.valueOf(Runtime.getRuntime().availableProcessors());
		String load = String.valueOf(SpecsGetter.getSystemLoad());
		String freespace = "FREE: " + String.valueOf(Main.p.getDataFolder().getFreeSpace()) + " USABLE: "
				+ Main.p.getDataFolder().getUsableSpace();
		s.sendMessage(os);
		s.sendMessage(arch);
		s.sendMessage(java);
		s.sendMessage(CPU);
		s.sendMessage(cores);
		s.sendMessage(load);
		s.sendMessage(freespace);
		;
	}

}
