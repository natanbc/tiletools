package com.github.natanbc.tiletools.items;

import com.github.natanbc.tiletools.blocks.FreezerTile;
import com.github.natanbc.tiletools.init.Registration;
import com.github.natanbc.tiletools.util.FrozenTiles;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nonnull;

public class TemporaryMelterItem extends Item {
    public TemporaryMelterItem() {
        super(new Item.Properties()
                      .group(Registration.ITEM_GROUP)
        );
    }
    
    @Override
    @Nonnull
    public ActionResultType onItemUse(@Nonnull ItemUseContext context) {
        if(context.getWorld().isRemote || context.getPlayer() == null ||
                   context.getPlayer() instanceof FakePlayer) {
            return super.onItemUse(context);
        }
        TileEntity te = context.getWorld().getTileEntity(context.getPos());
        if(!(te instanceof ITickableTileEntity)) {
            return ActionResultType.FAIL;
        }
        BlockPos freezer = FrozenTiles.get(context.getWorld(), context.getPos())
                .getFreezer(context.getPos());
        if(freezer == null) {
            return ActionResultType.FAIL;
        }
        if(!(context.getWorld().getTileEntity(freezer) instanceof FreezerTile)) {
            return ActionResultType.FAIL;
        }
        ((ITickableTileEntity)te).tick();
        return ActionResultType.SUCCESS;
    }
}
