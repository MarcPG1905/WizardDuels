package com.spellboundmc.wizardduels.match;

import com.spellboundmc.wizardduels.WizardDuels;
import com.spellboundmc.wizardduels.other.Translation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class GiveUpCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        if (WizardDuels.currentMatch == null) {
            player.sendMessage(Component.text(Translation.get(Locale.ROOT, "match.error.no_match"), TextColor.color(255, 0, 0)));
            return true;
        }

        if (WizardDuels.currentMatch instanceof Basic1v1 match) {
            match.lose(player);
            player.getWorld().getPlayers().forEach(p -> p.sendMessage(Component.text(Translation.get(p.locale(), "match.give-up", player.getName()), TextColor.color(255, 85, 255))));
        }

        return true;
    }
}
