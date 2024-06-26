package com.spellboundmc.wizard_duels.turning.wands;

import com.destroystokyo.paper.ParticleBuilder;
import com.marcpg.lang.Translation;
import com.marcpg.util.Randomizer;
import com.spellboundmc.wizard_duels.PlayerData;
import com.spellboundmc.wizard_duels.WizardDuels;
import com.spellboundmc.wizard_duels.match.Match;
import com.spellboundmc.wizard_duels.turning.TurnData;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

import static org.bukkit.Material.AIR;
import static org.bukkit.Material.END_STONE;
import static org.bukkit.block.BlockFace.*;
import static org.bukkit.entity.EntityType.*;
import static org.bukkit.potion.PotionEffectType.*;

public class WandUsage {
    public static final List<Class<? extends LivingEntity>> NECROMANCER_WAND_CREATURES = List.of(Zombie.class, Skeleton.class, Spider.class, Silverfish.class, Witch.class, Pillager.class, Guardian.class);
    public static final List<EntityType> NECROMANCER_WAND_CREATURE_TYPES = List.of(ZOMBIE, SKELETON, SPIDER, SILVERFISH, WITCH, PILLAGER, GUARDIAN);
    public static final Map<Material, Integer> SWORDS = Map.of(Material.IRON_SWORD, 30, Material.GOLDEN_SWORD, 30, Material.DIAMOND_SWORD, 25, Material.NETHERITE_SWORD, 10, Material.BOOK, 5);
    public static final Random RANDOM = new Random();
    public static final PotionEffectType[] BAD_EFFECTS = new PotionEffectType[]{ SLOW, SLOW_DIGGING, HARM, CONFUSION, BLINDNESS, WEAKNESS, POISON, LEVITATION, DARKNESS };
    public static final PotionEffectType[] GOOD_EFFECTS = new PotionEffectType[]{ SPEED, FAST_DIGGING, INCREASE_DAMAGE, HEAL, JUMP, REGENERATION, DAMAGE_RESISTANCE, FIRE_RESISTANCE, INVISIBILITY, NIGHT_VISION, ABSORPTION };

