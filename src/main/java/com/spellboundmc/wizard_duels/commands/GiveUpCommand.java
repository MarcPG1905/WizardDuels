package com.spellboundmc.wizard_duels.commands;

import com.marcpg.lang.Translation;
import com.spellboundmc.wizard_duels.match.Match;
import com.spellboundmc.wizard_duels.match.MatchManager;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GiveUpCommand implements CommandExecutor  {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;

        Match match = MatchManager.getMatchByPlayer(player);
        if (match == null) {
            player.sendMessage(Translation.component(player.locale(), "match.error.no_match").color(NamedTextColor.RED));
            return true;
        }

        match.lose(player);
        player.getWorld().getPlayers().forEach(p -> p.sendMessage(Translation.component(p.locale(), "match.give-up", player.getName()).color(NamedTextColor.LIGHT_PURPLE)));
        return true;
    }
}
