package net.spellboundmc.other;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.spellboundmc.PlayerData;
import net.spellboundmc.WizardDuels;
import net.spellboundmc.match.Basic1v1;
import net.spellboundmc.turn.spells.Spell;
import net.spellboundmc.turn.wands.Wand;
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

public class GuiEvents implements Listener {
    public static final ItemStack INVISIBLE_FILLER = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
    public static final ItemStack BLACK_FILLER = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    static {
        INVISIBLE_FILLER.editMeta(meta -> meta.displayName(Component.empty()));
        BLACK_FILLER.editMeta(meta -> meta.displayName(Component.empty()));
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (WizardDuels.currentMatch == null) return;
        if (Objects.requireNonNull(event.getCurrentItem()).getType() == Material.LIGHT_GRAY_STAINED_GLASS) return;

        String title = ((TextComponent) event.getView().title()).content().toLowerCase();

        Basic1v1 match = (Basic1v1) WizardDuels.currentMatch;
        if (title.contains("choose")) {
            event.setCancelled(true);
            switch (match.prePhase) {
                case TIME_OF_DAY -> WizardDuels.WORLD.setTime(switch (event.getSlot()) {
                    case 1 -> 23500;
                    case 2 -> 6000;
                    case 3 -> 10500;
                    case 5 -> 12500;
                    case 6 -> 15000;
                    case 7 -> 18000;
                    default -> throw new IllegalStateException("Unexpected value: " + event.getSlot());
                });
                case MAP_SIZE -> match.mapSize = switch (event.getSlot()) {
                    case 2 -> "Mini (8x8)";
                    case 3 -> "Small (16x16)";
                    case 4 -> "Normal (32x32)";
                    case 5 -> "Big (64x64)";
                    case 6 -> "Huge (128x128)";
                    default -> throw new IllegalStateException("Unexpected value: " + event.getSlot());
                };
                case TOKEN_AMOUNT -> {
                    int tokens = switch (event.getSlot()) {
                        case 2 -> 15;
                        case 3 -> 25;
                        case 5 -> 35;
                        case 6 -> 50;
                        default -> throw new IllegalStateException("Unexpected value: " + event.getSlot());
                    };
                    match.playerData1.tokens = tokens;
                    match.playerData2.tokens = tokens;
                }
                case WEATHER -> {
                    switch (event.getSlot()) {
                        case 2 -> WizardDuels.WORLD.setStorm(true);
                        case 4 -> {
                            WizardDuels.WORLD.setStorm(false);
                            WizardDuels.WORLD.setClearWeatherDuration(Integer.MAX_VALUE);
                        }
                        case 6 -> {
                            WizardDuels.WORLD.setStorm(true);
                            WizardDuels.WORLD.setThunderDuration(Integer.MAX_VALUE);
                            WizardDuels.WORLD.setThundering(true);
                        }
                    }

                    startShopDisplay(match.player1, match.playerData1);
                    startShopDisplay(match.player2, match.playerData2);

                    new BukkitRunnable() {
                        int secsLeft = 60;
                        @Override
                        public void run() {
                            match.player1.getInventory().setItem(14, new ItemStack(Material.IRON_NUGGET, secsLeft));
                            match.player2.getInventory().setItem(14, new ItemStack(Material.IRON_NUGGET, secsLeft));
                            secsLeft--;
                            if (secsLeft <= 0) {
                                match.player1.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                                match.player2.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                                match.startMain();
                                cancel();
                            }
                        }
                    }.runTaskTimer(WizardDuels.PLUGIN, 0, 20);

                    return;
                }
            }
            match.nextPrePhase();
        } else if (title.contains("shop")) {
            event.setCancelled(true);

            Material item = event.getCurrentItem().getType();
            if (item == Material.BLACK_STAINED_GLASS_PANE) return;

            Player player = (Player) event.getWhoClicked();
            PlayerData playerData = (match.player1 == player ? match.playerData1 : match.playerData2);

            if (title.contains("wands")) {
                if (item == Material.DIRT) {
                    displayShop(player, false, 1);
                } else if (item != Material.STICK && item != Material.AIR) {
                    Wand wand = Wand.getWand(item);
                    if (wand != null) {
                        player.getInventory().setItem(0, new ItemStack(wand.item));
                        playerData.selectedWand = wand;
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
                } else if (item != Material.DIRT && item != Material.AIR) {
                    Spell spell = Spell.getSpellShop(item);
                    if (spell != null) {
                        if (playerData.tokens >= spell.price && (player.getInventory().getItem(7) == null || Objects.requireNonNull(player.getInventory().getItem(7)).isEmpty())) {
                            playerData.tokens -= spell.price;
                            player.getInventory().addItem(new ItemStack(spell.shopItem));
                            playerData.spells.add(spell);
                            player.playSound(player, Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
                            player.getInventory().setItem(12, new ItemStack(Material.GOLD_NUGGET, playerData.tokens));

                            if (player.getInventory().getItem(7) != null || !Objects.requireNonNull(player.getInventory().getItem(7)).isEmpty()) {
                                playerData.shopDone = true;
                            }
                        } else {
                            player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        if (WizardDuels.currentMatch == null) return;
        if (event.getReason() != InventoryCloseEvent.Reason.PLAYER) return;

        Bukkit.getScheduler().runTaskLater(WizardDuels.PLUGIN, () -> event.getPlayer().openInventory(event.getInventory()), 3L);
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

    public static void startShopDisplay(Player player, @NotNull PlayerData playerData) {
        displayShop(player, true, 1);
        player.getInventory().setItem(8, new ItemStack(Material.STONE_SWORD));
        player.getInventory().setItem(0, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        player.getInventory().setItem(12, new ItemStack(Material.GOLD_NUGGET, playerData.tokens));
    }

    private static final int[] EMPTY_SPOTS = { 0, 1, 2, 4, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 46, 47, 48, 49, 50, 51, 52 };
    public static void displayShop(Player player, boolean wands, int page) {
        System.out.println(wands);

        Inventory inv = Bukkit.createInventory(player, 54, Component.text("Shop - " + (wands ? "Wands" : "Spells - Page " + page)));
        for (int i : EMPTY_SPOTS) {
            inv.setItem(i, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
        }

        inv.addItem(new ItemStack(Material.STICK), new ItemStack(Material.DIRT));

        if (wands) {
            inv.setItem(45, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
            inv.setItem(53, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
            for (Wand wand : Wand.values()) {
                ItemStack item = new ItemStack(wand.item);
                item.editMeta(meta -> meta.displayName(Component.text(Translation.get(player.locale(), wand.translationKey())).decorate(TextDecoration.BOLD)));
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
