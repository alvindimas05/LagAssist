package com.entryrise.lagassist.packets;

import com.entryrise.lagassist.safety.SafetyAnticrash;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

/*
 * Used for outright removal of packets from the moment the player was a bad boy.
 * 
 * Makes sure to prevent other plugins (ProtocolLib) from being spammed with bad packets.
 */
public class BlacklistHandler  extends ChannelDuplexHandler {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object arg1) throws Exception {
		Channel ch = ctx.channel();
		if (SafetyAnticrash.isDropped(ch)) {
			ch.disconnect().get();
			return;
		}
		super.channelRead(ctx, arg1);
	}
	
}
