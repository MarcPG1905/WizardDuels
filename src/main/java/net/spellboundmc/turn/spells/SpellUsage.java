package net.spellboundmc.turn.spells;

import me.marcpg1905.color.McFormat;
import net.kyori.adventure.text.Component;
import net.spellboundmc.PlayerData;
import net.spellboundmc.WizardDuels;
import net.spellboundmc.match.Basic1v1;
import net.spellboundmc.turn.TurnData;
import net.spellboundmc.turn.wands.WandUsage;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.HashMap;

public class SpellUsage {
    public static boolean spellUse(Spell spell, @NotNull Player player) {
        Location loc = player.getLocation();
        World world = player.getWorld();

        Basic1v1 basic1v1 = (Basic1v1) WizardDuels.currentMatch;

        PlayerData playerData = basic1v1.player1 == player ? basic1v1.playerData1 : basic1v1.playerData2;
        PlayerData opponentData = basic1v1.player1 == player ? basic1v1.playerData2 : basic1v1.playerData1;

        HashMap<Spell, Integer> map = playerData.spellCooldowns;
        if (map.get(spell) == null) {
            player.sendMessage("Missing spell entry!");
            WizardDuels.LOG.warning("Missing spell entry: " + spell.name() + " for " + player.getName());
        } else if (map.get(spell) >= 0) {
            player.playSound(loc, Sound.ENTITY_VILLAGER_NO, 0.25f, 1.0f);
            player.sendMessage("You're using your spells too fast, cool down!");
            return false;
        }

        switch (spell) {
            case GRASS_BLOCK -> {
                // Write code to rewrite map data live
                // Maybe use files (changing chunk data)
                // Maybe use Minecraft Structures / Schematics
                // Maybe use custom structure loading
            }
            case END_CRYSTAL -> {
                boolean worldHasCrystal = false;
                for (Entity entity : world.getEntities()) {
                    if (entity.getType() == EntityType.ENDER_CRYSTAL || entity instanceof EnderCrystal) {
                        worldHasCrystal = true;
                        break;
                    }
                }

                if (worldHasCrystal) {
                    player.sendMessage(Component.text(McFormat.RED + "There is already a crystal. Destroy it first, before summoning your own!"));
                    return false;
                } else {
                    playerData.spellCooldowns.put(spell, Integer.MAX_VALUE);
                    playerData.spellCrystalActive = true;
                    Location randomLocation = WandUsage.getRandomLocation(loc, 5);
                    world.spawn(randomLocation, EnderCrystal.class);
                }
            }
            case NETHERRACK -> Bukkit.getScheduler().runTaskLater(WizardDuels.PLUGIN, () -> {
                playerData.spellCooldowns.put(spell, Integer.MAX_VALUE);
                playerData.fireballsLeft = 2;
                world.spawn(loc.add(loc.getDirection().multiply(1)), Fireball.class);
            }, 10);

            case DARK_PRISMARINE -> {
                world.setThundering(true);
                world.setStorm(true);
                opponentData.thunderEffect = true;

                playerData.spellCooldowns.put(spell, Integer.MAX_VALUE);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!opponentData.thunderEffect) cancel();
                        world.strikeLightning(opponentData.player.getLocation());
                    }
                }.runTaskTimer(WizardDuels.PLUGIN, 0, 300);
            }
            case SMITHING_TABLE -> {
                if (player.getInventory().contains(Material.IRON_SWORD)) {
                    player.getInventory().remove(Material.IRON_SWORD);
                    player.getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD));
                    playerData.spellCooldowns.put(spell, 35);

                    Bukkit.getScheduler().runTaskLater(WizardDuels.PLUGIN, () -> {
                        player.getInventory().remove(Material.DIAMOND_SWORD);
                        player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
                    }, 300);
                } else {
                    player.sendMessage(Component.text("You can only use the smithing table when you have a sword!"));
                    return false;
                }
            }
            case FLETCHING_TABLE -> {
                ItemStack bow = new ItemStack(Material.BOW);
                bow.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 5);
                bow.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 2);

                if (player.getInventory().contains(Material.IRON_SWORD)) {
                    player.getInventory().remove(Material.IRON_SWORD);
                    player.getInventory().addItem(bow);
                    playerData.spellCooldowns.put(spell, 80);

                    Bukkit.getScheduler().runTaskLater(WizardDuels.PLUGIN, () -> {
                        player.getInventory().remove(Material.BOW);
                        player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
                    }, 400);
                } else {
                    player.sendMessage(Component.text("You can only use the fletching table when you have a sword!"));
                    return false;
                }
            }
            case OBSIDIAN -> playerData.spellLuck25 = true;
            case LAVA -> {
                for (double i = 0; i < 2 * Math.PI; i += 0.1) {
                    double x = (playerData.lavaBucketLevel + 1) * Math.cos(i);
                    double z = (playerData.lavaBucketLevel + 1) * Math.sin(i);

                    loc.toCenterLocation().add(x, 0, z).getBlock().setType(Material.LAVA);
                }
                if (playerData.lavaBucketLevel < 4) playerData.lavaBucketLevel++;
            }
            case WATER -> {
                for (double i = 0; i < 2 * Math.PI; i += 0.1) {
                    double x = (playerData.waterBucketLevel + 1) * Math.cos(i);
                    double z = (playerData.waterBucketLevel + 1) * Math.sin(i);
                    Location blockLoc = loc.toCenterLocation().add(x, 0, z);
                    for (int j = 0; j < 7; j++) {
                        blockLoc.add(0, 1, 0).getBlock().setType(Material.WATER);
                    }
                }
                if (playerData.waterBucketLevel < 3) playerData.waterBucketLevel++;
            }
        }
        basic1v1.history.add(new TurnData(spell, true, playerData, LocalDateTime.now()));
        return true;
    }
}
