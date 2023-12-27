package com.spellboundmc.wizardduels.match;

import com.marcpg.text.Completer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import com.spellboundmc.wizardduels.Config;
import com.spellboundmc.wizardduels.WizardDuels;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StartCommand implements CommandExecutor, TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You cannot use /match from the console.");
            return true;
        }

        if (args.length == 2) {
            Player player1 = (Player) sender;
            Player player2 = Bukkit.getPlayer(args[1]);
            if (player2 == null) {
                player1.sendMessage(Component.text("The player " + args[1] + " does not exist!", TextColor.color(255, 0, 0)));
                return true;
            }

            if (Config.DISALLOWED_TYPES.contains(args[0])) {
                player1.sendMessage(Component.text(args[0] + " games are disabled in this match!", TextColor.color(255, 0, 0)));
                return true;
            }

            if (args[0].equals("1v1")) {
                WizardDuels.currentMatch = new Basic1v1(player1, player2);
                sender.sendMessage(Component.text("Starting a 1v1...", TextColor.color(0, 255, 0)));
            } else {
                sender.sendMessage(Component.text("Invalid match type. Valid one for now are: 1v1", TextColor.color(255, 0, 0)));
            }

            return true;
        }

        return false;
    }

    final List<String> matchTypes = List.of("1v1", "2v2", "chaos");
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> strings = new ArrayList<>(matchTypes);
            strings.removeAll(Config.DISALLOWED_TYPES);
            return Completer.startComplete(args[0], strings);
        } else if (args.length == 2) {
            if (sender instanceof Player player) {
                List<String> playerNames = new ArrayList<>();
                player.getWorld().getPlayers().forEach(p -> playerNames.add(p.getName()));
                playerNames.remove(player.getName());
                return Completer.startComplete(args[1], playerNames);
            }
        }
        return List.of();
    }
}
