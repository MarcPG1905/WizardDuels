package net.spellboundmc;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface Turn {
    @NotNull String text(Player player, boolean translated);
    @NotNull String translationKey();
}
