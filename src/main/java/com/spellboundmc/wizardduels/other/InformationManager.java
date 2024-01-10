package com.spellboundmc.wizardduels.other;

import com.marcpg.data.time.Time;
import com.spellboundmc.wizardduels.PlayerData;
import com.spellboundmc.wizardduels.WizardDuels;
import com.spellboundmc.wizardduels.match.Basic1v1;
import com.spellboundmc.wizardduels.turn.spells.Spell;
import com.spellboundmc.wizardduels.turn.wands.Wand;
import net.hectus.Translation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;

import java.util.List;
import java.util.Locale;

import static com.marcpg.color.McFormat.*;

public class InformationManager {
    private static final ScoreboardManager manager = Bukkit.getScoreboardManager();

    public static BukkitTask basicScoreboard;

    public static void startBasic(Basic1v1 match) {
        basicScoreboard = Bukkit.getScheduler().runTaskTimer(WizardDuels.PLUGIN, () -> {
            updateBasic(match, true);
            updateBasic(match, false);

            updateActionBar(match.getPlayerData1());
            updateActionBar(match.getPlayerData2());
        }, 0, 20);
    }

    public static void updateBasic(@NotNull Basic1v1 match, boolean team1) {
        Player player = (team1 ? match.getPlayer1() : match.getPlayer2());
        Locale l = player.locale();

        PlayerData opponentData = match.getOpponentData(player);

        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("basic1v1", Criteria.DUMMY, Component.text(RED + "Wizard " + BLUE + "Duels " + RESET + "1v1"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<Score> scores = List.of(
                objective.getScore(GREEN + Translation.string(l, "scoreboard.hp.team") + (int) player.getHealth()),
                objective.getScore(GREEN + Translation.string(l, "scoreboard.hp.enemy") + (int) opponentData.player.getHealth()),
                objective.getScore(" "),
                objective.getScore(GOLD + Translation.string(l, "scoreboard.match_rank") + "1.0"),
                objective.getScore("  "),
                objective.getScore(DARK_RED + Translation.string(l, "scoreboard.enemy_wand") + Translation.string(l, opponentData.selectedWand.translationKey())),
                objective.getScore("   "),
                objective.getScore(BLUE + Translation.string(l, "scoreboard.time") + match.timer.getLeft().getOneUnitFormatted()),
                objective.getScore("    "),
                objective.getScore(GRAY + Translation.string(l, "scoreboard.map") + "PLACEHOLDER"),
                objective.getScore(GRAY + Translation.string(l, "scoreboard.map_size") + match.mapSize.translate(l))
        );

        for (int i = scores.size() - 1; i >= 0; i--) {
            scores.get(i).setScore(scores.size() - 1 - i);
        }

        player.setScoreboard(scoreboard);
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

        Spell spell = Spell.getSpellShop(mainHandItem);
        if (spell != null) {
            var cooldown = new Time(Math.max(0, playerData.spellCooldowns.get(spell)));
            playerData.player.sendActionBar(Component.text(cooldown.getOneUnitFormatted() + "s"));
        }
    }
}
