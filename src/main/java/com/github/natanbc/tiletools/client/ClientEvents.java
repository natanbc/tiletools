package com.github.natanbc.tiletools.client;

import com.github.natanbc.tiletools.TileTools;
import com.github.natanbc.tiletools.blocks.FrozenRenderer;
import com.github.natanbc.tiletools.init.Registration;
import com.github.natanbc.tiletools.items.TileInABottleItem;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = TileTools.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        ClientRegistry.bindTileEntityRenderer(Registration.FROZEN_TILE.tileEntityType(),
                FrozenRenderer::new);
    }
    
    @SubscribeEvent
    public static void registerColors(ColorHandlerEvent.Item event) {
        BlockColors blocks = event.getBlockColors();
        event.getItemColors().register((stack, i) -> {
            if(i > 0) {
                return -1;
            }
            BlockState state = TileInABottleItem.getBlockState(stack);
            if(state == null) {
                return -1;
            }
            int color = blocks.getColor(state, null, null, 0);
            if(color == -1) {
                color = state.getBlock().getMaterialColor().colorValue;
            }
            return color;
        }, Registration.TILE_IN_A_BOTTLE.get());
    }
}
