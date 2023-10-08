package com.muricagaming.chatping;

import java.util.List;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin
{
	public final Logger logger = Logger.getLogger("Minecraft");
	private PluginManager pm;
	ChatListener cl;
	Sound pingSound;
	char highlightColor;
	boolean soundEnabled;
	boolean colorEnabled;
	boolean messageEnabled;
	String prefix = ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "ChatPing" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET;
	List<PlayerPreferences> players;
	
	public void onEnable()
	{
		saveDefaultConfig();
		getConfig().options().copyDefaults(true);
		saveConfig();

		// Collect defaults for new players from config
		pingSound = Sound.valueOf(getConfig().getString("ping-sound"));
		highlightColor = getConfig().getString("highlight-color").charAt(0);
		soundEnabled = getConfig().getBoolean("sound-enabled");
		colorEnabled = getConfig().getBoolean("color-enabled");
		messageEnabled = getConfig().getBoolean("message-enabled");

		// Load custom preferences for players from config
		for (Entry)

		// Remove old config options
		getConfig().set("sound", null);
		getConfig().set("color", null);
		getConfig().set("soundEnabled", null);
		getConfig().set("colorEnabled", null);
		getConfig().set("messageEnabled", null);
		
		cl = new ChatListener(this);
		
		pm = getServer().getPluginManager();
		pm.registerEvents(cl, this);

		getCommand("chatping").setTabCompleter(new Tabby(this));
		
		logger.info(prefix + "ChatPing has been enabled!");
	}
	
	public void onDisable()
	{
		saveToConfig("ping-sound", pingSound.toString());
		saveToConfig("highlight-color", highlightColor);
		saveToConfig("sound-enabled", soundEnabled);
		saveToConfig("color-enabled", colorEnabled);
		saveToConfig("message-enabled", messageEnabled);

		logger.info(prefix + "ChatPing has been disabled!");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase("chatping") && args.length > 0)
		{
			if (args[0].equalsIgnoreCase("sound") && args.length > 1)
			{
				args[1] = args[1].toUpperCase();
				boolean worked = false;
				
				for (Sound s : Sound.values())
					if (s.toString().equalsIgnoreCase(args[1]))
					{
						saveToConfig("sound", args[1]);
						sender.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "ChatPing" + ChatColor.DARK_GRAY + "] " + ChatColor.GREEN + "Set sound to " + args[1]);
						if (sender instanceof Player)
							((Player) sender).playSound(((Player) sender).getLocation(), Sound.valueOf(args[1]), 1, 1);
						worked = true;
						break;
					}
				if (!worked)
					sender.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "ChatPing" + ChatColor.DARK_GRAY + "] " + ChatColor.RED + "Invalid sound! List of sounds: http://bit.ly/2aMdrxI");
			}
		}
		else
		{
			sender.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "ChatPing" + ChatColor.DARK_GRAY + "] " + ChatColor.RED + "Invalid arguments!");
		}
		return true;
	}
	
	public void saveToConfig(String key, Object s)
	{
		getConfig().set(key, s);
		saveConfig();
	}

	public void addPlayer() {

	}
}
