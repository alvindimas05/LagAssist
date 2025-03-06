package org.alvindimas05.lagassist.client;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Future;

import org.alvindimas05.lagassist.Main;
import org.alvindimas05.lagassist.gui.ClientGUI;
import org.alvindimas05.lagassist.Reflection;
import org.alvindimas05.lagassist.utils.WorldMgr;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.alvindimas05.lagassist.utils.VersionMgr;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public class ClientPacket {

    /**
     * Determines whether to hide a packet based on various conditions.
     *
     * @param p       the player instance
     * @param ctx     the channel handler context
     * @param msg     the packet object
     * @param promise the channel promise
     * @return true if the packet should be hidden, false otherwise
     */
    public static boolean hidePacket(Player p, ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (!ClientMain.enabled) {
            return false;
        }
        if (WorldMgr.isBlacklisted(p.getWorld())) {
            return false;
        }
        try {
            String name = msg.getClass().getSimpleName().toLowerCase();
            String entityType;
            if (name.equals("packetplayoutspawnentity")
                    && (ClientGUI.isOn(ClientGUI.ToggleState.TNT, p) || ClientGUI.isOn(ClientGUI.ToggleState.SAND, p))) {

                if (VersionMgr.isV1_8()) {
                    int x = ((int) Reflection.getFieldValue(msg, "b")) / 32;
                    int y = ((int) Reflection.getFieldValue(msg, "c")) / 32;
                    int z = ((int) Reflection.getFieldValue(msg, "d")) / 32;
                    Location loc = new Location(p.getWorld(), x, y, z);
                    entityType = Objects.requireNonNull(Reflection.getEntity(loc)).getType().toString();
                } else if (VersionMgr.isV1_17()) {
                    Object entityTypes = Reflection.getFieldValue(msg, "m");
                    entityTypes = Reflection.getFieldValue(entityTypes, "bv");
                    entityType = entityTypes == null ? "unknown" : entityTypes.toString();
                } else if (VersionMgr.isNewMaterials()) {
                    UUID uuid = (UUID) Reflection.getFieldValue(msg, "b");
                    Entity entity = getEntityAsync(p, uuid);
                    entityType = entity != null ? entity.getType().toString().toUpperCase() : null;
                } else {
                    double x = ((double) Reflection.getFieldValue(msg, "c"));
                    double y = ((double) Reflection.getFieldValue(msg, "d"));
                    double z = ((double) Reflection.getFieldValue(msg, "e"));
                    Location loc = new Location(p.getWorld(), x, y, z);
                    entityType = Objects.requireNonNull(Reflection.getEntity(loc)).getType().toString();
                }
                if (entityType == null) {
                    return false;
                }
                if (entityType.contains("tnt")) {
                    return ClientGUI.isOn(ClientGUI.ToggleState.TNT, p);
                } else if (entityType.contains("falling_block")) {
                    return ClientGUI.isOn(ClientGUI.ToggleState.SAND, p);
                }
            } else if (name.equals("packetplayoutworldparticles")) {
                return ClientGUI.isOn(ClientGUI.ToggleState.PARTICLES, p);
            } else if (name.equals("packetplayoutblockaction")) {
                Object block = Reflection.getFieldValue(msg, "d");
                String type = block.getClass().getSimpleName().toLowerCase();
                if (type.equals("blockpiston")) {
                    return ClientGUI.isOn(ClientGUI.ToggleState.PISTONS, p);
                }
            } else if (name.equals("packetplayoutentitystatus")) {
                byte status = (byte) Reflection.getFieldValue(msg, "b");
                return status == 3;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Safely retrieves an Entity by its UUID ensuring access on the main thread.
     *
     * @param p the player instance
     * @param u the UUID of the entity
     * @return the found Entity or null if not found
     */
    private static Entity getEntityAsync(Player p, UUID u) {
        if (!Bukkit.isPrimaryThread()) {
            try {
                Future<Entity> future = Bukkit.getScheduler().callSyncMethod(Main.p, () -> {
                    for (Entity ent : new ArrayList<>(p.getWorld().getEntities())) {
                        if (ent.getUniqueId().equals(u)) {
                            return ent;
                        }
                    }
                    return null;
                });
                return future.get();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            for (Entity ent : new ArrayList<>(p.getWorld().getEntities())) {
                if (ent.getUniqueId().equals(u)) {
                    return ent;
                }
            }
            return null;
        }
    }
}
