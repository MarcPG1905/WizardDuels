package net.spellboundmc;

import net.spellboundmc.wands.Ability;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayerData {
    public final Player player;
    public final HashMap<Material, Integer> spellCooldowns = new HashMap<>(Map.of(
            Material.GRASS_BLOCK, 0,
            Material.END_CRYSTAL, 0,
            Material.NETHERRACK, 0,
            Material.DARK_PRISMARINE, 0,
            Material.SMITHING_TABLE, 0,
            Material.FLETCHING_TABLE, 0,
            Material.WATER_BUCKET, 0,
            Material.LAVA_BUCKET, 0
    ));
    public final HashMap<Ability, Integer> abilityCooldowns = new HashMap<>();
    public boolean wandCrystalActive, spellCrystalActive, disabledWands, boostedAbilities, thunderEffect, spellLuck25;
    public int fireballsLeft, lavaBucketLevel = 1, waterBucketLevel = 1;
    public int tokens;
    public boolean shopDone;


    public PlayerData(Player player) {
        this.player = player;

        for (Ability ability : Ability.values()) {
            abilityCooldowns.put(ability, 0);
        }
    }
}
