package com.wizardduels.wands;

import com.wizardduels.WizardDuels;
import com.wizardduels.match.Basic1v1;
import com.wizardduels.match.Match;
import io.papermc.paper.event.entity.EntityMoveEvent;
import net.hectus.color.McColor;
import net.hectus.util.Randomizer;
import net.kyori.adventure.sound.SoundStop;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import static org.bukkit.entity.EntityType.*;

public class WandUseEvent implements Listener {
    public static final Random RANDOM = new Random();
    public static boolean CRYSTAL_ACTIVE = false;
    public static final List<Class<? extends Entity>> NECROMANCER_WAND_CREATURES = List.of(Zombie.class, Skeleton.class, Spider.class, Silverfish.class, Witch.class, Pillager.class, Guardian.class);
    public static final List<EntityType> NECROMANCER_WAND_CREATURE_TYPES = List.of(ZOMBIE, SKELETON, SPIDER, SILVERFISH, WITCH, PILLAGER, GUARDIAN);
    public static Player ICE_STORM = null;
    public static Player FIRE_RING = null;
    public static Player STORM_WALL = null;
    public static Player ICY_FEET = null;
    public static Player OPPONENTS_NO_MOVEMENT = null;


    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        if (!event.hasItem()) return;

        Player player = event.getPlayer();

