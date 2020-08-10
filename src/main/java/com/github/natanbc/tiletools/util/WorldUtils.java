package com.github.natanbc.tiletools.util;

import net.minecraft.util.math.BlockPos;

public class WorldUtils {
    public static Iterable<BlockPos> positionsAround(BlockPos center, int radius) {
        return BlockPos.getAllInBoxMutable(
                center.getX() - radius,
                center.getY() - radius,
                center.getZ() - radius,
                center.getX() + radius,
                center.getY() + radius,
                center.getZ() + radius
        );
    }
}
