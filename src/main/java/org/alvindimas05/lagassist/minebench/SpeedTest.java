package org.alvindimas05.lagassist.minebench;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import org.alvindimas05.lagassist.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpeedTest {

	protected static DecimalFormat df = new DecimalFormat("0.0");
	private static SplittableRandom sr = new SplittableRandom();

	protected static float getDownSpeed() {
		URLConnection ftp;
		try {
			URL ftpdlur = new URL(Main.config.getString("benchmark.download.link"));
			ftp = ftpdlur.openConnection();
			ftp.connect();

			InputStream dloader = ftp.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(dloader);
			
			
			long begin = System.currentTimeMillis();
			
			while (bis.read() != -1) {
				// Waste time while reading.
			}

			float taken = (System.currentTimeMillis() - begin) / 1000.0f;

			dloader.close();

			return 50.0f / taken;
		} catch (IOException e) {
			return -1;
		}

	}

	protected static float getUpSpeed() {
		HttpsURLConnection ftp;
		try {
			long upsize = Main.config.getLong("benchmark.upload.size");

			URL ftpupur = new URL(
					Main.config.getString("benchmark.upload.link").replaceAll("%RND%", UUID.randomUUID().toString()));

			ftp = (HttpsURLConnection) ftpupur.openConnection();
			
			ftp.setDoOutput(true);
			ftp.setRequestProperty("Content-Length", "" + upsize);
			ftp.connect();

			OutputStream of = ftp.getOutputStream();
			BufferedOutputStream bof = new BufferedOutputStream(of);
			
			
			long begin = System.currentTimeMillis();
			
			// Fast write. Makes bufferedoutputstream redundant (this is optimized without it), but it doesn't hurt.
			
			for (int i = 0; i < upsize; i++) {
				bof.write((byte) i);
			}
			
			bof.flush();
			of.flush();
			
			float taken = (System.currentTimeMillis() - begin) / 1000.0f;

			bof.close();
			of.close();
			
			Main.sendDebug("Speedtest with uploadsize: " + upsize + " took " + taken + " seconds", 1);
			return upsize / 1000000 / taken;
		} catch (IOException e) {
//			e.printStackTrace();
			return -1;
		}

	}

	public static void pingBenchmark(CommandSender s) {

		List<InetAddress> ips = new ArrayList<InetAddress>();

		int med = 0;
		int nr = 0;
		int max = -1;
		int min = -1;

		String namemax = "";
		String namemin = "";

		for (Player p : Bukkit.getOnlinePlayers()) {
//			int pping = Reflection.getPing(p);
			int pping = p.spigot().getPing();
			med += pping;
			nr++;
			if (max == -1) {
				max = pping;
				namemax = p.getName();
			} else if (max < pping) {
				max = pping;
				namemax = p.getName();
			}
			if (min == -1) {
				min = pping;
				namemin = p.getName();
			} else if (min > pping) {
				min = pping;
				namemin = p.getName();
			}

			ips.add(p.getAddress().getAddress());
		}

		s.sendMessage("");
		s.sendMessage("§2§l⬛⬛⬛⬛⬛⬛§f§l PINGTEST RESULTS §2§l⬛⬛⬛⬛⬛⬛");
		s.sendMessage("");
		s.sendMessage(" §2✸ §fLowest Ping: §e" + String.valueOf(min) + "ms  §2(" + namemin + ")");
		s.sendMessage(" §2✸ §fHighest Ping: §e" + String.valueOf(max) + "ms  §2(" + namemax + ")");
		s.sendMessage("");
		s.sendMessage(" §2✸ §fAverage Ping: §e" + df.format((double) (med / nr)) + "ms");
		s.sendMessage("");
		s.sendMessage("§2§l⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛");

	}
}
