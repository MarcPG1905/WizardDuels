package com.wizardduels.match;

import com.wizardduels.WizardDuels;
import com.wizardduels.MatchTimer;
import com.wizardduels.ScoreboardManager;
import net.hectus.color.McColor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import static net.hectus.color.McColor.*;

/**
 * Duos2v2 is where two players compete with each other.
 */
public class Basic1v1 implements Match {
    public static final PotionEffect witherEffect = new PotionEffect(PotionEffectType.WITHER, -1, 0, true, false, false);

    public final Player team1;
    public final Player team2;
    public final Audience audience;
    public final MatchTimer timer;

    public Basic1v1(@NotNull Player player1, @NotNull Player player2) {
        team1 = player1;
        team2 = player2;
        audience = player1.getWorld();
        timer = new MatchTimer(this);

        startMain();
    }

    @Override
    public void startMain() {
        timer.start();
        ScoreboardManager.startBasic(this);
    }

    @Override
    public void stop() {
        timer.stop();
        ScoreboardManager.stopBasic(team1, team2);
    }

    @Override
    public void withering() {
        team1.addPotionEffect(witherEffect);
        team2.addPotionEffect(witherEffect);

        audience.sendActionBar(Component.text(BLACK + "5min left, withering is now applied"));
    }

    public void lose(Player loser) {
        Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> {
            Player winner = loser == team1 ? team2 : team1;

            loser.showTitle(Title.title(Component.text(RED + "You lost!"), Component.text("Better luck next time!")));
            winner.showTitle(Title.title(Component.text(McColor.GREEN + "You won!"), Component.text("Keep it up!")));

            loser.sendMessage(Component.text(RED + "//=========================================================\\\\"));
            loser.sendMessage(Component.text(RED + "|| You sadly lost this match. You'll get better next time! ||"));
            loser.sendMessage(Component.text(RED + "\\\\=========================================================//"));

            winner.sendMessage(Component.text(GREEN + "//=======================================\\\\"));
            winner.sendMessage(Component.text(GREEN + "|| You won this match. Nice, keep it up! ||"));
            winner.sendMessage(Component.text(GREEN + "\\\\=======================================//"));
        }, 20);
    }

    public void tie() {
        Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> {
            Audience audience = team1.getWorld();
            audience.showTitle(Title.title(Component.text(McColor.YELLOW + "Tie!"), Component.text("It's a tie!")));

            audience.sendMessage(Component.text(McColor.GREEN + "//============================\\\\"));
            audience.sendMessage(Component.text(McColor.GREEN + "|| This match ended in a tie! ||"));
            audience.sendMessage(Component.text(McColor.GREEN + "\\\\============================//"));
        }, 20);
    }
}
