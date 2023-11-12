package net.spellboundmc.wands;

import io.papermc.paper.event.entity.EntityMoveEvent;
import net.hectus.color.McColor;
import net.hectus.util.Randomizer;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.spellboundmc.Translation;
import net.spellboundmc.WizardDuels;
import net.spellboundmc.match.Basic1v1;
import net.spellboundmc.match.Match;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;

import static org.bukkit.Material.*;
import static org.bukkit.block.BlockFace.*;
import static org.bukkit.entity.EntityType.*;

public class WandUseEvent implements Listener {
    public static final Random RANDOM = new Random();
    public static boolean CRYSTAL_ACTIVE = false;
    public static final List<Class<? extends Entity>> NECROMANCER_WAND_CREATURES = List.of(Zombie.class, Skeleton.class, Spider.class, Silverfish.class, Witch.class, Pillager.class, Guardian.class);
    public static final List<EntityType> NECROMANCER_WAND_CREATURE_TYPES = List.of(ZOMBIE, SKELETON, SPIDER, SILVERFISH, WITCH, PILLAGER, GUARDIAN);
    public static Player ICE_STORM, FIRE_RING, STORM_WALL, ICY_FEET, OPPONENTS_NO_MOVEMENT, TORNADO, POISON_SKELETONS, DISABLED_WANDS, BOOSTED_ABILITIES;
    public static final Map<Material, Integer> SWORDS = Map.of(
            Material.IRON_SWORD, 30,
            Material.GOLDEN_SWORD, 30,
            Material.DIAMOND_SWORD, 25,
            Material.NETHERITE_SWORD, 10,
            Material.BOOK, 5
    );

    public static final List<Material> glitchLmbPossibilities = List.of(GOLDEN_HOE, IRON_HOE, NETHERITE_HOE,
            NETHERITE_SHOVEL, DIAMOND_HOE, GOLDEN_SHOVEL, Material.TRIDENT, CLOCK, IRON_AXE, WOODEN_SWORD,
            IRON_PICKAXE, DIAMOND_AXE, WOODEN_AXE, WOODEN_HOE, STONE_HOE, STICK, COPPER_INGOT);

    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        if (!event.hasItem()) return;

        Player player = event.getPlayer();

