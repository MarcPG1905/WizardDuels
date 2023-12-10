package net.spellboundmc.other;

import net.spellboundmc.WizardDuels;
import net.spellboundmc.match.Basic1v1;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class GeneralEvents implements Listener {
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (WizardDuels.currentMatch == null) return;
        ((Basic1v1) WizardDuels.currentMatch).lose(event.getEntity());
    }
}
