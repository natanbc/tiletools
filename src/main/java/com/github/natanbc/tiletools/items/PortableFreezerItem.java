package com.github.natanbc.tiletools.items;

import com.github.natanbc.tiletools.blocks.FrozenTile;
import com.github.natanbc.tiletools.init.Registration;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PortableFreezerItem extends Item {
    public PortableFreezerItem() {
        super(new Item.Properties()
                .group(Registration.ITEM_GROUP)
                .maxDamage(500)
        );
    }
    
    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World w = context.getWorld();
        BlockPos pos = context.getPos();
        PlayerEntity player = context.getPlayer();
        if(player == null || player instanceof FakePlayer) {
            return super.onItemUse(context);
        }
        
        TileEntity te = w.getTileEntity(pos);
        
        if(te == null) {
            return super.onItemUse(context);
        }
        if(te instanceof FrozenTile) {
            FrozenTile f = (FrozenTile)te;
            if(!f.hasValidData()) {
                return ActionResultType.FAIL;
            }
            w.setBlockState(pos, f.getStoredState());
            TileEntity read = w.getTileEntity(pos);
            if(read != null) {
                read.read(f.getStoredState(), f.getStoredTileData());
            }
        } else {
            BlockState oldState = w.getBlockState(pos);
            CompoundNBT teData = te.serializeNBT();
            w.removeTileEntity(pos);
            BlockState frozenState = Registration.FROZEN_TILE.block().getDefaultState();
            w.setBlockState(pos, frozenState);
            TileEntity frozenTe = w.getTileEntity(pos);
            if(frozenTe instanceof FrozenTile) {
                ((FrozenTile)frozenTe).init(oldState, teData);
            }
        }
        context.getItem().damageItem(1, player, _1 -> {});
        return ActionResultType.SUCCESS;
    }
}