        wandUse(Objects.requireNonNull(event.getItem()).getType(), UseType.fromAction(event.getAction(), player), player);
    }

    public void wandUse(@NotNull Material item, UseType use, @NotNull Player player) {
        Location loc = player.getLocation();
        World world = player.getWorld();

        Match match = WizardDuels.currentMatch;

        switch (item) {
            // =========== EXPLOSION ===========
            case GOLDEN_HOE -> {
                switch (use) {
                    case LMB -> {
                        player.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
                        player.launchProjectile(Fireball.class);
                    }
                    case RMB -> {
                        player.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

                        Vector velocity = player.getVelocity();
                        double launchVelocityY = Math.sqrt(2 * 0.08 * 15.0 * ((velocity.getY() / 2) + 1));

                        player.setVelocity(new Vector(velocity.getX(), launchVelocityY, velocity.getZ()));
                    }
                    case SLMB -> {
                        player.playSound(loc, Sound.ENTITY_CREEPER_HURT, 1.0f, 1.0f);
                        Creeper creeper = player.getWorld().spawn(loc, Creeper.class);
                        creeper.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(2.5));
                    }
                    case SRMB -> {
                        for (int i = 0; i < 360; i += 5) {
                            double angle = Math.toRadians(i);
                            double radius = 2;
                            double x = loc.getX() + Math.cos(angle) * radius;
                            double z = loc.getZ() + Math.sin(angle) * radius;
                            Location particleLocation = new Location(world, x, loc.getY(), z);

                            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 1);
                            world.spawnParticle(Particle.REDSTONE, particleLocation, 1, 0, 0, 0, 1, dustOptions, true);

                            for (int j = 1; j <= 3; j++) {
                                double thickness = j * 0.1;
                                x = loc.getX() + Math.cos(angle) * (radius + thickness);
                                z = loc.getZ() + Math.sin(angle) * (radius + thickness);
                                particleLocation = new Location(world, x, loc.getY(), z);
                                world.spawnParticle(Particle.REDSTONE, particleLocation, 0, 0, 0, 0, 1, dustOptions, true);
                            }
                        }

                        Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> {
                            for (int x = -2; x <= 1; x++) {
                                for (int z = -2; z <= 1; z++) {
                                    Location tntLocation = loc.clone().add(x, 0, z);
                                    TNTPrimed tnt = world.spawn(tntLocation, TNTPrimed.class);
                                    tnt.setFuseTicks(10);
                                }
                            }
                        }, 60);
                    }
                }
            }
            // ============== ICE ==============
            case IRON_HOE -> {
                switch (use) {
                    case LMB -> {
                        player.playSound(loc, Sound.BLOCK_SNOW_PLACE, 1.5f, 2.5f);

                        Location playerLocation = player.getEyeLocation();
                        for (int i = 1; i <= 5; i++) {


                            Location spawnLocation = playerLocation.clone().add(playerLocation.getDirection().multiply(i + 3));
                            EvokerFangs evokerFang = world.spawn(spawnLocation, EvokerFangs.class);
                            evokerFang.setOwner(player);
                            evokerFang.stopSound(SoundStop.named(Sound.ENTITY_EVOKER_FANGS_ATTACK));
                        }
                    }
                    case RMB -> {
                        ICY_FEET = player;
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));

                        Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> ICY_FEET = null, 200L);
                    }
                    case SLMB -> {
                        OPPONENTS_NO_MOVEMENT = player;
                        Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> OPPONENTS_NO_MOVEMENT = null, 200L);
                    }
                    case SRMB -> {
                        ICE_STORM = player;
                        Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> ICE_STORM = null, 200L);

                        new BukkitRunnable() {
                            double angle = 0;
                            double duration = 0;

                            @Override
                            public void run() {
                                player.playSound(player.getLocation(), Sound.ENTITY_HORSE_BREATHE, 0.5f, 1.0f);

                                if (duration >= 10) {
                                    ICE_STORM = null;
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
                }
            }
            // ============= ENDER =============
            case NETHERITE_HOE -> {
                switch (use) {
                    case LMB -> player.launchProjectile(DragonFireball.class);
                    case RMB -> {
                        Block block = player.getTargetBlockExact(10);
                        if (block == null) {
                            player.sendMessage(McColor.YELLOW + "You have to look at a block in a 10 block range!");
                            return;
                        }
                        // TODO: Check if the block is inside the map
                        player.teleport(new Location(world, block.getX(), block.getY() + 1, block.getZ(), loc.getYaw(), loc.getPitch()));
                    }
                    case SLMB -> {
                        Location playerLocation = player.getLocation();
                        Location wallStartLocation = playerLocation.add(playerLocation.getDirection().normalize().multiply(5));

                        for (int x = -2; x <= 2; x++) {
                            for (int y = 0; y <= 4; y++) {
                                Location blockLocation = wallStartLocation.clone().add(x, y, 0);
                                blockLocation.getBlock().setType(Material.END_STONE);
                            }
                        }
                    }
                    case SRMB -> {
                        if (match instanceof Basic1v1 basic1v1) {
                            Location location1 = basic1v1.team1.getLocation();
                            basic1v1.team1.teleport(basic1v1.team2);
                            basic1v1.team2.teleport(location1);
                        }
                    }
                }
            }
            // ============ DRAGON =============
            case NETHERITE_SHOVEL -> {
                switch (use) {
                    case LMB -> {
                        Block block = player.getTargetBlockExact(10);
                        if (block == null) {
                            player.sendMessage(McColor.YELLOW + "You have to look at a block in a 10 block range!");
                            return;
                        }
                        Location spawnLocation = block.getLocation().clone().add(0, 1, 0);

                        Fireball fireball = world.spawn(spawnLocation, DragonFireball.class);
                        fireball.setIsIncendiary(false);
                        Vector direction = block.getLocation().toVector().subtract(spawnLocation.toVector()).normalize();
                        fireball.setDirection(direction);

                        fireball.setGravity(true);
                    }
                    case RMB -> {
                        // Wings and fly around
                    }
                    case SLMB -> {
                        if (match instanceof Basic1v1 basic1v1) {
                            Player target = basic1v1.team1 == player ? basic1v1.team1 : basic1v1.team2;

                            EnderDragon dragon = world.spawn(player.getLocation(), EnderDragon.class);

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    Vector direction = target.getLocation().toVector().subtract(dragon.getLocation().toVector()).normalize();

                                    dragon.setVelocity(direction.multiply(0.5));

                                    if (dragon.getLocation().distance(target.getLocation()) < 2.0) {
                                        Vector throwVelocity = new Vector(Math.random() - 0.5, Math.random() * 2, Math.random() - 0.5).normalize().multiply(2.0);
                                        target.setVelocity(throwVelocity);
                                        cancel();
                                    }
                                }
                            }.runTaskTimer(WizardDuels.getPlugin(WizardDuels.class), 0, 1);
                        }
                    }
                    case SRMB -> {
                        Location randomLocation = getRandomLocation(loc, 10);
                        world.spawn(randomLocation, EnderCrystal.class);
                        CRYSTAL_ACTIVE = true;
                        player.setNoDamageTicks(Integer.MAX_VALUE);
                    }
                }
            }
            // ========== NECROMANCER ==========
            case DIAMOND_HOE -> {
                switch (use) {
                    case LMB -> {
                        player.playSound(loc, Sound.ENTITY_VEX_CHARGE, 1.0f, 1.0f);

                        Entity entity = player.getWorld().spawn(loc, NECROMANCER_WAND_CREATURES.get(RANDOM.nextInt(NECROMANCER_WAND_CREATURES.size())));

                        entity.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(2.5));
                    }
                    case RMB -> {
                        Horse horse = world.spawn(loc, Horse.class);
                        horse.getInventory().setArmor(new ItemStack(Material.DIAMOND_HORSE_ARMOR));
                        horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
                        horse.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, -1, 2));
                        horse.setOwner(player);
                        horse.addPassenger(player);
                    }
                    case SLMB -> {
                        boolean bool = RANDOM.nextBoolean();
                        Class<? extends Entity> mob = bool ? Zombie.class : Skeleton.class;

                        for (int i = 0; i < 5; i++) {
                            Monster entity = (Monster) world.spawn(loc, mob);
                            entity.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
                            entity.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
                            entity.getEquipment().setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
                            entity.getEquipment().setBoots(new ItemStack(Material.NETHERITE_BOOTS));
                            entity.getEquipment().setItemInMainHand(new ItemStack(bool ? Material.IRON_SWORD : Material.BOW));

                            entity.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(2.5));
                        }
                    }
                    case SRMB -> {
                        EntityType entityType = NECROMANCER_WAND_CREATURE_TYPES.get(RANDOM.nextInt(NECROMANCER_WAND_CREATURE_TYPES.size()));

                        Location spawnerLocation = loc.clone();
                        spawnerLocation.add(0, 5, 0);

                        spawnerLocation.getBlock().setType(Material.SPAWNER);
                        CreatureSpawner spawner = (CreatureSpawner) spawnerLocation.getBlock().getState();
                        spawner.setSpawnedType(entityType);
                        spawner.update();
                    }
                }
            }
            // ============ NETHER =============
            case GOLDEN_SHOVEL -> {
                switch (use) {
                    case LMB -> {
                        Fireball fireball = player.launchProjectile(Fireball.class);
                        fireball.setVelocity(fireball.getVelocity());
                    }
                    case RMB -> {
                        Ghast ghast = world.spawn(loc, Ghast.class);
                        ghast.addPassenger(player);
                    }
                    case SLMB -> {
                        player.playSound(loc, Sound.ENTITY_GHAST_SCREAM, 1.0f, 1.0f);

                        new BukkitRunnable() {
                            int secsLeft = RANDOM.nextInt(6, 11);

                            @Override
                            public void run() {
                                player.launchProjectile(Fireball.class);

                                secsLeft--;
                                if (secsLeft == 0) cancel();
                            }
                        }.runTaskTimer(WizardDuels.getPlugin(WizardDuels.class), 0, 20L);
                    }
                    case SRMB -> {
                        FIRE_RING = player;

                        Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> FIRE_RING = null, 200);
                    }
                }
            }
            // ============ WEATHER ============
            case TRIDENT -> {
                switch (use) {
                    case LMB -> {
                        if (match instanceof Basic1v1 basic1v1) {
                            Player target = basic1v1.team1 == player ? basic1v1.team2 : basic1v1.team1;
                            world.strikeLightning(target.getLocation());
                        }
                    }
                    case RMB -> {
                        Vector direction = player.getLocation().getDirection().normalize();
                        player.setVelocity(new Vector(direction.getX() * 2, 1.2, direction.getZ() * 2));
                    }
                    case SLMB -> {
                        for (int i = 0; i < 100; i++) {
                            double angle = 2 * Math.PI * i / 100;
                            double xOffset = 2 * Math.cos(angle);
                            double zOffset = 2 * Math.sin(angle);

                            Location particleLocation = new Location(player.getWorld(), loc.getX() + xOffset, loc.getY(), loc.getZ() + zOffset);

                            player.spawnParticle(Particle.REDSTONE, particleLocation, 0, 0, 0, 0, 1);
                        }

                        STORM_WALL = player;

                        Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> STORM_WALL = null, 200);
                    }
                    case SRMB -> {
                        // Crazy tornado with custom AI and stuff
                        // Has to just wander around pretty randomly
                    }
                }
            }
            // TODO: ===== TIME WARP ===========
            case CLOCK -> {
                switch (use) {
                    case LMB -> {
                        // Throw clock as a projectile
                        // Create a field, where all players in it have 50% movement and attack speed
                    }
                    case RMB -> {
                        // Log movements of each player
                        // Teleport player to that location
                    }
                    case SLMB -> {
                        // Create time-warp bubble around player
                        // Stop projectiles from entering that bubble
                    }
                    case SRMB -> {
                        // Clone the player 2 blocks before him
                        // Mimic the player's movements 1:1
                    }
                }
            }
            // ============ GRAVITY ============
            case IRON_AXE -> {
                switch (use) {
                    case LMB -> {
                        // Fire graviton beam where player looks at
                        if (match instanceof Basic1v1 basic1v1) {
                            Player target = basic1v1.team1 == player ? basic1v1.team2 : basic1v1.team1;
                            target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 200, 127, true, false));
                            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 0, true, false));
                        }
                    }
                    case RMB -> {
                        player.setGravity(false);

                        Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> player.setGravity(true), 200L);
                    }
                    case SLMB -> {
                        player.playSound(loc, Sound.ENTITY_RAVAGER_STUNNED, 1.0f, 1.0f);

                        for (Player target : Bukkit.getOnlinePlayers()) {
                            if (target != player && loc.distance(target.getLocation()) <= 10.0) {
                                Vector direction = target.getLocation().toVector().subtract(loc.toVector()).normalize();

                                target.setVelocity(direction.multiply(1.5));
                            }
                        }
                    }
                    case SRMB -> {
                        // CRAZY BLACK HOLE?!?!
                    }
                }
            }
        }
    }

    @EventHandler
    public void onVehicleExit(@NotNull VehicleExitEvent event) {
        if (event.getVehicle() instanceof Horse horse) {
            horse.eject();
            horse.remove();
        } else if (event.getVehicle() instanceof Ghast ghast) {
            ghast.eject();
            ghast.remove();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (ICY_FEET != null) {
            if (ICY_FEET == event.getPlayer()) {
                Location loc = event.getPlayer().getLocation().subtract(0, 1, 0);
                Material originalBlock = loc.getBlock().getType();
                loc.getBlock().setType(Material.BLUE_ICE);

                Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> loc.getBlock().setType(originalBlock), 40L);
            }
        }

        if (FIRE_RING != null) {
            Location loc = FIRE_RING.getLocation();

            if (FIRE_RING == event.getPlayer()) {
                for (int i = 0; i < 360; i += 3) {
                    double angle = Math.toRadians(i);
                    double x = loc.getX() + 10 * Math.cos(angle);
                    double z = loc.getZ() + 10 * Math.sin(angle);
                    Location particleLocation = new Location(WizardDuels.WORLD, x, loc.getY(), z);

                    WizardDuels.WORLD.spawnParticle(Particle.REDSTONE, particleLocation, 0, new Particle.DustOptions(Color.RED, 1));
                }
            }

            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target != FIRE_RING && loc.distance(target.getLocation()) <= 10) {
                    target.setFireTicks(100);
                    target.damage(0.5);
                }
            }
        }

        if (ICE_STORM != null) {
            Location loc = ICE_STORM.getLocation();

            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target != ICE_STORM && loc.distance(target.getLocation()) <= 4.0) {
                    Vector direction = target.getLocation().toVector().subtract(loc.toVector()).normalize();

                    target.setVelocity(direction.multiply(0.5));
                }
            }
        }

        if (OPPONENTS_NO_MOVEMENT != null) {
            if (event.getPlayer() != OPPONENTS_NO_MOVEMENT) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityMove(@NotNull EntityMoveEvent event) {
        if (event.getEntity() instanceof Projectile projectile) {
            if (STORM_WALL == null) return;

            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target != STORM_WALL && STORM_WALL.getLocation().distance(target.getLocation()) <= 2) {
                    Vector currentVelocity = projectile.getVelocity();
                    Vector invertedVelocity = new Vector(-currentVelocity.getX(), -currentVelocity.getY(), -currentVelocity.getZ());
                    projectile.setVelocity(invertedVelocity);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof LightningStrike) {
            event.setDamage(6);
        }

        if (event.getDamager() instanceof Player player) {
            if (STORM_WALL == player) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(@NotNull EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (STORM_WALL == player) {
                event.setDamage(event.getDamage() / 2);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (CRYSTAL_ACTIVE) {
            if (event.getEntity() instanceof EnderCrystal) {
                CRYSTAL_ACTIVE = false;
                event.getDrops().clear();

                for (Player player : WizardDuels.WORLD.getPlayers()) {
                    if (player.getNoDamageTicks() != 0 && player.getInventory().contains(Material.NETHERITE_SHOVEL)) {
                        player.setNoDamageTicks(0);
                    }
                }
            }
        }
    }

    private @NotNull Location getRandomLocation(@NotNull Location center, int radius) {
        double x = center.getX() + (Math.random() - 0.5) * radius * 2;
        double z = center.getZ() + (Math.random() - 0.5) * radius * 2;
        return new Location(center.getWorld(), x, center.getY(), z);
    }

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
}
