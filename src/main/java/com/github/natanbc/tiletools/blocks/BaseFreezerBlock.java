package com.github.natanbc.tiletools.blocks;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public abstract class BaseFreezerBlock extends Block {
    public BaseFreezerBlock(AbstractBlock.Properties properties) {
        super(properties);
    }
    
    protected abstract Class<? extends BaseFreezerTile> tileEntityClass();
    
    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }
    
    @Override
    public abstract TileEntity createTileEntity(BlockState state, IBlockReader world);
    
    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        checkEnabled(worldIn, pos);
    }
    
    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        checkEnabled(worldIn, pos);
    }
    
    private void checkEnabled(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if(tileEntityClass().isInstance(te)) {
            ((BaseFreezerTile)te).setEnabled(!world.isBlockPowered(pos));
        }
    }
}
