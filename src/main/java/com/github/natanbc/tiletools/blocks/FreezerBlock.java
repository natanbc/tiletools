package com.github.natanbc.tiletools.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class FreezerBlock extends BaseFreezerBlock {
    public FreezerBlock() {
        super(Block.Properties.create(Material.IRON)
                      .sound(SoundType.METAL)
                      .hardnessAndResistance(2.0f)
        );
    }
    
    @Override
    protected Class<? extends BaseFreezerTile> tileEntityClass() {
        return FreezerTile.class;
    }
    
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new FreezerTile();
    }
}
