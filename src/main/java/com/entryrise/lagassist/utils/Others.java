package com.entryrise.lagassist.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.entryrise.lagassist.Main;

public class Others {

	public static boolean isInsideBorder(Location loc) {
		WorldBorder border = loc.getWorld().getWorldBorder();
		double radius = border.getSize() / 2;
		Location location = loc, center = border.getCenter();

		return center.distanceSquared(location) < (radius * radius);
	}

	public static YamlConfiguration getConfig(File f, int version) {
		YamlConfiguration fc = new YamlConfiguration();

		if (!f.exists()) {
			f.getParentFile().mkdirs();
			Main.p.saveResource(f.getName(), false);
		}

		try {
			fc.load(f);

			if (fc.contains("version")) {
				if (fc.getInt("version") != version) {
					Bukkit.getLogger().info("�c�lLag�f�lAssist �e� �fUpdating " + f.getName() + " file!");
					f.renameTo(getOldFile(f));
					Main.p.saveResource(f.getName(), false);
					fc.load(f);

				}
			} else {
				Bukkit.getLogger().info("�c�lLag�f�lAssist �e� �fUpdating " + f.getName() + " file!");
				f.renameTo(getOldFile(f));
				Main.p.saveResource(f.getName(), false);
				fc.load(f);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return fc;

	}

	private static File getOldFile(File f) {
		return new File(f.getParentFile(), f.getName() + "." + RandomStringUtils.randomAlphanumeric(3) + ".old");
	}

	public static String readInputStreamAsString(InputStream in) {

		try {
			BufferedInputStream bis = new BufferedInputStream(in);
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			int result = bis.read();
			while (result != -1) {
				byte b = (byte) result;
				buf.write(b);
				result = bis.read();
			}
			return buf.toString();
		} catch (IOException e) {
			return null;
		}

	}
	
	public static String firstHighcase(String stg) {
		String nw = stg.toLowerCase();
		return nw.substring(0, 1).toUpperCase() + nw.substring(1);
	}
	
	public static Item giveOrDrop(Player p, ItemStack itm) {
		PlayerInventory pinv = p.getInventory();
		
		if (pinv.firstEmpty() != 1) {
			pinv.addItem(itm);
			return null;
		}
		
		return p.getLocation().getWorld().dropItem(p.getLocation(), itm);
	}
}
