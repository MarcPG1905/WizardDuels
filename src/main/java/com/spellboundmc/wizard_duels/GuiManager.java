package com.spellboundmc.wizard_duels;

import com.marcpg.lang.Translation;
import com.spellboundmc.wizard_duels.match.Match;
import com.spellboundmc.wizard_duels.match.MatchManager;
import com.spellboundmc.wizard_duels.turning.spells.Spell;
import com.spellboundmc.wizard_duels.turning.wands.Wand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GuiManager implements Listener {
    public static final ItemStack INVISIBLE_FILLER = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
    public static final ItemStack BLACK_FILLER = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    static {
        INVISIBLE_FILLER.editMeta(meta -> meta.displayName(Component.empty()));
        BLACK_FILLER.editMeta(meta -> meta.displayName(Component.empty()));
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        if (Objects.requireNonNull(event.getCurrentItem()).getType().name().endsWith("_STAINED_GLASS_PANE")) return;

        Player player = (Player) event.getWhoClicked();
        Match match = MatchManager.getMatchByPlayer(player);
        if (match == null) return;

        String title = ((TextComponent) event.getView().title()).content().toLowerCase();

        if (title.contains("choose")) {
            event.setCancelled(true);

            switch (match.settingPhase) {
                case TIME_OF_DAY -> match.world.setTime(switch (event.getSlot()) {
                    case 1 -> 23500;
                    case 2 -> 6000;
                    case 5 -> 12500;
                    case 6 -> 15000;
                    case 7 -> 18000;
                    default -> 10500;
                });
                case MAP_SIZE -> match.size = switch (event.getSlot()) {
                    case 2 -> Match.MapSize.MINI;
                    case 3 -> Match.MapSize.SMALL;
                    case 5 -> Match.MapSize.BIG;
                    case 6 -> Match.MapSize.HUGE;
                    default -> Match.MapSize.NORMAL;
                };
                case TOKEN_AMOUNT -> {
                    int tokens = switch (event.getSlot()) {
                        case 2 -> 15;
                        case 3 -> 25;
                        case 6 -> 50;
                        default -> 35;
                    };
                    match.players.left().setTokens(tokens);
                    match.players.right().setTokens(tokens);
                }
                case WEATHER -> {
                    switch (event.getSlot()) {
                        case 2 -> match.world.setStorm(true);
                        case 6 -> {
                            match.world.setStorm(true);
                            match.world.setThunderDuration(Integer.MAX_VALUE);
                            match.world.setThundering(true);
                        }
                        default -> {
                            match.world.setStorm(false);
                            match.world.setClearWeatherDuration(Integer.MAX_VALUE);
                        }
                    }
                    startShopDisplay(match);

                    new BukkitRunnable() {
                        int secsLeft = 60;
                        @Override
                        public void run() {
                            match.players.left().player.getInventory().setItem(14, new ItemStack(Material.IRON_NUGGET, secsLeft));
                            match.players.right().player.getInventory().setItem(14, new ItemStack(Material.IRON_NUGGET, secsLeft));
                            secsLeft--;
                            if (secsLeft <= 0) {
                                match.start();
                                cancel();
                            }
                        }
                    }.runTaskTimer(WizardDuels.PLUGIN, 0, 20);
                    return;
                }
            }
            match.prePhase();
        } else if (title.contains("shop")) {
            event.setCancelled(true);

            Material item = event.getCurrentItem().getType();

            if (title.contains("wands")) {
                if (item == Material.DIRT) {
                    displayShop(player, false, 1);
                } else if (item != Material.STICK && !item.isEmpty()) {
                    Wand wand = Wand.getWand(item);
                    if (wand != null) {
                        player.getInventory().setItem(0, new ItemStack(wand.item));
                        match.getPlayerData(player).selectedWand = wand;
                    }
                }
            } else if (title.contains("spells")) {
                int currentPage = title.charAt(title.length() - 1);

                if (item == Material.STICK) {
                    displayShop(player, true, 1);
                } else if (item == Material.GREEN_DYE) {
                    displayShop(player, false, currentPage + 1);
                } else if (item == Material.RED_DYE) {
                    displayShop(player, false, Math.max(1, currentPage - 1));
                } else if (item != Material.DIRT && !item.isEmpty()) {
                    Spell spell = Spell.getSpellShop(item);
                    if (spell != null) {
                        PlayerData playerData = match.getPlayerData(player);
                        if (playerData.tokens() >= spell.price && (player.getInventory().getItem(7) == null || Objects.requireNonNull(player.getInventory().getItem(7)).isEmpty())) {
                            playerData.setTokens(playerData.tokens() - spell.price);
                            player.getInventory().addItem(new ItemStack(spell.shopItem));
                            playerData.selectedSpells.add(spell);

                            player.playSound(player, Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
                            player.getInventory().setItem(12, new ItemStack(Material.GOLD_NUGGET, playerData.tokens()));

                            if (player.getInventory().getItem(7) != null || !Objects.requireNonNull(player.getInventory().getItem(7)).isEmpty()) {
                                playerData.setShopDone(true);
                                if (match.getOpponentData(player).shopDone()) match.start();
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        if (MatchManager.getMatchByPlayer((Player) event.getPlayer()) == null) return;
        if (event.getReason() == InventoryCloseEvent.Reason.PLUGIN) return;

        Bukkit.getScheduler().runTaskLater(WizardDuels.PLUGIN, () -> event.getPlayer().openInventory(event.getInventory()), 3L);
    }

    private static final int[] EMPTY_SPOTS = { 0, 1, 2, 4, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 46, 47, 48, 49, 50, 51, 52 };
    private static final ItemStack WANDS_ITEM = new ItemStack(Material.STICK);
    private static final ItemStack SPELLS_ITEM = new ItemStack(Material.DIRT);

    static {
        WANDS_ITEM.editMeta(meta -> {
            meta.setCustomModelData(1);
            meta.displayName(Component.text("Wands").decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        });
        SPELLS_ITEM.editMeta(meta -> {
            meta.setCustomModelData(1);
            meta.displayName(Component.text("Spells").decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        });
    }

    public static void createInv(Player player, int rows, int @NotNull [] emptySpots, String title, ItemStack... items) {
        Inventory inv = Bukkit.createInventory(player, rows * 9, Component.text(title));
        for (int emptySpot : emptySpots) {
            inv.setItem(emptySpot, new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE));
        }
        inv.addItem(items);
        player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
        player.openInventory(inv);
    }

    public static void startShopDisplay(@NotNull Match match) {
        match.players.both(o -> {
            Player player = ((PlayerData) o).player;
            displayShop(player, true, 1);
            player.getInventory().setItem(8, new ItemStack(Material.STONE_SWORD));
            player.getInventory().setItem(0, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
            player.getInventory().setItem(12, new ItemStack(Material.GOLD_NUGGET, ((PlayerData) o).tokens()));
        });
    }

    public static void displayShop(Player player, boolean wands, int page) {
        Inventory inv = Bukkit.createInventory(player, 54, Component.text("Shop - " + (wands ? "Wands" : "Spells - Page " + page)));
        for (int i : EMPTY_SPOTS) {
            inv.setItem(i, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
        }

        inv.addItem(WANDS_ITEM, SPELLS_ITEM);

         if (wands) {
             inv.setItem(45, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
             inv.setItem(53, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
             for (Wand wand : Wand.values()) {
                 ItemStack item = new ItemStack(wand.item);
                 item.editMeta(meta -> meta.displayName(Translation.component(player.locale(), wand.translationKey()).decorate(TextDecoration.BOLD)));
                 inv.addItem(item);
             }
         } else {
             inv.setItem(45, new ItemStack(page == 1 ? Material.BLACK_STAINED_GLASS_PANE : Material.RED_DYE));
             inv.setItem(53, new ItemStack(((double) Spell.values().length / 28 > (double) page) ? Material.LIME_DYE : Material.BLACK_STAINED_GLASS_PANE));

             getPage(Spell.values(), page, 28);

             for (Spell spell : getPage(Spell.values(), page, 28)) {
                 inv.addItem(spell.getItem(player));
             }
         }
        player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
        player.openInventory(inv);
    }

    public static <T> @NotNull List<T> getPage(T[] array, int page, int pageSize) {
        return new ArrayList<>(Arrays.asList(array).subList((page - 1) * pageSize, Math.min(((page - 1) * pageSize) + pageSize, array.length)));
    }
}