        wandUse(Objects.requireNonNull(event.getItem()).getType(), UseType.fromAction(event.getAction(), player), player);
    }

    public void wandUse(@NotNull Material item, UseType use, @NotNull Player player) {
        Locale l = player.locale();

        if (player == DISABLED_WANDS) {
            player.sendMessage(McColor.RED + Translation.get(l, "wand.error.disabled"));
            return;
        }

        Location loc = player.getLocation();
        World world = player.getWorld();

        Match match = WizardDuels.currentMatch;
        Basic1v1 basic1v1 = (Basic1v1) match; // TODO: Switch this to be compatible with 2v2 and Chaos too
        Player target = basic1v1.team1 == player ? basic1v1.team2 : basic1v1.team1;

        switch (item) {
            case DIAMOND -> {
                PlayerInventory inv = player.getInventory();
                inv.addItem(new ItemStack(Material.GOLDEN_AXE));
                for (Material material : glitchLmbPossibilities) {
                    inv.addItem(new ItemStack(material));
                }
            }
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
                        double launchVelocityY = Math.sqrt(b(player, 1, 2) * 2 * 0.08 * 15.0 * ((velocity.getY() / 2) + 1));

                        player.setVelocity(new Vector(velocity.getX(), launchVelocityY, velocity.getZ()));
                    }
                    case SLMB -> {
                        player.playSound(loc, Sound.ENTITY_CREEPER_HURT, 1.0f, 1.0f);
                        Creeper creeper = player.getWorld().spawn(loc, Creeper.class);
                        creeper.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(b(player, 2.5, 4)));
                    }
                    case SRMB -> {
                        for (int i = 0; i < 360; i += 5) {
                            double angle = Math.toRadians(i);
                            double radius = b(player, 2, 3);
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
                            int size = b(player, 2, 3);

                            for (int x = -size; x <= (size - 1); x++) {
                                for (int z = -size; z <= (size - 1); z++) {
                                    Location tntLocation = loc.clone().add(x, 0, z);
                                    TNTPrimed tnt = world.spawn(tntLocation, TNTPrimed.class);
                                    tnt.setFuseTicks(b(player, 2, 3));
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
                        for (int i = 1; i <= b(player, 5, 10); i++) {
                            Location spawnLocation = playerLocation.clone().add(playerLocation.getDirection().multiply(i + 3));
                            EvokerFangs evokerFang = world.spawn(spawnLocation, EvokerFangs.class);
                            evokerFang.setOwner(player);
                            evokerFang.stopSound(SoundStop.named(Sound.ENTITY_EVOKER_FANGS_ATTACK));
                        }
                    }
                    case RMB -> {
                        ICY_FEET = player;
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, b(player, 200, 400), 1));

                        Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> ICY_FEET = null, 200L);
                    }
                    case SLMB -> {
                        OPPONENTS_NO_MOVEMENT = player;
                        Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> OPPONENTS_NO_MOVEMENT = null, b(player, 100, 200));
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
                        Block block = player.getTargetBlockExact(b(player, 10, 20));
                        if (block == null) {
                            player.sendMessage(McColor.YELLOW + Translation.get(l, "wand.error.range", b(player, 10, 20)));
                            return;
                        }
                        // TODO: Check if the block is inside the map
                        player.teleport(new Location(world, block.getX(), block.getY() + 1, block.getZ(), loc.getYaw(), loc.getPitch()));
                    }
                    case SLMB -> {
                        Location playerLocation = player.getLocation();
                        Vector direction = playerLocation.getDirection().normalize();

                        double cosYaw = direction.getX();
                        double sinYaw = direction.getZ();

                        for (int x = -2; x <= 2; x++) {
                            for (int y = 0; y <= 4; y++) {
                                int rotatedX = (int) (x * cosYaw - y * sinYaw);
                                int rotatedZ = (int) (x * sinYaw + y * cosYaw);
                                Location blockLocation = playerLocation.clone().add(rotatedX, y, rotatedZ);
                                blockLocation.getBlock().setType(Material.END_STONE);
                            }
                        }
                    }
                    case SRMB -> {
                        Location location1 = basic1v1.team1.getLocation();
                        basic1v1.team1.teleport(basic1v1.team2);
                        basic1v1.team2.teleport(location1);
                    }
                }
            }
            // ============ DRAGON =============
            case NETHERITE_SHOVEL -> {
                switch (use) {
                    case LMB -> {
                        Block block = player.getTargetBlockExact(b(player, 10, 20));
                        if (block == null) {
                            player.sendMessage(McColor.YELLOW + Translation.get(l, "wand.error.range", b(player, 10, 20)));
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
                        player.setAllowFlight(true);
                        player.setFlying(false);

                        ItemStack elytra = new ItemStack(Material.ELYTRA);
                        player.getInventory().setChestplate(elytra);
                        elytra.editMeta(meta -> meta.setCustomModelData(1));

                        Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> {
                            player.setFlying(false);
                            player.setAllowFlight(false);
                            player.setNoDamageTicks(40);
                            player.getInventory().setChestplate(null);
                        }, b(player, 120, 240));
                    }
                    case SLMB -> {
                        EnderDragon dragon = world.spawn(player.getLocation(), EnderDragon.class);
                        dragon.setTarget(target);

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (dragon.getLocation().distance(target.getLocation()) < 2.0) {
                                    dragon.remove();
                                    cancel();
                                }
                            }
                        }.runTaskTimer(WizardDuels.getPlugin(WizardDuels.class), 0, 2);
                    }
                    case SRMB -> {
                        Location randomLocation = getRandomLocation(loc, b(player, 10, 15));
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
                        entity.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(b(player, 2.5, 4)));
                    }
                    case RMB -> {
                        Horse horse = world.spawn(loc, Horse.class);
                        horse.getInventory().setArmor(new ItemStack(Material.DIAMOND_HORSE_ARMOR));
                        horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
                        horse.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, -1, b(player, 2, 5)));
                        horse.setOwner(player);
                        horse.addPassenger(player);
                    }
                    case SLMB -> {
                        boolean bool = RANDOM.nextBoolean();
                        Class<? extends Entity> mob = bool ? Zombie.class : Skeleton.class;

                        for (int i = 0; i < b(player, 5, 8); i++) {
                            Monster entity = (Monster) world.spawn(loc, mob);
                            entity.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
                            entity.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
                            entity.getEquipment().setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
                            entity.getEquipment().setBoots(new ItemStack(Material.NETHERITE_BOOTS));
                            entity.getEquipment().setItemInMainHand(new ItemStack(bool ? Material.IRON_SWORD : Material.BOW));

                            entity.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(b(player, 2.5, 4)));
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
                    case LMB -> player.launchProjectile(Fireball.class);
                    case RMB -> {
                        Ghast ghast = world.spawn(loc, Ghast.class);
                        ghast.addPassenger(player);

                        Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), ghast::remove, 200);
                    }
                    case SLMB -> {
                        player.playSound(loc, Sound.ENTITY_GHAST_SCREAM, 1.0f, 1.0f);

                        new BukkitRunnable() {
                            int secsLeft = RANDOM.nextInt(6, 11) * b(player, 1, 2);
                            @Override
                            public void run() {
                                player.launchProjectile(Fireball.class);

                                secsLeft--;
                                if (secsLeft == 0) cancel();
                            }
                        }.runTaskTimer(WizardDuels.getPlugin(WizardDuels.class), 0, b(player, 20, 15));
                    }
                    case SRMB -> {
                        FIRE_RING = player;
                        Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> FIRE_RING = null, b(player, 200, 300));
                    }
                }
            }
            // ============ WEATHER ============
            case TRIDENT -> {
                switch (use) {
                    case LMB -> world.strikeLightning(target.getLocation());
                    case RMB -> {
                        Vector direction = player.getLocation().getDirection().normalize();
                        player.setVelocity(new Vector(direction.getX(), 0.6, direction.getZ()).multiply(b(player, 2, 3)));
                    }
                    case SLMB -> {
                        for (int i = 0; i < 100; i++) {
                            double angle = 2 * Math.PI * i / 100;
                            double xOffset = 2 * Math.cos(angle);
                            double zOffset = 2 * Math.sin(angle);

                            Location particleLocation = new Location(player.getWorld(), loc.getX() + xOffset, loc.getY() + 1, loc.getZ() + zOffset);

                            player.spawnParticle(Particle.REDSTONE, particleLocation, 0, 0, 0, 0, 1);
                        }

                        STORM_WALL = player;

                        Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> STORM_WALL = null, 200);
                    }
                    case SRMB -> {
                        Horse horse = world.spawn(loc, Horse.class);
                        horse.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, -1, 1, true, false));

                        new BukkitRunnable() {
                            public int ticks = 0;
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

                                for (Player player : world.getPlayers()) {
                                    if (player != TORNADO && player.getLocation().distance(horseLoc) < 5) {
                                        Vector direction = horseLoc.toVector().subtract(player.getLocation().toVector()).normalize();
                                        player.setVelocity(direction.multiply(0.05));
                                    }
                                }

                                ticks++;
                                ticks++;
                                if (ticks >= 300) {
                                    horse.remove();
                                    cancel();
                                }
                            }
                        }.runTaskTimer(WizardDuels.getPlugin(WizardDuels.class), 5, 2);
                    }
                }
            }
            // TODO: ===== TIME WAND ===========
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
                        Vector direction = loc.getDirection().normalize().multiply(8);

                        for (int i = 0; i < 20; i++) {
                            Location particleLocation = loc.clone().add(direction.clone().multiply(i));
                            world.spawnParticle(Particle.VILLAGER_HAPPY, particleLocation, 1);

                            for (Entity entity : world.getNearbyEntities(particleLocation, 1.5, 1.5, 1.5)) {
                                if (entity instanceof Player p && entity != player) {
                                    p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, b(player, 200, 300), 127, true, false));
                                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, b(player, 200, 300), 255, true, false));
                                }
                                i = 13;
                            }
                        }
                    }
                    case RMB -> {
                        player.setGravity(false);

                        Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> player.setGravity(true), b(player, 200, 300));
                    }
                    case SLMB -> {
                        player.playSound(loc, Sound.ENTITY_RAVAGER_STUNNED, 1.0f, 1.0f);

                        for (int radius = 1; radius <= b(player, 10.2, 15.4); radius++) {
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
                            if (p != player && loc.distance(p.getLocation()) <= b(player, 10, 15)) {
                                Vector direction = p.getLocation().toVector().subtract(loc.toVector()).normalize();
                                p.setVelocity(direction.multiply(1.5));
                            }
                        }
                    }
                    case SRMB -> {
                        // CRAZY BLACK HOLE?!?!
                    }
                }
            }
            // ============= SWORD =============
            case WOODEN_SWORD -> {
                switch (use) {
                    case LMB -> {
                        ItemStack sword = new ItemStack(Material.IRON_SWORD);

                        int randomValue = new Random().nextInt(100);

                        int cumulativeWeight = 0;
                        for (Map.Entry<Material, Integer> entry : SWORDS.entrySet()) {
                            cumulativeWeight += entry.getValue();
                            if (randomValue < cumulativeWeight) {
                                sword = new ItemStack(entry.getKey());

                                if (entry.getKey() == Material.BOOK) sword.addEnchantment(Enchantment.DAMAGE_ALL, 3);
                            }
                        }

                        Snowball ball = player.launchProjectile(Snowball.class);
                        ball.setItem(sword);
                        ball.setGravity(false);
                    }
                    case RMB -> {
                        Vector direction = player.getLocation().getDirection().normalize();
                        player.setVelocity(new Vector(direction.getX(), 0.05, direction.getZ()).multiply(b(player, 1, 1.5)));

                        Vector perpendicular = new Vector(-direction.getZ(), 0.05, direction.getX()).normalize();

                        Snowball rightSnowball = player.launchProjectile(Snowball.class);
                        rightSnowball.setItem(new ItemStack(Material.IRON_SWORD));
                        rightSnowball.teleport(player.getLocation().add(perpendicular.multiply(2)));
                        rightSnowball.setVelocity(player.getVelocity());

                        Snowball leftSnowball = player.launchProjectile(Snowball.class);
                        leftSnowball.setItem(new ItemStack(Material.IRON_SWORD));
                        leftSnowball.teleport(player.getLocation().subtract(perpendicular.multiply(2)));
                        leftSnowball.setVelocity(player.getVelocity());
                    }
                    case SLMB -> {
                        ItemStack sword = new ItemStack(Material.IRON_SWORD);

                        int randomValue = new Random().nextInt(100);
                        int cumulativeWeight = 0;
                        for (Map.Entry<Material, Integer> entry : SWORDS.entrySet()) {
                            cumulativeWeight += entry.getValue();
                            if (randomValue < cumulativeWeight) {
                                sword = new ItemStack(entry.getKey());

                                if (entry.getKey() == Material.BOOK) sword.addEnchantment(Enchantment.DAMAGE_ALL, 3);
                            }
                        }

                        Snowball ball = player.launchProjectile(Snowball.class);
                        ball.setItem(sword);

                        ball.teleport(target.getLocation().add(0, 2, 0));
                        Vector velocity = new Vector(0, -0.1, 0);
                        ball.setVelocity(velocity);
                    }
                    case SRMB -> {
                        // TODO: Add the factor of the wand level

                        for (int i = 0; i < b(player, 6, 10); i++) {
                            Monster entity = world.spawn(loc, (Class<? extends Monster>) (i > b(player, 3, 5) ? Skeleton.class : Zombie.class));
                            entity.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(b(player, 2.5, 3)));
                        }
                    }
                }
            }
            // ========= ELECTRIC WAND =========
            case IRON_PICKAXE -> {
                switch (use) {
                    case LMB -> {
                        Location lightning = getRandomLocation(loc, 10);
                        world.spawn(lightning, LightningStrike.class);

                        Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> {
                            world.spawn(getRandomLocation(lightning, 1), LightningStrike.class);
                            world.spawn(getRandomLocation(lightning, 1), LightningStrike.class);
                        }, 20L);
                    }
                    case RMB -> player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, b(player, 400, 600), 2));
                    case SLMB -> new BukkitRunnable() {
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
                            if (duration >= b(player, 200, 300)) cancel();
                        }
                    }.runTaskTimer(WizardDuels.getPlugin(WizardDuels.class), 0, 1);
                    case SRMB -> {
                        for (int i = 0; i < b(player, 3, 6); i++) {
                            Phantom phantom = world.spawn(loc, Phantom.class);
                            phantom.setTarget(target);
                        }
                    }
                }
            }
            // ============= SCULK =============
            case DIAMOND_AXE -> {
                switch (use) {
                    case LMB -> {
                        Vector direction = loc.getDirection().normalize().multiply(8);

                        for (int i = 0; i < 8; i++) {
                            Location particleLocation = loc.clone().add(direction.clone().multiply(i));
                            world.spawnParticle(Particle.SONIC_BOOM, particleLocation, 1);

                            for (Entity entity : world.getNearbyEntities(particleLocation, 3, 3, 3)) {
                                if (entity instanceof Player p && entity != player) {
                                    p.damage(2.0);
                                }
                            }
                        }
                    }
                    case RMB -> {
                        Block block = player.getTargetBlockExact(99);
                        if (block == null) {
                            player.sendMessage(McColor.YELLOW + Translation.get(l, "wand.error.range", 99));
                            return;
                        }
                        if (!block.getType().name().contains("SCULK")) {
                            player.sendMessage(McColor.YELLOW + Translation.get(l, "wand.sculk.rmb.error"));
                            return;
                        }
                        // TODO: Check if the block is inside the map
                        player.teleport(new Location(world, block.getX(), block.getY() + 1, block.getZ(), loc.getYaw(), loc.getPitch()));
                    }
                    case SLMB -> {
                        Warden warden = world.spawn(getRandomLocation(loc, 10), Warden.class);
                        warden.setHealth(50);
                        warden.setTarget(target);
                        warden.setAnger(target, 100);
                    }
                    case SRMB -> {
                        final List<BlockFace> blockFaces = List.of(UP, DOWN, NORTH, EAST, SOUTH, WEST);

                        Block startBlock = loc.subtract(0, 1, 0).getBlock();
                        startBlock.setType(Material.SCULK);
                        new BukkitRunnable() {
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
                                left--;
                                if (left <= 0) cancel();
                            }
                        }.runTaskTimer(WizardDuels.getPlugin(WizardDuels.class), 0, 10);
                    }
                }
            }
            // ============= VENOM =============
            case WOODEN_AXE -> {
                switch (use) {
                    case LMB -> target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
                    case RMB -> {
                        LivingEntity closest = target;
                        for (LivingEntity entity : world.getLivingEntities()) {
                            if (entity.hasPotionEffect(PotionEffectType.POISON)) {
                                if (entity.getLocation().distance(loc) < closest.getLocation().distance(loc)) {
                                    closest = entity;
                                }
                            }
                        }
                        if (!closest.hasPotionEffect(PotionEffectType.POISON)) {
                            player.sendMessage(McColor.YELLOW + Translation.get(l, "wand.venom.rmb.error"));
                            return;
                        }
                        player.teleport(closest);
                    }
                    case SLMB -> {
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
                    case SRMB -> {
                        for (int i = 0; i < 3; i++) {
                            Skeleton skeleton = world.spawn(loc, Skeleton.class);
                            skeleton.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
                            skeleton.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                            skeleton.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                            skeleton.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));
                            skeleton.addScoreboardTag("venomous");
                            skeleton.setTarget(target);
                        }
                        POISON_SKELETONS = player;

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (POISON_SKELETONS == null) cancel();

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
                        }.runTaskTimer(WizardDuels.getPlugin(WizardDuels.class), 0, 20);
                    }
                }
            }
            // =========== DARK WAND ===========
            case WOODEN_HOE -> {
                switch (use) {
                    case LMB -> {
                        Vector direction = loc.getDirection().normalize().multiply(8);

                        for (int i = 0; i < 8; i++) {
                            Location particleLocation = loc.clone().add(direction.clone().multiply(i));
                            world.spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, particleLocation, 1);

                            if (!particleLocation.getBlock().getType().isAir()) {
                                particleLocation.getBlock().setType(Material.AIR);
                                world.spawn(particleLocation, TNTPrimed.class);
                                break;
                            }

                            for (Entity entity : world.getNearbyEntities(particleLocation, 3, 2, 3)) {
                                if (entity instanceof Player p && entity != player) {
                                    p.damage(2.0);
                                }
                            }
                        }
                    }
                    case RMB -> {
                        Vector direction = player.getLocation().getDirection().normalize();
                        player.setVelocity(new Vector(direction.getX(), 0.5, direction.getZ()));

                        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities(player.getEyeLocation(), direction, 5);

                        if (rayTraceResult != null && rayTraceResult.getHitEntity() instanceof LivingEntity hitEntity) {
                            double damage = 4;
                            hitEntity.damage(damage);
                            hitEntity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 5));
                        }
                    }
                    case SLMB -> target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 140, 5));
                    case SRMB -> {
                        Location spawnLoc = loc.clone();

                        Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> {
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
                }
            }
            // =========== OF HEALTH ===========
            case STONE_HOE -> {
                switch (use) {
                    case LMB -> player.setHealth(player.getHealth() + 10);
                    case RMB -> {
                        Vector direction = player.getLocation().getDirection().normalize();
                        player.setVelocity(new Vector(direction.getX(), 0.05, direction.getZ()));

                        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities(player.getEyeLocation(), direction, 5);
                        if (rayTraceResult != null && rayTraceResult.getHitEntity() instanceof LivingEntity hitEntity && rayTraceResult.getHitEntity() != player) hitEntity.damage(5);
                    }
                    case SLMB -> player.setNoDamageTicks(200);
                    case SRMB -> {
                        int fin = specialRandom10();
                        target.damage(fin);
                        player.setHealth(player.getHealth() + fin);
                    }
                }
            }
            // ========== GLITCH WAND ==========
            case GOLDEN_AXE -> {
                switch (use) {
                    case LMB -> {
                        if (Randomizer.boolByChance(80)) {
                            wandUse((Material) Randomizer.fromCollection(glitchLmbPossibilities), use, player);
                        } else {
                            player.playSound(player, Sound.BLOCK_GLASS_BREAK, 0.5f, 0.8f);
                            player.sendMessage(McColor.YELLOW + Translation.get(l, "wand.glitch.lmb.fail"));
                        }
                    }
                    case RMB -> {
                        Vector teleportVector = loc.getDirection().multiply(RANDOM.nextInt(25));
                        player.teleport(loc.add(teleportVector));
                    }
                    case SLMB -> {
                        int seconds = specialRandom10();
                        DISABLED_WANDS = target;
                        target.showTitle(Title.title(Component.empty(), Component.text(McColor.RED + Translation.get(l, "wand.glitch.slmb.opponent_title", seconds)), Title.Times.times(Duration.ZERO, Duration.ofSeconds(seconds / 2), Duration.ZERO)));
                        Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> {
                            DISABLED_WANDS = null;
                            player.sendMessage(McColor.GREEN + Translation.get(l, "wand.glitch.slmb.opponent_end"));
                        }, seconds * 20L);
                    }
                    case SRMB -> {
                        // Complicated shit
                    }
                }
            }
            // ============ WIZARDS ============
            case STICK -> {
                switch (use) {
                    case LMB -> {
                        Vector direction = loc.getDirection().normalize().multiply(8);

                        for (int i = 0; i < 80; i++) {
                            Location particleLocation = loc.clone().add(direction.clone().multiply(i));
                            world.spawnParticle((Particle) Randomizer.fromArray(Particle.values()), particleLocation, 1);

                            if (!particleLocation.getBlock().getType().isAir()) {
                                particleLocation.getBlock().setType(Material.AIR);
                                TNTPrimed tnt = world.spawn(particleLocation, TNTPrimed.class);
                                tnt.setFuseTicks(1);
                                break;
                            }
                        }
                    }
                    case RMB -> {
                        Location oldLoc = loc.clone();

                        Block block = player.getTargetBlockExact(10);
                        if (block == null) {
                            player.sendMessage(McColor.YELLOW + Translation.get(l, "wand.error.range", 10));
                            return;
                        }
                        // TODO: Check if the block is inside the map
                        player.teleport(new Location(world, block.getX(), block.getY() + 1, block.getZ(), loc.getYaw(), loc.getPitch()));

                        world.spawn(oldLoc, LightningStrike.class);
                    }
                    case SLMB -> {
                        Class<? extends Entity> entityClass;

                        do {
                            entityClass = ((EntityType) Randomizer.fromArray(EntityType.values())).getEntityClass();
                        } while (entityClass == null || !Monster.class.isAssignableFrom(entityClass));

                        Monster monster = (Monster) world.spawn(getRandomLocation(loc, 4), entityClass);
                        monster.setTarget(target);
                        monster.setHealth(monster.getHealth() * 1.5);
                        monster.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, -1, 2, true, false));
                    }
                    case SRMB -> {
                        Vector direction = loc.getDirection().normalize().multiply(8);

                        for (int i = 0; i < 12; i++) {
                            Location particleLocation = loc.clone().add(direction.clone().multiply(i));
                            world.spawnParticle(Particle.WAX_OFF, particleLocation, 1);

                            if (!particleLocation.getBlock().getType().isAir()) {
                                particleLocation.getBlock().setType(Material.AIR);
                                TNTPrimed tnt = world.spawn(particleLocation, TNTPrimed.class);
                                tnt.setFuseTicks(1);
                                break;
                            }

                            for (Entity entity : world.getNearbyEntities(particleLocation, 3, 3, 3)) {
                                if (entity != player) {
                                    if (entity instanceof Player p) {
                                        DISABLED_WANDS = p;
                                        Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> DISABLED_WANDS = null, 100);
                                    } else if (entity instanceof LivingEntity e) {
                                        e.setNoActionTicks(200);
                                    }
                                    i = 13;
                                }
                            }
                        }
                    }
                }
            }
            // ========= REDSTONE WAND =========
            case COPPER_INGOT -> {
                switch (use) {
                    case LMB -> {
                        Vector direction = loc.getDirection().normalize().multiply(8);

                        for (int i = 0; i < 12; i++) {
                            Location particleLocation = loc.clone().add(direction.clone().multiply(i));
                            world.spawnParticle(Particle.WAX_OFF, particleLocation, 1);

                            if (!particleLocation.getBlock().getType().isAir()) {
                                particleLocation.getBlock().setType(Material.AIR);
                                TNTPrimed tnt = world.spawn(particleLocation, TNTPrimed.class);
                                tnt.setFuseTicks(1);
                                break;
                            }

                            for (Entity entity : world.getNearbyEntities(particleLocation, 0.5, 0.8, 0.5)) {
                                if (entity != player) {
                                    if (entity instanceof Player p) {
                                        DISABLED_WANDS = p;
                                        Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> DISABLED_WANDS = null, 100);
                                    } else if (entity instanceof LivingEntity e) {
                                        e.setNoActionTicks(200);
                                    }
                                    i = 13;
                                }
                            }
                        }
                    }
                    case RMB -> {
                        Vector direction = player.getLocation().getDirection().normalize();
                        player.setVelocity(new Vector(direction.getX() * 1.5, 0.05, direction.getZ() * 1.5));
                    }
                    case SLMB -> {
                        // Make 3x3 wall of dispensers that constantly shoot
                    }
                    case SRMB -> {
                        BOOSTED_ABILITIES = player;
                        Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> BOOSTED_ABILITIES = null, RANDOM.nextLong(100, 180));
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
        }
    }

    @EventHandler
    public void onEntityTargetLivingEntity(@NotNull EntityTargetLivingEntityEvent event) {
        if (event.getEntity() instanceof Skeleton skeleton) {
            if (!skeleton.getScoreboardTags().contains("venomous")) return;

            Basic1v1 basic1v1 = (Basic1v1) WizardDuels.currentMatch; // TODO: Switch this to be compatible with 2v2 and Chaos too
            Player target = basic1v1.team1 == POISON_SKELETONS ? basic1v1.team1 : basic1v1.team2;

            if (event.getTarget() == POISON_SKELETONS) event.setTarget(target);
        }
    }

    @EventHandler
    public void onEntityShootBow(@NotNull EntityShootBowEvent event) {
        if (event.getEntity().getScoreboardTags().contains("venomous")) {
            if (event.getProjectile() instanceof Arrow arrow) {
                arrow.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 5, 0), true);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (ICY_FEET != null) {
            if (ICY_FEET == event.getPlayer()) {
                Location loc = event.getPlayer().getLocation().subtract(0, 1, 0);
                loc.getBlock().setType(Material.BLUE_ICE);
            }
        }

        if (FIRE_RING != null) {
            Location loc = FIRE_RING.getLocation();

            if (FIRE_RING == event.getPlayer()) {
                for (int i = 0; i < 360; i += 3) {
                    double angle = Math.toRadians(i);
                    double x = loc.getX() + b(FIRE_RING, 10, 15) * Math.cos(angle);
                    double z = loc.getZ() + b(FIRE_RING, 10, 15) * Math.sin(angle);
                    Location particleLocation = new Location(WizardDuels.WORLD, x, loc.getY(), z);

                    WizardDuels.WORLD.spawnParticle(Particle.REDSTONE, particleLocation, 0, new Particle.DustOptions(Color.RED, 1));
                }
            }

            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target != FIRE_RING && loc.distance(target.getLocation()) <= b(FIRE_RING, 10, 15)) {
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

        Block blockUnder = event.getPlayer().getLocation().subtract(0, 1, 0).getBlock();

        if (blockUnder.getType() == Material.SLIME_BLOCK) {
            if (event.getPlayer().getInventory().contains(Material.WOODEN_AXE)) {
                event.getPlayer().damage(1);
            }
        }

        if (blockUnder.getType() == Material.BLACK_CONCRETE) {
            if (event.getPlayer().getInventory().contains(Material.WOODEN_AXE)) {
                event.getPlayer().damage(0.5);
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5, 5));
            }
        }
    }

    @EventHandler
    public void onProjectileHit(@NotNull ProjectileHitEvent event) {
        if (event.getHitEntity() == null) return;

        Entity entity = event.getHitEntity();

        if (entity instanceof Player player && event.getEntity() instanceof Snowball snowball) {
            player.damage(
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
            event.setDamage(4);
        }

        if (event.getDamager() instanceof Player player) {
            if (STORM_WALL == player) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
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
        if (event.getEntity().getType() == SKELETON) {
            int skeletonsLeft = 0;
            for (Entity entity : WizardDuels.WORLD.getEntities()) {
                if (entity.getScoreboardTags().contains("venomous")) {
                    skeletonsLeft++;
                }
            }
            if (skeletonsLeft <= 0) POISON_SKELETONS = null;
        }
    }

    private int b(Player p, int o, int b) {
        return BOOSTED_ABILITIES == p ? b : o;
    }
    private double b(Player p, double o, double b) {
        return BOOSTED_ABILITIES == p ? b : o;
    }

    private @NotNull Location getRandomLocation(@NotNull Location center, int radius) {
        double x = center.getX() + (Math.random() - 0.5) * radius * 2;
        double z = center.getZ() + (Math.random() - 0.5) * radius * 2;
        return new Location(center.getWorld(), x, center.getY(), z);
    }

    private int specialRandom10() {
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
