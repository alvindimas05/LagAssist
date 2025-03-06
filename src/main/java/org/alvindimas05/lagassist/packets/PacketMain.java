package org.alvindimas05.lagassist.packets;

import org.alvindimas05.lagassist.Main;
import org.alvindimas05.lagassist.utils.CustomLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import org.alvindimas05.lagassist.client.ClientMain;
import org.alvindimas05.lagassist.safety.SafetyManager;

public class PacketMain implements Listener {

    public static void Enabler(boolean reload) {
        PacketInjector.Enabler();
        ClientMain.secondaryEnabler(reload);

        if (!reload) {
            Main.p.getServer().getPluginManager().registerEvents(new PacketMain(), Main.p);
        }
        CustomLogger.info("    §e[§a✔§e] §fInjecting PacketListener.");
    }

    public static boolean isPacketEnabled() {
        return ClientMain.enabled || SafetyManager.enabled;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        if (!isPacketEnabled()) {
            return;
        }
        Player p = e.getPlayer();
        PacketInjector.addPlayer(p);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent e) {
        if (!isPacketEnabled()) {
            return;
        }
        Player p = e.getPlayer();
        PacketInjector.removePlayer(p);
    }

}
