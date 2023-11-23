package net.spellboundmc;

import net.hectus.color.McColor;
import net.hectus.data.time.Time;
import net.hectus.data.time.Timer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.spellboundmc.match.Basic1v1;
import net.spellboundmc.match.Match;
import net.spellboundmc.wands.Ability;
import net.spellboundmc.wands.Wand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

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
                cooldown(basic1v1.cooldowns1);
                cooldown(basic1v1.cooldowns2);
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

    public static void cooldown(Map<Ability, Integer> cooldowns) {
        HashMap<Ability, Integer> newMap = new HashMap<>();
        for (Map.Entry<Ability, Integer> entry : cooldowns.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue() - 1);
        }
        cooldowns.clear();
        cooldowns.putAll(newMap);
    }
}
