package com.spellboundmc.wizard_duels.match;

import com.marcpg.data.time.Time;
import com.marcpg.data.time.Timer;
import com.marcpg.lang.Translation;
import com.spellboundmc.wizard_duels.PlayerData;
import com.spellboundmc.wizard_duels.WizardDuels;
import com.spellboundmc.wizard_duels.turning.spells.Spell;
import com.spellboundmc.wizard_duels.turning.wands.Wand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.marcpg.color.McFormat.*;

public class WizardTicks extends Timer {
    private static final ScoreboardManager MANAGER = Bukkit.getScoreboardManager();
    private static final int LAST_LOCATIONS_SIZE = 10;

    private final Match match;
    private BukkitTask task;

    public WizardTicks(Time time, Match match) {
        super(time);
        this.match = match;
    }

    @Override
    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(WizardDuels.PLUGIN, () -> {
            timer.decrement();

            match.players.both(o -> update((PlayerData) o));

            if (timer.get() == 300) match.withering();
            if (timer.get() == 0) match.stop();
        }, 0, 20);
    }

    @Override
    public void stop() {
        match.players.left().player.setScoreboard(MANAGER.getNewScoreboard());
        match.players.right().player.setScoreboard(MANAGER.getNewScoreboard());
        task.cancel();
        timer.decrement(timer.get());
    }

    private void update(@NotNull PlayerData playerData) {
        cooldown(playerData.abilityCooldowns);
        cooldown(playerData.spellCooldowns);

        updateBasic(match, playerData);
        // updateActionBar(playerData);

        if (playerData.matchModifiers.contains("spell_crystal"))
            playerData.player.setHealth(playerData.player.getHealth() + 1);

        playerData.lastLocations.addFirst(playerData.player.getLocation());
        if (playerData.lastLocations.size() > LAST_LOCATIONS_SIZE)
            playerData.lastLocations.removeLast();
    }

    private static <T> void cooldown(@NotNull Map<T, Integer> cooldowns) {
        HashMap<T, Integer> newMap = new HashMap<>();

        for (Map.Entry<T, Integer> entry : cooldowns.entrySet()) {
            if (entry.getValue() > 0) newMap.put(entry.getKey(), entry.getValue() - 1);
        }
        cooldowns.clear();
        cooldowns.putAll(newMap);
    }

    public static void updateBasic(@NotNull Match match, @NotNull PlayerData team) {
        Player player = team.player;
        Locale l = player.locale();

        PlayerData opponentData = match.getOpponentData(player);

        Scoreboard scoreboard = MANAGER.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("match", Criteria.DUMMY, Component.text(RED + "Wizard " + BLUE + "Duels " + RESET + "1v1"));
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
                objective.getScore(GRAY + Translation.string(l, "scoreboard.map_size") + match.size.getTranslated(l))
        );

        for (int i = scores.size() - 1; i >= 0; i--) {
            scores.get(i).setScore(scores.size() - 1 - i);
        }

        player.setScoreboard(scoreboard);
    }

    public static void updateActionBar(@NotNull PlayerData playerData) {
        Material mainHandItem = playerData.player.getInventory().getItemInMainHand().getType();

        Wand wand = Wand.getWand(mainHandItem);
        if (wand != null) {
            boolean sneaking = playerData.player.isSneaking();
            Time left = new Time(Math.max(0, sneaking ? playerData.abilityCooldowns.get(wand.slmb) : playerData.abilityCooldowns.get(wand.lmb)));
            Time right = new Time(Math.max(0, sneaking ? playerData.abilityCooldowns.get(wand.srmb) : playerData.abilityCooldowns.get(wand.rmb)));
            playerData.player.sendActionBar(Component.text(left.getOneUnitFormatted() + " | " + right.getOneUnitFormatted(), TextColor.color(200, 200, sneaking ? 255 : 200)));
            return;
        }

        Spell spell = Spell.getSpellShop(mainHandItem);
        if (spell != null) {
            Time cooldown = new Time(Math.max(0, playerData.spellCooldowns.get(spell)));
            playerData.player.sendActionBar(Component.text(cooldown.getOneUnitFormatted() + "s"));
        }
    }
}
