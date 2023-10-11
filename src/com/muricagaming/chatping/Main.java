package com.muricagaming.chatping;

import java.time.Instant;
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
	int cooldown;
	// String prefix = ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "ChatPing" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET;
	String prefix;
	String consolePrefix = "[ChatPing] ";
	ArrayList<PlayerPreferences> players;
	public static final Sound[] SOUNDS = Sound.values();
	public ArrayList<String> soundList;
	private static final List<String> COLORS = Arrays.asList(new String[]{"a", "b", "c", "d", "e", "f", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"});
	
	public void onEnable() {
		saveDefaultConfig();
		getConfig().options().copyDefaults(true);
		saveConfig();

		// Collect defaults for new players from config
		try {
			pingsEnabled = getConfig().getBoolean("defaults.pings-enabled");
			soundEnabled = getConfig().getBoolean("defaults.sound-enabled");
			aliasesEnabled = getConfig().getBoolean("defaults.aliases-enabled");
			pingSound = Sound.valueOf(getConfig().getString("defaults.ping-sound"));
			highlightColor = ChatColor.valueOf(getConfig().getString("defaults.highlight-color"));
			cooldown = getConfig().getInt("defaults.cooldown");
			prefix = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(getConfig().getString("prefix")));
		}
		catch (Exception e)
		{
			logger.warning(consolePrefix + "Couldn't load defaults! Using internal defaults and setting them in the config.");

			pingsEnabled = true;
			soundEnabled = true;
			aliasesEnabled = false;
			pingSound = Sound.ENTITY_CAT_AMBIENT;
			highlightColor = ChatColor.GOLD;
			cooldown = 30;
			prefix = ChatColor.translateAlternateColorCodes('&', "&8[&6ChatPing&8]&r ");

			saveToConfig("defaults.pings-enabled", pingsEnabled);
			saveToConfig("defaults.sound-enabled", soundEnabled);
			saveToConfig("defaults.aliases-enabled", aliasesEnabled);
			saveToConfig("defaults.ping-sound", pingSound.toString());
			saveToConfig("defaults.highlight-color", highlightColor.name());
			saveToConfig("defaults.cooldown", cooldown);
		}

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

		// Add cooldown to player-prefs missing it
		ConfigurationSection section = getConfig().getConfigurationSection("player-prefs");
		assert section != null;
		for (String idString : section.getKeys(false)) {
			if (!getConfig().contains("player-prefs." + idString + ".cooldown", false) || !getConfig().contains("player-prefs." + idString + ".last-ping", false)) {
				saveToConfig("player-prefs." + idString + ".cooldown", cooldown);
				saveToConfig("player-prefs." + idString + ".last-ping", Date.from(Instant.now()));
				logger.info(consolePrefix + "Added missing cooldown info to config for " + getServer().getOfflinePlayer(UUID.fromString(idString)).getUniqueId() + ".");
			}
		}
		
		cl = new ChatListener(this);
		jl = new JoinListener(this);
		
		pm = getServer().getPluginManager();
		pm.registerEvents(cl, this);
		pm.registerEvents(jl, this);

		Objects.requireNonNull(getCommand("chatping")).setTabCompleter(new Tabby(this));

		loadPlayers();

		logger.info(consolePrefix + "Defaults and user preferences loaded into memory.");
		logger.info(consolePrefix + "Changes to config file while server is running will be overwritten upon shutdown.");
		logger.info(consolePrefix + "ChatPing has been enabled!");
	}
	
	public void onDisable() {
		saveToConfig("defaults.pings-enabled", pingsEnabled);
		saveToConfig("defaults.sound-enabled", soundEnabled);
		saveToConfig("defaults.aliases-enabled", aliasesEnabled);
		saveToConfig("defaults.ping-sound", pingSound.toString());
		saveToConfig("defaults.highlight-color", highlightColor.name());
		saveToConfig("defaults.cooldown", cooldown);

		savePlayers();

		logger.info(consolePrefix + "Defaults and user preferences saved to config.");
		logger.info(consolePrefix + "ChatPing has been disabled!");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("chatping") && args.length > 0) {
			if (args[0].equalsIgnoreCase("prefs") && args.length > 2 && sender instanceof Player) {
				PlayerPreferences prefs = findPlayerPrefs(((Player) sender).getUniqueId());

				// Toggle ping, sound, and aliases
				if(args[1].equalsIgnoreCase("toggle")) {
					if (args[2].equalsIgnoreCase("ping")) {
						prefs.pingsOn = !prefs.pingsOn;
						sender.sendMessage(prefix + ChatColor.GREEN + "Toggled pings to " + ChatColor.AQUA + prefs.pingsOn + ChatColor.GREEN + ".");
					}
					if (args[2].equalsIgnoreCase("sound")) {
						prefs.soundOn = !prefs.soundOn;
						sender.sendMessage(prefix + ChatColor.GREEN + "Toggled ping sound to " + ChatColor.AQUA + prefs.soundOn + ChatColor.GREEN + ".");
					}
					if (args[2].equalsIgnoreCase("aliases")) {
						prefs.aliasesOn = !prefs.aliasesOn;
						sender.sendMessage(prefix + ChatColor.GREEN + "Toggled ping by alias to " + ChatColor.AQUA + prefs.aliasesOn + ChatColor.GREEN + ".");
					}
				}
				// Set sound
				else if (args[1].equalsIgnoreCase("sound")) {
					args[2] = args[2].toUpperCase();
					boolean worked = false;

					for (Sound s : Sound.values())
						if (s.toString().equalsIgnoreCase(args[2])) {
							prefs.pingSound = Sound.valueOf(args[2]);
							sender.sendMessage(prefix + ChatColor.GREEN + "Set sound to " + ChatColor.AQUA + args[2]+ ChatColor.GREEN + ".");
							((Player) sender).playSound(((Player) sender).getLocation(), prefs.pingSound, 1, 1);
							worked = true;
							break;
						}
					if (!worked)
						sender.sendMessage(prefix + ChatColor.RED + "Invalid sound! List of sounds: https://mrca.cc/sounds");
				}
				// Set color
				else if (args[1].equalsIgnoreCase("color")) {
					boolean worked = false;
					if(COLORS.contains(String.valueOf(args[2].charAt(0)))) {
						prefs.highlightColor = ChatColor.getByChar(args[2].charAt(0));
						sender.sendMessage(prefix + ChatColor.GREEN + "Set color to " + prefs.highlightColor + args[2] + ChatColor.GREEN + ".");
					}
					else
						sender.sendMessage(prefix + ChatColor.RED + "Invalid color! List of colors: " + ChatColor.translateAlternateColorCodes('&', "&00&11&22&33&44&55&66&77&88&99&AA&BB&CC&DD&EE&FF&r"));
				}
				// Set cooldown
				else if (args[1].equalsIgnoreCase("cooldown")) {
					try {
						if(Integer.parseInt(args[2]) >= 0) {
							prefs.cooldown = Integer.parseInt(args[2]);
							sender.sendMessage(prefix + ChatColor.GREEN + "Set cooldown to " + ChatColor.AQUA + args[2] + ChatColor.GREEN + " seconds.");
						}
						else
							throw new Exception();
					}
					catch (Exception e) {
						sender.sendMessage(prefix + ChatColor.RED + "Invalid cooldown! Use an integer >= 0.");
					}
				}
				// Add or remove alias
				else if(args[1].equalsIgnoreCase("alias"))
					if(prefs.addRemoveAlias(args[2]))
						sender.sendMessage(prefix + ChatColor.GREEN + "Added " + ChatColor.AQUA + args[2] + ChatColor.GRAY +  " to your list of aliases.");
					else
						sender.sendMessage(prefix + ChatColor.RED + "Removed " + ChatColor.AQUA + args[2] + ChatColor.GRAY +  " from your list of aliases.");
			}
			else if (args[0].equalsIgnoreCase("defaults") && sender.hasPermission("chatping.admin") && args.length > 2) {

				// Toggle ping, sound, and aliases
				if(args[1].equalsIgnoreCase("toggle")) {
					if (args[2].equalsIgnoreCase("ping")) {
						pingsEnabled = !pingsEnabled;
						sender.sendMessage(prefix + ChatColor.GREEN + "Toggled pings to " + ChatColor.AQUA + pingsEnabled + ChatColor.GREEN + " for new players.");
					}
					if (args[2].equalsIgnoreCase("sound")) {
						soundEnabled = !soundEnabled;
						sender.sendMessage(prefix + ChatColor.GREEN + "Toggled ping sound to " + ChatColor.AQUA + soundEnabled + ChatColor.GREEN + " for new players.");
					}
					if (args[2].equalsIgnoreCase("aliases")) {
						aliasesEnabled = !aliasesEnabled;
						sender.sendMessage(prefix + ChatColor.GREEN + "Toggled ping by alias to " + ChatColor.AQUA + aliasesEnabled + ChatColor.GREEN + " for new players.");
					}
				}
				// Set sound
				else if (args[1].equalsIgnoreCase("sound")) {
					args[2] = args[2].toUpperCase();
					boolean worked = false;

					for (Sound s : Sound.values())
						if (s.toString().equalsIgnoreCase(args[2])) {
							pingSound = Sound.valueOf(args[2]);
							sender.sendMessage(prefix + ChatColor.GREEN + "Set sound to " + ChatColor.AQUA + args[2] + ChatColor.GREEN + " for new players.");
							if (sender instanceof Player)
								((Player) sender).playSound(((Player) sender).getLocation(), pingSound, 1, 1);
							worked = true;
							break;
						}
					if (!worked)
						sender.sendMessage(prefix + ChatColor.RED + "Invalid sound! List of sounds: https://mrca.cc/sounds");
				}

				// Set default color
				else if (args[1].equalsIgnoreCase("color")) {
					boolean worked = false;
					if(COLORS.contains(String.valueOf(args[2].charAt(0)))) {
						highlightColor = ChatColor.getByChar(args[2].charAt(0));
						sender.sendMessage(prefix + ChatColor.GREEN + "Set color to " + highlightColor + args[2] + ChatColor.GREEN + " for new players.");
					}
					else
						sender.sendMessage(prefix + ChatColor.RED + "Invalid color! List of colors: " + ChatColor.translateAlternateColorCodes('&', "&00&11&22&33&44&55&66&77&88&99&AA&BB&CC&DD&EE&FF&r"));
				}

				// Set default cooldown
				else if (args[1].equalsIgnoreCase("cooldown")) {
					try {
						if(Integer.parseInt(args[2]) >= 0) {
							cooldown = Integer.parseInt(args[2]);
							sender.sendMessage(prefix + ChatColor.GREEN + "Set cooldown to " + ChatColor.AQUA + args[2] + ChatColor.GREEN + " seconds for new players.");
						}
						else
							throw new Exception();
					}
					catch (Exception e) {
						sender.sendMessage(prefix + ChatColor.RED + "Invalid cooldown! Use an integer >= 0.");
					}
				}

			}
			else if (args[0].equalsIgnoreCase("override") && sender.hasPermission("chatping.admin") && args.length > 3) {
				try {
					PlayerPreferences prefs = findPlayerPrefs(Objects.requireNonNull(getServer().getPlayer(args[1])).getUniqueId());

					// Toggle ping, sound, and aliases
					if(args[2].equalsIgnoreCase("toggle")) {
						if (args[3].equalsIgnoreCase("ping")) {
							prefs.pingsOn = !prefs.pingsOn;
							sender.sendMessage(prefix + ChatColor.GREEN + "Toggled pings to " + ChatColor.AQUA + prefs.pingsOn + ChatColor.GREEN + " for " + ChatColor.AQUA + getServer().getPlayer(args[1]) + ChatColor.GREEN + ".");
						}
						if (args[3].equalsIgnoreCase("sound")) {
							prefs.soundOn = !prefs.soundOn;
							sender.sendMessage(prefix + ChatColor.GREEN + "Toggled ping sound to " + ChatColor.AQUA + prefs.soundOn + ChatColor.GREEN + " for " + ChatColor.AQUA + getServer().getPlayer(args[1]) + ChatColor.GREEN + ".");
						}
						if (args[3].equalsIgnoreCase("aliases")) {
							prefs.aliasesOn = !prefs.aliasesOn;
							sender.sendMessage(prefix + ChatColor.GREEN + "Toggled ping by alias to " + ChatColor.AQUA + prefs.aliasesOn + ChatColor.GREEN + " for " + ChatColor.AQUA + getServer().getPlayer(args[1]) + ChatColor.GREEN + ".");
						}
					}
					// Set sound
					else if (args[2].equalsIgnoreCase("sound")) {
						args[3] = args[3].toUpperCase();
						boolean worked = false;

						for (Sound s : Sound.values())
							if (s.toString().equalsIgnoreCase(args[3])) {
								prefs.pingSound = Sound.valueOf(args[3]);
								sender.sendMessage(prefix + ChatColor.GREEN + "Set sound to " + ChatColor.AQUA + args[2] + ChatColor.GREEN + " for " + ChatColor.AQUA + getServer().getPlayer(args[1]) + ChatColor.GREEN + ".");
								((Player) sender).playSound(((Player) sender).getLocation(), prefs.pingSound, 1, 1);
								worked = true;
								break;
							}
						if (!worked)
							sender.sendMessage(prefix + ChatColor.RED + "Invalid sound! List of sounds: https://mrca.cc/sounds");
					}

					// Set color
					else if (args[2].equalsIgnoreCase("color")) {
						boolean worked = false;
						if(COLORS.contains(String.valueOf(args[3].charAt(0)))) {
							prefs.highlightColor = ChatColor.getByChar(args[3].charAt(0));
							sender.sendMessage(prefix + ChatColor.GREEN + "Set color to " + prefs.highlightColor + args[3] + " for " + ChatColor.AQUA + getServer().getPlayer(args[1]) + ChatColor.GREEN + ".");
						}
						else
							sender.sendMessage(prefix + ChatColor.RED + "Invalid color! List of colors: " + ChatColor.translateAlternateColorCodes('&', "&00&11&22&33&44&55&66&77&88&99&AA&BB&CC&DD&EE&FF&r"));					}

					// Set cooldown
					else if (args[2].equalsIgnoreCase("cooldown")) {
						try {
							if(Integer.parseInt(args[3]) >= 0) {
								prefs.cooldown = Integer.parseInt(args[2]);
								sender.sendMessage(prefix + ChatColor.GREEN + "Set cooldown to " + ChatColor.AQUA + args[2] + ChatColor.GREEN + " seconds for " + ChatColor.AQUA + getServer().getPlayer(args[1]) + ChatColor.GREEN + ".");
							}
							else
								throw new Exception();
						}
						catch (Exception e) {
							sender.sendMessage(prefix + ChatColor.RED + "Invalid cooldown! Use an integer >= 0.");
						}
					}

					else if(args[2].equalsIgnoreCase("alias"))
						if(prefs.addRemoveAlias(args[3]))
							sender.sendMessage(prefix + ChatColor.GREEN + "Added " + ChatColor.AQUA + args[3] + ChatColor.GRAY + " to " + ChatColor.AQUA + getServer().getPlayer(prefs.playerID) + ChatColor.GRAY + "'s list of aliases.");
						else
							sender.sendMessage(prefix + ChatColor.RED + "Removed " + ChatColor.AQUA + args[3] + ChatColor.GRAY + " from " + ChatColor.AQUA + getServer().getPlayer(prefs.playerID) + ChatColor.GRAY +  "'s list of aliases.");
				}
				catch (Exception e) {
					sender.sendMessage(prefix + ChatColor.RED + "Could not find that player!");
				}
			}
		}
		else {
			sender.sendMessage(prefix + ChatColor.RED + "Invalid arguments!");
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
					getConfig().getInt("player-prefs." + idString + ".cooldown"),
					(Date) getConfig().get("player-prefs." + idString + ".last-ping"),
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
			saveToConfig("player-prefs." + pp.playerID + ".cooldown", pp.cooldown);
			saveToConfig("player-prefs." + pp.playerID + ".last-ping", pp.lastPing);
			saveToConfig("player-prefs." + pp.playerID + ".aliases", pp.aliases);
		}
	}
	
	public void saveToConfig(String key, Object s) {
		getConfig().set(key, s);
		saveConfig();
	}
}
