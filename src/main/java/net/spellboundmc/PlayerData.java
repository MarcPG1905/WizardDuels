package net.spellboundmc;

import net.spellboundmc.turn.spells.Spell;
import net.spellboundmc.turn.wands.Ability;
import net.spellboundmc.turn.wands.Wand;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerData {

    public final Player player;
    public final HashMap<Spell, Integer> spellCooldowns = new HashMap<>(Map.of(
            Spell.GRASS_BLOCK, 0,
            Spell.END_CRYSTAL, 0,
            Spell.NETHERRACK, 0,
            Spell.DARK_PRISMARINE, 0,
            Spell.SMITHING_TABLE, 0,
            Spell.FLETCHING_TABLE, 0,
            Spell.WATER, 0,
            Spell.LAVA, 0
    ));
    public final LinkedList<Location> locationQueue = new LinkedList<>();
    public final HashMap<Ability, Integer> abilityCooldowns = new HashMap<>();
    public final List<Spell> spells = new ArrayList<>();
    public boolean wandCrystalActive, spellCrystalActive, disabledWands, boostedAbilities, thunderEffect, spellLuck25, constantSpawning;
    public int fireballsLeft, lavaBucketLevel = 1, waterBucketLevel = 1;
    public Location cobwebCenter;
    public int tokens;
    public boolean shopDone;
    public Wand selectedWand;


    public PlayerData(Player player) {
        this.player = player;

        for (Ability ability : Ability.values()) {
            abilityCooldowns.put(ability, 0);
        }
    }
}
