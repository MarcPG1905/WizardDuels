package net.spellboundmc;

import com.marcpg.text.Completer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class Config implements CommandExecutor, TabExecutor {
    public static boolean ALLOW_JOIN;
    public static boolean EXPERIMENTAL;
    public static boolean COMPETITIVE;
    public static boolean LOCALIZATION;
    public static List<String> DISALLOWED_TYPES;

    public static void init(@NotNull FileConfiguration config) {
        ALLOW_JOIN = config.getBoolean("allow-join");
        EXPERIMENTAL = config.getBoolean("experimental");
        COMPETITIVE = config.getBoolean("competitive");
        LOCALIZATION = config.getBoolean("localization");
        DISALLOWED_TYPES = Objects.requireNonNull(config.getStringList("disallowed-types"));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args[0].equals("reload") && args.length == 1) {
            WizardDuels.PLUGIN.reloadConfig();
            init(WizardDuels.PLUGIN.getConfig());
            sender.sendMessage(Component.text("Configuration reloaded!", TextColor.color(0, 255, 0)));
        } else if (args[0].equals("set") && args.length == 3) {
            if (!WizardDuels.PLUGIN.getConfig().contains(args[1])) {
                sender.sendMessage(Component.text("The key " + args[1] + " could not be found!", TextColor.color(255, 0, 0)));
                return true;
            }
            if (args[1].equals("allow-types")) {
                sender.sendMessage(Component.text("You can't set lists, due to technical limitations!", TextColor.color(255, 0, 0)));
                return true;
            }

            WizardDuels.PLUGIN.getConfig().set(args[1], args[2]);
            WizardDuels.PLUGIN.saveConfig();

            sender.sendMessage(Component.text("Saved the new value. Please keep in mind that providing a wrong value type can break the plugin. To reload, please type '/config-wd reload'!", TextColor.color(255, 255, 0)));
        } else if (args[0].equals("get") && args.length == 2) {
            if (!WizardDuels.PLUGIN.getConfig().contains(args[1])) {
                sender.sendMessage(Component.text("The key " + args[1] + " could not be found!", TextColor.color(255, 0, 0)));
                return true;
            }
            sender.sendMessage(Component.text(args[1] + " = " + WizardDuels.PLUGIN.getConfig().get(args[1])));
        } else {
            sender.sendMessage(Component.text("Wrong arguments! Valid commands are: ", TextColor.color(255, 0, 0)));
            sender.sendMessage(Component.text("- /config-wd reload"));
            sender.sendMessage(Component.text("- /config-wd get <key>"));
            sender.sendMessage(Component.text("- /config-wd set <key> <value>"));
            return false;
        }

        return true;
    }


    private static final List<String> KEYS = List.of("game-id", "allow-join", "experimental", "competitive", "localization");
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Completer.startComplete(args[0], List.of("reload", "get", "set"));
        } else if (args.length == 2 && (args[0].equals("get") || args[0].equals("set"))) {
            return Completer.containComplete(args[1], KEYS);
        } else if (args.length == 3 && args[0].equals("set")) {
            if (KEYS.contains(args[2]) && args[2].equals("game-id")) {
                return Completer.startComplete(args[2], List.of("true", "false"));
            }
        }
        return List.of();
    }
}
