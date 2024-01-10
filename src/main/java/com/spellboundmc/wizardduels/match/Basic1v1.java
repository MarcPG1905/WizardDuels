package com.spellboundmc.wizardduels.match;

import com.spellboundmc.wizardduels.PlayerData;
import com.spellboundmc.wizardduels.WizardDuels;
import com.spellboundmc.wizardduels.other.GuiManager;
import com.spellboundmc.wizardduels.other.InformationManager;
import com.spellboundmc.wizardduels.turn.TurnData;
import net.hectus.Translation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Duos2v2 is where two players compete with each other.
 */
public class Basic1v1 implements Match {
    public final List<TurnData> history = new ArrayList<>();
    private final Player player1;
    private final Player player2;
    private final PlayerData playerData1;
    private final PlayerData playerData2;
    public final MatchTimer timer;
    public Match.MapSize mapSize = null;
    public Player ICE_STORM, FIRE_RING, ICY_FEET, OPPONENTS_NO_MOVEMENT, POISON_SKELETONS, NO_GRAVITATION, STORM_WALL; // TODO: Add no gravitation feature
    public PrePhase prePhase = PrePhase.NONE;

    public Basic1v1(@NotNull Player player1, @NotNull Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        playerData1 = new PlayerData(player1);
        playerData2 = new PlayerData(player2);

        timer = new MatchTimer(this);

        nextPrePhase();
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public PlayerData getPlayerData1() {
        return playerData1;
    }

    public PlayerData getPlayerData2() {
        return playerData2;
    }

    public PlayerData getPlayerData(Player player) {
        return player == player1 ? playerData1 : playerData2;
    }

    public PlayerData getOpponentData(Player player) {
        return player == player1 ? playerData2 : playerData1;
    }

    public void nextPrePhase() {
        player1.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
        player2.closeInventory(InventoryCloseEvent.Reason.PLUGIN);

        prePhase = PrePhase.values()[prePhase.ordinal() + 1];

        switch (prePhase) {
            case TIME_OF_DAY -> GuiManager.createInv(player2, 1, new int[]{ 0, 4, 8 }, "Choose the time of the day!",
                    item(Material.PINK_WOOL, Component.text("Morning")),
                    item(Material.LIGHT_BLUE_WOOL, Component.text("Noon")),
                    item(Material.CYAN_WOOL, Component.text("Afternoon")),
                    item(Material.ORANGE_WOOL, Component.text("Evening")),
                    item(Material.BLUE_WOOL, Component.text("Night")),
                    item(Material.BLACK_WOOL, Component.text("Midnight"))
            );
            case MAP_SIZE -> GuiManager.createInv(player1, 1, new int[]{ 0, 1, 7, 8 }, "Choose the map size!",
                    item(Material.CYAN_WOOL, Component.text("Mini (8x8)")),
                    item(Material.LIME_WOOL, Component.text("Small (16x16)")),
                    item(Material.YELLOW_WOOL, Component.text("Normal (32x32)")),
                    item(Material.ORANGE_WOOL, Component.text("Big (64x64)")),
                    item(Material.RED_WOOL, Component.text("Huge (128x128)"))
            );
            case TOKEN_AMOUNT -> GuiManager.createInv(player2, 1, new int[]{ 0, 1, 4, 7, 8 }, "Choose the token amount!",
                    item(Material.CYAN_WOOL, Component.text("Poor (15 Tokens)")),
                    item(Material.LIME_WOOL, Component.text("Low (25 Tokens)")),
                    item(Material.YELLOW_WOOL, Component.text("Normal (35 Tokens)")),
                    item(Material.ORANGE_WOOL, Component.text("High (50 Tokens)"))
            );
            case WEATHER -> GuiManager.createInv(player1, 1, new int[]{ 0, 1, 3, 5, 7, 8 }, "Choose the weather!",
                    item(Material.BLUE_WOOL, Component.text("Rain")),
                    item(Material.LIGHT_BLUE_WOOL, Component.text("Clear")),
                    item(Material.GRAY_WOOL, Component.text("Thunder"))
            );
        }
    }

    @Override
    public void startMain() {
        player1.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
        player2.closeInventory(InventoryCloseEvent.Reason.PLUGIN);

        player1.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, -1, 3, true, false));
        player2.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, -1, 3, true, false));
        player1.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, -1, 0, true, false));
        player2.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, -1, 0, true, false));
        player1.setHealth(40);
        player2.setHealth(40);

        timer.start();
        InformationManager.startBasic(this);
    }

    @Override
    public void stop() {
        timer.stop();
        InformationManager.stopBasic(player1, player2);
    }

    @Override
    public void withering() {
        player1.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, -1, 0, true, false, false));
        player2.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, -1, 0, true, false, false));

        player1.getWorld().getPlayers().forEach(p -> p.sendActionBar(Translation.component(p.locale(), "match.withering").color(NamedTextColor.RED)));
    }

    @Override
    public void lose(Player loser) {
        Bukkit.getScheduler().runTaskLater(WizardDuels.PLUGIN, () -> {
            Player winner = loser == player1 ? player2 : player1;

            Locale ll = loser.locale();
            Locale wl = winner.locale();

            loser.showTitle(Title.title(Translation.component(ll, "match.lose").color(NamedTextColor.RED), Translation.component(ll, "match.lose.text")));
            loser.sendMessage(Translation.component(ll, "match.lose.chat").color(NamedTextColor.RED));

            winner.showTitle(Title.title(Translation.component(wl, "match.win").color(NamedTextColor.GREEN), Translation.component(wl, "match.win.text")));
            winner.sendMessage(Translation.component(wl, "match.win.chat").color(NamedTextColor.GREEN));

            reset();
        }, 20);
    }

    @Override
    public void tie() {
        Bukkit.getScheduler().runTaskLater(WizardDuels.PLUGIN, () -> {
            for (Player player : player1.getWorld().getPlayers()) {
                Locale l = player.locale();
                player.showTitle(Title.title(Translation.component(l, "match.tie").color(NamedTextColor.YELLOW), Translation.component(l, "match.tie.text")));
                player.sendMessage(Translation.component(l, "match.tie.chat").color(NamedTextColor.YELLOW));
            }
            reset();
        }, 20);
    }

    @Override
    public void reset() {
        mapSize = null;
        ICE_STORM = null;
        FIRE_RING = null;
        ICY_FEET = null;
        OPPONENTS_NO_MOVEMENT = null;
        POISON_SKELETONS = null;
        NO_GRAVITATION = null;
        STORM_WALL = null;
        prePhase = null;

        WizardDuels.currentMatch = null;
    }

    public static @NotNull ItemStack item(Material material, Component name) {
        ItemStack item = new ItemStack(material);
        item.editMeta(meta -> meta.displayName(name));
        return item;
    }
}
