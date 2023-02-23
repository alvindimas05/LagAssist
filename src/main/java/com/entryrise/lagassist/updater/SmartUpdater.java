package com.entryrise.lagassist.updater;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.entryrise.lagassist.Main;

public class SmartUpdater {

	private static String updatesurl = "https://api.spiget.org/v2/resources/56399/updates?size=15&sort=-date";
	private static String versionurl = "https://api.spiget.org/v2/resources/56399/versions?size=15&sort=-releaseDate";

	public static UpdateInfo ui = null;

	public static void Enabler() {
		if (!Main.config.getBoolean("smart-updater.enabled")) {
			return;
		}

		UpdateCondition.Enabler();

		Bukkit.getScheduler().runTaskAsynchronously(Main.p, new Runnable() {
			@Override
			public void run() {
				ui = getNextUpdate();

				if (ui == null) {
					Bukkit.getLogger().info("§2§lLag§f§lAssist §e» §fYou are up to date with LagAssist.");
					return;
				}

				Bukkit.getLogger().info("§2§lLag§f§lAssist §e» §fWe found a newer LagAssist version:");
				if (ui.isUnsafe()) {
					Bukkit.getLogger().warning("§2§lLag§f§lAssist §e» §fThis version is considered Unsafe!");
				}
				Bukkit.getLogger().info("    §2[§e֍§2] §fVersion: " + ui.getVersion());
				Bukkit.getLogger().info("    §2[§e֍§2] §fReviews: " + ((int) (ui.getRating() * 100)) / 100f + "§e★");
				Bukkit.getLogger().info("    §2[§e֍§2] §fDownloads: " + ui.getDownloads());
				Bukkit.getLogger().info("    §2[§e֍§2] §fFor more info, use §2/lagassist changelog");

			}
		});

	}

	private static UpdateInfo getNextUpdate() {
		try {

			URL uurl = new URL(updatesurl);
			URL vurl = new URL(versionurl);

			JSONArray updateData = getWebData(uurl);
			JSONArray versionData = getWebData(vurl);

			if (updateData == null) {
				return null;
			}
			if (versionData == null) {
				return null;
			}

			Map<Long, UpdateInfo> updates = new HashMap<Long, UpdateInfo>();
			Map<Long, UpdateInfo> filteredupdates = new HashMap<Long, UpdateInfo>();

			// GET all update posts (update messages, etc) and create a rudimentary
			// UpdateInfo.
			for (int i = 0; i < updateData.size(); i++) {
				JSONObject obj = (JSONObject) updateData.get(i);

				String title = (String) obj.get("title");
				String description = (java.lang.String) obj.get("description");
				long date = (long) obj.get("date");
				long likes = (long) obj.get("likes");

				UpdateInfo up = new UpdateInfo();

				up.setTitle(title);
				up.setDescription(description);
				up.setDate(date);
				up.setLikes((int) likes);

				updates.put(date, up);
			}

			// Fill UpdateInfo with reviews and other useful information.
			for (int i = 0; i < versionData.size(); i++) {
				JSONObject obj = (JSONObject) versionData.get(i);

				long date = (long) obj.get("releaseDate");

				if (!updates.containsKey(date)) {
					continue;
				}

				String version = (String) obj.get("name");

				long downloads = (long) obj.get("downloads");
				double rating = Double.valueOf(String.valueOf(((JSONObject) obj.get("rating")).get("average")));
				long id = (long) obj.get("id");

				UpdateInfo up = updates.get(date);

				up.setId(id);
				up.setVersion(version);
				up.setRating((int) rating);
				up.setDownloads((int) downloads);

				filteredupdates.put(date, up);
			}

			// Sort map and do other juicy stuff.
			LinkedHashMap<Long, UpdateInfo> sorted = filteredupdates.entrySet().stream()
					.sorted(Collections.reverseOrder(Map.Entry.comparingByKey()))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue,
							LinkedHashMap::new));

			for (long key : sorted.keySet()) {
				UpdateInfo up = updates.get(key);

				if (UpdateCondition.shouldUpgrade(up)) {
					return up;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Bukkit.getLogger().warning("§e[§a✖§e] §fCouldn't recognize Update data.");
		}
		return null;
	}

	private static JSONArray getWebData(URL u) {
		InputStream is;
		try {
			is = u.openStream();
			return (JSONArray) JSONValue.parse(new InputStreamReader(is));
		} catch (IOException e) {
			Bukkit.getLogger().warning("§e[§a✖§e] §fCouldn't connect to Update Data.");
		}
		return null;
	}

	private static String getFormattedDesc() {
		if (ui == null) {
			return null;
		}

		return ui.getDescription().substring(0, Math.min(ui.getDescription().length(), 1000)).replace('\n', ' ');

	}

	public static void showChangelog(CommandSender s) {
		if (ui == null) {
			s.sendMessage("§2§lLag§f§lAssist §e» §fThere is no new recommended version available.");
			return;
		}

		String formatdesc = getFormattedDesc();

		s.sendMessage("§2§l⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛§f§l LAGASSIST CHANGELOG §2§l⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛");
		s.sendMessage("");
		s.sendMessage("  §2✸ §fNew Version: §e" + ui.getVersion());
		s.sendMessage("");
		s.sendMessage("  §2✸ §fTitle:");
		s.sendMessage(" §e" + ui.getTitle());
		s.sendMessage("");
		s.sendMessage("  §2✸ §fDescription:");
		s.sendMessage(" §e" + formatdesc);
		s.sendMessage("");
		s.sendMessage("  §2✸ §fRating: §e" + ui.getRating() + "★");
		s.sendMessage("  §2✸ §fDownloads: §e" + ui.getDownloads());
		s.sendMessage("  §2✸ §fLikes: §e" + ui.getLikes());
		s.sendMessage("");
		s.sendMessage("§2§l⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛");
	}

}
