package com.entryrise.lagassist.client;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.entryrise.lagassist.gui.ClientGUI;
import com.entryrise.lagassist.gui.ClientGUI.ToggleState;
import com.entryrise.lagassist.packets.Reflection;
import com.entryrise.lagassist.utils.VersionMgr;
import com.entryrise.lagassist.utils.WorldMgr;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public class ClientPacket {

	public static boolean hidePacket(Player p, ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
		if (!ClientMain.enabled) {
			return false;
		}
		if (WorldMgr.isBlacklisted(p.getWorld())) {
			return false;
		}
		try {
			String name = msg.getClass().getSimpleName().toLowerCase();
			String entitytype = null;
			if (name.equals("packetplayoutspawnentity")
					&& (ClientGUI.isOn(ToggleState.TNT, p) || ClientGUI.isOn(ToggleState.SAND, p))) {


				if (VersionMgr.isV1_8()) {
					int x = ((int) Reflection.getFieldValue(msg, "b")) / 32;
					int y = ((int) Reflection.getFieldValue(msg, "c")) / 32;
					int z = ((int) Reflection.getFieldValue(msg, "d")) / 32;
					Location loc = new Location(p.getWorld(), x, y, z);
					entitytype = Reflection.getEntity(loc).getType().toString();
				} else if (VersionMgr.isV1_17()) {
					Object entitytypes = Reflection.getFieldValue(msg, "m");
					entitytypes = Reflection.getFieldValue(entitytypes, "bv");
					
					entitytype = entitytypes == null ? "unknown" : entitytypes.toString();
				} else if (VersionMgr.isNewMaterials()) {
					// TODO: SYNC METHOD FIX MAY CAUSE MAJOR LAG!
					UUID u = (UUID) Reflection.getFieldValue(msg, "b");
					entitytype = getEntityAsync(p, u).getType().toString().toUpperCase();
				} else {
					double x = ((double) Reflection.getFieldValue(msg, "c"));
					double y = ((double) Reflection.getFieldValue(msg, "d"));
					double z = ((double) Reflection.getFieldValue(msg, "e"));
					Location loc = new Location(p.getWorld(), x, y, z);
					entitytype = Reflection.getEntity(loc).getType().toString();
				}

				if (entitytype == null) {
					return false;
				}
				

				if (entitytype.contains("tnt")) {
					return ClientGUI.isOn(ToggleState.TNT, p);
				} else if (entitytype.contains("falling_block")) {
					return ClientGUI.isOn(ToggleState.SAND, p);
				}
			} else if (name.equals("packetplayoutworldparticles")) {
				return ClientGUI.isOn(ToggleState.PARTICLES, p);
			} else if (name.equals("packetplayoutblockaction")) {
				Object block = Reflection.getFieldValue(msg, "d");
				String type = block.getClass().getSimpleName().toLowerCase();
				if (type.equals("blockpiston")) {
					return ClientGUI.isOn(ToggleState.PISTONS, p);
				}
			} else if (name.equals("packetplayoutentitystatus")) {
				// int ent = (int) Reflection.getFieldValue(msg, "a");
				byte status = (byte) Reflection.getFieldValue(msg, "b");

				if (status == 3) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}
	
	private static Entity getEntityAsync(Player p, UUID u) {
		for (Entity ent : new ArrayList<>(p.getWorld().getEntities())) {
			if (ent.getUniqueId() == u) return ent;
		}
		return null;
	}

}
