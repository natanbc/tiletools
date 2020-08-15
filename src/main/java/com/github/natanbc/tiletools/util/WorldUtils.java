package com.github.natanbc.tiletools.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
    
    public static double tps(World world) {
        MinecraftServer server = world.getServer();
        //if we can't compute, assume 20
        if(server == null) {
            return 20;
        }
        double meanTickTime = mean(server.tickTimeArray) * 1.0E-6D;
        return Math.min(1000.0/meanTickTime, 20);
    }
    
    private static long mean(long[] values) {
        long sum = 0L;
        for(long v : values) {
            sum += v;
        }
        return sum / values.length;
    }
}
