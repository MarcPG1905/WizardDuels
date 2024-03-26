package com.spellboundmc.wizard_duels.turning.spells;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.spellboundmc.wizard_duels.PlayerData;
import com.spellboundmc.wizard_duels.match.Match;
import com.spellboundmc.wizard_duels.match.MatchManager;
import com.spellboundmc.wizard_duels.turning.wands.WandUsage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Fireball;
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
        if (MatchManager.getMatchByPlayer(event.getPlayer()) == null) return;

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
        Match match = MatchManager.getMatchByPlayer(event.getPlayer());
        if (match == null) return;

        PlayerData playerData = match.getPlayerData(event.getPlayer());
        PlayerData opponentData = match.getOpponentData(event.getPlayer());

        if (event.getBlock().getType() == Material.DARK_PRISMARINE) {
            playerData.matchModifiers.remove("thunder");
            opponentData.matchModifiers.remove("thunder");
            event.getPlayer().getWorld().setThundering(false);
            event.getPlayer().getWorld().setStorm(false);
        } else if (event.getBlock().getType() == Material.SPAWNER) {
            playerData.matchModifiers.remove("constant_spawning");
            opponentData.matchModifiers.remove("constant_spawning");
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {
        Match match = MatchManager.getMatchByPlayer(event.getPlayer());
        if (match == null) return;

        PlayerData playerData = match.getPlayerData(event.getPlayer());

        if (playerData.matchProperties.containsKey("cobweb_center")) {
            if (event.getFrom().getBlock().getType() == Material.COBWEB && event.getTo().getBlock().getType() != Material.COBWEB) {
                if (((Location) playerData.matchProperties.get("cobweb_center")).distance(event.getFrom()) < ((Location) playerData.matchProperties.get("cobweb_center")).distance(event.getTo())) {
                    WandUsage.generate3dBall(playerData.player.getLocation(), 3.5, 64, location -> {
                        if (location.getBlock().getType() == Material.COBWEB) {
                            location.getBlock().setType(Material.AIR);
                        }
                    });
                    playerData.matchProperties.remove("cobweb_center");
                    playerData.player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 600, 0));
                    match.getOpponentData(playerData.player).spellCooldowns.put(Spell.getSpellPlaced(Material.COBWEB), 50);
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(@NotNull PlayerLaunchProjectileEvent event) {
        Match match = MatchManager.getMatchByPlayer(event.getPlayer());
        if (match == null) return;

        if (event.getProjectile() instanceof Fireball) {
            PlayerData playerData = match.getPlayerData(event.getPlayer());

            if ((Integer) playerData.matchProperties.get("fireballs_left") >= 0) {
                playerData.matchProperties.put("fireballs_left", (Integer) playerData.matchProperties.get("fireballs_left") + 1);
                Location loc = playerData.player.getLocation();
                loc.getWorld().spawn(loc.add(loc.getDirection().multiply(1)), Fireball.class);

                if ((Integer) playerData.matchProperties.get("fireballs_left") < 0) {
                    playerData.spellCooldowns.put(Spell.NETHERRACK, 15);
                }
            } else if ((Integer) match.getOpponentData(event.getPlayer()).matchProperties.get("fireballs_left") >= 0) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockFromTo(@NotNull BlockFromToEvent event) {
        if (event.getBlock().isLiquid()) event.setCancelled(true);
    }
}
