package com.github.natanbc.tiletools.blocks;

import com.github.natanbc.tiletools.Config;
import com.github.natanbc.tiletools.init.Registration;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class DeceleratorTile extends BaseFreezerTile {
    private int tick;
    
    public DeceleratorTile() {
        super(Registration.DECELERATOR.tileEntityType());
    }
    
    @Override
    protected boolean isBlacklisted(BlockPos pos, TileEntity te) {
        return Config.isDeceleratorBlacklisted(te);
    }
    
    @Override
    protected int radius() {
        return Config.DECELERATOR_RADIUS.get();
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        if(!enabled) tick = 0;
        super.setEnabled(enabled);
    }
    
    @Override
    public void read(@Nonnull BlockState state, CompoundNBT compound) {
        tick = compound.getInt("tick");
        super.read(state, compound);
    }
    
    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putInt("tick", tick);
        return super.write(compound);
    }
    
    @Override
    public void tick() {
        if(world == null || world.isRemote) return;
    
        if(enabled) {
            tick++;
            if(tick >= Config.DECELERATOR_FACTOR.get()) {
                tick = 0;
                tiles.values().forEach(te -> ((ITickableTileEntity)te).tick());
            }
            markDirty();
        }
        
        super.tick();
    }
}
