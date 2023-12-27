package com.spellboundmc.wizardduels.turn.spells;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.spellboundmc.wizardduels.PlayerData;
import com.spellboundmc.wizardduels.WizardDuels;
import com.spellboundmc.wizardduels.match.Basic1v1;
import com.spellboundmc.wizardduels.turn.wands.WandUsage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class SpellEvents implements Listener {
    @EventHandler
    public void onBlockPlace(@NotNull BlockPlaceEvent event) {
        if (WizardDuels.currentMatch == null) return;

        event.setCancelled(true);

        Spell spell = Spell.getSpellPlaced(event.getBlock().getType());
        if (spell == null) return;

        if (SpellUsage.spellUse(spell, event.getPlayer())) {
            Block block = event.getBlockPlaced();
            block.setType(block.getType());
        }
    }

    @EventHandler
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        if (WizardDuels.currentMatch == null) return;

        Basic1v1 match = (Basic1v1) WizardDuels.currentMatch;
        PlayerData playerData = match.getPlayerData(event.getPlayer());
        PlayerData opponentData = match.getOpponentData(event.getPlayer());

        if (event.getBlock().getType() == Material.DARK_PRISMARINE) {
            playerData.thunderEffect = false;
            opponentData.thunderEffect = false;
            event.getPlayer().getWorld().setThundering(false);
            event.getPlayer().getWorld().setStorm(false);
        } else if (event.getBlock().getType() == Material.SPAWNER) {
            playerData.constantSpawning = false;
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (WizardDuels.currentMatch == null) return;

        Basic1v1 match = (Basic1v1) WizardDuels.currentMatch;
        Player player = event.getPlayer();
        PlayerData playerData = match.getPlayerData(player);

        if (playerData.cobwebCenter != null) {
            if (event.getFrom().getBlock().getType() == Material.COBWEB && event.getTo().getBlock().getType() != Material.COBWEB) {
                if (playerData.cobwebCenter.distance(event.getFrom()) < playerData.cobwebCenter.distance(event.getTo())) {
                    WandUsage.generate3dBall(playerData.player.getLocation(), 3.5, 64, location -> {
                        if (location.getBlock().getType() == Material.COBWEB) {
                            location.getBlock().setType(Material.AIR);
                        }
                    });
                    playerData.cobwebCenter = null;
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 600, 0));
                    match.getOpponentData(player).spellCooldowns.put(Spell.getSpellPlaced(Material.COBWEB), 50);
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(@NotNull PlayerLaunchProjectileEvent event) {
        if (WizardDuels.currentMatch == null) return;

        Basic1v1 match = (Basic1v1) WizardDuels.currentMatch;
        if (event.getProjectile() instanceof Fireball) {
            PlayerData playerData = match.getPlayerData(event.getPlayer());

            if (playerData.fireballsLeft >= 0) {
                playerData.fireballsLeft--;
                Location loc = playerData.player.getLocation();
                loc.getWorld().spawn(loc.add(loc.getDirection().multiply(1)), Fireball.class);

                if (playerData.fireballsLeft < 0) {
                    playerData.spellCooldowns.put(Spell.NETHERRACK, 15);
                }
            } else if (match.getOpponentData(event.getPlayer()).fireballsLeft >= 0) {
                event.setCancelled(true);
            }
        }
    }

    // Preventing lava and water from flowing
    @EventHandler
    public void onBlockFromTo(@NotNull BlockFromToEvent event) {
        if (event.getBlock().isLiquid()) event.setCancelled(true);
    }
}
