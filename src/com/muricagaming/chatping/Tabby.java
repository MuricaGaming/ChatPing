package com.muricagaming.chatping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class Tabby implements TabCompleter {
    List<String> completions;
    private static final String[] ADMIN_OPTIONS = { "prefs", "defaults", "override" };
    private static final String[] PLAYER_OPTIONS = { "prefs" };
    private static final String[] PREFS_OPTIONS = { "toggle", "sound", "color", "alias" };
    private static final String[] TOGGLE_OPTIONS = { "ping", "sound", "aliases" };
    private static final String[] COLORS = { "a", "b", "c", "d", "e", "f", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
    private final Main main;

    public Tabby(Main main) { this.main = main; }
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        completions = new ArrayList<>();
        List<String> onlinePlayers = new ArrayList<>();

        for(Player p: main.getServer().getOnlinePlayers())
            onlinePlayers.add(p.getName());

    if (command.getName().equalsIgnoreCase("chatping") && sender.hasPermission("chatping.admin")) {
        if (args.length == 1)
            StringUtil.copyPartialMatches(args[0], Arrays.asList(ADMIN_OPTIONS), completions);
        if (args.length == 2 && (args[0].equalsIgnoreCase("prefs") || args[0].equalsIgnoreCase("defaults")))
            StringUtil.copyPartialMatches(args[1], Arrays.asList(PREFS_OPTIONS), completions);
        if (args.length == 3 && args[1].equalsIgnoreCase("toggle"))
            StringUtil.copyPartialMatches(args[2], Arrays.asList(TOGGLE_OPTIONS), completions);
        if (args.length == 3 && args[1].equalsIgnoreCase("color"))
            StringUtil.copyPartialMatches(args[2], Arrays.asList(COLORS), completions);
        if (args.length == 2 && args[0].equalsIgnoreCase("override"))
            StringUtil.copyPartialMatches(args[1], onlinePlayers, completions);
        if (args.length == 3 && args[0].equalsIgnoreCase("override"))
            StringUtil.copyPartialMatches(args[2], Arrays.asList(PREFS_OPTIONS), completions);
        if (args.length == 4 && args[0].equalsIgnoreCase("override") && args[2].equalsIgnoreCase("toggle"))
            StringUtil.copyPartialMatches(args[3], Arrays.asList(TOGGLE_OPTIONS), completions);
    }
    else if(command.getName().equalsIgnoreCase("chatping") && sender.hasPermission("chatping.user")) {
        if(args.length == 1)
            StringUtil.copyPartialMatches(args[0], Arrays.asList(PLAYER_OPTIONS), completions);
        if (args.length == 2 && args[0].equalsIgnoreCase("prefs"))
            StringUtil.copyPartialMatches(args[1], Arrays.asList(PREFS_OPTIONS), completions);
        if (args.length == 3 && args[1].equalsIgnoreCase("toggle"))
            StringUtil.copyPartialMatches(args[2], Arrays.asList(TOGGLE_OPTIONS), completions);
        if (args.length == 3 && args[1].equalsIgnoreCase("color"))
            StringUtil.copyPartialMatches(args[2], Arrays.asList(COLORS), completions);
    }
        Collections.sort(completions);
        return completions;
    }
}
