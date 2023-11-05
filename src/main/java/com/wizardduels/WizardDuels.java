package com.wizardduels;

import com.wizardduels.match.GiveUpCommand;
import com.wizardduels.match.Match;
import com.wizardduels.match.MatchCommand;
import com.wizardduels.structures.StructureCommand;
import com.wizardduels.structures.StructureManager;
import com.wizardduels.wands.WandUseEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;
import java.util.logging.Logger;

public final class WizardDuels extends JavaPlugin {
    public static final Logger LOG = Bukkit.getLogger();
    public static World WORLD;
    public static File DATA_FOLDER;
    public static Match currentMatch;

    @Override
    public void onEnable() {
        LOG.info("Successfully started!");

        Objects.requireNonNull(getCommand("match")).setExecutor(new MatchCommand());
        Objects.requireNonNull(getCommand("giveup")).setExecutor(new GiveUpCommand());
        Objects.requireNonNull(getCommand("structure")).setExecutor(new StructureCommand());

        getServer().getPluginManager().registerEvents(new WandUseEvent(), this);

        WORLD = Bukkit.getWorld("world");
        DATA_FOLDER = getDataFolder();

        StructureManager.loadAll(false);
    }

    @Override
    public void onDisable() {
        LOG.info("Successfully stopped!");
    }
}
