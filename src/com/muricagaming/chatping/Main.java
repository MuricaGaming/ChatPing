package com.muricagaming.chatping;

import java.util.*;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	public final Logger logger = Logger.getLogger("Minecraft");
	private PluginManager pm;
	ChatListener cl;
	JoinListener jl;
	boolean pingsEnabled;
	boolean soundEnabled;
	boolean aliasesEnabled;
	Sound pingSound;
	ChatColor highlightColor;
	// String prefix = ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "ChatPing" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET;
	String prefix;
	ArrayList<PlayerPreferences> players;
	public static final Sound[] SOUNDS = Sound.values();
	public ArrayList<String> soundList;
	
	public void onEnable() {
		saveDefaultConfig();
		getConfig().options().copyDefaults(true);
		saveConfig();

		// Collect defaults for new players from config
		pingsEnabled = getConfig().getBoolean("defaults.pings-enabled");
		soundEnabled = getConfig().getBoolean("defaults.sound-enabled");
		aliasesEnabled = getConfig().getBoolean("defaults.aliases-enabled");
		pingSound = Sound.valueOf(getConfig().getString("defaults.ping-sound"));
		highlightColor = ChatColor.valueOf(getConfig().getString("defaults.highlight-color"));
		prefix = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(getConfig().getString("prefix")));

		players = new ArrayList<>();
		soundList = new ArrayList<>();

		for(Sound s: SOUNDS)
			soundList.add(s.toString());

		// Remove old config options
		getConfig().set("sound", null);
		getConfig().set("color", null);
		getConfig().set("soundEnabled", null);
		getConfig().set("colorEnabled", null);
		getConfig().set("messageEnabled", null);
		
		cl = new ChatListener(this);
		jl = new JoinListener(this);
		
		pm = getServer().getPluginManager();
		pm.registerEvents(cl, this);
		pm.registerEvents(jl, this);

		Objects.requireNonNull(getCommand("chatping")).setTabCompleter(new Tabby(this));

		loadPlayers();
		
		logger.info(prefix + "ChatPing has been enabled!");
	}
	
	public void onDisable() {
		saveToConfig("defaults.pings-enabled", pingsEnabled);
		saveToConfig("defaults.sound-enabled", soundEnabled);
		saveToConfig("defaults.aliases-enabled", aliasesEnabled);
		saveToConfig("defaults.ping-sound", pingSound.toString());
		saveToConfig("defaults.highlight-color", highlightColor.name());

		savePlayers();

		logger.info(prefix + "Defaults and preferences saved to config.");
		logger.info(prefix + "ChatPing has been disabled!");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("chatping") && args.length > 0) {
			if (args[0].equalsIgnoreCase("sound") && args.length > 1) {
				args[1] = args[1].toUpperCase();
				boolean worked = false;
				
				for (Sound s : Sound.values())
					if (s.toString().equalsIgnoreCase(args[1])) {
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
		else {
			sender.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "ChatPing" + ChatColor.DARK_GRAY + "] " + ChatColor.RED + "Invalid arguments!");
		}
		return true;
	}

	public void setPreference() {

	}

	public PlayerPreferences findPlayerPrefs(UUID id) {
		for(PlayerPreferences pp: players)
		{
			if(pp.getUUID().equals(id))
				return pp;
		}
		return null;
	}

	private void loadPlayers() {
		ConfigurationSection section = getConfig().getConfigurationSection("player-prefs");

		assert section != null;
		for (String idString : section.getKeys(false)) {

			// Add player's preferences to the list
			if(idString != null)
				players.add(new PlayerPreferences(
					UUID.fromString(idString),
					getConfig().getBoolean("player-prefs." + idString + ".pings-enabled"),
					getConfig().getBoolean("player-prefs." + idString + ".sound-enabled"),
					getConfig().getBoolean("player-prefs." + idString + ".aliases-enabled"),
					Sound.valueOf(getConfig().getString("player-prefs." + idString + ".ping-sound")),
					ChatColor.valueOf(getConfig().getString("player-prefs." + idString + ".highlight-color")),
					getConfig().getStringList("player-prefs." + idString + ".aliases"),
					this));
		}
	}

	private void savePlayers() {
		for (PlayerPreferences pp: players) {
			saveToConfig("player-prefs." + pp.playerID + ".pings-enabled", pp.pingsOn);
			saveToConfig("player-prefs." + pp.playerID + ".sound-enabled", pp.soundOn);
			saveToConfig("player-prefs." + pp.playerID + ".aliases-enabled", pp.aliasesOn);
			saveToConfig("player-prefs." + pp.playerID + ".ping-sound", pp.pingSound.toString());
			saveToConfig("player-prefs." + pp.playerID + ".highlight-color", pp.highlightColor.name());
			saveToConfig("player-prefs." + pp.playerID + ".aliases", pp.aliases);
		}
	}
	
	public void saveToConfig(String key, Object s) {
		getConfig().set(key, s);
		saveConfig();
	}
}
