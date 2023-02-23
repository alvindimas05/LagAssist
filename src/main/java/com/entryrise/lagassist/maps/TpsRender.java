package com.entryrise.lagassist.maps;

import java.text.DecimalFormat;
import java.util.SplittableRandom;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MinecraftFont;

import com.entryrise.lagassist.Main;
import com.entryrise.lagassist.MonTools;
import com.entryrise.lagassist.Monitor;

public class TpsRender extends MapRenderer {

	public static byte yellow = 74;
	public static byte green = -122;

	private static boolean fullrder = Main.config.getBoolean("lag-map.fully-reserve");
	private static DecimalFormat fmt = new DecimalFormat("##.###");
	private static SplittableRandom r = new SplittableRandom();

	@Override
	public void render(MapView mapView, MapCanvas mapCanvas, Player p) {
		if (!MonTools.mapusers.contains(p.getUniqueId()) && !fullrder) {
			return;
		}
		
		MapCursorCollection crs = mapCanvas.getCursors();
		
		// Should delete all cursors smart
		while (crs.size() > 0) {
			crs.removeCursor(crs.getCursor(0));
		}

		// BEGIN GRAPHING
		for (int i = 3; i < 125; i++) {
			for (int j = 3; j < 90; j++) {
				if (Monitor.colors[i][j] == 32) {
					int cul = r.nextInt(2);
					if (cul == 1) {
						mapCanvas.setPixel(i, j, (byte) 32);
					} else {
						mapCanvas.setPixel(i, j, (byte) 34);
					}
				} else {
					mapCanvas.setPixel(i, j, Monitor.colors[i][j]);
				}
			}
		}

		// YELLOW DOWN
		for (int i = 0; i < 128; i++) {
			for (int j = 93; j < 128; j++) {
				mapCanvas.setPixel(i, j, yellow);
			}
		}

		// BLUE LINES

		for (int y = 0; y < 90; y++) {
			mapCanvas.setPixel(125, y, (byte) 20);
			mapCanvas.setPixel(126, y, (byte) 20);
			mapCanvas.setPixel(127, y, (byte) 20);
		}

		for (int y = 0; y < 90; y++) {
			mapCanvas.setPixel(0, y, (byte) 20);
			mapCanvas.setPixel(1, y, (byte) 20);
			mapCanvas.setPixel(2, y, (byte) 20);
		}

		for (int i = 0; i < 128; i++) {
			mapCanvas.setPixel(i, 90, (byte) 20);
			mapCanvas.setPixel(i, 91, (byte) 20);
			mapCanvas.setPixel(i, 92, (byte) 20);
		}

		for (int i = 0; i < 128; i++) {
			mapCanvas.setPixel(i, 0, (byte) 20);
			mapCanvas.setPixel(i, 1, (byte) 20);
			mapCanvas.setPixel(i, 2, (byte) 20);
		}

		// TEXTAREA
		String TPS = "TPS: " + fmt.format(Monitor.exactTPS);
		String FREEMEM = "FREE MEM: " + String.valueOf(Monitor.freeMEM() + "MB");

		mapCanvas.drawText(5, 115, MinecraftFont.Font, TPS);
		mapCanvas.drawText(5, 100, MinecraftFont.Font, FREEMEM);
	}

	public static void Enabler() {

	}

}
