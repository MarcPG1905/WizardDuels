package net.spellboundmc.spells;

import me.marcpg1905.util.Formatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.spellboundmc.Turn;
import net.spellboundmc.other.Translation;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public enum Spell implements Turn {
    GRASS_BLOCK(true, 10, Material.GRASS_BLOCK),
    END_CRYSTAL(true, 8, Material.END_CRYSTAL),
    NETHERRACK(true, 4, Material.NETHERRACK),
    DARK_PRISMARINE(true, 4, Material.DARK_PRISMARINE),
    SMITHING_TABLE(true, 10, Material.SMITHING_TABLE),
    FLETCHING_TABLE(true, 15, Material.FLETCHING_TABLE),
    OBSIDIAN(false, 0, null),
    LAVA(true, 10, Material.LAVA_BUCKET),
    WATER(true, 10, Material.WATER_BUCKET);

    public final boolean shop;
    public final int price;
    public final Material item;

    Spell(boolean shop, int price, Material item) {
        this.shop = shop;
        this.price = price;
        this.item = item;
    }

    public @NotNull ItemStack getItem(Player player) {
        ItemStack itemStack = new ItemStack(shop ? item : Material.RED_STAINED_GLASS_PANE);
        itemStack.editMeta(meta -> {
            // meta.displayName(Component.text(Translation.get(player.locale(), translationKey()), TextColor.color(160, shop ? 255 : 160, 160)));
            meta.displayName(Component.text("TEST", TextColor.color(160, shop ? 255 : 160, 160)));
            if (shop) meta.lore(List.of(Component.text(Translation.get(player.locale(), "shop.price", price))));
        });
        return itemStack;
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

    public static @Nullable Spell getSpell(Material material) {
        for (Spell spell : Spell.values()) {
            if (spell.item == material) return spell;
        }
        return null;
    }
}
