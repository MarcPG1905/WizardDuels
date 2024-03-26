package com.spellboundmc.wizard_duels.turning;

import com.spellboundmc.wizard_duels.PlayerData;

import java.time.LocalDateTime;

public record TurnData(Turn turn, boolean success, PlayerData playerData, LocalDateTime time) {}
