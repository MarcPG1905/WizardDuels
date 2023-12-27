package com.spellboundmc.wizardduels.match;

import com.marcpg.data.time.Time;
import com.marcpg.data.time.Timer;
import com.spellboundmc.wizardduels.PlayerData;
import com.spellboundmc.wizardduels.WizardDuels;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MatchTimer implements Timer {
    public static final int LOCATION_QUEUE_SIZE = 10;
    private final Match match;
    private Time time;
    private BukkitTask timer;

    public MatchTimer(Match match) {
        this.match = match;
        time = new Time(480);
    }

    @Override
    public void start() {
        timer = Bukkit.getScheduler().runTaskTimer(WizardDuels.PLUGIN, () -> {
            time.decrement();

            if (match instanceof Basic1v1 basic1v1) {
                doStuff(basic1v1.getPlayerData1());
                doStuff(basic1v1.getPlayerData2());
            }

            if (time.getAs(Time.Unit.MINUTES) == 5) match.withering();
            if (time.getAs(Time.Unit.SECONDS) == 0) match.stop();
        }, 0L, 20L);
    }

    @Override
    public void stop() {
        timer.cancel();
        time = new Time(0);
    }

    @Override
    public Time getLeft() {
        return time;
    }

    @Override
    public Time getDone() {
        return new Time(480 - time.getAs(Time.Unit.SECONDS));
    }

    private static <T> void cooldown(@NotNull Map<T, Integer> cooldowns) {
        HashMap<T, Integer> newMap = new HashMap<>();
        for (Map.Entry<T, Integer> entry : cooldowns.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue() - 1);
        }
        cooldowns.clear();
        cooldowns.putAll(newMap);
    }

    private void doStuff(@NotNull PlayerData playerData) {
        cooldown(playerData.abilityCooldowns);
        cooldown(playerData.spellCooldowns);

        if (playerData.spellCrystalActive)
            playerData.player.setHealth(playerData.player.getHealth() + 1);

        playerData.locationQueue.addFirst(playerData.player.getLocation());
        if (playerData.locationQueue.size() > LOCATION_QUEUE_SIZE)
            playerData.locationQueue.removeLast();
    }
}
