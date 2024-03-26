package com.spellboundmc.wizard_duels.turning.spells;

import com.marcpg.lang.Translation;
import com.marcpg.text.Formatter;
import com.spellboundmc.wizard_duels.turning.Turn;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public enum Spell implements Turn {
    // Simple Spells
    DARK_PRISMARINE(true, 4, Material.DARK_PRISMARINE, Material.DARK_PRISMARINE, -1, false), // Only one use!
    SMITHING_TABLE(true, 10, Material.SMITHING_TABLE, Material.SMITHING_TABLE, 60, false),
    FLETCHING_TABLE(true, 15, Material.FLETCHING_TABLE, Material.FLETCHING_TABLE, 60, true), // After bow ends

    // General Spells
    GRASS_BLOCK(true, 10, Material.GRASS_BLOCK, Material.GRASS_BLOCK, 30, false),
    END_CRYSTAL(true, 8, Material.END_CRYSTAL, Material.END_CRYSTAL, 35, true), // After destroyed
    NETHERRACK(true, 4, Material.NETHERRACK, Material.NETHERRACK, 15, true),
    LAVA(true, 10, Material.LAVA, Material.LAVA_BUCKET, 20, false), // Complicated
    WATER(true, 10, Material.WATER, Material.WATER_BUCKET, 20, false), // Complicated
    OBSIDIAN(false, 0, Material.OBSIDIAN, null, 45, false),
    PISTON(true, 8, Material.PISTON, Material.PISTON, 45, false),
    CHAIN(true, 15, Material.CHAIN, Material.CHAIN, -1, false), // Waiting for MaybeVlad to fix...
    COBWEB(true, 12, Material.COBWEB, Material.COBWEB, 110, false), // 90s Effect + 20s Cooldown
    SPAWNER(true, 12, Material.SPAWNER, Material.SPAWNER, 20, false), // After destroyed
    OAK_PLANKS(true, 8, Material.OAK_PLANKS, Material.OAK_PLANKS, 15, false);

    public final boolean shop;
    public final int price;
    public final Material placedItem;
    public final Material shopItem;
    public final int cooldown;
    public final boolean customCooldown;

    Spell(boolean shop, int price, Material placedItem, Material shopItem, int cooldown, boolean customCooldown) {
        this.shop = shop;
        this.price = price;
        this.placedItem = placedItem;
        this.shopItem = shopItem;
        this.cooldown = cooldown;
        this.customCooldown = customCooldown;
    }

    public @NotNull ItemStack getItem(Player player) {
        ItemStack itemStack = new ItemStack(shop ? shopItem : Material.RED_STAINED_GLASS_PANE);
        itemStack.editMeta(meta -> {
            meta.displayName(itemStack.displayName().color(TextColor.color(160, shop ? 255 : 160, 160)));
            if (shop) meta.lore(List.of(Translation.component(player.locale(), "shop.price", price)));
        });
        return itemStack;
    }

    @Override
    public @NotNull String text(Player player, boolean translated) {
        if (translated) {
            Locale l = player.locale();
            return Translation.string(l, "spell.usage", player.getName(), Translation.string(l, translationKey()));
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
