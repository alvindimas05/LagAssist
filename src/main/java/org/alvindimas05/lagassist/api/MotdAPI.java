package org.alvindimas05.lagassist.api;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

import org.alvindimas05.lagassist.utils.CustomLogger;
import org.alvindimas05.lagassist.utils.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.util.CachedServerIcon;

import org.alvindimas05.lagassist.Main;

public class MotdAPI implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerPing(ServerListPingEvent e) {
        InetAddress addr = e.getAddress();
        String saddr = addr.getHostAddress();

        Main.sendDebug("Received ping from " + saddr, 1);

        boolean found = Main.config.getStringList("api.server-icon.allowed-ips").stream()
                .map(stg -> Pattern.compile(stg.replace("*", "(.*.)")))
                .anyMatch(pattern -> pattern.matcher(saddr).find());

        if (!found) {
            return;
        }

        Main.sendDebug("API ping response to " + saddr, 1);

        try {
            CachedServerIcon icon = generateIcon();
            if (icon != null) {
                e.setServerIcon(icon);
            }
        } catch (Exception ex) {
            Main.sendDebug("Error generating server icon: " + ex.getMessage(), 1);
        }
    }

    private static CachedServerIcon generateIcon() {
        try {
            double[] tpsValues = Bukkit.getTPS();
            float[] tps = new float[]{(float) tpsValues[0], (float) tpsValues[1], (float) tpsValues[2]};

            for (float tp : tps) {
                CustomLogger.info("TPS: " + tp);
            }

            BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < 64; y++) {
                for (int x = 0; x < 64; x++) {
                    int color = (int) (tps[0] * 12) << 16 | (int) (tps[1] * 12) << 8 | (int) (tps[2] * 12);
                    image.setRGB(x, y, color);
                }
            }

            return Bukkit.getServer().loadServerIcon(image);

        } catch (Exception ex) {
            CustomLogger.severe("Error generating server icon: " + ex.getMessage());
            return null;
        }
    }

    public static BufferedImage convertBytesToImage(byte[] bts) {
        int[] pixels = MathUtils.bytesToIntegers(bts);
        int size = 64;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);

        int pixel;
        for (pixel = 0; pixel < pixels.length; pixel++) {
            int x = pixel % size;
            int y = pixel / size;
            img.setRGB(x, y, pixels[pixel]);
        }

        return img;
    }
}
