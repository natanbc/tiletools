package com.github.natanbc.tiletools.blocks;

import com.github.natanbc.tiletools.Config;
import com.github.natanbc.tiletools.TileTools;
import com.github.natanbc.tiletools.init.Registration;
import com.github.natanbc.tiletools.util.TileEntityDecelerator;
import com.github.natanbc.tiletools.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class DeceleratorTile extends TileEntity implements ITickableTileEntity {
    private boolean enabled;
    
    public DeceleratorTile() {
        super(Registration.DECELERATOR.tileEntityType());
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        markDirty();
    }
    
    @Override
    public void func_230337_a_(BlockState state, CompoundNBT compound) {
        enabled = compound.getBoolean("enabled");
        super.func_230337_a_(state, compound);
    }
    
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putBoolean("enabled", enabled);
        return super.write(compound);
    }
    
    @Override
    public void remove() {
        super.remove();
        if(world == null) return;
        disable();
    }
    
    @Override
    public void tick() {
        if(world == null || world.isRemote) return;
        
        if(!enabled) {
            disable();
            return;
        }
        
        for(BlockPos p : WorldUtils.positionsAround(pos, Config.DECELERATOR_RADIUS.get())) {
            if(!world.isAreaLoaded(p, 0)) continue;
            TileEntity te = world.getTileEntity(p);
            //box it
            if(te instanceof ITickableTileEntity) {
                if(te instanceof DeceleratorTile || TileEntityDecelerator.isBoxed(te)) continue;
                world.setTileEntity(p, TileEntityDecelerator
                                               .decelerate(this, te, Config.DECELERATOR_FACTOR.get()));
            }
        }
    }
    
    private void disable() {
        for(BlockPos p : WorldUtils.positionsAround(pos, Config.DECELERATOR_RADIUS.get())) {
            TileEntity te = world.getTileEntity(p);
            //unbox it
            if(TileEntityDecelerator.isBoxedBy(this, te)) {
                TileEntity unboxed = TileEntityDecelerator.unbox(this, te);
                world.setTileEntity(p, unboxed);
            }
        }
    }
}
