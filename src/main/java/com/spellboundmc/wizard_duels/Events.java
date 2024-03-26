package com.spellboundmc.wizard_duels;

import com.spellboundmc.wizard_duels.match.Match;
import com.spellboundmc.wizard_duels.match.MatchManager;
import com.spellboundmc.wizard_duels.turning.spells.Spell;
import com.spellboundmc.wizard_duels.turning.spells.SpellUsage;
import org.bukkit.Material;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Events implements Listener {
    @EventHandler
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        Match match = MatchManager.getMatchByPlayer(event.getPlayer());
        if (match != null) match.lose(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getMaterial() != Material.END_CRYSTAL) return;
        // Long ass code for just summoning an ender crystal
        event.getPlayer().getWorld().spawn(Objects.requireNonNull(event.getClickedBlock()).getRelative(event.getBlockFace()).getLocation(), EnderCrystal.class);
        SpellUsage.spellUse(Spell.END_CRYSTAL, event.getPlayer());
    }
}
