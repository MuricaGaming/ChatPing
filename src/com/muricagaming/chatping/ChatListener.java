package com.muricagaming.chatping;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener
{
	Main main;
	String message;
	String coloredmessage;
	Collection<? extends Player> players;
	PlayerPreferences recipientPrefs;
	boolean aliasFound;
	boolean nameFound;
	Matcher matcher;
	ArrayList<String> matches;
	
	public ChatListener(Main plugin)
	{
		main = plugin;
		matches = new ArrayList<>();
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerChat(AsyncPlayerChatEvent chat) {

		players = main.getServer().getOnlinePlayers();
		aliasFound = false;
		message = chat.getMessage();

		for (Player p : players) {

			recipientPrefs = main.findPlayerPrefs(p.getUniqueId());

			if(recipientPrefs.aliasesOn && recipientPrefs.aliases != null)
				for (String s: recipientPrefs.aliases) {
					matcher = Pattern.compile("(?i)\\b" + s + "\\b", Pattern.CASE_INSENSITIVE).matcher(message);
					if (matcher.find())
						aliasFound = true;
				}

			matcher = Pattern.compile("(?i)\\b" + p.getName() + "\\b", Pattern.CASE_INSENSITIVE).matcher(message);
			if(matcher.find())
				nameFound = true;

			if (p.hasPermission("chatping.user") && recipientPrefs.pingsOn && (nameFound || aliasFound)) {
				matches = new ArrayList<>();
				if (recipientPrefs.soundOn && Duration.between(recipientPrefs.lastPing.toInstant(), Instant.now()).toMillis() > (recipientPrefs.cooldown * 1000L))
					p.playSound(p.getLocation(), recipientPrefs.pingSound, 1, 1);
				chat.getRecipients().remove(p);
				coloredmessage = message;
				matcher = Pattern.compile("(?i)\\b" + p.getName() + "\\b", Pattern.CASE_INSENSITIVE).matcher(message);
				while(matcher.find()) {
					matches.add(matcher.group());
				}
				for (String s: matches) {
					coloredmessage = coloredmessage.replaceAll("\\b" + s + "\\b", recipientPrefs.highlightColor + s + ChatColor.RESET);
				}
				if(recipientPrefs.aliasesOn && recipientPrefs.aliases != null && aliasFound) {
					matches = new ArrayList<>();
					for (String a : recipientPrefs.aliases) {
						matcher = Pattern.compile("(?i)\\b" + a + "\\b", Pattern.CASE_INSENSITIVE).matcher(coloredmessage);
						while (matcher.find()) {
							matches.add(matcher.group());
						}
						for (String s: matches) {
							coloredmessage = coloredmessage.replaceAll("\\b" + s + "\\b", recipientPrefs.highlightColor + s + ChatColor.RESET);
						}
					}
				}
				p.sendMessage(chat.getFormat().replace("%1$s", chat.getPlayer().getDisplayName()).replace("%2$s", coloredmessage));
				recipientPrefs.lastPing = new Date();
			}
			aliasFound = false;
			nameFound = false;
		}
	}
}