package com.muricagaming.chatping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class Tabby implements TabCompleter {
    List<String> completions;
    private static final String[] ADMIN_OPTIONS = { "sound", "color", "toggle", "alias"};
    private static final String[] PLAYER_OPTIONS = { "sound", "color", "toggle", "alias"};
    private static final String[] OPTIONS_TOGGLE = { "sound", "color", "message", "alias"};
    private static final Sound[] SOUNDS = Sound.values();
    private List<String> onlinePlayers;
    private List<String> soundList;
    private Main main;

    public Tabby(Main main) {this.main = main;}
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        completions = new ArrayList<>();
        onlinePlayers = new ArrayList<>();
        soundList = new ArrayList<>();

        for(Player p: main.getServer().getOnlinePlayers())
            onlinePlayers.add(p.getName());

        for(Sound s: SOUNDS)
            soundList.add(s.toString());

        if(args.length == 1 && command.getName().equalsIgnoreCase("chatping"))
            StringUtil.copyPartialMatches(args[0], Arrays.asList(OPTIONS), completions);
        else if(args.length == 1)
            StringUtil.copyPartialMatches(args[0], Arrays.asList(PLAYER_COMMANDS), completions);
        else if (args.length == 2) {
            if(args[0].equalsIgnoreCase("adjust") || args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("check"))
                StringUtil.copyPartialMatches(args[1], onlinePlayers, completions);
        }

        Collections.sort(completions);
        return completions;
    }
}