    public static void wandUse(Ability ability, @NotNull Player player, Match match) {
        Locale l = player.locale();
        Location loc = player.getLocation();
        World world = player.getWorld();

        PlayerData playerData = match.getPlayerData(player);
        PlayerData opponentData = match.getOpponentData(player);

        if (playerData.matchModifiers.contains("disabled_wands")) {
            player.sendMessage(Translation.component(l, "wand.error.disabled").color(NamedTextColor.RED));
            return;
        }

        if (playerData.abilityCooldowns.get(ability) >= 0) {
            player.playSound(player, Sound.ENTITY_VILLAGER_NO, 0.25f, 1.0f);
            player.sendMessage(Component.text("You're using your abilities too fast, cool down!", NamedTextColor.RED));
            return;
        }

        playerData.abilityCooldowns.put(ability, ability.cooldown);

        switch (ability) {
            // =========== EXPLOSION ===========
            case EXPLOSION_CHARGE -> {
                player.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
                player.launchProjectile(Fireball.class);
            }
            case PRESSURE_WAVE -> {
                player.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
                Vector velocity = player.getVelocity();
                double launchVelocityY = Math.sqrt(b(playerData, 1, 2) * 2 * 0.08 * 15.0 * ((velocity.getY() / 2) + 1));
                player.setVelocity(new Vector(velocity.getX(), launchVelocityY, velocity.getZ()));
            }
            case CREEPER_THROW -> {
                player.playSound(loc, Sound.ENTITY_CREEPER_HURT, 1.0f, 1.0f);
                Creeper creeper = player.getWorld().spawn(loc, Creeper.class);
                creeper.setTarget(opponentData.player);
                creeper.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(b(playerData, 2.5, 4)));
            }
            case SUPER_BLAST -> {
                double originalX = loc.getX();
                double originalY = loc.getY();
                double originalZ = loc.getZ();
                double radius = b(playerData, 2, 3);
                for (int i = 0; i < 360; i += 5) {
                    double angle = Math.toRadians(i);
                    double x = originalX + Math.cos(angle) * radius;
                    double z = originalZ + Math.sin(angle) * radius;
                    Location particleLocation = new Location(world, x, originalY, z);

                    Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 1);
                    world.spawnParticle(Particle.REDSTONE, particleLocation, 1, 0, 0, 0, 1, dustOptions, true);

                    for (int j = 1; j <= 3; j++) {
                        double thickness = j * 0.1;
                        x = originalX + Math.cos(angle) * (radius + thickness);
                        z = originalZ + Math.sin(angle) * (radius + thickness);
                        particleLocation = new Location(world, x, originalY, z);
                        world.spawnParticle(Particle.REDSTONE, particleLocation, 0, 0, 0, 0, 1, dustOptions, true);
                    }
                }

                Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> {
                    int size = b(playerData, 2, 3);

                    for (int x = -size; x <= (size - 1); x++) {
                        for (int z = -size; z <= (size - 1); z++) {
                            world.spawn(loc.clone().add(x, 0, z), TNTPrimed.class)
                                    .setFuseTicks(b(playerData, 2, 3));
                        }
                    }
                }, 60);
            }

            // ============== ICE ==============
            case ICE_WALL -> {
                player.playSound(loc, Sound.BLOCK_SNOW_PLACE, 1.5f, 2.5f);

                Location playerLocation = player.getEyeLocation();
                for (int i = 1; i <= b(playerData, 10, 20); i++) {
                    Location spawnLocation = playerLocation.clone().add(playerLocation.getDirection().multiply(i / 2 + 3));
                    spawnLocation.setY(loc.getY());
                    EvokerFangs evokerFang = world.spawn(spawnLocation, EvokerFangs.class);
                    evokerFang.setOwner(player);
                }
                player.stopSound(SoundStop.named(Sound.ENTITY_EVOKER_FANGS_ATTACK));
                opponentData.player.stopSound(SoundStop.named(Sound.ENTITY_EVOKER_FANGS_ATTACK));
            }
            case ICE_ROAD -> {
                playerData.matchModifiers.add("icy_feet");
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, b(playerData, 200, 400), 1));
                Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> playerData.matchModifiers.remove("icy_feet"), 200L);
            }
            case FREEZE -> {
                opponentData.matchModifiers.add("frozen");
                Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> opponentData.matchModifiers.remove("frozen"), b(playerData, 100, 200));
            }
            case ICE_STORM -> {
                playerData.matchModifiers.add("ice_storm");
                new BukkitRunnable() {
                    double angle = 0;
                    double duration = 0;

                    @Override
                    public void run() {
                        player.playSound(player.getLocation(), Sound.ENTITY_HORSE_BREATHE, 0.5f, 1.0f);

                        if (duration >= 10) {
                            playerData.matchModifiers.remove("ice_storm");
                            cancel();
                        }

                        for (int i = 0; i < 6; i++) {
                            double x = 4 * Math.cos(angle + 30 * i);
                            double z = 4 * Math.sin(angle + 30 * i);
                            player.getWorld().spawnParticle((Randomizer.boolByChance(90) ? Particle.WAX_OFF : Particle.SNOWFLAKE), player.getLocation().add(x, RANDOM.nextDouble(2.2), z), 1);
                        }

                        angle += Math.toRadians(3);
                        duration += 0.1;
                    }
                }.runTaskTimer(WizardDuels.getPlugin(WizardDuels.class), 0, 1);
            }

            // ============= ENDER =============
            case ENDER_BALL -> player.launchProjectile(DragonFireball.class);
            case ENDERMAN_TELEPORT -> {
                player.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 0.8f);

                Block block = player.getTargetBlockExact(b(playerData, 10, 20));
                if (block == null) {
                    player.sendMessage(Translation.component(l, "wand.error.range", b(playerData, 10, 20)).color(NamedTextColor.YELLOW));
                    return;
                }
                // TODO: Check if the block is inside the map
                Location newLoc = new Location(world, block.getX(), block.getY() + 1, block.getZ(), loc.getYaw(), loc.getPitch());
                player.teleport(newLoc);
                player.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            }
            case END_STONE_WALL -> {
                boolean isXAxis = Math.abs(loc.getDirection().getX()) < Math.abs(loc.getDirection().getZ());

                Location startLocation = loc.clone().add(loc.getDirection().multiply(5));
                startLocation.setY(player.getY());

                for (int x = -2; x < 3; x++) {
                    for (int y = 0; y < 5; y++) {
                        world.getBlockAt(startLocation.clone().add(x * (isXAxis ? 1 : 0), 0, x * (isXAxis ? 0 : 1)).add(0, y, 0)).setType(END_STONE);
                    }
                }
            }
            case POSITION_SWAP -> {
                Location location1 = playerData.player.getLocation();
                playerData.player.teleport(opponentData.player);
                playerData.player.playSound(playerData.player, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                opponentData.player.teleport(location1);
                opponentData.player.playSound(opponentData.player, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            }

            // ============ DRAGON =============
            case DRAGONS_BREATH -> {
                Block block = player.getTargetBlockExact(b(playerData, 10, 20));
                if (block == null) {
                    player.sendMessage(Translation.component(l, "wand.error.range", b(playerData, 10, 20)).color(NamedTextColor.YELLOW));
                    return;
                }
                Location spawnLocation = block.getLocation().clone().add(0, 1, 0);

                Fireball fireball = world.spawn(spawnLocation, DragonFireball.class);
                fireball.setIsIncendiary(false);
                Vector direction = block.getLocation().toVector().subtract(spawnLocation.toVector()).normalize();
                fireball.setDirection(direction);

                fireball.setGravity(true);
            }
            case DRAGONS_WINGS -> {
                player.playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1f);

                player.setAllowFlight(true);
                player.setFlying(true);

                ItemStack elytra = new ItemStack(Material.ELYTRA);
                player.getInventory().setChestplate(elytra);
                elytra.editMeta(meta -> meta.setCustomModelData(1));

                Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> {
                    player.setFlying(false);
                    player.setAllowFlight(false);
                    player.setNoDamageTicks(40);
                    player.getInventory().setChestplate(null);
                }, b(playerData, 120, 240));
            }
            case DRAGON -> {
                // TODO: Make dragon fly to opponent and then despawn.
            }
            case CRYSTAL_SHIELD -> {
                Location randomLocation = getRandomLocation(loc, b(playerData, 10, 15));
                world.spawn(randomLocation, EnderCrystal.class);
                playerData.matchModifiers.add("wand_crystal");
                player.setNoDamageTicks(99999);
            }

            // ========== NECROMANCER ==========
            case REVIVING_THE_DEAD -> {
                player.playSound(loc, Sound.ENTITY_VEX_CHARGE, 1.0f, 1.0f);

                LivingEntity entity = player.getWorld().spawn(loc, NECROMANCER_WAND_CREATURES.get(RANDOM.nextInt(NECROMANCER_WAND_CREATURES.size())));
                if (entity instanceof Monster monster) monster.setTarget(opponentData.player);
                entity.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(b(playerData, 2.5, 4)));
            }
            case HORSEMAN -> {
                player.playSound(loc, Sound.ENTITY_HORSE_ANGRY, 1.5f, 1.2f);

                Horse horse = world.spawn(loc, Horse.class);
                horse.getInventory().setArmor(new ItemStack(Material.DIAMOND_HORSE_ARMOR));
                horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
                horse.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, -1, b(playerData, 2, 5)));
                horse.setOwner(player);
                horse.addPassenger(player);
            }
            case ELITE_SUMMON -> {
                player.playSound(loc, Sound.ENTITY_VEX_CHARGE, 1.5f, 0.2f);

                boolean bool = RANDOM.nextBoolean();
                Class<? extends Entity> mob = bool ? Zombie.class : Skeleton.class;

                for (int i = 0; i < b(playerData, 5, 8); i++) {
                    Monster entity = (Monster) world.spawn(loc, mob);
                    entity.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
                    entity.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
                    entity.getEquipment().setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
                    entity.getEquipment().setBoots(new ItemStack(Material.NETHERITE_BOOTS));
                    entity.getEquipment().setItemInMainHand(new ItemStack(bool ? Material.IRON_SWORD : Material.BOW));

                    entity.setTarget(opponentData.player);
                    entity.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(b(playerData, 2.5, 4)));
                }
            }
            case SPAWNER -> {
                player.playSound(loc, Sound.ENTITY_IRON_GOLEM_REPAIR, 1.2f, 1.0f);

                EntityType entityType = NECROMANCER_WAND_CREATURE_TYPES.get(RANDOM.nextInt(NECROMANCER_WAND_CREATURE_TYPES.size()));

                Location spawnerLocation = loc.clone();
                spawnerLocation.add(0, 5, 0);

                spawnerLocation.getBlock().setType(Material.SPAWNER);
                CreatureSpawner spawner = (CreatureSpawner) spawnerLocation.getBlock().getState();
                spawner.setSpawnedType(entityType);
                spawner.update();
            }

            // ============ NETHER =============
            case FIREBALL -> player.launchProjectile(Fireball.class);
            case GHAST_RIDER -> {
                Ghast ghast = world.spawn(loc, Ghast.class);
                ghast.addPassenger(player);
                ghast.setTarget(opponentData.player);

                Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), ghast::remove, 200);
                player.setNoDamageTicks(40);
            }
            case HOT_BREATH -> {
                player.playSound(loc, Sound.ENTITY_GHAST_SCREAM, 1.0f, 1.0f);
                new BukkitRunnable() {
                    int secsLeft = RANDOM.nextInt(6, 11) * b(playerData, 1, 2);

                    @Override
                    public void run() {
                        player.launchProjectile(Fireball.class);

                        secsLeft--;
                        if (secsLeft == 0) cancel();
                    }
                }.runTaskTimer(WizardDuels.getPlugin(WizardDuels.class), 0, b(playerData, 20, 15));
            }
            case FIRE_RING -> {
                player.playSound(loc, Sound.ITEM_FLINTANDSTEEL_USE, 1.5f, 1.0f);
                playerData.matchModifiers.add("fire_ring");
                Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> playerData.matchModifiers.remove("fire_ring"), b(playerData, 200, 300));
            }

            // ============ WEATHER ============
            case LIGHTNING_STRIKE -> world.strikeLightning(opponentData.player.getLocation());
            case GUST_OF_WIND -> {
                player.playSound(player, Sound.AMBIENT_UNDERWATER_ENTER, 1.2f, 1.0f);
                Vector direction = player.getLocation().getDirection().normalize();
                player.setVelocity(new Vector(direction.getX(), 0.6, direction.getZ()).multiply(b(playerData, 2, 3)));
            }
            case STORM_SHIELD -> {
                // TODO: Make a shield of particles around the player and projectiles lose velocity once entering.
            }
            case TORNADO -> {
                Horse horse = world.spawn(loc, Horse.class);
                horse.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, -1, 1, true, false));

                new BukkitRunnable() {
                    private int ticks = 0;

                    @Override
                    public void run() {
                        Location horseLoc = horse.getLocation();

                        world.playSound(horseLoc, Sound.ENTITY_HORSE_BREATHE, 5f, 2.0f);

                        double x, y, z;
                        double r = 0;
                        for (y = horse.getY(); y <= horse.getY() + 15; y += 0.01) {
                            x = horseLoc.getX() + r * Math.cos(6 * y);
                            z = horseLoc.getZ() + r * Math.sin(6 * y);
                            world.spawnParticle(Particle.SMOKE_LARGE, x, y + RANDOM.nextDouble(-1, 2), z, 1, 0, 0, 0, 1);
                            r += 0.005;
                        }

                        for (Entity entity : horse.getNearbyEntities(30, 30, 30)) {
                            Vector direction = horseLoc.toVector().subtract(entity.getLocation().toVector()).normalize();
                            entity.setVelocity(direction.multiply(0.06));
                        }

                        ticks += 2;
                        if (ticks >= 300) {
                            horse.remove();
                            cancel();
                        }
                    }
                }.runTaskTimer(WizardDuels.getPlugin(WizardDuels.class), 5, 2);
            }

            // TODO: ===== TIME WARP ===========
            case TIME_FREEZE -> new BukkitRunnable() {
                private int counter;

                @Override
                public void run() {
                    generate3dBall(player.getLocation(), 8.0, 30, location -> new ParticleBuilder(Particle.REDSTONE).color(255, 255, 0).location(location).spawn());

                    for (Entity entity : player.getNearbyEntities(16, 16, 16)) {
                        entity.setVelocity(entity.getVelocity().multiply(0.5));
                        if (entity instanceof Player p && p.getAttackCooldown() > 2) {
                            p.setCooldown(p.getInventory().getItemInMainHand().getType(), (int) (p.getAttackCooldown() * 2));
                        }
                    }

                    counter++;
                    if (counter >= 100) cancel();
                }
            }.runTaskTimer(WizardDuels.getPlugin(WizardDuels.class), 1, 1);
            case CTRL_Z -> {
                Location timeDestination = playerData.lastLocations.get(5);
                if (timeDestination == null) {
                    player.sendMessage(Translation.component(l, "wand.time.time_error").color(NamedTextColor.RED));
                    return;
                }
                player.teleport(timeDestination);
            }
            case PARADOX_SHIELD -> new BukkitRunnable() {
                private int counter;

                @Override
                public void run() {
                    generate3dBall(player.getLocation(), 4.0, 30, location -> new ParticleBuilder(Particle.REDSTONE).color(255, 255, 0).location(location).spawn());

                    for (Entity entity : player.getNearbyEntities(8, 8, 8)) {
                        if (entity instanceof Projectile projectile) {
                            projectile.setVelocity(new Vector());
                        }
                    }

                    counter++;
                    if (counter >= 100) {
                        for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
                            if (entity instanceof Projectile projectile) {
                                Vector direction = projectile.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                                projectile.setVelocity(direction.multiply(1.2));
                            }
                        }
                        cancel();
                    }
                }
            }.runTaskTimer(WizardDuels.getPlugin(WizardDuels.class), 1, 2);
            case CLONE -> {
                // TODO: Time Warp Clone Ability
                // Clone the player 2 blocks before him
                // Mimic the player's movements 1:1
            }

            // ============ GRAVITY ============
            case GRAVI_BEAM -> {
                player.playSound(loc, Sound.ENTITY_RAVAGER_ATTACK, 2.0f, 1.0f);

                generateParticleBeam(player, 20, new ParticleBuilder(Particle.REDSTONE).color(0, 0, 0).count(3), true, null, null, hitPlayer -> {
                    hitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, b(playerData, 200, 300), 127, true, false));
                    hitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, b(playerData, 200, 300), 255, true, false));
                });
            }
            case LOW_GRAVITY -> {
                playerData.matchModifiers.add("no_gravity");
                player.setGravity(false);

                Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> {
                    player.setGravity(true);
                    playerData.matchModifiers.remove("no_gravity");
                    player.setNoDamageTicks(40);
                }, b(playerData, 200, 300));
            }
            case GRAVI_WAVE -> {
                player.playSound(loc, Sound.ENTITY_RAVAGER_STUNNED, 1.0f, 1.0f);

                for (int radius = 1; radius <= b(playerData, 10.2, 15.4); radius++) {
                    for (int angle = 0; angle < 360; angle += 5) {
                        double radians = Math.toRadians(angle);
                        double x = loc.getX() + radius * Math.cos(radians);
                        double z = loc.getZ() + radius * Math.sin(radians);
                        double y = loc.getY();

                        Location particleLocation = new Location(world, x, y, z);

                        world.spawnParticle(Particle.CRIT_MAGIC, particleLocation, 1);
                    }
                }

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p != player && loc.distance(p.getLocation()) <= b(playerData, 10, 15)) {
                        Vector direction = p.getLocation().toVector().subtract(loc.toVector()).normalize();
                        p.setVelocity(direction.multiply(1.5));
                    }
                }
            }
            case MINI_BLACK_HOLE -> {
                // CRAZY BLACK HOLE?!?!
            }

            // ============= SWORD =============
            case SWORD_THROW -> {
                ItemStack sword = new ItemStack(Material.IRON_SWORD);

                int randomValue = new Random().nextInt(100);

                int cumulativeWeight = 0;
                for (Map.Entry<Material, Integer> entry : SWORDS.entrySet()) {
                    cumulativeWeight += entry.getValue();
                    if (randomValue < cumulativeWeight) {
                        sword = new ItemStack(entry.getKey());

                        if (entry.getKey() == Material.BOOK) sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2);
                    }
                }

                Snowball ball = player.launchProjectile(Snowball.class);
                ball.setItem(sword);
                ball.setGravity(false);
            }
            case SWORD_DASH -> {
                Vector direction = player.getLocation().getDirection().normalize();
                player.setVelocity(new Vector(direction.getX(), 0.05, direction.getZ()).multiply(b(playerData, 1, 1.5)));

                Vector perpendicular = new Vector(-direction.getZ(), 0.15, direction.getX()).normalize();

                Snowball rightSnowball = player.launchProjectile(Snowball.class);
                rightSnowball.setItem(new ItemStack(Material.IRON_SWORD));
                rightSnowball.teleport(player.getLocation().add(perpendicular.multiply(2)));
                rightSnowball.setVelocity(player.getVelocity());

                Snowball leftSnowball = player.launchProjectile(Snowball.class);
                leftSnowball.setItem(new ItemStack(Material.IRON_SWORD));
                leftSnowball.teleport(player.getLocation().subtract(perpendicular.multiply(2)));
                leftSnowball.setVelocity(player.getVelocity());
            }
            case SWORD_STAB -> {
                int randomValue = new Random().nextInt(100);
                int cumulativeWeight = 0;
                ItemStack sword;
                for (Map.Entry<Material, Integer> entry : SWORDS.entrySet()) {
                    cumulativeWeight += entry.getValue();
                    if (randomValue < cumulativeWeight) {
                        sword = new ItemStack(entry.getKey());
                        if (entry.getKey() == Material.BOOK)
                            sword.addEnchantment(Enchantment.DAMAGE_ALL, 3);
                    }
                }
            }
            case SWORD_HORDE -> {
                // TODO: Add the factor of the wand level

                for (int i = 0; i < b(playerData, 6, 10); i++) {
                    Monster entity = world.spawn(loc, (Class<? extends Monster>) (i > b(playerData, 3, 5) ? Skeleton.class : Zombie.class));
                    entity.setTarget(opponentData.player);
                    entity.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(b(playerData, 2.5, 3)));
                }
            }

            // ========= ELECTRIC WAND =========
            case LIGHTNING_SHOT -> {
                Location lightning = getRandomLocation(loc, 10);
                world.spawn(lightning, LightningStrike.class);

                Bukkit.getScheduler().runTaskLater(WizardDuels.PLUGIN, () -> {
                    world.spawn(getRandomLocation(lightning, 1), LightningStrike.class);
                    world.spawn(getRandomLocation(lightning, 1), LightningStrike.class);
                }, 20L);
            }
            case SPEEDY_OVERCHARGE -> player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, b(playerData, 400, 600), 2));
            case ELECTRIC_ZONE -> new BukkitRunnable() {
                double angle = 0;
                int duration = 0;

                @Override
                public void run() {
                    for (int i = 0; i < 6; i++) {
                        double x = 4 * Math.cos(angle + 30 * i);
                        double z = 4 * Math.sin(angle + 30 * i);
                        player.getWorld().spawnParticle((RANDOM.nextBoolean() ? Particle.WAX_ON : Particle.CRIT), player.getLocation().add(x, RANDOM.nextDouble(2.2), z), 1);
                    }

                    if (duration % 10 == 0) {
                        for (Player target : Bukkit.getOnlinePlayers()) {
                            if (target != player && loc.distance(target.getLocation()) <= 10) {
                                world.spawn(target.getLocation(), LightningStrike.class);
                                target.damage(0.5);
                            }
                        }
                    }

                    angle += Math.toRadians(3);
                    duration++;
                    if (duration >= b(playerData, 200, 300)) cancel();
                }
            }.runTaskTimer(WizardDuels.PLUGIN, 0, 1);
            case ELECTRO_PHANTOMS -> {
                for (int i = 0; i < b(playerData, 3, 6); i++) {
                    Phantom phantom = world.spawn(loc, Phantom.class);
                    phantom.setTarget(opponentData.player);
                }
            }

            // ============= SCULK =============
            case SONIC_BOOM -> generateParticleBeam(player, 8, new ParticleBuilder(Particle.SONIC_BOOM), false, null, entity -> entity.damage(2.0), null);
            case SCULK_TELEPORT -> {
                Block block = player.getTargetBlockExact(99);
                if (block == null) {
                    player.sendMessage(Translation.component(l, "wand.error.range", 99).color(NamedTextColor.YELLOW));
                    return;
                }
                if (!block.getType().name().contains("SCULK")) {
                    player.sendMessage(Translation.component(l, "wand.sculk.rmb.error").color(NamedTextColor.YELLOW));
                    return;
                }
                // TODO: Check if the block is inside the map
                player.teleport(new Location(world, block.getX(), block.getY() + 1, block.getZ(), loc.getYaw(), loc.getPitch()));
            }
            case WARDEN -> {
                Warden warden = world.spawn(getRandomLocation(loc, 10), Warden.class);
                warden.setHealth(50);
                warden.setTarget(opponentData.player);
                warden.setAnger(opponentData.player, 100);
            }
            case SCULK_GROWTH -> {
                final List<BlockFace> blockFaces = List.of(UP, DOWN, NORTH, EAST, SOUTH, WEST);

                Block startBlock = loc.subtract(0, 1, 0).getBlock();
                startBlock.setType(Material.SCULK);
                new BukkitRunnable() {
                    final HashSet<Block> allBlocks = new HashSet<>(Set.of(startBlock));
                    HashSet<Block> currentBlocks = new HashSet<>(Set.of(startBlock));
                    int left = 10;
                    @Override
                    public void run() {
                        HashSet<Block> newCurrentBlocks = new HashSet<>(Set.of(startBlock));
                        for (Block block : currentBlocks) {
                            for (BlockFace face : blockFaces) {
                                if (RANDOM.nextBoolean()) {
                                    Block target = block.getRelative(face);
                                    if (!target.getType().isAir()){
                                        target.setType(Material.SCULK);
                                        newCurrentBlocks.add(target);
                                    }
                                }
                            }
                        }
                        currentBlocks = newCurrentBlocks;
                        allBlocks.addAll(newCurrentBlocks);

                        left--;
                        if (left <= 0) {
                            for (Block block : allBlocks) {
                                block.setType(AIR);
                                world.playSound(block.getLocation(), Sound.BLOCK_BASALT_BREAK, 2.0f, 1.0f);
                            }
                            cancel();
                        }
                    }
                }.runTaskTimer(WizardDuels.PLUGIN, 0, 10);
            }

            // ============= VENOM =============
            case POISON -> opponentData.player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
            case POISON_TELEPORT -> {
                LivingEntity closest = opponentData.player;
                for (LivingEntity entity : world.getLivingEntities()) {
                    if (entity.hasPotionEffect(PotionEffectType.POISON)) {
                        if (entity.getLocation().distance(loc) < closest.getLocation().distance(loc)) {
                            closest = entity;
                        }
                    }
                }
                if (!closest.hasPotionEffect(PotionEffectType.POISON)) {
                    player.sendMessage(Translation.component(l, "wand.venom.rmb.error").color(NamedTextColor.YELLOW));
                    return;
                }
                player.teleport(closest);
            }
            case POISON_SPILL -> {
                int radius = RANDOM.nextInt(3, 7);

                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (x * x + z * z <= radius * radius) {
                            Block block = world.getBlockAt(loc.getBlockX() + x, loc.getBlockY() - 1, loc.getBlockZ() + z);
                            block.setType(Material.SLIME_BLOCK);
                        }
                    }
                }
            }
            case POISON_MOBS -> {
                for (int i = 0; i < 3; i++) {
                    Skeleton skeleton = world.spawn(loc, Skeleton.class);
                    skeleton.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
                    skeleton.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                    skeleton.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                    skeleton.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));
                    skeleton.addScoreboardTag("venomous");
                    skeleton.setTarget(opponentData.player);
                }
                playerData.matchModifiers.add("poison_skeletons");

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!playerData.matchModifiers.contains("poison_skeletons")) cancel();

                        for (LivingEntity entity : world.getLivingEntities()) {
                            if (entity.getScoreboardTags().contains("venomous")) {
                                if (entity.getLocation().distance(player.getLocation()) <= 5) {
                                    entity.setHealth(entity.getHealth() + 2);
                                    for (int i = 0; i < 5; i++) {
                                        world.spawnParticle(Particle.VILLAGER_HAPPY, getRandomLocation(entity.getLocation().add(0, 1, 0), 1), 2);
                                    }
                                }
                            }
                        }
                    }
                }.runTaskTimer(WizardDuels.PLUGIN, 0, 20);
            }

            // =========== DARK WAND ===========
            case VOID_BEAM -> generateParticleBeam(player, 8, new ParticleBuilder(Particle.REDSTONE).color(0, 0, 0), true, block -> {
                block.setType(AIR);
                world.spawn(block.getLocation(), TNTPrimed.class);
            }, null, hitPlayer -> hitPlayer.damage(2.0));
            case DARK_DASH -> {
                Vector direction = player.getLocation().getDirection().normalize();
                player.setVelocity(new Vector(direction.getX(), 0.5, direction.getZ()));

                RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities(player.getEyeLocation(), direction, 5);

                if (rayTraceResult != null && rayTraceResult.getHitEntity() instanceof LivingEntity hitEntity) {
                    double damage = 4;
                    hitEntity.damage(damage);
                    hitEntity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 5));
                }
            }
            case BLINDER -> opponentData.player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 140, 5));
            case BLACK_DEATH -> {
                Location spawnLoc = loc.clone();

                Bukkit.getScheduler().runTaskLater(WizardDuels.PLUGIN, () -> {
                    int radius = 3;

                    for (int x = -radius; x <= radius; x++) {
                        for (int z = -radius; z <= radius; z++) {
                            if (x * x + z * z <= radius * radius) {
                                Block block = world.getBlockAt(spawnLoc.getBlockX() + x, spawnLoc.getBlockY() - 1, spawnLoc.getBlockZ() + z);
                                block.setType(Material.BLACK_CONCRETE);
                            }
                        }
                    }
                }, 50);

                world.spawn(spawnLoc, TNTPrimed.class).setFuseTicks(50);
                world.spawn(spawnLoc, TNTPrimed.class).setFuseTicks(50);
            }

            // =========== OF HEALTH ===========
            case HEAL -> player.setHealth(player.getHealth() + 10);
            case DASH -> {
                Vector direction = player.getLocation().getDirection().normalize();
                player.setVelocity(new Vector(direction.getX(), 0.05, direction.getZ()));

                RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities(player.getEyeLocation(), direction, 5);
                if (rayTraceResult != null && rayTraceResult.getHitEntity() instanceof LivingEntity hitEntity && rayTraceResult.getHitEntity() != player) hitEntity.damage(5);
            }
            case HEALTH_SHIELD -> player.setNoDamageTicks(200);
            case LIFE_STEAL -> {
                int fin = specialRandom10();
                opponentData.player.damage(fin);
                player.setHealth(player.getHealth() + fin);
            }

            // ========== GLITCH WAND ==========
            case GLITCH -> {
                if (Randomizer.boolByChance(80)) {
                    List<Ability> abilities = new ArrayList<>(Arrays.stream(Ability.values()).toList());
                    abilities.remove(Ability.GLITCH);
                    wandUse(Randomizer.fromCollection(abilities), player, match);
                } else {
                    player.playSound(player, Sound.BLOCK_GLASS_BREAK, 0.5f, 0.8f);
                    player.sendMessage(Translation.component(l, "wand.glitch.lmb.fail").color(NamedTextColor.YELLOW));
                }
            }
            case GLITCH_DASH -> {
                int distance = RANDOM.nextInt(2, 25);
                Vector teleportVector = loc.getDirection().multiply(distance);

                Block block = player.getTargetBlockExact(distance);
                if (block != null && block.isCollidable()) {
                    player.teleport(new Location(world, block.getX(), block.getY() + 1, block.getZ(), player.getYaw(), player.getPitch()));
                } else {
                    player.teleport(loc.add(teleportVector.setY(teleportVector.getY() / 2)));
                }
            }
            case VIRUS -> {
                int seconds = specialRandom10();
                opponentData.matchModifiers.add("disabled_wands");
                opponentData.player.showTitle(Title.title(Component.empty(), Translation.component(l, "wand.glitch.slmb.opponent_title", seconds).color(NamedTextColor.RED), Title.Times.times(Duration.ZERO, Duration.ofSeconds(seconds / 2), Duration.ZERO)));
                Bukkit.getScheduler().runTaskLater(WizardDuels.PLUGIN, () -> {
                    opponentData.matchModifiers.remove("disabled_wands");
                    opponentData.player.sendMessage(Translation.component(l, "wand.glitch.slmb.opponent_end").color(NamedTextColor.GREEN));
                }, seconds * 20L);
            }
            case GLITCH_SUMMON -> {
                // Complicated shit
            }

            // ============ WIZARDS ============
            case WIZ_BLAST -> generateParticleBeam(player, 80, new ParticleBuilder(Randomizer.fromArray(Particle.values())), true, block -> {
                        block.setType(Material.AIR);
                        TNTPrimed tnt = world.spawn(block.getLocation(), TNTPrimed.class);
                        tnt.setFuseTicks(1);
                    }, null, null
            );
            case TELEPORTER -> {
                Location oldLoc = loc.clone();

                Block block = player.getTargetBlockExact(10);
                if (block == null) {
                    player.sendMessage(Translation.component(l, "wand.error.range", 10).color(NamedTextColor.YELLOW));
                    return;
                }
                // TODO: Check if the block is inside the map
                player.teleport(new Location(world, block.getX(), block.getY() + 1, block.getZ(), loc.getYaw(), loc.getPitch()));

                world.spawn(oldLoc, LightningStrike.class);
            }
            case NECROMANCER -> {
                Class<? extends Entity> entityClass;
                do entityClass = Randomizer.fromArray(EntityType.values()).getEntityClass();
                while (entityClass == null || !Monster.class.isAssignableFrom(entityClass) || entityClass == Wither.class || entityClass == EnderDragon.class);

                Monster monster = (Monster) world.spawn(getRandomLocation(loc, 4), entityClass);
                monster.setTarget(opponentData.player);
                monster.setHealth(monster.getHealth() * 1.5);
                monster.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, -1, 2, true, false));
            }
            case WIZARD_BEAM -> generateParticleBeam(player, 80, new ParticleBuilder(Particle.REDSTONE).color(255, 255, 255), true, block -> {
                TNTPrimed tnt = world.spawn(block.getLocation(), TNTPrimed.class);
                tnt.setFuseTicks(1);
            }, hitEntity -> hitEntity.setNoActionTicks(200), hitPlayer -> {
                opponentData.matchModifiers.add("disabled_wands");
                Bukkit.getScheduler().runTaskLater(WizardDuels.PLUGIN, () -> opponentData.matchModifiers.remove("disabled_wands"), 100);
            });

            // ========= REDSTONE WAND =========
            case REDSTONE_BLAST -> generateParticleBeam(player, 80, new ParticleBuilder(Particle.REDSTONE), true, block -> {
                block.setType(AIR);
                world.spawn(block.getLocation(), TNTPrimed.class);
            }, null, hitPlayer -> {
                hitPlayer.damage(3.0);
                hitPlayer.setNoActionTicks(100);
            });
            case REDSTONE_DASH -> {
                Vector direction = player.getLocation().getDirection().normalize();
                player.setVelocity(new Vector(direction.getX() * 1.5, 0.05, direction.getZ() * 1.5));
            }
            case DISPENSER_WALL -> {
                // Make 3x3 wall of dispensers that constantly shoot
            }
            case POWER_BOOST -> {
                opponentData.matchModifiers.add("boosted_abilities");
                Bukkit.getScheduler().runTaskLater(WizardDuels.PLUGIN, () -> playerData.matchModifiers.remove("boosted_abilities"), RANDOM.nextLong(100, 180));
            }

            // ========= POTION MASTER =========
            case LITTLE_ACCIDENT -> {
                ThrownPotion potion = player.launchProjectile(ThrownPotion.class);
                PotionMeta meta = potion.getPotionMeta();
                meta.addCustomEffect(new PotionEffect(Randomizer.fromArray(BAD_EFFECTS), 10, 0), true);
                meta.setColor(Color.WHITE);
                potion.setPotionMeta(meta);
            }
            case COCKTAIL -> {
                ThrownPotion potion = player.launchProjectile(ThrownPotion.class);
                PotionMeta meta = potion.getPotionMeta();
                meta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 10, 2), false);
                meta.addCustomEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 10, 0), false);
                meta.addCustomEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10, 0), false);
                meta.setColor(Color.GREEN);
                potion.setPotionMeta(meta);
            }
            case MAGIC_CULT -> {
                for (int i = 0; i < b(playerData, 6, 8); i++) {
                    Monster entity = world.spawn(loc, (Class<? extends Monster>) (i > b(playerData, 3, 4) ? Evoker.class : Witch.class));
                    entity.setTarget(opponentData.player);
                    entity.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(b(playerData, 2.5, 3)));
                }
            }
            case ORANGE_JUICE -> {
                ThrownPotion potion = player.launchProjectile(ThrownPotion.class);
                PotionMeta meta = potion.getPotionMeta();
                meta.addCustomEffect(new PotionEffect(Randomizer.fromArray(GOOD_EFFECTS), 10, 0), true);
                meta.setColor(Color.WHITE);
                potion.setPotionMeta(meta);
            }
        }
        match.history.add(new TurnData(ability, true, playerData, LocalDateTime.now()));
    }

    @Contract(pure = true)
    private static int b(@NotNull PlayerData player, int original, int boosted) {
        return player.matchModifiers.contains("boosted_abilities") ? boosted : original;
    }

    @Contract(pure = true)
    private static double b(@NotNull PlayerData player, double original, double boosted) {
        return player.matchModifiers.contains("boosted_abilities") ? boosted : original;
    }

    public static @NotNull Location getRandomLocation(@NotNull Location center, int radius) {
        double x = center.getX() + (Math.random() - 0.5) * radius * 2;
        double z = center.getZ() + (Math.random() - 0.5) * radius * 2;
        return new Location(center.getWorld(), x, center.getY(), z);
    }

    public static int specialRandom10() {
        final int[] thresholds = { 1, 3, 6, 10, 15, 21, 28, 36, 45, 55 };

        int random = RANDOM.nextInt(1, 56);
        int fin = 10;
        for (int i = 0; i < thresholds.length; i++) {
            if (random <= thresholds[i]) {
                fin = 10 - i;
                break;
            }
        }
        return fin;
    }

    public static void generateParticleBeam(@NotNull Player player, int length, ParticleBuilder particle, boolean physics, Consumer<Block> blockHitAction, Consumer<LivingEntity> nonPlayerEntityHitAction, Consumer<Player> playerHitAction) {
        Location startLocation = player.getEyeLocation();
        Vector direction = startLocation.getDirection().normalize();

        int distanceTraveled = 0;

        while (distanceTraveled < length) {
            Location particleLocation = startLocation.clone().add(direction.clone().multiply(distanceTraveled));
            particle.location(particleLocation).spawn();

            for (LivingEntity entity : particleLocation.getWorld().getNearbyLivingEntities(particleLocation, 1, 1, 1)) {
                if (entity instanceof Player hitPlayer && !entity.equals(player) && playerHitAction != null) {
                    playerHitAction.accept(hitPlayer);
                } else if (nonPlayerEntityHitAction != null) {
                    nonPlayerEntityHitAction.accept(entity);
                }
            }

            Block block = particleLocation.getBlock();
            if (!block.getType().isAir() && block.getType().isBlock()) {
                if (blockHitAction != null) blockHitAction.accept(block);
                if (physics) distanceTraveled = length + 1;
            }

            distanceTraveled++;
        }
    }

    public static void generate3dBall(@NotNull Location center, double radius, int points, Consumer<Location> forPoint) {
        double centerX = center.x();
        double centerY = center.y();
        double centerZ = center.z();

        for (int i = 0; i < points; i++) {
            for (int j = 0; j < points; j++) {
                double theta = 2 * Math.PI * i / points;
                double phi = Math.PI * j / points;
                double x = centerX + radius * Math.sin(phi) * Math.cos(theta);
                double y = centerY + radius * Math.cos(phi);
                double z = centerZ + radius * Math.sin(phi) * Math.sin(theta);

                forPoint.accept(new Location(center.getWorld(), x, y, z));
            }
        }
    }
}
