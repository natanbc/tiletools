package com.github.natanbc.tiletools.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemTier;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;

public class DeceleratorBlock extends BaseFreezerBlock {
    public DeceleratorBlock() {
        super(Block.Properties.create(Material.IRON)
                      .sound(SoundType.METAL)
                      .hardnessAndResistance(2.0f)
                      .harvestLevel(ItemTier.IRON.getHarvestLevel())
                      .harvestTool(ToolType.PICKAXE)
        );
    }
    
    @Override
    protected Class<? extends BaseFreezerTile> tileEntityClass() {
        return DeceleratorTile.class;
    }
    
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new DeceleratorTile();
    }
}
