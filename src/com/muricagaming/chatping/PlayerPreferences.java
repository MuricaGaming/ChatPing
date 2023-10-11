package com.muricagaming.chatping;

import org.bukkit.ChatColor;
import org.bukkit.Sound;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.util.Date.from;

public class PlayerPreferences {
    UUID playerID;
    Sound pingSound;
    ChatColor highlightColor;
    List<String> aliases;
    boolean pingsOn;
    boolean soundOn;
    boolean aliasesOn;
    int cooldown;
    Date lastPing;

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
        cooldown = main.cooldown;
        lastPing = from(Instant.now());
        aliases = new ArrayList<>();
    }

    public PlayerPreferences(UUID id, boolean pE, boolean sE, boolean aE, Sound ping, ChatColor highlight, int cd, Date lp, List<String> names, Main plugin) {
        // Initialize player preferences from config

        playerID = id;
        main = plugin;

        pingsOn = pE; // pingsEnabled
        soundOn = sE; // soundEnabled
        aliasesOn = aE;
        pingSound = ping;
        highlightColor = highlight;
        cooldown = cd;
        lastPing = lp;
        aliases = names;
    }

    public boolean addRemoveAlias(String a) {
        if (!aliases.contains(a.toLowerCase())) {
            aliases.add(a.toLowerCase());
            return true;
        } else {
            aliases.remove(a.toLowerCase());
            return false;
        }
    }

    public UUID getUUID() {
        return playerID;
    }
}
