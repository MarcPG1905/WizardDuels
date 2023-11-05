package net.spellboundmc.structures;

import com.google.gson.Gson;
import net.spellboundmc.WizardDuels;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

public class StructureManager {
    public static HashSet<Structure> LOADED_STRUCTURES = new HashSet<>();
    private static final Gson GSON = new Gson();
    private static final File STRUCTURE_FILE = new File(WizardDuels.DATA_FOLDER, "structures.json");

    public static boolean save(Structure structure) {
        loadAll(false);
        try {
            if (LOADED_STRUCTURES.contains(structure)) return false;
            for (Structure struct : LOADED_STRUCTURES) {
                if (struct.name.equals(structure.name)) return false;
            }

            LOADED_STRUCTURES.add(structure);

            Structure[] existingStructures;
            try (FileReader reader = new FileReader(STRUCTURE_FILE)) {
                existingStructures = GSON.fromJson(reader, Structure[].class);
            } catch (IOException e) {
                existingStructures = new Structure[0];
            }

            Structure[] combinedStructures = Arrays.copyOf(existingStructures, existingStructures.length + 1);
            combinedStructures[existingStructures.length] = structure;

            String json = GSON.toJson(combinedStructures);
            try (FileWriter writer = new FileWriter(STRUCTURE_FILE)) {
                writer.write(json);
            }

            loadAll(false);

            return true;
        } catch (IOException e) {
            WizardDuels.LOG.info("Something went wrong while saving a Structure:\n" + structure);
            return false;
        }
    }

    public static void loadAll(boolean force) {
        try (FileReader reader = new FileReader(STRUCTURE_FILE)) {
            if (force) LOADED_STRUCTURES.clear();

            Structure[] structures = GSON.fromJson(reader, Structure[].class);
            if (structures != null) {
                for (Structure structure : structures) {
                    LOADED_STRUCTURES.add(structure);
                    LOADED_STRUCTURES.add(structure.rotated());
                }
            }
        } catch (IOException e) {
            WizardDuels.LOG.warning("Couldn't load all structures! Cause: IOException in reading the structure file!");
        }
    }

    @Contract(pure = true)
    public static @Nullable Structure get(String name) {
        for (Structure structure : LOADED_STRUCTURES) {
            if (structure.name.equalsIgnoreCase(name)) {
                return structure;
            }
        }
        return null;
    }

    public static boolean remove(String structure) {
        try {
            loadAll(true);

            for (Structure struct : LOADED_STRUCTURES) {
                if (struct.name.equals(structure)) {
                    LOADED_STRUCTURES.remove(struct);

                    Structure[] structures = LOADED_STRUCTURES.toArray(new Structure[0]);

                    String json = GSON.toJson(structures);
                    try (FileWriter writer = new FileWriter(STRUCTURE_FILE)) {
                        writer.flush();
                        writer.write(json);
                    }

                    return true;
                }
            }
        } catch (IOException e) {
            WizardDuels.LOG.info("Something went wrong while saving a Structure:\n" + structure);
        }
        return false;
    }

    public static boolean placeStructure(String name, Location loc) {
        Structure structure = StructureManager.get(name);
        if (structure == null) return false;

        World world = loc.getWorld();

        for (Structure.BlockData data : structure.blockData) {
            Location relativeLocation = new Location(world, loc.getBlockX() + data.x(), loc.getBlockY() + data.y(), loc.getBlockZ() + data.z());
            Block block = relativeLocation.getBlock();

            block.setType(data.material());
        }
        return true;
    }
}
