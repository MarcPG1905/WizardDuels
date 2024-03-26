package com.spellboundmc.wizard_duels.match;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MatchManager {
    public static final List<Match> MATCHES = new ArrayList<>();

    public static @Nullable Match getMatchByPlayer(Player player) {
        return MATCHES.stream()
                .filter(match -> match.players.right().player == player || match.players.left().player == player)
                .findFirst()
                .orElse(null);
    }
}
