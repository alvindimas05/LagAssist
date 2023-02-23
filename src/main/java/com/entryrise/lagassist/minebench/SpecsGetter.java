package com.entryrise.lagassist.minebench;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.entryrise.lagassist.utils.Others;

public class SpecsGetter {

	private static OperatingSystemMXBean osmx = ManagementFactory.getOperatingSystemMXBean();

	private static String getLinuxCPU() {

		final String regex = "model name	: (.*\\n)";

		File fl = new File("/proc/cpuinfo");
		if (!fl.exists()) {
			return "unknown";
		}
		if (!fl.canRead()) {
			return "unknown";
		}
		try {
			String stg = Others.readInputStreamAsString(new FileInputStream(fl));

			final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
			final Matcher matcher = pattern.matcher(stg);
			matcher.find();

			return matcher.group(1).replaceAll("\n", "");

		} catch (IOException e) {
			e.printStackTrace();
			return "unknown";
		}

	}

	private static String getWindowsCPU() {
		Runtime rt = Runtime.getRuntime();
		try {
			Process proc = rt.exec("wmic cpu get name");
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			reader.readLine();
			reader.readLine();
			return reader.readLine();

		} catch (IOException e) {
			return "unknown";
		}

	}

	private static String getMacCPU() {
		Runtime rt = Runtime.getRuntime();
		try {
			Process proc = rt.exec("sysctl -n machdep.cpu.brand_string");
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			return reader.readLine();

		} catch (IOException e) {
			return "unknown";
		}

	}

	public static String getCPU(String OS) {
		if (OS.equals("windows")) {
			return getWindowsCPU();
		} else if (OS.equals("linux")) {
			return getLinuxCPU();
		} else if (OS.equals("mac")) {
			return getMacCPU();
		} else {
			return "unknown";
		}
	}

	public static String getOS() {
		String OS = System.getProperty("os.name").toLowerCase();
		if (OS.contains("linux")) {
			return "linux";
		} else if (OS.contains("win")) {
			return "windows";
		} else if (OS.contains("mac")) {
			return "mac";
		} else {
			return "other";
		}
	}

	public static int MaxRam() {
		return (int) (Runtime.getRuntime().maxMemory() / 1024 / 1024);

	}

	public static double FreeRam() {
		return (double) Runtime.getRuntime().freeMemory() / 1024 / 1024;
	}

	public static int threadCount() {
		return osmx.getAvailableProcessors();
	}

	public static float getSystemLoad() {
		return (float) osmx.getSystemLoadAverage();
	}

	public static BenchResponse getBenchmark() {

		String cpu = getCPU(getOS());

		if (cpu.equals("unknown")) {
			return new BenchResponse(-1, -1, false);
		}

		HTTPClient conn;
		try {
			conn = new HTTPClient("https://lagassist.rz.al/benchmark/" + URLEncoder.encode(cpu, "UTF-8"));

			return conn.getBenchmark();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;

	}

	public static int getSingleThreadScore() {
		return getBenchmark().getSinglethread();
	}

	public static int getMultiThreadScore() {
		return getBenchmark().getMultithread();
	}

}
