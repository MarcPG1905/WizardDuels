package com.spellboundmc.wizardduels;

import com.spellboundmc.wizardduels.match.GiveUpCommand;
import com.spellboundmc.wizardduels.match.Match;
import com.spellboundmc.wizardduels.match.StartCommand;
import com.spellboundmc.wizardduels.other.GuiManager;
import com.spellboundmc.wizardduels.turn.spells.SpellEvents;
import com.spellboundmc.wizardduels.turn.wands.WandEvents;
import net.hectus.PostgreConnection;
import net.hectus.Translation;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Logger;

public final class WizardDuels extends JavaPlugin {
    public static final Logger LOG = Bukkit.getLogger();

    public static final PostgreConnection DATABASE;
    static {
        try {
            DATABASE = new PostgreConnection("jdbc:postgresql://localhost:5432/spellbound", "wizardduels_plugin", "rz@JwJs3z5", "wd_pd");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Plugin PLUGIN;
    public static World WORLD;
    public static Match currentMatch;

    @Override
    public void onEnable() {
        Objects.requireNonNull(getCommand("start")).setExecutor(new StartCommand());
        Objects.requireNonNull(getCommand("give-up")).setExecutor(new GiveUpCommand());
        getServer().getPluginManager().registerEvents(new WandEvents(), this);
        getServer().getPluginManager().registerEvents(new SpellEvents(), this);
        getServer().getPluginManager().registerEvents(new GuiManager(), this);

        WORLD = Bukkit.getWorld("world");
        PLUGIN = getPlugin(WizardDuels.class);

        try {
            Translation.load(new File(getDataFolder(), "lang"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LOG.info("Successfully started!");
    }

    @Override
    public void onDisable() {
        try {
            DATABASE.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        currentMatch.tie();
        LOG.info("Successfully stopped!");
    }

    /*
    POSTGRESQL DATABASE CONFIGURATION:
    | Name    | player_uuid | player_name  | matches | wins | loses | ties | kills | deaths | playtime | spell_uses | wand_uses |
    | Type    | UUID        | VARCHAR(255) | INT     | INT  | INT   | INT  | INT   | INT    | INTERVAL | INT        | INT       |
    | Default | PRIMARY KEY | NOT NULL     | 0       | 0    | 0     | 0    | 0     | 0      | 00:00:00 | 0          | 0         |
     */
}
