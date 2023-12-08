package net.spellboundmc.spells;

import me.marcpg1905.util.Formatter;
import net.spellboundmc.Turn;
import net.spellboundmc.other.Translation;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum Spell implements Turn {
    GRASS_BLOCK(true, Material.GRASS_BLOCK),
    END_CRYSTAL(true, Material.END_CRYSTAL),
    NETHERRACK(true, Material.NETHERRACK),
    DARK_PRISMARINE(true, Material.DARK_PRISMARINE),
    SMITHING_TABLE(true, Material.SMITHING_TABLE),
    FLETCHING_TABLE(true, Material.FLETCHING_TABLE),
    OBSIDIAN(false, null),
    LAVA(true, Material.LAVA_BUCKET),
    WATER(true, Material.WATER_BUCKET);

    public final boolean shop;
    public final Material item;

    Spell(boolean shop, Material item) {
        this.shop = shop;
        this.item = item;
    }

    public @NotNull ItemStack getItem() {
        return shop ? new ItemStack(item) : ItemStack.empty();
    }

    @Override
    public @NotNull String text(Player player, boolean translated) {
        if (translated) {
            Locale l = player.locale();
            return Translation.get(l, "spell.usage", player.getName(), Translation.get(l, translationKey()));
        } else {
            return player.getName() + " used the " + Formatter.toPascalCase(name()) + " spell.";
        }
    }

    @Override
    public @NotNull String translationKey() {
        return "spell." + name().toLowerCase(Locale.ROOT);
    }
}
