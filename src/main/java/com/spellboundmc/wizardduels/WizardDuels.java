package com.spellboundmc.wizardduels;

import com.spellboundmc.wizardduels.match.Match;
import com.spellboundmc.wizardduels.match.StartCommand;
import com.spellboundmc.wizardduels.other.GuiManager;
import com.spellboundmc.wizardduels.turn.spells.SpellEvents;
import com.spellboundmc.wizardduels.turn.wands.WandEvents;
import com.spellboundmc.wizardduels.match.GiveUpCommand;
import com.spellboundmc.wizardduels.other.Translation;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;
import java.util.logging.Logger;

public final class WizardDuels extends JavaPlugin {
    public static final Logger LOG = Bukkit.getLogger();
    public static Plugin PLUGIN;
    public static World WORLD;
    public static File DATA_FOLDER;
    public static Match currentMatch;

    @Override
    public void onEnable() {
        Objects.requireNonNull(getCommand("start")).setExecutor(new StartCommand());
        Objects.requireNonNull(getCommand("give-up")).setExecutor(new GiveUpCommand());
        Objects.requireNonNull(getCommand("config-wd")).setExecutor(new Config());
        getServer().getPluginManager().registerEvents(new WandEvents(), this);
        getServer().getPluginManager().registerEvents(new SpellEvents(), this);
        getServer().getPluginManager().registerEvents(new GuiManager(), this);

        saveDefaultConfig();
        Config.init(getConfig());
        DATA_FOLDER = getDataFolder();

        Translation.init();

        WORLD = Bukkit.getWorld("world");
        PLUGIN = getPlugin(WizardDuels.class);

        LOG.info("Successfully started!");
    }

    @Override
    public void onDisable() {
        LOG.info("Successfully stopped!");
    }
}
