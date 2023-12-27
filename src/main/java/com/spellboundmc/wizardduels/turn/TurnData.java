package com.spellboundmc.wizardduels.turn;

import com.spellboundmc.wizardduels.PlayerData;

import java.time.LocalDateTime;

public record TurnData(Turn turn, boolean success, PlayerData playerData, LocalDateTime time) { }
