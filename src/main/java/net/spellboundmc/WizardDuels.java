package net.spellboundmc;

import net.spellboundmc.match.GiveUpCommand;
import net.spellboundmc.match.Match;
import net.spellboundmc.match.StartCommand;
import net.spellboundmc.other.GuiManager;
import net.spellboundmc.other.Translation;
import net.spellboundmc.turn.spells.SpellEvents;
import net.spellboundmc.turn.wands.WandEvents;
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
