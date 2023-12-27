package com.spellboundmc.wizardduels.match;

public interface Match {
    enum PrePhase {
        NONE,
        TIME_OF_DAY,
        MAP_SIZE,
        TOKEN_AMOUNT,
        WEATHER,
        SHOP
    }

    void stop();
    void withering();
}
