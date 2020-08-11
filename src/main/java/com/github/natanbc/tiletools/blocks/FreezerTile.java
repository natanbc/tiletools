package com.github.natanbc.tiletools.blocks;

import com.github.natanbc.tiletools.Config;
import com.github.natanbc.tiletools.init.Registration;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class FreezerTile extends BaseFreezerTile {
    public FreezerTile() {
        super(Registration.FREEZER.tileEntityType());
    }
    
    @Override
    protected boolean isBlacklisted(BlockPos pos, TileEntity te) {
        return Config.isFreezerBlacklisted(te);
    }
    
    @Override
    protected int radius() {
        return Config.FREEZER_RADIUS.get();
    }
}
