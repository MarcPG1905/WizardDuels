package net.spellboundmc.turn;

import net.spellboundmc.PlayerData;

import java.time.LocalDateTime;

public record TurnData(Turn turn, boolean success, PlayerData playerData, LocalDateTime time) { }
