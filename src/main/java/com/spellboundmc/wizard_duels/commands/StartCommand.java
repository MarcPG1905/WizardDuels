package com.spellboundmc.wizard_duels.commands;

import com.marcpg.text.Completer;
import com.spellboundmc.wizard_duels.match.Match;
import com.spellboundmc.wizard_duels.match.MatchManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StartCommand implements TabExecutor {
    private static final List<String> MAP_SIZES = List.of("8", "16", "32", "64", "128");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 3) {
            Player player1 = Bukkit.getPlayer(args[1]);
            Player player2 = Bukkit.getPlayer(args[2]);
            if (player1 == null || player2 == null) {
                sender.sendMessage(Component.text("One or both of the specified players couldn't be found!", NamedTextColor.RED));
                return true;
            }
            Match match = new Match(player1, player2);
            MatchManager.MATCHES.add(match);
            match.prePhase();
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) return Completer.startComplete(args[0], MAP_SIZES);

        if (args.length == 0 || args.length > 3) return List.of();

        List<String> names = new ArrayList<>(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
        if (args.length == 3)
            names.remove(args[1]);

        return names;
    }
}
