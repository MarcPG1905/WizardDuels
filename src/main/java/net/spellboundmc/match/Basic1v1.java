package net.spellboundmc.match;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.spellboundmc.MatchTimer;
import net.spellboundmc.ScoreboardManager;
import net.spellboundmc.Translation;
import net.spellboundmc.WizardDuels;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import static net.hectus.color.McColor.*;

/**
 * Duos2v2 is where two players compete with each other.
 */
public class Basic1v1 implements Match {
    public static final PotionEffect witherEffect = new PotionEffect(PotionEffectType.WITHER, -1, 0, true, false, false);

    public final Player team1;
    public final Player team2;
    public final MatchTimer timer;

    public Basic1v1(@NotNull Player player1, @NotNull Player player2) {
        team1 = player1;
        team2 = player2;
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

        team1.getWorld().getPlayers().forEach(p -> p.sendActionBar(Component.text(Translation.get(p.locale(), "match.withering"))));
    }

    public void lose(Player loser) {
        Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> {
            Player winner = loser == team1 ? team2 : team1;
            Locale ll = loser.locale();
            Locale wl = winner.locale();

            loser.showTitle(Title.title(Component.text(RED + Translation.get(ll, "match.lose")), Component.text(Translation.get(ll, "match.lose.text"))));
            loser.sendMessage(Component.text(RED + Translation.get(ll, "match.lose.chat")));

            winner.showTitle(Title.title(Component.text(GREEN + Translation.get(wl, "match.win")), Component.text(Translation.get(wl, "match.win.text"))));
            winner.sendMessage(Component.text(GREEN + Translation.get(ll, "match.win.chat")));
        }, 20);
    }

    public void tie() {
        Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> {
            for (Player player : team1.getWorld().getPlayers()) {
                Locale l = player.locale();
                player.showTitle(Title.title(Component.text(YELLOW + Translation.get(l, "match.tie")), Component.text(Translation.get(l, "match.tie.text"))));
                player.sendMessage(Component.text(YELLOW + Translation.get(l, "match.tie.chat")));
            }
        }, 20);
    }
}
