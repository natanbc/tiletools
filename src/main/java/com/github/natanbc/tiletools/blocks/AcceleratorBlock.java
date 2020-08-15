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

import javax.annotation.Nonnull;

public class AcceleratorBlock extends Block {
    public AcceleratorBlock() {
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
        return new AcceleratorTile();
    }
    
    @Override
    public void onBlockPlacedBy(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, LivingEntity placer, @Nonnull ItemStack stack) {
        checkEnabled(worldIn, pos);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(@Nonnull BlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos, boolean isMoving) {
        checkEnabled(worldIn, pos);
    }
    
    private static void checkEnabled(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof AcceleratorTile) {
            ((AcceleratorTile)te).setEnabled(!world.isBlockPowered(pos));
        }
    }
}
