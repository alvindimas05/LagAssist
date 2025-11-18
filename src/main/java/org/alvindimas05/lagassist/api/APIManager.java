package org.alvindimas05.lagassist.api;

import org.alvindimas05.lagassist.utils.CustomLogger;
import org.alvindimas05.lagassist.Main;
import org.bukkit.ChatColor;

public class APIManager {

    public static void enable(boolean reload) {

        CustomLogger.info(ChatColor.YELLOW + "    [" + ChatColor.GREEN + "✔" + ChatColor.YELLOW + "] "
                + ChatColor.WHITE + "API Tools initialized.");

        if (!reload) {
            Main.p.getServer().getPluginManager().registerEvents(new MotdAPI(), Main.p);
        }
    }
}
