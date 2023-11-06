package net.spellboundmc;

import net.hectus.color.McColor;
import net.spellboundmc.match.Basic1v1;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static net.hectus.color.McColor.*;

public class ScoreboardManager {
    private static final org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();

    public static BukkitTask basicScoreboard;
    public static void startBasic(Basic1v1 match) {
        basicScoreboard = Bukkit.getScheduler().runTaskTimer(WizardDuels.getPlugin(WizardDuels.class), () -> {
            updateBasic(match, true);
            updateBasic(match, false);
        }, 0, 20);
    }

    public static void updateBasic(@NotNull Basic1v1 match, boolean team1) {
        Player player = (team1 ? match.team1 : match.team2);
        Locale l = player.locale();

        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("basic1v1", Criteria.DUMMY, Component.text(RED + "Wizard " + BLUE + "Duels" + RESET + "1v1"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<Score> scores = new ArrayList<>();

        scores.add(objective.getScore(GREEN + Translation.get(l, "scoreboard.hp.team") + (int) (team1 ? match.team1 : match.team2).getHealth()));
        scores.add(objective.getScore(GREEN + Translation.get(l, "scoreboard.hp.enemy") + (int) (team1 ? match.team2 : match.team1).getHealth()));
        scores.add(objective.getScore(" "));
        scores.add(objective.getScore(GOLD + Translation.get(l, "scoreboard.match_rank") + "1.0"));
        scores.add(objective.getScore("  "));
        scores.add(objective.getScore(BLUE + Translation.get(l, "scoreboard.time") + match.timer.getLeft().getOneUnitFormatted()));
        scores.add(objective.getScore("   "));
        scores.add(objective.getScore(GRAY + Translation.get(l, "scoreboard.map") + "PLACEHOLDER"));
        scores.add(objective.getScore(GRAY + Translation.get(l, "scoreboard.map_size") + "PLACEHOLDER"));

        int scoreValue = scores.size() - 1;
        for (int i = scoreValue; i >= 0; i--) {
            scores.get(i).setScore(scoreValue - i);
        }

        (team1 ? match.team1 : match.team2).setScoreboard(scoreboard);
    }

    public static void stopBasic(@NotNull Player player1, @NotNull Player player2) {
        player1.setScoreboard(manager.getNewScoreboard());
        player2.setScoreboard(manager.getNewScoreboard());
        basicScoreboard.cancel();
    }
}
