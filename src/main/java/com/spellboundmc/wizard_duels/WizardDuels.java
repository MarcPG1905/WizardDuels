package com.spellboundmc.wizard_duels;

import com.marcpg.data.database.sql.AutoCatchingSQLConnection;
import com.marcpg.data.database.sql.SQLConnection;
import com.marcpg.lang.Translation;
import com.spellboundmc.wizard_duels.commands.GiveUpCommand;
import com.spellboundmc.wizard_duels.commands.StartCommand;
import com.spellboundmc.wizard_duels.match.Match;
import com.spellboundmc.wizard_duels.match.MatchManager;
import com.spellboundmc.wizard_duels.turning.spells.SpellEvents;
import com.spellboundmc.wizard_duels.turning.wands.WandEvents;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Logger;

public class WizardDuels extends JavaPlugin {
    public static Logger LOG;
    public static FileConfiguration CONFIG;
    public static Plugin PLUGIN;
    public static AutoCatchingSQLConnection DATABASE;
    /*
    POSTGRESQL DATABASE CONFIGURATION:
    | Name    | player_uuid | player_name  | matches | wins | loses | ties | kills | deaths | playtime | spell_uses | wand_uses |
    | Type    | UUID        | VARCHAR(255) | INT     | INT  | INT   | INT  | INT   | INT    | INTERVAL | INT        | INT       |
    | Default | PRIMARY KEY | NOT NULL     | 0       | 0    | 0     | 0    | 0     | 0      | 00:00:00 | 0          | 0         |
     */

    @Override
    public void onEnable() {
        saveDefaultConfig();

        LOG = getLogger();
        CONFIG = getConfig();
        PLUGIN = this;

        try {
            File langDirectory = new File(getDataFolder(), "lang");
            if (langDirectory.mkdirs() || new File(langDirectory, "en_US.properties").createNewFile()) {
                LOG.info("Created translation directories (plugins/WizardDuels/lang/), as they didn't exist before!");
                LOG.warning("Please download the latest translations now, as the demo en_US.properties doesn't contain any translations.");
            }
            Translation.loadProperties(langDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            Class.forName("org.postgresql.Driver");
            DATABASE = new AutoCatchingSQLConnection(
                    SQLConnection.DatabaseType.POSTGRESQL,
                    Objects.requireNonNull(CONFIG.getString("postgresql.url")),
                    CONFIG.getString("postgresql.user"),
                    CONFIG.getString("postgresql.passwd"),
                    "invade_playerdata",
                    e -> LOG.warning("Couldn't interact with database: " + e.getMessage())
            );
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }

        Objects.requireNonNull(getCommand("start")).setExecutor(new StartCommand());
        Objects.requireNonNull(getCommand("give-up")).setExecutor(new GiveUpCommand());

        getServer().getPluginManager().registerEvents(new SpellEvents(), this);
        getServer().getPluginManager().registerEvents(new WandEvents(), this);
        getServer().getPluginManager().registerEvents(new Events(), this);
        getServer().getPluginManager().registerEvents(new GuiManager(), this);

        LOG.info("Successfully started up!");
    }

    @Override
    public void onDisable() {
        MatchManager.MATCHES.forEach(Match::tie);
        DATABASE.closeConnection();
        LOG.info("Successfully shut down!");
    }
}
