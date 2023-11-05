package com.wizardduels.match;

import com.wizardduels.WizardDuels;
import net.hectus.color.McColor;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GiveUpCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (WizardDuels.currentMatch == null) {
            sender.sendMessage(McColor.GOLD + "There is currently no match running!");
            return true;
        }

        if (WizardDuels.currentMatch instanceof Basic1v1 match) {
            match.lose((Player) sender);
            ((Player) sender).getWorld().sendMessage(Component.text(McColor.PINK + sender.getName() + " gave up!"));
        }

        return true;
    }
}
