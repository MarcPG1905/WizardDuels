package net.spellboundmc.spells;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import net.spellboundmc.PlayerData;
import net.spellboundmc.WizardDuels;
import net.spellboundmc.match.Basic1v1;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Fireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

public class SpellUseEvent implements Listener {
    @EventHandler
    public void onBlockPlace(@NotNull BlockPlaceEvent event) {
        if (WizardDuels.currentMatch == null) return;

        if (SpellUsage.spellUse(event.getBlock().getType(), event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        if (WizardDuels.currentMatch == null) return;

        if (event.getBlock().getType() == Material.DARK_PRISMARINE) {
            World world = event.getPlayer().getWorld();
            world.setThundering(false);
            world.setStorm(false);

            Basic1v1 basic1v1 = (Basic1v1) WizardDuels.currentMatch;
            (event.getPlayer() == basic1v1.player1 ? basic1v1.playerData1 : basic1v1.playerData2).thunderEffect = false;
            (event.getPlayer() == basic1v1.player1 ? basic1v1.playerData2 : basic1v1.playerData1).thunderEffect = false;
        }
    }

    @EventHandler
    public void onProjectileHit(@NotNull PlayerLaunchProjectileEvent event) {
        if (WizardDuels.currentMatch == null) return;

        Basic1v1 basic1v1 = (Basic1v1) WizardDuels.currentMatch;
        if (event.getProjectile() instanceof Fireball) {
            PlayerData data = event.getPlayer() == basic1v1.player1 ? basic1v1.playerData1 : basic1v1.playerData2;
            PlayerData dataOpponent = event.getPlayer() == basic1v1.player1 ? basic1v1.playerData2 : basic1v1.playerData1;

            if (data.fireballsLeft >= 0) {
                data.fireballsLeft--;
                Location loc = data.player.getLocation();
                loc.getWorld().spawn(loc.add(loc.getDirection().multiply(1)), Fireball.class);

                if (data.fireballsLeft < 0) {
                    data.spellCooldowns.put(Material.NETHERRACK, 15);
                }
            } else if (dataOpponent.fireballsLeft >= 0) {
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
