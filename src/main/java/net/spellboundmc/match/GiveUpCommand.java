package net.spellboundmc.match;

import me.marcpg1905.color.McFormat;
import net.spellboundmc.Translation;
import net.spellboundmc.WizardDuels;
import net.kyori.adventure.text.Component;
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
            player.sendMessage(McFormat.RED + Translation.get(Locale.ROOT, "match.error.no_match"));
            return true;
        }

        if (WizardDuels.currentMatch instanceof Basic1v1 match) {
            match.lose(player);
            player.getWorld().getPlayers().forEach(p -> p.sendMessage(Component.text(McFormat.PINK + Translation.get(p.locale(), "match.giveup", player.getName()))));
        }

        return true;
    }
}
