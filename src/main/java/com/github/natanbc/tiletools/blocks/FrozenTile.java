package com.github.natanbc.tiletools.blocks;

import com.github.natanbc.tiletools.init.Registration;
import com.github.natanbc.tiletools.util.ReflectionHelper;
import com.github.natanbc.tiletools.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;

public class FrozenTile extends TileEntity {
    private static final MethodHandle SET_CACHED_BLOCK_STATE =
            ReflectionHelper.unreflectSetter(TileEntity.class, "field_195045_e");
    
    private CompoundNBT tag;
    private BlockState cachedState;
    private TileEntity cachedTile;
    
    public FrozenTile() {
        super(Registration.FROZEN_TILE.tileEntityType());
    }
    
    public void init(BlockState state, CompoundNBT tileEntityData) {
        tag = new CompoundNBT();
        tag.put("te_state", NBTUtil.writeBlockState(state));
        tag.put("te_data", tileEntityData);
        cachedState = state;
        cachedTile = null;
    }
    
    public boolean hasValidData() {
        return tag != null && tag.contains("te_state") && tag.contains("te_data");
    }
    
    public BlockState getStoredState() {
        if(cachedState == null) {
            if(tag == null) {
                cachedState = Blocks.AIR.getDefaultState();
            } else {
                cachedState = NBTUtil.readBlockState(tag.getCompound("te_state"));
            }
        }
        return cachedState;
    }
    
    public TileEntity getStoredTile() {
        if(cachedTile == null) {
            if(tag == null) {
                return null;
            }
            cachedTile = TileEntity.readTileEntity(getStoredState(), tag.getCompound("te_data"));
            if(cachedTile != null) {
                //noinspection ConstantConditions
                cachedTile.setWorldAndPos(world, pos);
                try {
                    SET_CACHED_BLOCK_STATE.invokeExact(cachedTile, getStoredState());
                } catch(Throwable t) {
                    throw new AssertionError(t);
                }
            }
        }
        return cachedTile;
    }
    
    public CompoundNBT getStoredTileData() {
        return tag != null ? tag.getCompound("te_data") : null;
    }
    
    @Override
    public void onLoad() {
        fixStoredPosition();
    }
    
    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }
    
    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        read(state, tag);
    }
    
    @Override
    public void read(@Nonnull BlockState state, CompoundNBT nbt) {
        tag = nbt.getCompound("data");
        super.read(state, nbt);
        fixStoredPosition();
    }
    
    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        //might be null when eg picking it up with the Tile in a Bottle
        //since the state has already been saved, just don't write it
        //when null (not sure why a new instance with an invalid state
        //gets created, but oh well)
        if(tag != null) {
            compound.put("data", tag);
        }
        return super.write(compound);
    }
    
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return hasValidData() ? getStoredTile().getRenderBoundingBox() : super.getRenderBoundingBox();
    }
    
    private void fixStoredPosition() {
        CompoundNBT te = getStoredTileData();
        if(te != null) {
            tag.put("te_data", WorldUtils.fixPosition(te, pos));
        }
    }
}
