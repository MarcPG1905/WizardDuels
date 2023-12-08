package net.spellboundmc.other;

import net.kyori.adventure.text.TextComponent;
import net.spellboundmc.WizardDuels;
import net.spellboundmc.match.Basic1v1;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GuiEvents implements Listener {
    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (WizardDuels.currentMatch == null) return;

        if (event.isLeftClick()) return;
        if (Objects.requireNonNull(event.getClickedInventory()).getType() != InventoryType.CHEST) return;
        if (!Objects.requireNonNull(event.getCurrentItem()).getType().isAir()) return;
        if (event.getCurrentItem().getType() == Material.LIGHT_GRAY_STAINED_GLASS) return;

        Basic1v1 match = (Basic1v1) WizardDuels.currentMatch;
        if (((TextComponent) event.getView().title()).content().startsWith("Choose the")) {
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
                        case 2 -> 10;
                        case 3 -> 20;
                        case 5 -> 40;
                        case 6 -> 80;
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
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        if (event.getReason() == InventoryCloseEvent.Reason.PLUGIN) return;

        if (((TextComponent) event.getView().title()).content().startsWith("Choose the")) {
            Bukkit.getScheduler().runTaskLater(WizardDuels.getPlugin(WizardDuels.class), () -> {
                event.getPlayer().openInventory(event.getInventory());
            }, 3L);
        }
    }
}
