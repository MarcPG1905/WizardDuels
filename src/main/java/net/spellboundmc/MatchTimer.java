package net.spellboundmc;

import net.spellboundmc.match.Match;
import net.hectus.data.time.Time;
import net.hectus.data.time.Timer;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

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
}
