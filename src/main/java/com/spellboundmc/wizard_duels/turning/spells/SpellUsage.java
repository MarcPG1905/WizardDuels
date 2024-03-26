package com.spellboundmc.wizard_duels.turning.spells;

import com.marcpg.util.Randomizer;
import com.spellboundmc.wizard_duels.PlayerData;
import com.spellboundmc.wizard_duels.WizardDuels;
import com.spellboundmc.wizard_duels.match.Match;
import com.spellboundmc.wizard_duels.match.MatchManager;
import com.spellboundmc.wizard_duels.turning.TurnData;
import com.spellboundmc.wizard_duels.turning.wands.WandUsage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

public class SpellUsage {
    public static boolean spellUse(Spell spell, @NotNull Player player) {
        Location loc = player.getLocation();
        World world = player.getWorld();

        Match match = Objects.requireNonNull(MatchManager.getMatchByPlayer(player));
        PlayerData playerData = match.getPlayerData(player);
        PlayerData opponentData = match.getOpponentData(player);

        if (playerData.spellCooldowns.get(spell) == null) {
            player.sendMessage("Missing spell entry!");
            WizardDuels.LOG.warning("Missing spell entry: " + spell.name() + " for " + player.getName());
        } else if (playerData.spellCooldowns.get(spell) >= 0) {
            player.playSound(loc, Sound.ENTITY_VILLAGER_NO, 0.25f, 1.0f);
            player.sendMessage("You're using your spells too fast, cool down!");
            return false;
        }

        switch (spell) {
            case DARK_PRISMARINE -> {
                opponentData.matchModifiers.add("lighting");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!opponentData.matchModifiers.contains("lighting")) cancel();
                        world.strikeLightning(opponentData.player.getLocation());
                    }
                }.runTaskTimer(WizardDuels.PLUGIN, 0, 300);
            }
            case SMITHING_TABLE -> {
                if (player.getInventory().contains(Material.STONE_SWORD)) {
                    player.getInventory().setItem(8, new ItemStack(Material.DIAMOND_SWORD));
                    Bukkit.getScheduler().runTaskLater(WizardDuels.PLUGIN, () -> player.getInventory().setItem(8, new ItemStack(Material.STONE_SWORD)), 300);
                } else
                    return false;
            }
            case FLETCHING_TABLE -> {
                ItemStack bow = new ItemStack(Material.BOW);
                bow.addEnchantment(Enchantment.ARROW_DAMAGE, 5);
                bow.addEnchantment(Enchantment.ARROW_FIRE, 2);
                bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
                if (player.getInventory().contains(Material.STONE_SWORD)) {
                    player.getInventory().setItem(8, bow);
                    player.getInventory().setItem(35, new ItemStack(Material.ARROW));
                    Bukkit.getScheduler().runTaskLater(WizardDuels.PLUGIN, () -> {
                        player.getInventory().setItem(8, new ItemStack(Material.STONE_SWORD));
                        player.getInventory().setItem(35, ItemStack.empty());
                    }, 300);
                } else
                    return false;
            }
            case GRASS_BLOCK -> {
                // TODO: Reset the board.
            }
            case END_CRYSTAL -> {
                if (match.players.left().matchModifiers.contains("spell_crystal") || match.players.right().matchModifiers.contains("spell_crystal")) {
                    player.sendMessage(Component.text("There is already a crystal! Please destroy it first before summoning your own.", NamedTextColor.RED));
                    return true;
                }

                playerData.matchModifiers.add("spell_crystal");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.setHealth(player.getHealth() + 1.0);
                        if (!playerData.matchModifiers.contains("spell_crystal")) {
                            player.setHealth(player.getHealth() + 10.0);
                            cancel();
                        }
                    }
                }.runTaskTimer(WizardDuels.PLUGIN, 0, 20);
            }
            case NETHERRACK -> {
                // TODO: Fix 3 fireballs spawning.
            }
            case LAVA -> {
                for (double i = 0; i < 2 * Math.PI; i += 0.1) {
                    double x = (Integer) playerData.matchProperties.get("lava_bucket_level") * Math.cos(i);
                    double z = (Integer) playerData.matchProperties.get("lava_bucket_level") * Math.sin(i);
                    loc.toCenterLocation().add(x, 0, z).getBlock().setType(Material.LAVA);
                }
                if ((Integer) playerData.matchProperties.get("lava_bucket_level") < 3)
                    playerData.matchProperties.put("lava_bucket_level", (Integer) playerData.matchProperties.get("lava_bucket_level") + 1);
            }
            case WATER -> {
                for (double i = 0; i < 2 * Math.PI; i += 0.1) {
                    double x = (Integer) playerData.matchProperties.get("water_bucket_level") * Math.cos(i);
                    double z = (Integer) playerData.matchProperties.get("water_bucket_level") * Math.sin(i);
                    Location blockLoc = loc.toCenterLocation().add(x, 0, z);
                    for (int j = 0; j < 7; j++) {
                        blockLoc.add(0, 1, 0).getBlock().setType(Material.WATER);
                    }
                }
                if ((Integer) playerData.matchProperties.get("water_bucket_level") < 2)
                    playerData.matchProperties.put("water_bucket_level", (Integer) playerData.matchProperties.get("water_bucket_level") + 1);
            }
            case OBSIDIAN -> {
                // TODO: -25% Spell Cooldowns.
            }
            case PISTON -> {
                int blocks = match.size.ordinal() * 2 + 4;
                for (Player p : world.getPlayers()) {
                    if (p != player) {
                        p.playSound(p, Sound.BLOCK_PISTON_EXTEND, 2.0f, 1.0f);
                        p.setVelocity(p.getLocation().getDirection().normalize().multiply(blocks));
                    }
                }
            }
            case CHAIN -> {
                // TODO: Add the cage and stuff.
                // Can't code right now, because MaybeVlad is too dumb to
                // just update it, because turns obviously don't exist...
            }
            case COBWEB -> {
                WandUsage.generate3dBall(opponentData.player.getLocation(), 3.5, 64, location -> {
                    if (location.getBlock().getType().isAir())
                        location.getBlock().setType(Material.COBWEB);
                });
                opponentData.matchProperties.put("cobweb_center", opponentData.player.getLocation());
            }
            case SPAWNER -> {
                playerData.matchModifiers.add("constant_spawning");
                new BukkitRunnable() {
                    int periods = 0;
                    @Override
                    public void run() {
                        if (!playerData.matchModifiers.contains("constant_spawning")) cancel();

                        Class<? extends Entity> entityClass;
                        do entityClass = Randomizer.fromArray(EntityType.values()).getEntityClass();
                        while (entityClass == null || !Monster.class.isAssignableFrom(entityClass) || entityClass == Wither.class || entityClass == EnderDragon.class || entityClass == Warden.class);

                        Monster monster = (Monster) player.getWorld().spawn(WandUsage.getRandomLocation(player.getLocation(), 5), entityClass);
                        monster.setTarget(opponentData.player);

                        if (periods++ == 3) {
                            playerData.matchModifiers.remove("constant_spawning");
                            cancel();
                        }
                    }
                }.runTaskTimer(WizardDuels.PLUGIN, 0, 200);
            }
            case OAK_PLANKS -> {
                boolean isXAxis = Math.abs(loc.getDirection().getX()) < Math.abs(loc.getDirection().getZ());
                Location startLocation = loc.clone().add(loc.getDirection().multiply(2));
                startLocation.setY(player.getY());

                for (int x = -1; x < 2; x++) {
                    for (int y = 0; y < 3; y++) {
                        world.getBlockAt(startLocation.clone().add(x * (isXAxis ? 1 : 0), 0, x * (isXAxis ? 0 : 1)).add(0, y, 0)).setType(Material.OAK_PLANKS);
                    }
                }
            }
        }
        if (!spell.customCooldown) playerData.spellCooldowns.put(spell, spell.cooldown);
        match.history.add(new TurnData(spell, true, playerData, LocalDateTime.now()));
        return true;
    }
}
