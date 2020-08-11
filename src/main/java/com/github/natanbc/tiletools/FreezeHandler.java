package com.github.natanbc.tiletools;

import com.github.natanbc.tiletools.util.FrozenTiles;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

@Mod.EventBusSubscriber(modid = TileTools.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FreezeHandler {
    private static final int QUEUE_LARGE_THRESHOLD = 100;
    private static final ResourceLocation CAPABILITY = new ResourceLocation(
            TileTools.MOD_ID,
            "frozen_tiles"
    );
    private static final ThreadLocal<List<BlockPos>> TO_REMOVE =
            ThreadLocal.withInitial(() -> new ArrayList<>(0));
    private static Queue<TileEntity> queuedRemovals = new ArrayDeque<>();
    private static Queue<TileEntity> queuedAdditions = new ArrayDeque<>();
    
    /**
     * Queues a tile entity to be removed from the list of tickable
     * tile entities at the end of the current world tick.
     *
     * @param te TileEntity to stop ticking.
     */
    public static void queueTickStop(TileEntity te) {
        queuedRemovals.add(te);
    }
    
    /**
     * Adds a tile entity to the list of tickable tile entities
     * at the end of the current world tick.
     *
     * @param te TileEntity to start ticking.
     */
    public static void queueTickStart(TileEntity te) {
        queuedAdditions.add(te);
    }
    
    @SubscribeEvent
    public static void worldTickDone(TickEvent.WorldTickEvent event) {
        if(event.phase == TickEvent.Phase.END) {
            if(queuedRemovals.size() > 0) {
                event.world.tickableTileEntities.removeAll(queuedRemovals);
                queuedRemovals = trim(queuedRemovals);
            }
            if(queuedAdditions.size() > 0) {
                event.world.tickableTileEntities.addAll(queuedAdditions);
                queuedAdditions = trim(queuedAdditions);
            }
        }
    }
    
    @SubscribeEvent
    public static void attachCapability(AttachCapabilitiesEvent<Chunk> event) {
        FrozenTiles.Provider provider = new FrozenTiles.Provider();
        event.addCapability(CAPABILITY, provider);
        event.addListener(provider::invalidate);
    }
    
    @SubscribeEvent
    public static void chunkLoad(ChunkEvent.Load e) {
        IChunk iChunk = e.getChunk();
        if(!(iChunk instanceof Chunk)) return;
        Chunk c = (Chunk)iChunk;
        FrozenTiles cap = FrozenTiles.get(c);
        List<BlockPos> remove = TO_REMOVE.get();
        for(BlockPos p : cap) {
            TileEntity te = e.getChunk().getTileEntity(p);
            if(te instanceof ITickableTileEntity) {
                c.getWorld().tickableTileEntities.remove(te);
            } else {
                remove.add(p);
            }
        }
        if(!remove.isEmpty()) {
            remove.forEach(cap::remove);
            c.markDirty();
            remove.clear();
        }
    }
    
    /**
     * Creates a new queue if the size is bigger than {@link #QUEUE_LARGE_THRESHOLD}.
     * If the queue is small, it's just cleared instead.
     *
     * This prevents large internal arrays from taking up memory when large amounts
     * of tile entities are queued for tick stop/start (for example, when loading a
     * world full of decelerators/freezers).
     *
     * <b>Do not call clear() before calling this method.</b>
     *
     * @param queue Queue to downsize.
     * @param <T> Type of elements stored in the queue.
     *
     * @return The new queue.
     */
    private static <T> Queue<T> trim(Queue<T> queue) {
        if(queue.size() > QUEUE_LARGE_THRESHOLD) {
            return new ArrayDeque<>();
        }
        queue.clear();
        return queue;
    }
}
