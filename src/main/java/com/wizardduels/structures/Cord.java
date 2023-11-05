package com.wizardduels.structures;

import com.wizardduels.WizardDuels;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public record Cord(int x, int y, int z) {
    public static @NotNull Cord of(@NotNull Location loc) {
        return new Cord((int) loc.x(), (int) loc.y(), (int) loc.z());
    }

    public @NotNull Location toLocation() {
        return new Location(WizardDuels.WORLD, x, y, z);
    }

    @Override
    public String toString() {
        return String.format("X: %d, Y: %d, Z: %d", x, y, z);
    }
}
