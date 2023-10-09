package com.muricagaming.chatping;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerPreferences {
    UUID playerID;
    Sound pingSound;
    ChatColor highlightColor;
    List<String> aliases;
    boolean pingsOn;
    boolean soundOn;
    boolean aliasesOn;

    Main main;

    public PlayerPreferences(UUID id, Main plugin) {
        // Initialize current running defaults for new player

        playerID = id;
        main = plugin;

        pingsOn = main.pingsEnabled; // pingsEnabled
        soundOn = main.soundEnabled; // soundEnabled
        aliasesOn = main.aliasesEnabled; // aliasesEnabled
        pingSound = main.pingSound;
        highlightColor = main.highlightColor;
        aliases = new ArrayList<>();
    }

    public PlayerPreferences(UUID id, boolean pE, boolean sE, boolean aE, Sound ping, ChatColor highlight, List<String> names, Main plugin) {
        // Initialize player preferences from config

        playerID = id;
        main = plugin;

        pingsOn = pE; // pingsEnabled
        soundOn = sE; // soundEnabled
        aliasesOn = aE;
        pingSound = ping;
        highlightColor = highlight;
        aliases = names;
    }

    public void addAlias(String a) {
        aliases.add(a);
    }

    public boolean removeAlias(String a) {
        if (aliases.contains(a)) {
            aliases.remove(a);
            return true;
        }
        else
            return false;
    }

    public UUID getUUID() {
        return playerID;
    }
}
