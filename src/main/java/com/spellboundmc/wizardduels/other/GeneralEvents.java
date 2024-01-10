package com.spellboundmc.wizardduels.other;

import com.spellboundmc.wizardduels.WizardDuels;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class GeneralEvents implements Listener {
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (WizardDuels.currentMatch == null) return;
        WizardDuels.currentMatch.lose(event.getEntity());
    }
}
