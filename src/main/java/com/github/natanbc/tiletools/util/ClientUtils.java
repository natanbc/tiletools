package com.github.natanbc.tiletools.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.function.Function;

public class ClientUtils {
    public static void addPropertyOverride(Item item, String name, Function<ItemStack, Float> override) {
        if(FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }
        ItemModelsProperties.func_239418_a_(item, new ResourceLocation(name),
                (stack, __1, __2) -> override.apply(stack));
    }
}
