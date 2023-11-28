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
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

public class SpellUseEvent implements Listener {
    @EventHandler
    public void onBlockPlace(@NotNull BlockPlaceEvent event) {
        SpellUsage.spellUse(event.getBlockPlaced().getType(), event.getPlayer());
    }

    @EventHandler
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.DARK_PRISMARINE) {
            World world = event.getPlayer().getWorld();
            world.setThundering(false);
            world.setStorm(false);
        }
    }

    @EventHandler
    public void onProjectileHit(@NotNull PlayerLaunchProjectileEvent event) {
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
}
