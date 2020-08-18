package com.github.natanbc.tiletools.blocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.data.EmptyModelData;

import javax.annotation.Nonnull;

public class FrozenRenderer extends TileEntityRenderer<FrozenTile> {
    public FrozenRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }
    
    @Override
    public void render(FrozenTile tileEntityIn, float partialTicks, @Nonnull MatrixStack matrixStackIn,
                       @Nonnull IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if(!tileEntityIn.hasValidData()) {
            return;
        }
        BlockState state = tileEntityIn.getStoredState();
        if(state.getRenderType() == BlockRenderType.MODEL) {
            matrixStackIn.push();
            Minecraft.getInstance().getBlockRendererDispatcher()
                    .renderBlock(tileEntityIn.getStoredState(), matrixStackIn, bufferIn,
                            combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
            matrixStackIn.pop();
        }
        TileEntity te = tileEntityIn.getStoredTile();
        if(te != null) {
            TileEntityRenderer<TileEntity> renderer =
                    TileEntityRendererDispatcher.instance.getRenderer(te);
            if(renderer != null) {
                matrixStackIn.push();
                renderer.render(te, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
                matrixStackIn.pop();
            }
        }
    }
}
