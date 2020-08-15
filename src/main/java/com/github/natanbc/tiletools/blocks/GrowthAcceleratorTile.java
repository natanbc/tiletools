package com.github.natanbc.tiletools.blocks;

import com.github.natanbc.tiletools.Config;
import com.github.natanbc.tiletools.init.Registration;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nonnull;

public class GrowthAcceleratorTile extends TileEntity implements ITickableTileEntity {
    private boolean enabled;
    private long lastTick;
    
    public GrowthAcceleratorTile() {
        super(Registration.GROWTH_ACCELERATOR.tileEntityType());
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
        if(!enabled || world == null || world.isRemote) return;
    
        //only update once per game tick period
        if(lastTick == world.getGameTime()) {
            return;
        }
        lastTick = world.getGameTime();
        
        BlockPos pos = this.pos.up(2);
        BlockState s = world.getBlockState(pos);
        if(!s.ticksRandomly()) return;
        
        ServerWorld serverWorld = (ServerWorld)world;
        for(int i = 0; i < Config.GROWTH_FACTOR.get(); i++) {
            s.randomTick(serverWorld, pos, serverWorld.rand);
            //if the block changed, stop forcing growth
            if(serverWorld.getBlockState(pos) != s) break;
        }
    }
}
