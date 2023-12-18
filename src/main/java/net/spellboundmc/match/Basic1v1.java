package net.spellboundmc.match;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.spellboundmc.PlayerData;
import net.spellboundmc.WizardDuels;
import net.spellboundmc.other.GuiManager;
import net.spellboundmc.other.InformationManager;
import net.spellboundmc.other.Translation;
import net.spellboundmc.turn.TurnData;
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

import static com.marcpg.color.McFormat.*;

/**
 * Duos2v2 is where two players compete with each other.
 */
public class Basic1v1 implements Match {
    public final List<TurnData> history = new ArrayList<>();
    public final Player player1;
    public final Player player2;
    public final PlayerData playerData1;
    public final PlayerData playerData2;
    public final MatchTimer timer;
    public String mapSize = "ERROR";
    public Player ICE_STORM, FIRE_RING, ICY_FEET, OPPONENTS_NO_MOVEMENT, POISON_SKELETONS, NO_GRAVITATION, STORM_WALL;
    public PrePhase prePhase = PrePhase.NONE;

    public Basic1v1(@NotNull Player player1, @NotNull Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        playerData1 = new PlayerData(player1);
        playerData2 = new PlayerData(player2);

        timer = new MatchTimer(this);

        nextPrePhase();
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

    public void startMain() {
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

        player1.getWorld().getPlayers().forEach(p -> p.sendActionBar(Component.text(Translation.get(p.locale(), "match.withering"))));
    }

    public void lose(Player loser) {
        Bukkit.getScheduler().runTaskLater(WizardDuels.PLUGIN, () -> {
            Player winner = loser == player1 ? player2 : player1;
            Locale ll = loser.locale();
            Locale wl = winner.locale();

            loser.showTitle(Title.title(Component.text(RED + Translation.get(ll, "match.lose")), Component.text(Translation.get(ll, "match.lose.text"))));
            loser.sendMessage(Component.text(RED + Translation.get(ll, "match.lose.chat")));

            winner.showTitle(Title.title(Component.text(GREEN + Translation.get(wl, "match.win")), Component.text(Translation.get(wl, "match.win.text"))));
            winner.sendMessage(Component.text(GREEN + Translation.get(ll, "match.win.chat")));
        }, 20);
    }

    public void tie() {
        Bukkit.getScheduler().runTaskLater(WizardDuels.PLUGIN, () -> {
            for (Player player : player1.getWorld().getPlayers()) {
                Locale l = player.locale();
                player.showTitle(Title.title(Component.text(YELLOW + Translation.get(l, "match.tie")), Component.text(Translation.get(l, "match.tie.text"))));
                player.sendMessage(Component.text(YELLOW + Translation.get(l, "match.tie.chat")));
            }
        }, 20);
    }

    public static @NotNull ItemStack item(Material material, Component name) {
        ItemStack item = new ItemStack(material);
        item.editMeta(meta -> meta.displayName(name));
        return item;
    }
}
