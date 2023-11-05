package net.spellboundmc.structures;

import net.hectus.text.Completer;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.hectus.color.McColor.*;

public class StructureCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        Player player = (Player) sender;

        switch (args.length) {
            case 8 -> {
                if (args[0].equals("save")) {
                    long start = System.currentTimeMillis();

                    Cord corner1 = new Cord(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                    Cord corner2 = new Cord(Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]));

                    Structure structure = Structure.read(corner1, corner2, args[7], player.getWorld());
                    boolean worked = StructureManager.save(structure);

                    player.sendMessage(Component.text(GRAY + "Time elapsed : " + (System.currentTimeMillis() - start) + "ms"));
                    player.sendMessage(Component.text(worked ? GREEN + "Success!" : RED + "Something went wrong!"));
                }
            }
            case 2 -> {
                if (args[0].equals("place")) {
                    long start = System.currentTimeMillis();
                    if (StructureManager.placeStructure(args[1], player.getLocation())) {
                        player.sendMessage(Component.text(RED + "The structure with the name \"" + args[1] + "\" doesn't exist!"));
                    }
                    player.sendMessage(Component.text(GRAY + "Time elapsed : " + (System.currentTimeMillis() - start) + "ms"));
                } else if (args[0].equals("remove")) {
                    player.sendMessage(Component.text(GRAY + "Removing structure with name: " + args[1]));

                    long start = System.currentTimeMillis();

                    boolean worked = StructureManager.remove(args[1]);
                    StructureManager.loadAll(true);

                    player.sendMessage(Component.text(GRAY + "Time elapsed : " + (System.currentTimeMillis() - start) + "ms"));
                    player.sendMessage(Component.text(worked ? GREEN + "Success!" : RED + "Something went wrong! A structure with this name probably doesn't exist."));
                }
            }
            case 1 -> {
                if (args[0].equals("reload")) {
                    player.sendMessage(Component.text(GRAY + "Reloading the structure data..."));
                    StructureManager.loadAll(true);
                    player.sendMessage(Component.text(GREEN + "Done!"));
                }
            }
            default -> {
                player.sendMessage(Component.text(RED + "Wrong usage! Possible usages:"));
                player.sendMessage(Component.text("/structure save x1 y1 z1 x2 y2 z2 NAME"));
                player.sendMessage(Component.text("/structure place NAME"));
                player.sendMessage(Component.text("/structure remove NAME (currently not working)"));
                player.sendMessage(Component.text("/structure reload"));
            }
        }
        return true;
    }

    private static final String[] MAIN_ARGS = { "save", "place", "remove", "reload" };
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Completer.startComplete(args[0], MAIN_ARGS);
        } else if (args.length == 2) {
            if (args[0].equals("load") || args[0].equals("remove")) {
                ArrayList<String> structures = new ArrayList<>();
                for (Structure struct : StructureManager.LOADED_STRUCTURES) structures.add(struct.name);
                return Completer.containComplete(args[1], structures);
            }
        } else if (args.length == 8) {
            return Completer.startComplete(args[7], new String[]{"true", "false"});
        }

        return List.of();
    }
}
