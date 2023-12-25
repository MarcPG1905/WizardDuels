package net.spellboundmc.turn.wands;

import com.marcpg.text.Formatter;
import net.spellboundmc.turn.Turn;
import net.spellboundmc.other.Translation;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum Ability implements Turn {
    EXPLOSION_CHARGE(5, Wand.EXPLOSION),
    PRESSURE_WAVE(15, Wand.EXPLOSION),
    CREEPER_THROW(15, Wand.EXPLOSION),
    SUPER_BLAST(40, Wand.EXPLOSION),
    ICE_WALL(15, Wand.ICE),
    ICE_ROAD(60, Wand.ICE),
    FREEZE(60, Wand.ICE),
    ICE_STORM(30, Wand.ICE),
    ENDER_BALL(5, Wand.ENDER),
    ENDERMAN_TELEPORT(25, Wand.ENDER),
    END_STONE_WALL(25, Wand.ENDER),
    POSITION_SWAP(60, Wand.ENDER),
    DRAGONS_BREATH(20, Wand.DRAGON),
    DRAGONS_WINGS(30, Wand.DRAGON),
    DRAGON(45, Wand.DRAGON),
    CRYSTAL_SHIELD(60, Wand.DRAGON),
    REVIVING_THE_DEAD(30, Wand.NECROMANCER),
    HORSEMAN(90, Wand.NECROMANCER),
    ELITE_SUMMON(120, Wand.NECROMANCER),
    SPAWNER(40, Wand.NECROMANCER),
    FIREBALL(6, Wand.NETHER),
    GHAST_RIDER(20, Wand.NETHER),
    HOT_BREATH(45, Wand.NETHER),
    FIRE_RING(30, Wand.NETHER),
    LIGHTNING_STRIKE(25, Wand.WEATHER),
    GUST_OF_WIND(15, Wand.WEATHER),
    STORM_SHIELD(30, Wand.WEATHER),
    TORNADO(60, Wand.WEATHER),
    TIME_FREEZE(30, Wand.TIME),
    CTRL_Z(30, Wand.TIME),
    PARADOX_SHIELD(120, Wand.TIME),
    CLONE(120, Wand.TIME),
    GRAVI_BEAM(30, Wand.GRAVITY),
    LOW_GRAVITY(40, Wand.GRAVITY),
    GRAVI_WAVE(25, Wand.GRAVITY),
    MINI_BLACK_HOLE(60, Wand.GRAVITY),
    SWORD_THROW(5, Wand.SWORD),
    SWORD_DASH(25, Wand.SWORD),
    SWORD_STAB(50, Wand.SWORD),
    SWORD_HORDE(120, Wand.SWORD),
    LIGHTNING_SHOT(10, Wand.ELECTRIC),
    SPEEDY_OVERCHARGE(40, Wand.ELECTRIC),
    ELECTRIC_ZONE(30, Wand.ELECTRIC),
    ELECTRO_PHANTOMS(25, Wand.ELECTRIC),
    SONIC_BOOM(15, Wand.SCULK),
    SCULK_TELEPORT(10, Wand.SCULK),
    WARDEN(60, Wand.SCULK),
    SCULK_GROWTH(45, Wand.SCULK),
    POISON(15, Wand.VENOM),
    POISON_TELEPORT(10, Wand.VENOM),
    POISON_SPILL(25, Wand.VENOM),
    POISON_MOBS(30, Wand.VENOM),
    VOID_BEAM(15, Wand.DARK),
    DARK_DASH(15, Wand.DARK),
    BLINDER(25, Wand.DARK),
    BLACK_DEATH(40, Wand.DARK),
    HEAL(10, Wand.HEALTH),
    DASH(10, Wand.HEALTH),
    HEALTH_SHIELD(30, Wand.HEALTH),
    LIFE_STEAL(25, Wand.HEALTH),
    GLITCH(10, Wand.GLITCH),
    GLITCH_DASH(10, Wand.GLITCH),
    VIRUS(25, Wand.GLITCH),
    GLITCH_SUMMON(20, Wand.GLITCH),
    WIZ_BLAST(10, Wand.WIZARDS),
    TELEPORTER(20, Wand.WIZARDS),
    NECROMANCER(25, Wand.WIZARDS),
    WIZARD_BEAM(20, Wand.WIZARDS),
    REDSTONE_BLAST(25, Wand.REDSTONE),
    REDSTONE_DASH(6, Wand.REDSTONE),
    DISPENSER_WALL(20, Wand.REDSTONE),
    POWER_BOOST(30, Wand.REDSTONE),
    LITTLE_ACCIDENT(20, Wand.POTION_MASTER),
    COCKTAIL(20, Wand.POTION_MASTER),
    MAGIC_CULT(30, Wand.POTION_MASTER),
    ORANGE_JUICE(15, Wand.POTION_MASTER);

    public final int cooldown;
    public final Wand wand;

    Ability(int cooldown, Wand wand) {
        this.cooldown = cooldown;
        this.wand = wand;
    }

    @Override
    public @NotNull String text(Player player, boolean translated) {
        if (translated) {
            Locale l = player.locale();
            return Translation.get(l, "wand.usage", player.getName(), Translation.get(l, translationKey()), Translation.get(l, "wand." + wand.name().toLowerCase()));
        } else {
            return player.getName() + " used the " + Formatter.toPascalCase(name()) + " ability from the " + Formatter.toPascalCase(wand.name()) + " wand.";
        }
    }

    @Override
    public @NotNull String translationKey() {
        return "wand." + wand.name().toLowerCase() + "." + name().toLowerCase();
    }
}
