package com.github.natanbc.tiletools.integration;

import com.github.natanbc.tiletools.init.Registration;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@JeiPlugin
public class JEIIntegration implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation("tiletools");
    }
    
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(
                new ItemStack(Registration.TILE_IN_A_BOTTLE.get()),
                VanillaTypes.ITEM,
                "message.tiletools.bottle_info1",
                "message.tiletools.empty",
                "message.tiletools.bottle_info2"
        );
    }
}
