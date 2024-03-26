package com.spellboundmc.wizard_duels;

import com.spellboundmc.wizard_duels.turning.spells.Spell;
import com.spellboundmc.wizard_duels.turning.wands.Ability;
import com.spellboundmc.wizard_duels.turning.wands.Wand;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerData {
    public final Player player;
    public final Map<Spell, Integer> spellCooldowns = new HashMap<>();
    public final Map<Ability, Integer> abilityCooldowns = new HashMap<>();
    public final List<Location> lastLocations = new LinkedList<>();
    public final Map<String, Object> matchProperties = new HashMap<>();
    public final Set<String> matchModifiers = new HashSet<>();
    public final Set<Spell> selectedSpells = new HashSet<>();
    public Wand selectedWand;
    private int tokens;
    private boolean shopDone;

    public PlayerData(Player player) {
        this.player = player;

        for (Spell spell : Spell.values())
            spellCooldowns.put(spell, 0);

        for (Ability ability : Ability.values())
            abilityCooldowns.put(ability, 0);
    }

    public int tokens() {
        return tokens;
    }

    public void setTokens(int tokens) {
        this.tokens = tokens;
    }

    public boolean shopDone() {
        return shopDone;
    }

    public void setShopDone(boolean shopDone) {
        this.shopDone = shopDone;
    }
}
