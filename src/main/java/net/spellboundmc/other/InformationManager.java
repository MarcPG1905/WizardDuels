package net.spellboundmc.other;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.spellboundmc.PlayerData;
import net.spellboundmc.WizardDuels;
import net.spellboundmc.match.Basic1v1;
import net.spellboundmc.wands.Wand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static me.marcpg1905.color.McFormat.*;

public class InformationManager {
    private static final ScoreboardManager manager = Bukkit.getScoreboardManager();

    public static BukkitTask basicScoreboard;
    public static void startBasic(Basic1v1 match) {
        basicScoreboard = Bukkit.getScheduler().runTaskTimer(WizardDuels.getPlugin(WizardDuels.class), () -> {
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
        Wand wand = Wand.NONE;
        for (Wand tempWand : List.of(Wand.values())) {
            if (tempWand.item == mainHandItem) {
                wand = tempWand;
                break;
            }
        }

        if (wand != Wand.NONE) {
            boolean sneak = playerData.player.isSneaking();
            int lmbCooldown = Math.max(0, playerData.abilityCooldowns.get(wand.abilities[sneak ? 2 : 0]));
            System.out.println(lmbCooldown);
            int rmbCooldown = Math.max(0, playerData.abilityCooldowns.get(wand.abilities[sneak ? 3 : 1]));
            System.out.println(rmbCooldown);
            playerData.player.sendActionBar(Component.text(lmbCooldown + "s | " + rmbCooldown + "s", sneak ? TextColor.color(150, 180, 255) : TextColor.color(255, 255, 255)));
        } else {
            if (!playerData.spellCooldowns.containsKey(mainHandItem)) return;

            Integer cooldown = playerData.spellCooldowns.get(mainHandItem);
            System.out.println(cooldown);

            if (cooldown <= 0){
                playerData.player.sendActionBar(Component.text(cooldown + "s", TextColor.color(255, 200, 200)));
            } else {
                playerData.player.sendActionBar(Component.text("Ready!", TextColor.color(200, 255, 200)));
            }
        }
    }
}
