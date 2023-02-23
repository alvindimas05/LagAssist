package com.entryrise.lagassist.updater;

import com.entryrise.lagassist.Main;

public class UpdateCondition {

	private static int mindownloads = 0;
	private static int minlikes = 0;
	private static float minrating = 0;

	private static boolean unsafe = true;
	private static VersionComparator vc = new VersionComparator();

	public static void Enabler() {
		unsafe = Main.config.getBoolean("smart-updater.announce.unsafe");

		minlikes = Main.config.getInt("smart-updater.announce.min-likes");
		mindownloads = Main.config.getInt("smart-updater.announce.min-downloads");
		minrating = (float) Main.config.getDouble("smart-updater.announce.min-rating");

	}

	public static boolean shouldUpgrade(UpdateInfo info) {
		if (info.isUnsafe() && !unsafe) {
			return false;
		}

		String currentver = Main.p.getDescription().getVersion();
		String newver = info.getVersion();

		if (newver == null || currentver == null) {
			return false;
		}

		if (vc.compare(currentver, newver) > 0) {
			return false;
		}

		if (info.getLikes() < minlikes) {
			return false;
		}

		if (info.getDownloads() < mindownloads) {
			return false;
		}

		if (info.getRating() < minrating) {
			return false;
		}

		return true;

	}

}
