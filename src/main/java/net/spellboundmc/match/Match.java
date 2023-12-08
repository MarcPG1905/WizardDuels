package net.spellboundmc.match;

public interface Match {
    enum PrePhase {
        NONE,
        TIME_OF_DAY,
        MAP_SIZE,
        TOKEN_AMOUNT, // tf are tokens
        WEATHER,
        SHOP
    }

    void startPrePhase();
    void startMain();
    void stop();
    void withering();
}
