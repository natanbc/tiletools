package com.github.natanbc.tiletools.blocks;

import com.github.natanbc.tiletools.Config;
import com.github.natanbc.tiletools.init.Registration;
import com.github.natanbc.tiletools.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.profiler.IProfiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class AcceleratorTile extends TileEntity implements ITickableTileEntity {
    private boolean enabled;
    private long lastTick;
    
    public AcceleratorTile() {
        super(Registration.ACCELERATOR.tileEntityType());
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        markDirty();
    }
    
    @Override
    public void read(@Nonnull BlockState state, CompoundNBT compound) {
        enabled = compound.getBoolean("enabled");
        super.read(state, compound);
    }
    
    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putBoolean("enabled", enabled);
        return super.write(compound);
    }
    
    @Override
    public void tick() {
        if(!enabled || world == null || world.isRemote) {
            return;
        }
        
        //only update once per game tick period
        if(lastTick == world.getGameTime()) {
            return;
        }
        lastTick = world.getGameTime();
        
        int factor = Config.ACCELERATOR_FACTOR.get();
        factor = Config.ACCELERATOR_TPS_HELPER.get().recomputeFactor(factor, tps(world));
        
        IProfiler profiler = world.getProfiler();
        profiler.startSection("tiletools_accelerator");
        try {
            for(BlockPos p : WorldUtils.positionsAround(pos, Config.ACCELERATOR_RADIUS.get())) {
                if(!world.isAreaLoaded(p, 0)) continue;
                TileEntity te = world.getTileEntity(p);
                if(te instanceof ITickableTileEntity) {
                    if(te instanceof AcceleratorTile) continue;
                    if(Config.isAcceleratorBlacklisted(te)) continue;
                    //          v -- This starts at 1 because the world itself already ticks
                    //          v    the TE once.
                    for(int i = 1; i < factor; i++) {
                        ((ITickableTileEntity)te).tick();
                    }
                }
            }
        } finally {
            profiler.endSection();
        }
    }
    
    private static double tps(World world) {
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
