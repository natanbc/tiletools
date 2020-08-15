package com.github.natanbc.tiletools.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Stores frozen tile entities in a chunk, as well as which tile
 * entity is responsible for freezing them.
 *
 * Positions stored in this capability will be removed from the list
 * of tickable tile entities when the respective chunk is loaded.
 *
 * Iterating this class will yield the positions of the frozen tile
 * entities.
 */
public class FrozenTiles implements Iterable<BlockPos> {
    @SuppressWarnings("CanBeFinal")
    @CapabilityInject(FrozenTiles.class)
    public static Capability<FrozenTiles> CAPABILITY = null;
    
    //frozen tile -> freezer tile
    private final Map<BlockPos, BlockPos> positions = new HashMap<>();
    
    /**
     * Returns whether or not a given position is frozen.
     *
     * @param target Position to test.
     *
     * @return Whether or not this position is frozen.
     */
    public boolean isFrozen(BlockPos target) {
        return positions.containsKey(target);
    }
    
    /**
     * Returns whether or not a given position is frozen by a specific
     * freezer position.
     *
     * @param target  Position to test.
     * @param freezer Freezer position.
     *
     * @return Whether or not the target is frozen by the freezer.
     */
    public boolean isFrozenBy(BlockPos target, BlockPos freezer) {
        return Objects.equals(positions.get(target), freezer);
    }
    
    /**
     * Marks a position as being frozen by a given freezer position.
     *
     * @param target  Target to be frozen.
     * @param freezer Freezer responsible for freezing the target.
     */
    public void set(BlockPos target, BlockPos freezer) {
        positions.put(target.toImmutable(), freezer.toImmutable());
    }
    
    /**
     * Unfreezes a target if it's frozen by the given freezer,
     * otherwise does nothing.
     *
     * @param target  Target to unfreeze.
     * @param freezer Freezer to test.
     *
     * @return {@code true} if the target was frozen by the given freezer,
     *         {@code false} otherwise.
     */
    public boolean remove(BlockPos target, BlockPos freezer) {
        return positions.remove(target, freezer);
    }
    
    /**
     * Unfreezes a target, regardless of who froze it.
     *
     * @param target Position to unfreeze.
     */
    public void remove(BlockPos target) {
        positions.remove(target);
    }
    
    @Nonnull
    @Override
    public Iterator<BlockPos> iterator() {
        return positions.keySet().iterator();
    }
    
    /**
     * Returns the capability for the chunk at the given position.
     *
     * @param world World of the chunk.
     * @param pos   Position of a block in the chunk.
     *
     * @return The capability for that chunk.
     *
     * @throws java.lang.IllegalStateException If the capability isn't found.
     */
    public static FrozenTiles get(World world, BlockPos pos) {
        return get(world.getChunkAt(pos));
    }
    
    /**
     * Returns the capability for a given chunk.
     *
     * @param chunk Chunk of the capability.
     *
     * @return The capability for that chunk.
     *
     * @throws java.lang.IllegalStateException If the capability isn't found.
     */
    public static FrozenTiles get(Chunk chunk) {
        return chunk
                       .getCapability(CAPABILITY)
                       .orElseThrow(() -> new IllegalStateException("Capability not found"));
    }
    
    public static void register() {
        CapabilityManager.INSTANCE.register(
                FrozenTiles.class,
                new Storage(),
                FrozenTiles::new
        );
    }
    
    public static class Provider implements ICapabilitySerializable<ListNBT> {
        private final FrozenTiles capability = new FrozenTiles();
        private final LazyOptional<FrozenTiles> optional = LazyOptional.of(() -> capability);
        
        public void invalidate() {
            optional.invalidate();
        }
        
        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
            return optional.cast();
        }
    
        @Override
        public ListNBT serializeNBT() {
            if(CAPABILITY == null) {
                return new ListNBT();
            } else {
                return (ListNBT) CAPABILITY.writeNBT(capability, null);
            }
        }
    
        @Override
        public void deserializeNBT(ListNBT nbt) {
            if(CAPABILITY != null) {
                CAPABILITY.readNBT(capability, null, nbt);
            }
        }
    }
    
    private static class Storage implements Capability.IStorage<FrozenTiles> {
        @Override
        public INBT writeNBT(Capability<FrozenTiles> capability, FrozenTiles instance, Direction side) {
            ListNBT list = new ListNBT();
            for(Map.Entry<BlockPos, BlockPos> entry : instance.positions.entrySet()) {
                CompoundNBT compound = new CompoundNBT();
                compound.put("target", NBTUtil.writeBlockPos(entry.getKey()));
                compound.put("freezer", NBTUtil.writeBlockPos(entry.getValue()));
                list.add(compound);
            }
            return list;
        }
    
        @Override
        public void readNBT(Capability<FrozenTiles> capability, FrozenTiles instance, Direction side, INBT nbt) {
            for(INBT inbt : (ListNBT)nbt) {
                CompoundNBT compound = (CompoundNBT)inbt;
                instance.positions.put(
                        NBTUtil.readBlockPos(compound.getCompound("target")),
                        NBTUtil.readBlockPos(compound.getCompound("freezer"))
                );
            }
        }
    }
}
