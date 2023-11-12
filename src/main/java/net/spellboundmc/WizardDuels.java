package net.spellboundmc;

import net.spellboundmc.match.GiveUpCommand;
import net.spellboundmc.match.Match;
import net.spellboundmc.match.MatchCommand;
import net.spellboundmc.structures.StructureCommand;
import net.spellboundmc.structures.StructureManager;
import net.spellboundmc.wands.WandUseEvent;
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

        Translation.init();
    }

    @Override
    public void onDisable() {
        LOG.info("Successfully stopped!");
    }
}
