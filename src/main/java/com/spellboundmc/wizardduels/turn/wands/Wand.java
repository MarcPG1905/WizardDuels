package com.spellboundmc.wizardduels.turn.wands;

import com.marcpg.text.Formatter;
import com.spellboundmc.wizardduels.other.Translation;
import com.spellboundmc.wizardduels.turn.Turn;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.spellboundmc.wizardduels.turn.wands.Ability.*;
import static org.bukkit.Material.*;

public enum Wand implements Turn {
    EXPLOSION(GOLDEN_HOE, EXPLOSION_CHARGE, PRESSURE_WAVE, CREEPER_THROW, SUPER_BLAST),
    ICE(IRON_HOE, ICE_WALL, ICE_ROAD, FREEZE, ICE_STORM),
    ENDER(NETHERITE_HOE, ENDER_BALL, ENDERMAN_TELEPORT, END_STONE_WALL, POSITION_SWAP),
    DRAGON(NETHERITE_SHOVEL, DRAGONS_BREATH, DRAGONS_WINGS, Ability.DRAGON, CRYSTAL_SHIELD),
    NECROMANCER(DIAMOND_HOE, REVIVING_THE_DEAD, HORSEMAN, ELITE_SUMMON, Ability.SPAWNER),
    NETHER(GOLDEN_SHOVEL, FIREBALL, GHAST_RIDER, HOT_BREATH, FIRE_RING),
    WEATHER(TRIDENT, LIGHTNING_STRIKE, GUST_OF_WIND, STORM_SHIELD, TORNADO),
    TIME(CLOCK, TIME_FREEZE, CTRL_Z, PARADOX_SHIELD, CLONE),
    GRAVITY(IRON_AXE, GRAVI_BEAM, LOW_GRAVITY, GRAVI_WAVE, MINI_BLACK_HOLE),
    SWORD(IRON_SWORD, SWORD_THROW, SWORD_DASH, SWORD_STAB, SWORD_HORDE),
    ELECTRIC(IRON_PICKAXE, LIGHTNING_SHOT, SPEEDY_OVERCHARGE, ELECTRIC_ZONE, ELECTRO_PHANTOMS),
    SCULK(DIAMOND_AXE, SONIC_BOOM, SCULK_TELEPORT, WARDEN, SCULK_GROWTH),
    VENOM(WOODEN_AXE, POISON, POISON_TELEPORT, POISON_SPILL, POISON_MOBS),
    DARK(WOODEN_HOE, VOID_BEAM, DARK_DASH, BLINDER, BLACK_DEATH),
    HEALTH(STONE_HOE, HEAL, DASH, HEALTH_SHIELD, LIFE_STEAL),
    GLITCH(GOLDEN_AXE, Ability.GLITCH, GLITCH_DASH, VIRUS, GLITCH_SUMMON),
    WIZARDS(STICK, WIZ_BLAST, TELEPORTER, Ability.NECROMANCER, WIZARD_BEAM),
    REDSTONE(COPPER_INGOT, REDSTONE_BLAST, REDSTONE_DASH, DISPENSER_WALL, POWER_BOOST),
    POTION_MASTER(GLASS_BOTTLE, LITTLE_ACCIDENT, COCKTAIL, MAGIC_CULT, ORANGE_JUICE);

    public final Material item;
    public final Ability[] abilities;
    public final Ability lmb;
    public final Ability rmb;
    public final Ability slmb;
    public final Ability srmb;

    Wand(Material item, Ability lmb, Ability rmb, Ability slmb, Ability srmb) {
        this.item = item;
        this.lmb = lmb;
        this.rmb = rmb;
        this.slmb = slmb;
        this.srmb = srmb;
        abilities = new Ability[]{ lmb, rmb, slmb, srmb };
    }

    @Override
    public @NotNull String text(Player player, boolean translated) {
        return translated ? Translation.get(player.locale(), translationKey()) : Formatter.toPascalCase(name());
    }

    @Override
    public @NotNull String translationKey() {
        return "wand." + name().toLowerCase();
    }

    public static @Nullable Wand getWand(Material material) {
        for (Wand wand : Wand.values()) {
            if (wand.item == material) return wand;
        }
        return null;
    }
}
