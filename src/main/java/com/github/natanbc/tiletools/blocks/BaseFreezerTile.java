package com.github.natanbc.tiletools.blocks;

import com.github.natanbc.tiletools.FreezeHandler;
import com.github.natanbc.tiletools.Config;
import com.github.natanbc.tiletools.init.Registration;
import com.github.natanbc.tiletools.util.FrozenTiles;
import com.github.natanbc.tiletools.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseFreezerTile extends TileEntity implements ITickableTileEntity {
    protected final Map<BlockPos, TileEntity> tiles = new HashMap<>();
    protected boolean enabled;
    
    public BaseFreezerTile(TileEntityType<? extends BaseFreezerTile> type) {
        super(type);
    }
    
    protected abstract boolean isBlacklisted(BlockPos pos, TileEntity te);
    protected abstract int radius();
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        markDirty();
    }
    
    @Override
    public void read(BlockState state, CompoundNBT compound) {
        enabled = compound.getBoolean("enabled");
        super.read(state, compound);
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
        
        for(BlockPos p : WorldUtils.positionsAround(pos, radius())) {
            if(!world.isAreaLoaded(p, 0)) continue;
            TileEntity te = world.getTileEntity(p);
            Chunk chunk = world.getChunkAt(p);
            FrozenTiles cap = FrozenTiles.get(chunk);
            if(te instanceof ITickableTileEntity) {
                if(isBlacklisted(p, te)) continue;
                if(te instanceof BaseFreezerTile) continue;
                
                boolean frozenByThis;
                if(!cap.isFrozen(p)) {
                    cap.set(p, pos);
                    chunk.markDirty();
                    frozenByThis = true;
                } else {
                    frozenByThis = cap.isFrozenBy(p, pos);
                }
                if(frozenByThis) {
                    TileEntity old = tiles.put(p.toImmutable(), te);
                    if(old != te) {
                        FreezeHandler.queueTickStop(te);
                    }
                }
            } else {
                if(cap.remove(p, pos)) {
                    tiles.remove(p);
                    chunk.markDirty();
                }
            }
        }
    }
    
    private void disable() {
        for(BlockPos p : WorldUtils.positionsAround(pos, radius())) {
            TileEntity te = world.getTileEntity(p);
            Chunk chunk = world.getChunkAt(p);
            FrozenTiles cap = FrozenTiles.get(chunk);
            if(cap.remove(p, pos)) {
                tiles.remove(p);
                chunk.markDirty();
                if(te instanceof ITickableTileEntity) {
                    FreezeHandler.queueTickStart(te);
                }
            }
        }
    }
}
