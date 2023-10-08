package com.muricagaming.chatping;

import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;

public class PlayerPreferences {
    UUID playerID;
    Sound pingSound;
    char highlightColor;
    List<String> aliases;
    BitSet preferences;

    Main main;

    public PlayerPreferences(UUID id) {

        // Byte arrays maybe? Instead of a million booleans

        playerID = id;

        preferences = new BitSet(11);

        // 12 bits instead of 12 booleans.
        //  0 - pingsEnabled
        //  1 - cPingsEnabled
        //  2 - soundEnabled
        //  3 - cSoundEnabled
        //  4 - colorEnabled
        //  5 - cColorEnabled
        //  6 - messageEnabled
        //  7 - cMessageEnabled
        //  8 - cSound
        //  9 - cColor
        // 10 - aliasesEnabled
        // Default: 10101000001
        preferences.set(0, true);
        preferences.set(1, false);
        preferences.set(2, main.soundEnabled);
        preferences.set(3, false);
        preferences.set(4, main.colorEnabled);
        preferences.set(5, false);
        preferences.set(6, main.messageEnabled);
        preferences.set(7, false);
        preferences.set(8, false);
        preferences.set(9, false);
        preferences.set(10, true);

        pingSound = main.pingSound;
        highlightColor = main.highlightColor;

        aliases = new ArrayList<>();
    }

    public PlayerPreferences(UUID id, String prefs, char color, Sound sound) {
        playerID = id;
        preferences = new BitSet(11);

        for(int i = 0; i < 11; i++) {
            if(prefs.charAt(i) == '1')
                preferences.set(i);
        }
    }

    public void setPref(int option, Object value) {
        if(option == 0) {
            pingSound = Sound.valueOf((String) value);
            preferences.set(8, true);
        }
        else if(option == 1) {
            highlightColor = (Character) value;
            preferences.set(9, true);
        }
    }

    public void setToggle(int option, boolean newValue) {
        if (option < 8) {
            preferences.set(option, newValue);
            preferences.set(option + 1, true);
        } else if(option == 10)
            preferences.set(10, newValue);
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
}
