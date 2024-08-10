package org.alvindimas05.lagassist.minebench;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import org.alvindimas05.lagassist.utils.Others;

class BenchmarkData {
    public List<BenchmarkCPU> data;
    public static class BenchmarkCPU {
        public String name;
        public String cpumark;
        public String thread;
        public String cores;
    }
}

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
		}
        return "unknown";
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

    private static String formatCPU(String cpuname){
        return String.join(" ",
            Arrays.copyOfRange(
                cpuname.replaceAll("\\(R\\)| CPU", "").split(" "),
                0, 4
            )
        );
    }

	public static String getCPU(String OS) {
        return switch (OS) {
            case "windows" -> getWindowsCPU();
            case "linux" -> getLinuxCPU();
            case "mac" -> getMacCPU();
            default -> "unknown";
        };
	}

    public static int getCores() {
        String OS = getOS();
        String command = switch (OS) {
            case "mac" -> "sysctl -n machdep.cpu.core_count";
            case "linux" -> "lscpu";
            case "windows" -> "cmd /C WMIC CPU Get /Format:List";
            default -> "";
        };
        Process process = null;
        int numberOfCores = 0;
        int sockets = 0;
        try {
            if(OS.equals("mac")){
                String[] cmd = { "/bin/sh", "-c", command};
                process = Runtime.getRuntime().exec(cmd);
            }else{
                process = Runtime.getRuntime().exec(command);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream()));
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                if(OS.equals("mac")){
                    numberOfCores = !line.isEmpty() ? Integer.parseInt(line) : 0;
                }else if (OS.equals("linux")) {
                    if (line.contains("Core(s) per socket:")) {
                        numberOfCores = Integer.parseInt(line.split("\\s+")[line.split("\\s+").length - 1]);
                    }
                    if(line.contains("Socket(s):")){
                        sockets = Integer.parseInt(line.split("\\s+")[line.split("\\s+").length - 1]);
                    }
                } else if (OS.equals("windows")) {
                    if (line.contains("NumberOfCores")) {
                        numberOfCores = Integer.parseInt(line.split("=")[1]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(OS.equals("linux")){
            return numberOfCores * sockets;
        }
        return numberOfCores;
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

		String cpuname = formatCPU(getCPU(getOS()));

		if (cpuname.equals("unknown")) {
			return new BenchResponse(-1, -1, -1, -1, false);
		}

		try {
            URL url = new URL("https://github.com/alvindimas05/LagAssist/releases/latest/download/benchmark-data.json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            StringBuilder response = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while (scanner.hasNextLine()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            Gson gson = new Gson();
            BenchmarkData data = gson.fromJson(response.toString(), BenchmarkData.class);

            BenchmarkData.BenchmarkCPU benchmarkCPU = data.data.stream()
                .filter(cpu -> cpu.name.contains(cpuname))
                .toList().getFirst();

            return new BenchResponse(
                Integer.parseInt(benchmarkCPU.cpumark.replaceAll(",", "")),
                0,
                Integer.parseInt(benchmarkCPU.thread.replaceAll(",", "")),
                Integer.parseInt(benchmarkCPU.cores),
                true
            );
		} catch (Exception e) {
            e.printStackTrace();
        }
        return new BenchResponse(-1, -1, -1, -1, false);

	}

	public static int getSingleThreadScore() {
		return getBenchmark().getSinglethread();
	}

	public static int getMultiThreadScore() {
		return getBenchmark().getMultithread();
	}

}
