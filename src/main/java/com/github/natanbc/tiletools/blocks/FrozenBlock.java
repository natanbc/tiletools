package com.github.natanbc.tiletools.blocks;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

public class FrozenBlock extends Block {
    public FrozenBlock() {
        super(AbstractBlock.Properties.create(Material.ICE)
                .sound(SoundType.GLASS)
                .variableOpacity() //disable state cache
                .notSolid()
                .setBlocksVision((s, w, p) -> false)
                .setOpaque((s, w, p) -> false)
                .hardnessAndResistance(2)
        );
    }
    
    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }
    
    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new FrozenTile();
    }
    
    @Override
    public void onBlockExploded(BlockState state, World world, BlockPos pos, Explosion explosion) {
        if(!remove(world, pos)) {
            super.onBlockExploded(state, world, pos, explosion);
        }
    }
    
    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
        return !remove(world, pos);
    }
    
    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        if(!remove(worldIn, pos)) {
            super.onBlockHarvested(worldIn, pos, state, player);
        }
    }
    
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }
    
    @Override
    public float getSlipperiness(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
        return Blocks.ICE.getDefaultState().getSlipperiness(world, pos, entity);
    }
    
    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return getStateValue(world, pos,
                s -> s.getLightValue(world, pos) / 2,
                () -> super.getLightValue(state, world, pos));
    }
    
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return getStateValue(worldIn, pos,
                s -> s.getShape(worldIn, pos, context),
                () -> super.getShape(state, worldIn, pos, context));
    }
    
    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos pos) {
        return getStateValue(reader, pos,
                s -> s.getCollisionShape(reader, pos),
                () -> super.getCollisionShape(state, reader, pos));
    }
    
    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return getStateValue(worldIn, pos,
                s -> s.getCollisionShape(worldIn, pos, context),
                () -> super.getCollisionShape(state, worldIn, pos, context));
    }
    
    @Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return getStateValue(worldIn, pos,
                s -> s.getRenderShape(worldIn, pos),
                () -> super.getRenderShape(state, worldIn, pos));
    }
    
    private static boolean remove(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof FrozenTile && ((FrozenTile) te).hasValidData()) {
            FrozenTile f = (FrozenTile)te;
            world.setBlockState(pos, f.getStoredState());
            TileEntity read = world.getTileEntity(pos);
            if(read != null) {
                read.read(f.getStoredState(), f.getStoredTileData());
            }
            return true;
        }
        return false;
    }
    
    private static <T> T getStateValue(IBlockReader world, BlockPos pos, Function<BlockState, T> ifPresent,
                                       Supplier<T> ifAbsent) {
        TileEntity te = world.getTileEntity(pos);
        if(!(te instanceof FrozenTile)) {
            return ifAbsent.get();
        }
        if(!((FrozenTile) te).hasValidData()) {
            return ifAbsent.get();
        }
        return ifPresent.apply(((FrozenTile)te).getStoredState());
    }
}
