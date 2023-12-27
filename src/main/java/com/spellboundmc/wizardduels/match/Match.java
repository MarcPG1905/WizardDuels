package com.spellboundmc.wizardduels.match;

import com.spellboundmc.wizardduels.other.Translation;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public interface Match {
    enum MapSize {
        MINI(8), SMALL(16), NORMAL(32), BIG(64), HUGE(128);

        public final int size;

        MapSize(int size) {
            this.size = size;
        }

        public @NotNull String translate(Locale locale) {
            return Translation.get(locale, "scoreboard.map_size." + name().toLowerCase()) + " ("+size+"x"+size+")";
        }
    }

    enum PrePhase {
        NONE,
        TIME_OF_DAY,
        MAP_SIZE,
        TOKEN_AMOUNT,
        WEATHER,
        SHOP
    }

    void startMain();
    void stop();
    void withering();
    void tie();
    void lose(Player loser);
    void reset();
}
