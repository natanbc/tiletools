package com.github.natanbc.tiletools.init;

import com.github.natanbc.tiletools.TileTools;
import com.github.natanbc.tiletools.blocks.AcceleratorBlock;
import com.github.natanbc.tiletools.blocks.AcceleratorTile;
import com.github.natanbc.tiletools.blocks.DeceleratorBlock;
import com.github.natanbc.tiletools.blocks.DeceleratorTile;
import com.github.natanbc.tiletools.blocks.GrowthAcceleratorBlock;
import com.github.natanbc.tiletools.blocks.GrowthAcceleratorTile;
import com.github.natanbc.tiletools.crafting.TileInABottleUpgradeRecipe;
import com.github.natanbc.tiletools.items.TileInABottleItem;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

import static com.github.natanbc.tiletools.TileTools.MOD_ID;

public class Registration {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    private static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_ID);
    private static final DeferredRegister<IRecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID);
    
    public static void init() {
        TileTools.logger().info("Registering things!");
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
        RECIPES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
    
    public static final ItemGroup ITEM_GROUP = new ItemGroup("tiletools") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(TILE_IN_A_BOTTLE.get());
        }
    };
    
    public static final RegistryObject<TileInABottleItem> TILE_IN_A_BOTTLE =
            ITEMS.register("tile_in_a_bottle", TileInABottleItem::new);
    
    public static final RegistryBlockWithTE<AcceleratorBlock, AcceleratorTile> ACCELERATOR
            = new RegistryBlockWithTE<>("accelerator", AcceleratorBlock::new, AcceleratorTile::new);
    
    public static final RegistryBlockWithTE<DeceleratorBlock, DeceleratorTile> DECELERATOR
            = new RegistryBlockWithTE<>("decelerator", DeceleratorBlock::new, DeceleratorTile::new);
    
    public static final RegistryBlockWithTE<GrowthAcceleratorBlock, GrowthAcceleratorTile> GROWTH_ACCELERATOR
            = new RegistryBlockWithTE<>("growth_accelerator", GrowthAcceleratorBlock::new, GrowthAcceleratorTile::new);
    
    public static final RegistryObject<IRecipeSerializer<?>> TILE_IN_A_BOTTLE_UPGRADE_RECIPE
            = RECIPES.register("crafting_special_bottle_upgrade", () -> TileInABottleUpgradeRecipe.SERIALIZER);
    
    public static class RegistryBlockWithTE<B extends Block, TE extends TileEntity> {
        private final String name;
        private final RegistryObject<B> block;
        private final RegistryObject<Item> item;
        private final RegistryObject<TileEntityType<TE>> tileEntity;
        
        RegistryBlockWithTE(String name, Supplier<B> blockFactory, Supplier<TE> teFactory) {
            this.name = name;
            this.block = BLOCKS.register(name, blockFactory);
            this.item = ITEMS.register(name,
                    () -> new BlockItem(block.get(), new Item.Properties().group(ITEM_GROUP)));
            this.tileEntity = TILES.register(name,
                    () -> TileEntityType.Builder.create(teFactory, block.get()).build(null));
        }
        
        public String name() { return name; }
        
        public B block() { return block.get(); }
        
        public Item item() { return item.get(); }
        
        public TileEntityType<TE> tileEntityType() { return tileEntity.get(); }
    }
}
