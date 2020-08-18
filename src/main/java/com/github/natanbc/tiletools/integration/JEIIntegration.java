package com.github.natanbc.tiletools.integration;

import com.github.natanbc.tiletools.init.Registration;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@JeiPlugin
public class JEIIntegration implements IModPlugin {
    @Nonnull
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
                "message.tiletools.bottle_info2"
        );
    
        registration.addIngredientInfo(
                new ItemStack(Registration.ACCELERATION_WAND.get()),
                VanillaTypes.ITEM,
                "message.tiletools.wand_info1",
                "message.tiletools.wand_info2"
        );
    
        registration.addIngredientInfo(
                new ItemStack(Registration.ACCELERATOR.item()),
                VanillaTypes.ITEM,
                "message.tiletools.accelerator_info",
                "message.tiletools.redstone_disable"
        );
    
        registration.addIngredientInfo(
                new ItemStack(Registration.GROWTH_ACCELERATOR.item()),
                VanillaTypes.ITEM,
                "message.tiletools.growth_accelerator_info",
                "message.tiletools.redstone_disable"
        );
    
        registration.addIngredientInfo(
                new ItemStack(Registration.DECELERATOR.item()),
                VanillaTypes.ITEM,
                "message.tiletools.decelerator_info",
                "message.tiletools.redstone_disable"
        );
    
        registration.addIngredientInfo(
                new ItemStack(Registration.FREEZER.item()),
                VanillaTypes.ITEM,
                "message.tiletools.freezer_info",
                "message.tiletools.redstone_disable"
        );
        
        ItemStack protectTeBook = new ItemStack(Items.ENCHANTED_BOOK);
        Map<Enchantment, Integer> protectTe = new HashMap<>();
        protectTe.put(Registration.PROTECT_TE_ENCHANTMENT.get(), 1);
        EnchantmentHelper.setEnchantments(protectTe, protectTeBook);
        registration.addIngredientInfo(
                protectTeBook,
                VanillaTypes.ITEM,
                "message.tiletools.protect_te_info"
        );
    
        registration.addIngredientInfo(
                new ItemStack(Registration.TEMPORARY_MELTER.get()),
                VanillaTypes.ITEM,
                "message.tiletools.melter_info1",
                "message.tiletools.melter_info2"
        );
    
        registration.addIngredientInfo(
                new ItemStack(Registration.PORTABLE_FREEZER.get()),
                VanillaTypes.ITEM,
                "message.tiletools.portable_freezer_info1",
                "message.tiletools.portable_freezer_info2"
        );
    }
}
