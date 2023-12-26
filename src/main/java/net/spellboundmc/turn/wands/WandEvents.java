package net.spellboundmc.turn.wands;

import com.marcpg.color.McFormat;
import com.marcpg.text.Formatter;
import io.papermc.paper.event.entity.EntityMoveEvent;
import net.kyori.adventure.text.Component;
import net.spellboundmc.PlayerData;
import net.spellboundmc.WizardDuels;
import net.spellboundmc.match.Basic1v1;
import net.spellboundmc.other.Translation;
import net.spellboundmc.turn.spells.Spell;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class WandEvents implements Listener {
    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        if (WizardDuels.currentMatch == null || !event.hasItem()) return;

        Player player = event.getPlayer();
        wandUse(Objects.requireNonNull(event.getItem()).getType(), UseType.fromAction(event.getAction(), player), player);
    }

    public void wandUse(@NotNull Material item, UseType use, @NotNull Player player) {
        if (item == Material.DIAMOND && use == UseType.SLMB) {
            PlayerInventory inv = player.getInventory();
            for (Wand wand : Wand.values()) {
                ItemStack wandItem = new ItemStack(wand.item);
                wandItem.editMeta(itemMeta -> itemMeta.displayName(Component.text(McFormat.LIME + Formatter.toPascalCase(wand.name()) + " Wand")));
                inv.addItem(wandItem);
            }
        }

        switch (item) {
            // =========== EXPLOSION ===========
            case GOLDEN_HOE -> {
                switch (use) {
                    case LMB -> WandUsage.wandUse(Ability.EXPLOSION_CHARGE, player);
                    case RMB -> WandUsage.wandUse(Ability.PRESSURE_WAVE, player);
                    case SLMB -> WandUsage.wandUse(Ability.CREEPER_THROW, player);
                    case SRMB -> WandUsage.wandUse(Ability.SUPER_BLAST, player);
                }
            }
            // ============== ICE ==============
            case IRON_HOE -> {
                switch (use) {
                    case LMB -> WandUsage.wandUse(Ability.ICE_WALL, player);
                    case RMB -> WandUsage.wandUse(Ability.ICE_ROAD, player);
                    case SLMB -> WandUsage.wandUse(Ability.FREEZE, player);
                    case SRMB -> WandUsage.wandUse(Ability.ICE_STORM, player);
                }
            }
            // ============= ENDER =============
            case NETHERITE_HOE -> {
                switch (use) {
                    case LMB -> WandUsage.wandUse(Ability.ENDER_BALL, player);
                    case RMB -> WandUsage.wandUse(Ability.ENDERMAN_TELEPORT, player);
                    case SLMB -> WandUsage.wandUse(Ability.END_STONE_WALL, player);
                    case SRMB -> WandUsage.wandUse(Ability.POSITION_SWAP, player);
                }
            }
            // ============ DRAGON =============
            case NETHERITE_SHOVEL -> {
                switch (use) {
                    case LMB -> WandUsage.wandUse(Ability.DRAGONS_BREATH, player);
                    case RMB -> WandUsage.wandUse(Ability.DRAGONS_WINGS, player);
                    case SLMB -> WandUsage.wandUse(Ability.DRAGON, player);
                    case SRMB -> WandUsage.wandUse(Ability.CRYSTAL_SHIELD, player);
                }
            }
            // ========== NECROMANCER ==========
            case DIAMOND_HOE -> {
                switch (use) {
                    case LMB -> WandUsage.wandUse(Ability.REVIVING_THE_DEAD, player);
                    case RMB -> WandUsage.wandUse(Ability.HORSEMAN, player);
                    case SLMB -> WandUsage.wandUse(Ability.ELITE_SUMMON, player);
                    case SRMB -> WandUsage.wandUse(Ability.SPAWNER, player);
                }
            }
            // ============ NETHER =============
            case GOLDEN_SHOVEL -> {
                switch (use) {
                    case LMB -> WandUsage.wandUse(Ability.FIREBALL, player);
                    case RMB -> WandUsage.wandUse(Ability.GHAST_RIDER, player);
                    case SLMB -> WandUsage.wandUse(Ability.HOT_BREATH, player);
                    case SRMB -> WandUsage.wandUse(Ability.FIRE_RING, player);
                }
            }
            // ============ WEATHER ============
            case TRIDENT -> {
                switch (use) {
                    case LMB -> WandUsage.wandUse(Ability.LIGHTNING_STRIKE, player);
                    case RMB -> WandUsage.wandUse(Ability.GUST_OF_WIND, player);
                    case SLMB -> WandUsage.wandUse(Ability.STORM_SHIELD, player);
                    case SRMB -> WandUsage.wandUse(Ability.TORNADO, player);
                }
            }
            // TODO: ===== TIME WAND ===========
            case WOODEN_SHOVEL -> {
                switch (use) {
                    case LMB -> WandUsage.wandUse(Ability.TIME_FREEZE, player);
                    case RMB -> WandUsage.wandUse(Ability.CTRL_Z, player);
                    case SLMB -> WandUsage.wandUse(Ability.PARADOX_SHIELD, player);
                    case SRMB -> WandUsage.wandUse(Ability.CLONE, player);
                }
            }
            // ============ GRAVITY ============
            case IRON_AXE -> {
                switch (use) {
                    case LMB -> WandUsage.wandUse(Ability.GRAVI_BEAM, player);
                    case RMB -> WandUsage.wandUse(Ability.LOW_GRAVITY, player);
                    case SLMB -> WandUsage.wandUse(Ability.GRAVI_WAVE, player);
                    case SRMB -> WandUsage.wandUse(Ability.MINI_BLACK_HOLE, player);
                }
            }
            // ============= SWORD =============
            case WOODEN_SWORD -> {
                switch (use) {
                    case LMB -> WandUsage.wandUse(Ability.SWORD_THROW, player);
                    case RMB -> WandUsage.wandUse(Ability.SWORD_DASH, player);
                    case SLMB -> WandUsage.wandUse(Ability.SWORD_STAB, player);
                    case SRMB -> WandUsage.wandUse(Ability.SWORD_HORDE, player);
                }
            }
            // ========= ELECTRIC WAND =========
            case IRON_PICKAXE -> {
                switch (use) {
                    case LMB -> WandUsage.wandUse(Ability.LIGHTNING_SHOT, player);
                    case RMB -> WandUsage.wandUse(Ability.SPEEDY_OVERCHARGE, player);
                    case SLMB -> WandUsage.wandUse(Ability.ELECTRIC_ZONE, player);
                    case SRMB -> WandUsage.wandUse(Ability.ELECTRO_PHANTOMS, player);
                }
            }
            // ============= SCULK =============
            case DIAMOND_AXE -> {
                switch (use) {
                    case LMB -> WandUsage.wandUse(Ability.SONIC_BOOM, player);
                    case RMB -> WandUsage.wandUse(Ability.SCULK_TELEPORT, player);
                    case SLMB -> WandUsage.wandUse(Ability.WARDEN, player);
                    case SRMB -> WandUsage.wandUse(Ability.SCULK_GROWTH, player);
                }
            }
            // ============= VENOM =============
            case WOODEN_AXE -> {
                switch (use) {
                    case LMB -> WandUsage.wandUse(Ability.POISON, player);
                    case RMB -> WandUsage.wandUse(Ability.POISON_TELEPORT, player);
                    case SLMB -> WandUsage.wandUse(Ability.POISON_SPILL, player);
                    case SRMB -> WandUsage.wandUse(Ability.POISON_MOBS, player);
                }
            }
            // =========== DARK WAND ===========
            case WOODEN_HOE -> {
                switch (use) {
                    case LMB -> WandUsage.wandUse(Ability.VOID_BEAM, player);
                    case RMB -> WandUsage.wandUse(Ability.DARK_DASH, player);
                    case SLMB -> WandUsage.wandUse(Ability.BLINDER, player);
                    case SRMB -> WandUsage.wandUse(Ability.BLACK_DEATH, player);
                }
            }
            // =========== OF HEALTH ===========
            case STONE_HOE -> {
                switch (use) {
                    case LMB -> WandUsage.wandUse(Ability.HEAL, player);
                    case RMB -> WandUsage.wandUse(Ability.DASH, player);
                    case SLMB -> WandUsage.wandUse(Ability.HEALTH_SHIELD, player);
                    case SRMB -> WandUsage.wandUse(Ability.LIFE_STEAL, player);
                }
            }
            // ========== GLITCH WAND ==========
            case GOLDEN_AXE -> {
                switch (use) {
                    case LMB -> WandUsage.wandUse(Ability.GLITCH, player);
                    case RMB -> WandUsage.wandUse(Ability.GLITCH_DASH, player);
                    case SLMB -> WandUsage.wandUse(Ability.VIRUS, player);
                    case SRMB -> WandUsage.wandUse(Ability.GLITCH_SUMMON, player);
                }
            }
            // ============ WIZARDS ============
            case STICK -> {
                switch (use) {
                    case LMB -> WandUsage.wandUse(Ability.WIZ_BLAST, player);
                    case RMB -> WandUsage.wandUse(Ability.TELEPORTER, player);
                    case SLMB -> WandUsage.wandUse(Ability.NECROMANCER, player);
                    case SRMB -> WandUsage.wandUse(Ability.WIZARD_BEAM, player);
                }
            }
            // ========= REDSTONE WAND =========
            case COPPER_INGOT -> {
                switch (use) {
                    case LMB -> WandUsage.wandUse(Ability.REDSTONE_BLAST, player);
                    case RMB -> WandUsage.wandUse(Ability.REDSTONE_DASH, player);
                    case SLMB -> WandUsage.wandUse(Ability.DISPENSER_WALL, player);
                    case SRMB -> WandUsage.wandUse(Ability.POWER_BOOST, player);
                }
            }

            // ========= POTION MASTER =========
            case IRON_SHOVEL -> {
                switch (use) {
                    case LMB -> WandUsage.wandUse(Ability.LITTLE_ACCIDENT, player);
                    case RMB -> WandUsage.wandUse(Ability.COCKTAIL, player);
                    case SLMB -> WandUsage.wandUse(Ability.MAGIC_CULT, player);
                    case SRMB -> WandUsage.wandUse(Ability.ORANGE_JUICE, player);
                }
            }
        }
    }

    @EventHandler
    public void onVehicleExit(@NotNull VehicleExitEvent event) {
        if (WizardDuels.currentMatch == null) return;

        if (event.getVehicle() instanceof Horse horse) {
            horse.eject();
            horse.remove();
        }
    }

    @EventHandler
    public void onEntityTargetLivingEntity(@NotNull EntityTargetLivingEntityEvent event) {
        if (WizardDuels.currentMatch == null) return;

        if (event.getEntity() instanceof Skeleton skeleton) {
            if (!skeleton.getScoreboardTags().contains("venomous")) return;

            Basic1v1 basic1v1 = (Basic1v1) WizardDuels.currentMatch; // TODO: Switch this to be compatible with 2v2 and Chaos too
            Player target = basic1v1.player1 == basic1v1.POISON_SKELETONS ? basic1v1.player1 : basic1v1.player2;

            if (event.getTarget() == basic1v1.POISON_SKELETONS) event.setTarget(target);
        }
    }

    @EventHandler
    public void onEntityShootBow(@NotNull EntityShootBowEvent event) {
        if (WizardDuels.currentMatch == null) return;

        if (event.getEntity().getScoreboardTags().contains("venomous")) {
            if (event.getProjectile() instanceof Arrow arrow) {
                arrow.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 5, 0), true);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (WizardDuels.currentMatch == null) return;
        Basic1v1 basic1v1 = (Basic1v1) WizardDuels.currentMatch;

        if (basic1v1.ICY_FEET != null) {
            if (basic1v1.ICY_FEET == event.getPlayer()) {
                Location loc = event.getPlayer().getLocation().subtract(0, 1, 0);
                loc.getBlock().setType(Material.BLUE_ICE);
            }
        }

        if (basic1v1.FIRE_RING != null) {
            Location loc = basic1v1.FIRE_RING.getLocation();

            PlayerData playerData = basic1v1.player1 == basic1v1.FIRE_RING ? basic1v1.playerData1 : basic1v1.playerData2;

            if (basic1v1.FIRE_RING == event.getPlayer()) {
                for (int i = 0; i < 360; i += 3) {
                    double angle = Math.toRadians(i);
                    double x = loc.getX() + (playerData.boostedAbilities ? 15 : 10) * Math.cos(angle);
                    double z = loc.getZ() + (playerData.boostedAbilities ? 15 : 10) * Math.sin(angle);
                    Location particleLocation = new Location(WizardDuels.WORLD, x, loc.getY(), z);

                    WizardDuels.WORLD.spawnParticle(Particle.REDSTONE, particleLocation, 0, new Particle.DustOptions(Color.RED, 1));
                }
            }

            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target != basic1v1.FIRE_RING && loc.distance(target.getLocation()) <= (playerData.boostedAbilities ? 15 : 10)) {
                    target.setFireTicks(100);
                    target.damage(0.5);
                }
            }
        }

        if (basic1v1.ICE_STORM != null) {
            Location loc = basic1v1.ICE_STORM.getLocation();

            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target != basic1v1.ICE_STORM && loc.distance(target.getLocation()) <= 4.0) {
                    Vector direction = target.getLocation().toVector().subtract(loc.toVector()).normalize();

                    target.setVelocity(direction.multiply(0.5));
                }
            }
        }

        if (basic1v1.OPPONENTS_NO_MOVEMENT != null) {
            if (event.getPlayer() != basic1v1.OPPONENTS_NO_MOVEMENT) {
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
        if (WizardDuels.currentMatch == null) return;

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
        if (WizardDuels.currentMatch == null) return;
        Basic1v1 basic1v1 = (Basic1v1) WizardDuels.currentMatch;

        if (event.getEntity() instanceof Projectile projectile) {
            if (basic1v1.STORM_WALL == null) return;

            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target != basic1v1.STORM_WALL && basic1v1.STORM_WALL.getLocation().distance(target.getLocation()) <= 2) {
                    Vector currentVelocity = projectile.getVelocity();
                    Vector invertedVelocity = new Vector(-currentVelocity.getX(), -currentVelocity.getY(), -currentVelocity.getZ());
                    projectile.setVelocity(invertedVelocity);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event) {
        if (WizardDuels.currentMatch == null) return;
        Basic1v1 basic1v1 = (Basic1v1) WizardDuels.currentMatch;

        if (event.getDamager() instanceof LightningStrike) {
            event.setDamage(4);
        }

        if (event.getDamager() instanceof Player player) {
            if (basic1v1.STORM_WALL == player) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(@NotNull EntityDamageEvent event) {
        if (WizardDuels.currentMatch == null) return;
        Basic1v1 basic1v1 = (Basic1v1) WizardDuels.currentMatch;

        if (event.getEntity() instanceof Player player) {
            if (basic1v1.STORM_WALL == player) {
                event.setDamage(event.getDamage() / 2);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(@NotNull EntityDeathEvent event) {
        if (WizardDuels.currentMatch == null) return;
        Basic1v1 basic1v1 = (Basic1v1) WizardDuels.currentMatch;

        if (event.getEntity() instanceof EnderCrystal) {
            event.getDrops().clear();
            if (basic1v1.playerData1.wandCrystalActive || basic1v1.playerData2.wandCrystalActive) {
                basic1v1.playerData1.wandCrystalActive = false;
                basic1v1.playerData2.wandCrystalActive = false;

                for (Player player : WizardDuels.WORLD.getPlayers()) {
                    if (player.getNoDamageTicks() != 0 && player.getInventory().contains(Material.NETHERITE_SHOVEL)) {
                        player.setNoDamageTicks(1);
                        player.sendMessage(McFormat.RED + Translation.get(player.locale(), "wand.dragon.srmb.end"));
                    }
                }
            }
            if (basic1v1.playerData1.spellCrystalActive) {
                basic1v1.player1.setHealth(basic1v1.player1.getHealth() + 10);
                basic1v1.playerData1.spellCooldowns.put(Spell.END_CRYSTAL, 35);
            }
            if (basic1v1.playerData2.spellCrystalActive) {
                basic1v1.player2.setHealth(basic1v1.player2.getHealth() + 10);
                basic1v1.playerData2.spellCooldowns.put(Spell.END_CRYSTAL, 35);
            }
        }
        if (event.getEntity().getType() == EntityType.SKELETON) {
            int skeletonsLeft = 0;
            for (Entity entity : WizardDuels.WORLD.getEntities()) {
                if (entity.getScoreboardTags().contains("venomous")) {
                    skeletonsLeft++;
                }
            }
            if (skeletonsLeft <= 0) basic1v1.POISON_SKELETONS = null;
        }
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
