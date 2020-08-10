package com.github.natanbc.tiletools.crafting;

import com.github.natanbc.tiletools.items.TileInABottleItem;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class TileInABottleUpgradeRecipe extends SpecialRecipe {
    public static final SpecialRecipeSerializer<TileInABottleUpgradeRecipe> SERIALIZER
            = new SpecialRecipeSerializer<>(TileInABottleUpgradeRecipe::new);
    
    public TileInABottleUpgradeRecipe(ResourceLocation idIn) {
        super(idIn);
    }
    
    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        boolean foundBottle = false;
        boolean foundPick = false;
        for(int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if(stack.getItem() instanceof TileInABottleItem) {
                    if(foundBottle) return false;
                    foundBottle = true;
                } else if(stack.getItem() instanceof PickaxeItem) {
                    if(foundPick) return false;
                    foundPick = true;
                } else {
                    return false;
                }
            }
        }
        return foundBottle & foundPick;
    }
    
    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        ItemStack bottle = ItemStack.EMPTY;
        ItemStack pick = ItemStack.EMPTY;
        for(int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if(stack.getItem() instanceof TileInABottleItem) {
                    if(!bottle.isEmpty()) return ItemStack.EMPTY;
                    bottle = stack;
                } else if(stack.getItem() instanceof PickaxeItem) {
                    if(!pick.isEmpty()) return ItemStack.EMPTY;
                    pick = stack;
                } else {
                    return ItemStack.EMPTY;
                }
            }
        }
        if(bottle.isEmpty() || pick.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int level = ((PickaxeItem)pick.getItem()).getTier().getHarvestLevel();
        if(level <= TileInABottleItem.getHarvestLevel(bottle)) {
            return ItemStack.EMPTY;
        }
        return TileInABottleItem.setHarvestLevel(bottle, level);
    }
    
    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }
    
    @Override
    public IRecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
