package com.muricagaming.chatping;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    Main main;

    public JoinListener(Main plugin)
    {
        main = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerJoin(PlayerJoinEvent event) {
        if(main.players.stream().map(PlayerPreferences::getUUID).noneMatch(event.getPlayer().getUniqueId()::equals)) {
            main.players.add(new PlayerPreferences(event.getPlayer().getUniqueId(), main));
            main.logger.info(main.prefix + "Added " + event.getPlayer().getName() + " to user preferences list.");
        }
    }
}
