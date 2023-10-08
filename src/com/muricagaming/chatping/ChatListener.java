package com.muricagaming.chatping;

import java.util.Collection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
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
	
	public ChatListener(Main plugin)
	{
		this.main = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerChat(AsyncPlayerChatEvent chat) {
		players = main.getServer().getOnlinePlayers();
		for (Player p : players) {
			if (p.hasPermission("chatping.ping")) {
				message = chat.getMessage();
				if (message.contains(p.getName())) {
					if (main.soundEnabled) {
						p.playSound(p.getLocation(), main.pingSound, 1, 1);
					}
					if (main.colorEnabled) {
						chat.getRecipients().remove(p);
						coloredmessage = message.replace(p.getName(), ChatColor.getByChar(main.highlightColor) + p.getName() + ChatColor.RESET);
						p.sendMessage(chat.getFormat().replace("%1$s", chat.getPlayer().getDisplayName()).replace("%2$s", coloredmessage));
					}
					if (main.messageEnabled) {
						p.sendMessage(ChatColor.AQUA + chat.getPlayer().getName() + ChatColor.RESET + " mentioned you: \"" + coloredmessage + "\"");
					}
				}
			}
		}
	}
}