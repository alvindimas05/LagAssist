package com.entryrise.lagassist.utils;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class Chat {

	public static TextComponent genHoverAndSuggestTextComponent(String show, String hover, String click) {
		TextComponent msg = new TextComponent(show);
		msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
		msg.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + click));
		return msg;
	}

	public static TextComponent genHoverAndRunCommandTextComponent(String show, String hover, String click) {
		TextComponent msg = new TextComponent(show);
		msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
		msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + click));
		return msg;
	}

	public static TextComponent genHoverTextComponent(String show, String hover) {
		TextComponent msg = new TextComponent(show);
		msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
		return msg;
	}
	
	public static TextComponent genHoverAndLinkComponent(String show, String url, String hover) {
		TextComponent msg = new TextComponent(show);
		msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
		msg.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
		return msg;
	}
	
	public static String capitalize(String stg) {
		return stg.substring(0, 1).toUpperCase() + stg.substring(1);
	}

}
