package com.spellboundmc.wizard_duels.turning.wands;

import com.marcpg.lang.Translation;
import com.spellboundmc.wizard_duels.PlayerData;
import com.spellboundmc.wizard_duels.match.Match;
import com.spellboundmc.wizard_duels.match.MatchManager;
import com.spellboundmc.wizard_duels.turning.spells.Spell;
import io.papermc.paper.event.entity.EntityMoveEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class WandEvents implements Listener {
    public enum UseType {
        RMB, LMB, SRMB, SLMB;

        public static UseType fromAction(@NotNull Action action, Player player) {
            if (action.isRightClick()) {
                return player.isSneaking() ? SRMB : RMB;
            } else {
                return player.isSneaking() ? SLMB : LMB;
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        if (!event.hasItem()) return;

        Player player = event.getPlayer();

        Match match = MatchManager.getMatchByPlayer(player);
        if (match == null) return;

        UseType use = UseType.fromAction(event.getAction(), player);
        
        WandUsage.wandUse(switch (Objects.requireNonNull(event.getItem()).getType()) {
            // =========== EXPLOSION ===========
            case GOLDEN_HOE -> switch (use) {
                case LMB -> Ability.EXPLOSION_CHARGE;
                case RMB -> Ability.PRESSURE_WAVE;
                case SLMB -> Ability.CREEPER_THROW;
                case SRMB -> Ability.SUPER_BLAST;
            };
            // ============== ICE ==============
            case IRON_HOE -> switch (use) {
                case LMB -> Ability.ICE_WALL;
                case RMB -> Ability.ICE_ROAD;
                case SLMB -> Ability.FREEZE;
                case SRMB -> Ability.ICE_STORM;
            };
            // ============= ENDER =============
            case NETHERITE_HOE -> switch (use) {
                case LMB -> Ability.ENDER_BALL;
                case RMB -> Ability.ENDERMAN_TELEPORT;
                case SLMB -> Ability.END_STONE_WALL;
                case SRMB -> Ability.POSITION_SWAP;
            };
            // ============ DRAGON =============
            case NETHERITE_SHOVEL -> switch (use) {
                case LMB -> Ability.DRAGONS_BREATH;
                case RMB -> Ability.DRAGONS_WINGS;
                case SLMB -> Ability.DRAGON;
                case SRMB -> Ability.CRYSTAL_SHIELD;
            };
            // ========== NECROMANCER ==========
            case DIAMOND_HOE -> switch (use) {
                case LMB -> Ability.REVIVING_THE_DEAD;
                case RMB -> Ability.HORSEMAN;
                case SLMB -> Ability.ELITE_SUMMON;
                case SRMB -> Ability.SPAWNER;
            };
            // ============ NETHER =============
            case GOLDEN_SHOVEL -> switch (use) {
                case LMB -> Ability.FIREBALL;
                case RMB -> Ability.GHAST_RIDER;
                case SLMB -> Ability.HOT_BREATH;
                case SRMB -> Ability.FIRE_RING;
            };
            // ============ WEATHER ============
            case TRIDENT -> switch (use) {
                case LMB -> Ability.LIGHTNING_STRIKE;
                case RMB -> Ability.GUST_OF_WIND;
                case SLMB -> Ability.STORM_SHIELD;
                case SRMB -> Ability.TORNADO;
            };
            // =========== TIME WAND ===========
            case WOODEN_SHOVEL -> switch (use) {
                case LMB -> Ability.TIME_FREEZE;
                case RMB -> Ability.CTRL_Z;
                case SLMB -> Ability.PARADOX_SHIELD;
                case SRMB -> Ability.CLONE;
            };
            // ============ GRAVITY ============
            case IRON_AXE -> switch (use) {
                case LMB -> Ability.GRAVI_BEAM;
                case RMB -> Ability.LOW_GRAVITY;
                case SLMB -> Ability.GRAVI_WAVE;
                case SRMB -> Ability.MINI_BLACK_HOLE;
            };
            // ============= SWORD =============
            case WOODEN_SWORD -> switch (use) {
                case LMB -> Ability.SWORD_THROW;
                case RMB -> Ability.SWORD_DASH;
                case SLMB -> Ability.SWORD_STAB;
                case SRMB -> Ability.SWORD_HORDE;
            };
            // ========= ELECTRIC WAND =========
            case IRON_PICKAXE -> switch (use) {
                case LMB -> Ability.LIGHTNING_SHOT;
                case RMB -> Ability.SPEEDY_OVERCHARGE;
                case SLMB -> Ability.ELECTRIC_ZONE;
                case SRMB -> Ability.ELECTRO_PHANTOMS;
            };
            // ============= SCULK =============
            case DIAMOND_AXE -> switch (use) {
                case LMB -> Ability.SONIC_BOOM;
                case RMB -> Ability.SCULK_TELEPORT;
                case SLMB -> Ability.WARDEN;
                case SRMB -> Ability.SCULK_GROWTH;
            };
            // ============= VENOM =============
            case WOODEN_AXE -> switch (use) {
                case LMB -> Ability.POISON;
                case RMB -> Ability.POISON_TELEPORT;
                case SLMB -> Ability.POISON_SPILL;
                case SRMB -> Ability.POISON_MOBS;
            };
            // =========== DARK WAND ===========
            case WOODEN_HOE -> switch (use) {
                case LMB -> Ability.VOID_BEAM;
                case RMB -> Ability.DARK_DASH;
                case SLMB -> Ability.BLINDER;
                case SRMB -> Ability.BLACK_DEATH;
            };
            // =========== OF HEALTH ===========
            case STONE_HOE -> switch (use) {
                case LMB -> Ability.HEAL;
                case RMB -> Ability.DASH;
                case SLMB -> Ability.HEALTH_SHIELD;
                case SRMB -> Ability.LIFE_STEAL;
            };
            // ========== GLITCH WAND ==========
            case GOLDEN_AXE -> switch (use) {
                case LMB -> Ability.GLITCH;
                case RMB -> Ability.GLITCH_DASH;
                case SLMB -> Ability.VIRUS;
                case SRMB -> Ability.GLITCH_SUMMON;
            };
            // ============ WIZARDS ============
            case STICK -> switch (use) {
                case LMB -> Ability.WIZ_BLAST;
                case RMB -> Ability.TELEPORTER;
                case SLMB -> Ability.NECROMANCER;
                case SRMB -> Ability.WIZARD_BEAM;
            };
            // ========= REDSTONE WAND =========
            case COPPER_INGOT -> switch (use) {
                case LMB -> Ability.REDSTONE_BLAST;
                case RMB -> Ability.REDSTONE_DASH;
                case SLMB -> Ability.DISPENSER_WALL;
                case SRMB -> Ability.POWER_BOOST;
            };
            // ========= POTION MASTER =========
            case IRON_SHOVEL -> switch (use) {
                case LMB -> Ability.LITTLE_ACCIDENT;
                case RMB -> Ability.COCKTAIL;
                case SLMB -> Ability.MAGIC_CULT;
                case SRMB -> Ability.ORANGE_JUICE;
            };
            // == NO WAND ==
            default -> null;
        }, event.getPlayer(), match);
    }

    @EventHandler
    public void onVehicleExit(@NotNull VehicleExitEvent event) {
        if (event.getExited() instanceof Player player) {
            Match match = MatchManager.getMatchByPlayer(player);
            if (match == null) return;

            if (event.getVehicle() instanceof Horse horse) {
                horse.eject();
                horse.remove();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityTargetLivingEntity(@NotNull EntityTargetLivingEntityEvent event) {
        if (event.getEntity() instanceof Skeleton skeleton) {
            if (!skeleton.getScoreboardTags().contains("venomous")) return;

            for (Match match : MatchManager.MATCHES) {
                if (match.properties.get("poison_skeletons") != null && event.getTarget() == match.properties.get("poison_skeletons")) {
                    event.setTarget(match.getPlayerData((Player) match.properties.get("poison_skeletons")).player);
                }
            }
        }
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (event.getEntity().getScoreboardTags().contains("venomous")) {
            if (event.getProjectile() instanceof Arrow arrow) {
                arrow.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 5, 0), true);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Match match = MatchManager.getMatchByPlayer(player);
        if (match == null) return;

        Location loc = event.getTo();
        PlayerData playerData = match.getPlayerData(player);
        Block blockUnder = loc.clone().subtract(0, 1, 0).getBlock();

        if (event.hasChangedBlock() && match.properties.containsKey("icy_feet") && match.properties.get("icy_feet") == event.getPlayer()) {
            blockUnder.setType(Material.BLUE_ICE);
        }

        if (playerData.matchModifiers.contains("fire_ring")) {
            int radius = playerData.matchModifiers.contains("ability_boost") ? 15 : 10;

            double initialX = loc.getX() + radius;
            double initialZ = loc.getZ() + radius;
            for (int i = 0; i < 360; i += 3) {
                double angle = Math.toRadians(i);
                double x = initialX * Math.cos(angle);
                double z = initialZ * Math.sin(angle);
                match.world.spawnParticle(Particle.REDSTONE, new Location(match.world, x, loc.getY(), z), 0, new Particle.DustOptions(Color.RED, 1));
            }

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p != player && loc.distance(p.getLocation()) <= radius) {
                    p.setFireTicks(100);
                    p.damage(0.5);
                }
            }
        }

        if (playerData.matchModifiers.contains("ice_storm")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p != player && loc.distance(p.getLocation()) <= 4.0) {
                    p.setVelocity(p.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(0.5));
                }
            }
        }

        if (playerData.matchModifiers.contains("frozen")) {
            event.setCancelled(true);
        }

        if (blockUnder.getType() == Material.SLIME_BLOCK && playerData.selectedWand != Wand.VENOM) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 1, 0));
        }

        if (blockUnder.getType() == Material.BLACK_CONCRETE && playerData.selectedWand != Wand.DARK) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 1, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5, 5));
        }
    }

    @EventHandler
    public void onProjectileHit(@NotNull ProjectileHitEvent event) {
        if (event.getEntity() instanceof Snowball snowball && event.getEntity().getShooter() instanceof Player player) {
            if (MatchManager.getMatchByPlayer(player) == null) return;

            Entity hitEntity = event.getHitEntity();
            if (hitEntity instanceof Player target) {
                target.damage(
                        switch (snowball.getItem().getType()) {
                            case IRON_SWORD -> 4;
                            case GOLD_ORE -> 5;
                            case DIAMOND -> 6;
                            case NETHERITE_SWORD -> snowball.getItem().containsEnchantment(Enchantment.DAMAGE_ALL) ? 9 : 7;
                            default -> 1;
                        }
                );
            }
        }
    }

    @EventHandler
    public void onEntityMove(@NotNull EntityMoveEvent event) {
        if (event.getEntity() instanceof Projectile projectile) {
            // TODO: Add logic for the storm shield projectile freezing!
            projectile.remove(); // Temporary
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof LightningStrike) {
            event.setDamage(4);
        }
        // TODO: Add logic for the storm shield damage absorption!
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        // TODO: Add logic for the storm shield damage reduction!
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        List<Player> players = (List<Player>) entity.getLocation().getNearbyPlayers(64);
        if (!players.isEmpty()) {
            Match match = MatchManager.getMatchByPlayer(players.getFirst());
            if (match == null) return;

            if (entity instanceof EnderCrystal) {
                event.getDrops().clear();
                match.players.both(o -> {
                    PlayerData p = (PlayerData) o;

                    if (p.matchModifiers.contains("wand_crystal")) {
                        p.matchModifiers.remove("wand_crystal");
                        if (p.player.getNoDamageTicks() != 0 && p.selectedWand == Wand.ENDER) {
                            p.player.setNoDamageTicks(1);
                            p.player.sendMessage(Translation.component(p.player.locale(), "wand.dragon.srmb.end").color(NamedTextColor.RED));
                        }
                    }
                    if (p.matchModifiers.contains("spell_crystal")) {
                        p.player.setHealth(p.player.getHealth() + 10.0);
                        p.spellCooldowns.put(Spell.END_CRYSTAL, 35);
                    }
                });
            }

            if (entity instanceof Skeleton) {
                int skeletonsLeft = 0;
                for (Entity e : match.world.getEntities())
                    if (e.getScoreboardTags().contains("venomous")) skeletonsLeft++;

                if (skeletonsLeft == 0)
                    match.players.both(o -> ((PlayerData) o).matchModifiers.remove("poison_skeletons"));
            }
        }
    }
}
