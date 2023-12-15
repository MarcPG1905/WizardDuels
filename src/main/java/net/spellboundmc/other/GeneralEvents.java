package net.spellboundmc.other;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.spellboundmc.Config;
import net.spellboundmc.WizardDuels;
import net.spellboundmc.match.Basic1v1;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class GeneralEvents implements Listener {
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (WizardDuels.currentMatch == null) return;
        ((Basic1v1) WizardDuels.currentMatch).lose(event.getEntity());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if ((!Config.ALLOW_JOIN || Config.EXPERIMENTAL) && !event.getPlayer().isOp()) {
            Bukkit.getScheduler().runTaskLater(WizardDuels.PLUGIN, () -> event.getPlayer().kick(Component.text("This match cannot be joined, due to maintenance, testing or it being a private round.", TextColor.color(255, 0, 0))), 2);
        }
    }
}
