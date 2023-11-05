package net.spellboundmc.match;

import net.spellboundmc.WizardDuels;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MatchCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You cannot use /match from the console.");
            return true;
        }

        if (args.length == 2) {
            Player player1 = (Player) sender;
            Player player2 = Bukkit.getPlayer(args[1]);

            if (args[0].equals("1v1")) {
                WizardDuels.currentMatch = new Basic1v1(player1, player2);
                sender.sendMessage("Starting a 1v1...");
            } else {
                sender.sendMessage("Invalid match type. Valid one for now are: 1v1");
            }

            return true;
        }

        return false;
    }
}
