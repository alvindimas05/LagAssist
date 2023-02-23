package com.entryrise.lagassist.api;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.util.regex.Pattern;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.util.CachedServerIcon;

import com.entryrise.lagassist.Main;
import com.entryrise.lagassist.Monitor;
import com.entryrise.lagassist.utils.MathUtils;

public class MotdAPI implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onServerPing(ServerListPingEvent e) {
		InetAddress addr = e.getAddress();
		
		String saddr = addr.getHostAddress();
		
		Main.sendDebug("Received ping from " + saddr, 1);
		
		boolean found = false;

		for (String stg : Main.config.getStringList("api.server-icon.allowed-ips")) {
			Pattern pat = Pattern.compile(stg.replace("*", "(.*.)"));
			found = pat.matcher(saddr).find();
			
			if (found) {
				break;
			}
		}
		
		if (!found) {
			return;
		}
		
		Main.sendDebug("API ping response to " + saddr, 1);
		
		try {
			e.setServerIcon(generateIcon());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private static CachedServerIcon generateIcon() throws Exception{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		
		float tps[] = new float[]{(Double.valueOf(Monitor.getTPS(0)).floatValue()), (Double.valueOf(Monitor.getTPS(1)).floatValue()), (Double.valueOf(Monitor.getTPS(2)).floatValue())};
		
		for (int i = 0; i< tps.length; i++) {
			System.out.println(tps[i]);
			dos.writeFloat(tps[i]);
		}
		
		dos.flush();
		baos.flush();
		
		return new CachedServerIcon() {
			
			@Override
			public String getData() {
				return javax.xml.bind.DatatypeConverter.printBase64Binary(baos.toByteArray());
			}
		};
		
	}
	
	public static BufferedImage convertBytesToImage(byte[] bts) {

		int[] pixels = MathUtils.bytesToIntegers(bts);
		int size = 64;
		
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);

		img.setRGB(0, 0, bts.length);
		
		int pixel;
		
		for (pixel = 1; pixel <= pixels.length; pixel++) {
			int x = pixel%size;
			int y = pixel/size;
			
			img.setRGB(x, y, pixels[pixel-1]);
		}
		
		
		return img;

	}
}
