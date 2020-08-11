package com.github.natanbc.tiletools.blocks;

import com.github.natanbc.tiletools.init.Registration;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class FreezerTile extends BaseFreezerTile {
    public FreezerTile() {
        super(Registration.FREEZER.tileEntityType());
    }
    
    @Override
    protected boolean isBlacklisted(BlockPos pos, TileEntity te) {
        return false;
    }
    
    @Override
    protected int radius() {
        return 2;
    }
}
