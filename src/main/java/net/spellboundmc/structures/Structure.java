package net.spellboundmc.structures;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.PointedDripstone;
import org.bukkit.block.data.type.Slab;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class Structure {
    public final String name;
    public final HashSet<BlockData> blockData;

    public Structure(String name) {
        this.name = name;
        blockData = new HashSet<>();
    }

    public Structure rotated() {
        Structure rotated = new Structure(name);

        for (BlockData block : blockData) {
            rotated.blockData.add(new BlockData(block.material, block.z, block.y, block.x, block.direction, block.blockBound, block.open));
        }

        return rotated;
    }

    public record BlockData(Material material, int x, int y, int z, BlockFace direction, BlockBound blockBound, boolean open) {}
    public enum BlockBound { NONE, TOP, BOTTOM, DOUBLE_SLAB, STALACTITE, STALAGMITE }

    public static @NotNull Structure read(@NotNull Cord corner1, @NotNull Cord corner2, String name, World world) {
        int hX, lX, hY, lY, hZ, lZ;

        hX = Math.max(corner1.x(), corner2.x());
        lX = Math.min(corner1.x(), corner2.x());
        hY = Math.max(corner1.y(), corner2.y());
        lY = Math.min(corner1.y(), corner2.y());
        hZ = Math.max(corner1.z(), corner2.z());
        lZ = Math.min(corner1.z(), corner2.z());

        Structure structure = new Structure(name);

        Cord startCord = new Cord(lX, lY, lZ);

        for (int x = lX; x <= hX; x++) {
            for (int y = lY; y <= hY; y++) {
                for (int z = lZ; z <= hZ; z++) {
                    Block block = new Location(world, x, y, z).getBlock();
                    if (!block.getType().isAir()) {
                        structure.blockData.add(new BlockData(
                                block.getType(),
                                x - startCord.x(),
                                y - startCord.y(),
                                z - startCord.z(),
                                blockFace(block),
                                blockBound(block),
                                isOpen(block))
                        );
                    }
                }
            }
        }

        return structure;
    }


    public static BlockFace blockFace(@NotNull Block block) {
        org.bukkit.block.data.BlockData blockData = block.getBlockData();

        if (blockData instanceof Directional) {
            return ((Directional) blockData).getFacing();
        } else if (blockData instanceof Rotatable) {
            return ((Rotatable) blockData).getRotation();
        }

        return BlockFace.SELF;
    }

    public static BlockBound blockBound(@NotNull Block block) {
        org.bukkit.block.data.BlockData blockData = block.getBlockData();

        if (blockData instanceof Slab slab) {
            if (slab.getType() == Slab.Type.TOP) return BlockBound.TOP;
            else if (slab.getType() == Slab.Type.BOTTOM) return BlockBound.BOTTOM;
            else return BlockBound.DOUBLE_SLAB;
        } else if (blockData instanceof PointedDripstone dripstone) {
            if (dripstone.getVerticalDirection() == BlockFace.UP) return BlockBound.STALAGMITE;
            else return BlockBound.STALACTITE;
        }

        return BlockBound.NONE;
    }

    public static boolean isOpen(@NotNull Block block) {
        org.bukkit.block.data.BlockData blockData = block.getBlockData();

        if (blockData instanceof Openable data) {
            return data.isOpen();
        }

        return false;
    }
}
