package com.spellboundmc.wizard_duels.match;

import com.marcpg.data.time.Time;
import com.marcpg.lang.Translation;
import com.marcpg.storing.Pair;
import com.spellboundmc.wizard_duels.GuiManager;
import com.spellboundmc.wizard_duels.PlayerData;
import com.spellboundmc.wizard_duels.WizardDuels;
import com.spellboundmc.wizard_duels.turning.TurnData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Match {
    public enum MapSize {
        MINI(8), SMALL(16), NORMAL(32), BIG(64), HUGE(128);

        public final int size;

        MapSize(int size) {
            this.size = size;
        }

        public @NotNull String getTranslated(Locale locale) {
            return Translation.string(locale, "scoreboard.map_size." + name().toLowerCase()) + " ("+size+"x"+size+")";
        }
    }

    public enum SettingPhase { NONE, TIME_OF_DAY, MAP_SIZE, TOKEN_AMOUNT, WEATHER, SHOP }

    public final List<TurnData> history = new ArrayList<>();
    public final WizardTicks timer = new WizardTicks(new Time(8, Time.Unit.MINUTES), this);
    public final Pair<PlayerData, PlayerData> players;
    public final Map<String, Object> properties = new HashMap<>();
    public final World world;
    public MapSize size;
    public SettingPhase settingPhase = SettingPhase.NONE;

    public Match(Player player1, Player player2) {
        this.players = new Pair<>(new PlayerData(player1), new PlayerData(player2));
        this.world = player1.getWorld();
    }

    public PlayerData getPlayerData(Player player) {
        return players.right().player == player ? players.right() : players.left();
    }

    public PlayerData getOpponentData(Player player) {
        return players.right().player == player ? players.left() : players.right();
    }

    public void prePhase() {
        players.both(o -> ((PlayerData) o).player.closeInventory(InventoryCloseEvent.Reason.PLUGIN));

        settingPhase = SettingPhase.values()[settingPhase.ordinal() + 1];
        switch (settingPhase) {
            case TIME_OF_DAY -> GuiManager.createInv(players.left().player, 1, new int[]{ 0, 4, 8 }, "Choose the time of the day!",
                    item(Material.PINK_WOOL, Component.text("Morning")),
                    item(Material.LIGHT_BLUE_WOOL, Component.text("Noon")),
                    item(Material.CYAN_WOOL, Component.text("Afternoon")),
                    item(Material.ORANGE_WOOL, Component.text("Evening")),
                    item(Material.BLUE_WOOL, Component.text("Night")),
                    item(Material.BLACK_WOOL, Component.text("Midnight"))
            );
            case MAP_SIZE -> GuiManager.createInv(players.right().player, 1, new int[]{ 0, 1, 7, 8 }, "Choose the map size!",
                    item(Material.CYAN_WOOL, Component.text("Mini (8x8)")),
                    item(Material.LIME_WOOL, Component.text("Small (16x16)")),
                    item(Material.YELLOW_WOOL, Component.text("Normal (32x32)")),
                    item(Material.ORANGE_WOOL, Component.text("Big (64x64)")),
                    item(Material.RED_WOOL, Component.text("Huge (128x128)"))
            );
            case TOKEN_AMOUNT -> GuiManager.createInv(players.left().player, 1, new int[]{ 0, 1, 4, 7, 8 }, "Choose the token amount!",
                    item(Material.CYAN_WOOL, Component.text("Poor (15 Tokens)")),
                    item(Material.LIME_WOOL, Component.text("Low (25 Tokens)")),
                    item(Material.YELLOW_WOOL, Component.text("Normal (35 Tokens)")),
                    item(Material.ORANGE_WOOL, Component.text("High (50 Tokens)"))
            );
            case WEATHER -> GuiManager.createInv(players.right().player, 1, new int[]{ 0, 1, 3, 5, 7, 8 }, "Choose the weather!",
                    item(Material.BLUE_WOOL, Component.text("Rain")),
                    item(Material.LIGHT_BLUE_WOOL, Component.text("Clear")),
                    item(Material.GRAY_WOOL, Component.text("Thunder"))
            );
        }
    }

    public void start() {
        players.both(o -> {
            Player player = ((PlayerData) o).player;
            player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
            player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, -1, 4, true, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, -1, 0, true, false, false));
            player.setHealth(40.0);
        });

        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    public void withering() {
        players.both(o -> ((PlayerData) o).player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, -1, 0, true, false, false)));
        players.left().player.getWorld().getPlayers().forEach(p -> p.sendActionBar(Translation.component(p.locale(), "match.withering").color(NamedTextColor.RED)));
    }

    public void lose(Player loser) {
        Bukkit.getScheduler().runTaskLater(WizardDuels.PLUGIN, () -> {
            Player winner = getOpponentData(loser).player;

            Locale wl = winner.locale();
            winner.showTitle(Title.title(Translation.component(wl, "match.win").color(NamedTextColor.GREEN), Translation.component(wl, "match.win.text")));
            winner.sendMessage(Translation.component(wl, "match.win.chat").color(NamedTextColor.GREEN));

            Locale ll = loser.locale();
            loser.showTitle(Title.title(Translation.component(ll, "match.lose").color(NamedTextColor.RED), Translation.component(ll, "match.lose.text")));
            loser.sendMessage(Translation.component(ll, "match.lose.chat").color(NamedTextColor.RED));

            stop();
            MatchManager.MATCHES.remove(this);
        }, 20);
    }

    public void tie() {
        Bukkit.getScheduler().runTaskLater(WizardDuels.PLUGIN, () -> {
            for (Player player : players.left().player.getWorld().getPlayers()) {
                Locale l = player.locale();
                player.showTitle(Title.title(Translation.component(l, "match.tie").color(NamedTextColor.YELLOW), Translation.component(l, "match.tie.text")));
                player.sendMessage(Translation.component(l, "match.tie.chat").color(NamedTextColor.YELLOW));
            }
            stop();
            MatchManager.MATCHES.remove(this);
        }, 20);
    }

    public static @NotNull ItemStack item(Material material, Component name) {
        ItemStack item = new ItemStack(material);
        item.editMeta(meta -> meta.displayName(name));
        return item;
    }
}
