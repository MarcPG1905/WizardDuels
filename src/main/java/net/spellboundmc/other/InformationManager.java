package net.spellboundmc.other;

import me.marcpg1905.data.time.Time;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.spellboundmc.PlayerData;
import net.spellboundmc.WizardDuels;
import net.spellboundmc.match.Basic1v1;
import net.spellboundmc.spells.Spell;
import net.spellboundmc.wands.Wand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static me.marcpg1905.color.McFormat.*;

public class InformationManager {
    private static final ScoreboardManager manager = Bukkit.getScoreboardManager();

    public static BukkitTask basicScoreboard;
    public static void startBasic(Basic1v1 match) {
        basicScoreboard = Bukkit.getScheduler().runTaskTimer(WizardDuels.PLUGIN, () -> {
            updateBasic(match, true);
            updateBasic(match, false);

            updateActionBar(match.playerData1);
            updateActionBar(match.playerData1);
        }, 0, 5);
    }

    public static void updateBasic(@NotNull Basic1v1 match, boolean team1) {
        Player player = (team1 ? match.player1 : match.player2);
        Locale l = player.locale();

        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("basic1v1", Criteria.DUMMY, Component.text(RED + "Wizard " + BLUE + "Duels" + RESET + "1v1"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<Score> scores = new ArrayList<>();

        scores.add(objective.getScore(GREEN + Translation.get(l, "scoreboard.hp.team") + (int) (team1 ? match.player1 : match.player2).getHealth()));
        scores.add(objective.getScore(GREEN + Translation.get(l, "scoreboard.hp.enemy") + (int) (team1 ? match.player2 : match.player1).getHealth()));
        scores.add(objective.getScore(" "));
        scores.add(objective.getScore(GOLD + Translation.get(l, "scoreboard.match_rank") + "1.0"));
        scores.add(objective.getScore("  "));
        scores.add(objective.getScore(BLUE + Translation.get(l, "scoreboard.time") + match.timer.getLeft().getOneUnitFormatted()));
        scores.add(objective.getScore("   "));
        scores.add(objective.getScore(GRAY + Translation.get(l, "scoreboard.map") + "PLACEHOLDER"));
        scores.add(objective.getScore(GRAY + Translation.get(l, "scoreboard.map_size") + match.mapSize));

        int scoreValue = scores.size() - 1;
        for (int i = scoreValue; i >= 0; i--) {
            scores.get(i).setScore(scoreValue - i);
        }

        (team1 ? match.player1 : match.player2).setScoreboard(scoreboard);
    }

    public static void stopBasic(@NotNull Player player1, @NotNull Player player2) {
        player1.setScoreboard(manager.getNewScoreboard());
        player2.setScoreboard(manager.getNewScoreboard());
        basicScoreboard.cancel();
    }

    public static void updateActionBar(@NotNull PlayerData playerData) {
        Material mainHandItem = playerData.player.getInventory().getItemInMainHand().getType();

        Wand wand = Wand.getWand(mainHandItem);
        if (wand != null) {
            boolean sneaking = playerData.player.isSneaking();
            var left = new Time(Math.max(0, sneaking ? playerData.abilityCooldowns.get(wand.slmb) : playerData.abilityCooldowns.get(wand.lmb)));
            var right = new Time(Math.max(0, sneaking ? playerData.abilityCooldowns.get(wand.srmb) : playerData.abilityCooldowns.get(wand.rmb)));
            playerData.player.sendActionBar(Component.text(left.getOneUnitFormatted() + " | " + right.getOneUnitFormatted(), TextColor.color(200, 200, sneaking ? 255 : 200)));
            return;
        }

        Spell spell = Spell.getSpell(mainHandItem);
        if (spell != null) {
            var cooldown = new Time(Math.max(0, playerData.spellCooldowns.get(spell.item)));
            playerData.player.sendActionBar(Component.text(cooldown + "s"));
        }
    }
}
