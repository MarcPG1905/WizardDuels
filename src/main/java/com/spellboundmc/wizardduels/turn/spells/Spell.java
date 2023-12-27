package com.spellboundmc.wizardduels.turn.spells;

import com.marcpg.text.Formatter;
import com.spellboundmc.wizardduels.other.Translation;
import com.spellboundmc.wizardduels.turn.Turn;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public enum Spell implements Turn {
    GRASS_BLOCK(true, 10, Material.GRASS_BLOCK, Material.GRASS_BLOCK),
    END_CRYSTAL(true, 8, Material.END_CRYSTAL, Material.END_CRYSTAL),
    NETHERRACK(true, 4, Material.NETHERRACK, Material.NETHERRACK),
    DARK_PRISMARINE(true, 4, Material.DARK_PRISMARINE, Material.DARK_PRISMARINE),
    SMITHING_TABLE(true, 10, Material.SMITHING_TABLE, Material.SMITHING_TABLE),
    FLETCHING_TABLE(true, 15, Material.FLETCHING_TABLE, Material.FLETCHING_TABLE),
    OBSIDIAN(false, 0, Material.OBSIDIAN, null),
    LAVA(true, 10, Material.LAVA, Material.LAVA_BUCKET),
    WATER(true, 10, Material.WATER, Material.WATER_BUCKET),
    PISTON(true, 8, Material.PISTON, Material.PISTON),
    CHAIN(true, 15, Material.CHAIN, Material.CHAIN),
    COBWEB(true, 12, Material.COBWEB, Material.COBWEB),
    SPAWNER(true, 12, Material.SPAWNER, Material.SPAWNER),
    OAK_PLANKS(true, 8, Material.OAK_PLANKS, Material.OAK_PLANKS);

    public final boolean shop;
    public final int price;
    public final Material placedItem;
    public final Material shopItem;

    Spell(boolean shop, int price, Material placedItem, Material shopItem) {
        this.shop = shop;
        this.price = price;
        this.placedItem = placedItem;
        this.shopItem = shopItem;
    }

    public @NotNull ItemStack getItem(Player player) {
        ItemStack itemStack = new ItemStack(shop ? shopItem : Material.RED_STAINED_GLASS_PANE);
        itemStack.editMeta(meta -> {
            meta.displayName(itemStack.displayName().color(TextColor.color(160, shop ? 255 : 160, 160)));
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

    public static @Nullable Spell getSpellShop(Material material) {
        for (Spell spell : Spell.values()) {
            if (spell.shopItem == material) return spell;
        }
        return null;
    }

    public static @Nullable Spell getSpellPlaced(Material material) {
        for (Spell spell : Spell.values()) {
            if (spell.placedItem == material) return spell;
        }
        return null;
    }
}
