package net.spellboundmc;

import me.marcpg1905.data.time.Time;
import me.marcpg1905.data.time.Timer;
import net.spellboundmc.match.Basic1v1;
import net.spellboundmc.match.Match;
import net.spellboundmc.wands.Ability;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class MatchTimer implements Timer {
    private final Match match;
    private Time time;
    private BukkitTask timer;

    public MatchTimer(Match match) {
        this.match = match;
        time = new Time(480);
    }

    @Override
    public void start() {
        timer = Bukkit.getScheduler().runTaskTimer(WizardDuels.getPlugin(WizardDuels.class), () -> {
            time.decrement();

            if (match instanceof Basic1v1 basic1v1) {
                cooldown(basic1v1.playerData1.abilityCooldowns);
                cooldown(basic1v1.playerData1.spellCooldowns);

                cooldown(basic1v1.playerData2.abilityCooldowns);
                cooldown(basic1v1.playerData2.spellCooldowns);

                if (basic1v1.playerData1.spellCrystalActive) {
                    basic1v1.player1.setHealth(basic1v1.player1.getHealth() + 1);
                }
                if (basic1v1.playerData2.spellCrystalActive) {
                    basic1v1.player2.setHealth(basic1v1.player2.getHealth() + 1);
                }
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

    public static <T> void cooldown(Map<T, Integer> cooldowns) {
        HashMap<T, Integer> newMap = new HashMap<>();
        for (Map.Entry<T, Integer> entry : cooldowns.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue() - 1);
        }
        cooldowns.clear();
        cooldowns.putAll(newMap);
    }
}
