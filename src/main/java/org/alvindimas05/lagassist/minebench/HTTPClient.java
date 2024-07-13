package org.alvindimas05.lagassist.minebench;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.bukkit.Bukkit;

import org.alvindimas05.lagassist.utils.Others;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HTTPClient {

	private InputStream is;

	public HTTPClient(String url) {

		is = getInputStream(url);

	}

	private static InputStream getInputStream(String url) {

		HttpURLConnection cnn;
		try {
			URL ur = new URL(url);
			cnn = (HttpURLConnection) ur.openConnection();
			cnn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
			cnn.addRequestProperty("Referer", "google.com");
			cnn.connect();
			String header = cnn.getHeaderField("location");
			if (header != null) {
				HttpURLConnection redur = (HttpURLConnection) new URL(header.replaceAll("http", "https"))
						.openConnection();
				redur.setRequestProperty("User-Agent",
						"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
				redur.connect();

				return redur.getInputStream();
			} else {
				return cnn.getInputStream();
			}
		} catch (IOException e) {
			Bukkit.getLogger().warning("§e[§a✖§e] §fCouldn't connect to PassMark Data.");

			return null;
		}
	}

//	private static int getMultiThread(String s) {
//
//		String regex = "<span style=\"font-family: Arial, Helvetica, sans-serif;font-size: 44px;	font-weight: bold; color: #F48A18;\">(.*.)<\\/span>";
//
//		final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
//		final Matcher matcher = pattern.matcher(s);
//
//		if (!matcher.find()) {
//			return -1;
//		}
//
//		return Integer.valueOf(matcher.group(1));
//	}
//
//	private static int getSingleThread(String s) {
//
//		String regex = "Single Thread Rating: <\\/strong>(.*.)<br>";
//
//		final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
//		final Matcher matcher = pattern.matcher(s);
//
//		if (!matcher.find()) {
//			return -1;
//		}
//
//		float fl = Float.valueOf(matcher.group(1));
//
//		return (int) fl;
//	}

	public BenchResponse getBenchmark() {
		if (is == null) {
			return new BenchResponse(-1, -1, false);
		}
		
		String s = Others.readInputStreamAsString(is);
		
		JsonObject response = new JsonParser().parse(s).getAsJsonObject();
		
		String status = response.get("status").getAsString();
		
		if (!status.equalsIgnoreCase("OK")) {
			return new BenchResponse(-1, -1, false);
		}
		
		return new BenchResponse(response.get("single").getAsInt(), response.get("multi").getAsInt(), true);
	}

}
