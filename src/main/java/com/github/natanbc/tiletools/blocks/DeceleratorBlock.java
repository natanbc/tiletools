package com.github.natanbc.tiletools.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class DeceleratorBlock extends Block {
    public DeceleratorBlock() {
        super(Block.Properties.create(Material.IRON)
                      .sound(SoundType.METAL)
                      .hardnessAndResistance(2.0f)
        );
    }
    
    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }
    
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new DeceleratorTile();
    }
    
    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        checkEnabled(worldIn, pos);
    }
    
    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        checkEnabled(worldIn, pos);
    }
    
    private static void checkEnabled(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof DeceleratorTile) {
            ((DeceleratorTile)te).setEnabled(!world.isBlockPowered(pos));
        }
    }
}
