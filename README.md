# ChatPing

![51bf9e0ba88cb8e36efd0bdb0241528841f196bf](https://github.com/MuricaGaming/ChatPing/assets/26010345/6cda8549-b306-40d4-8bad-c877941aa683)

This is a fairly straightforward plugin - when a player's username is mentioned in chat, it will be highlighted and a sound will be played.

## Command arguments for /chatping (alias: cping)

    prefs - personal preferences
        toggle - toggle pinging, just the sound, or ping by alias
        sound <Sound.SOUND> - set ping sound
        color <color code> - set ping highlight color
        cooldown <int> - set the sound cooldown, in seconds
        alias <String> - add/remove an alias
    defaults
        Same as prefs, but this sets the defaults for new players
    override <player>
        Same as prefs, but with a player argument to force a certain setting for a certain player

## Permissions

    chatping.user (default: op)
        Ability to receive pings and adjust personal prefs
    chatping.admin (default: op)
        Access to defaults and override arguments

Aliases (suggestion by llaurin!)

    Add your own extra names to be pinged with
    Command will add the alias, or remove it if it's already present
    Case-insensitive


Official server: play.murica.gg
