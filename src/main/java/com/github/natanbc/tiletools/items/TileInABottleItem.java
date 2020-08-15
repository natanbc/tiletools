package com.github.natanbc.tiletools.items;

import com.github.natanbc.tiletools.Config;
import com.github.natanbc.tiletools.init.Registration;
import com.github.natanbc.tiletools.util.ClientUtils;
import com.github.natanbc.tiletools.util.TextUtils;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CakeBlock;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.properties.BedPart;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class TileInABottleItem extends Item {
    private static final String[] TIER_NAMES = { "wood", "stone", "iron", "diamond", "netherite" };
    private static final Color[] TIER_COLORS = {
            Color.func_240743_a_(MaterialColor.WOOD.colorValue),
            Color.func_240743_a_(MaterialColor.STONE.colorValue),
            Color.func_240743_a_(MaterialColor.IRON.colorValue),
            Color.func_240743_a_(MaterialColor.DIAMOND.colorValue),
            Color.func_240743_a_(0x40383a)
    };
    private static final int DEFAULT_TIER = ItemTier.WOOD.getHarvestLevel();
    
    public TileInABottleItem() {
        super(new Item.Properties()
                .maxStackSize(1)
                .group(Registration.ITEM_GROUP));
        ClientUtils.addPropertyOverride(
                this, "tiletools:filled", stack -> stack.getChildTag("stored_tile") == null ? 0f : 1f
        );
    }
    
    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        if(isInGroup(group)) {
            ItemStack stack = new ItemStack(this);
            for(int i = 0; i < TIER_NAMES.length; i++) {
                items.add(setHarvestLevel(stack, i));
            }
        }
    }
    
    @Override
    public int getMaxDamage(ItemStack stack) {
        return Config.BOTTLE_RECHARGE_TIME.get();
    }
    
    @Override
    public void inventoryTick(ItemStack stack, @Nonnull World worldIn, @Nonnull Entity entityIn, int itemSlot, boolean isSelected) {
        if(stack.getDamage() > 0 && worldIn.getGameTime() % 10 == 0) {
            int tier = getHarvestLevel(stack);
            int recharge = (tier + 1) * Config.BOTTLE_RECHARGE_TIER_SCALE.get();
            stack.setDamage(Math.max(0, stack.getDamage() - recharge));
        }
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, World worldIn, List<ITextComponent> tooltip,
                               @Nonnull ITooltipFlag flagIn) {
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new TranslationTextComponent("message.tiletools.tier_message", tierMessage(stack)));
        CompoundNBT stored = stack.getChildTag("stored_tile");
        if(stored != null) {
            BlockState state = NBTUtil.readBlockState(stored.getCompound("state"));
            ITextComponent block = TextUtils.withFormatting(
                    new TranslationTextComponent(state.getBlock().getTranslationKey()),
                    TextFormatting.GREEN);
            tooltip.add(new TranslationTextComponent("message.tiletools.bottle_block", block));
        } else {
            tooltip.add(new TranslationTextComponent("message.tiletools.bottle_empty"));
        }
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
    
    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        if(context.getHand() != Hand.MAIN_HAND) return super.onItemUse(context);
        ItemStack s = context.getItem();
        CompoundNBT stored = s.getChildTag("stored_tile");
        World world = context.getWorld();
        if(stored == null) {
            BlockPos pos = context.getPos();
            BlockState state = world.getBlockState(pos);
            
            //properly pick up doors and tall flowers
            if(state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) &&
                       state.get(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
                pos = pos.down();
                state = world.getBlockState(pos);
            }
            //fix piston
            if(state.getBlock() instanceof PistonHeadBlock) {
                pos = pos.offset(state.get(PistonHeadBlock.FACING).getOpposite());
                state = world.getBlockState(pos);
            }
            //fix bed
            if(state.getBlock() instanceof BedBlock) {
                BedPart part = state.get(BedBlock.PART);
                if(part == BedPart.FOOT) {
                    pos = pos.offset(state.get(BedBlock.HORIZONTAL_FACING));
                    state = world.getBlockState(pos);
                }
            }
            
            //if the block is unbreakable, can't be harvested or the bottle
            //is still recharging, drop it
            if(state.getBlockHardness(world, pos) < 0 ||
                       state.getHarvestLevel() > getHarvestLevel(s) ||
                       Config.isBottleBlacklisted(state) ||
                       s.getDamage() > 0) {
                if(context.getPlayer() != null) {
                    context.getPlayer().drop(true);
                }
                return ActionResultType.SUCCESS;
            }
            stored = s.getOrCreateChildTag("stored_tile");
            stored.put("state", NBTUtil.writeBlockState(state));
            TileEntity te = world.getTileEntity(pos);
            if(te == null) {
                stored.putBoolean("has_te", false);
            } else {
                world.removeTileEntity(pos);
                stored.putBoolean("has_te", true);
                stored.put("te", te.serializeNBT());
            }
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
        } else {
            BlockPos pos = context.getPos().offset(context.getFace());
            BlockState state = NBTUtil.readBlockState(stored.getCompound("state"));
            if(!world.getBlockState(pos).getMaterial().isReplaceable() ||
                       World.isOutsideBuildHeight(pos) ||
                        !state.isValidPosition(world, pos)) {
                return super.onItemUse(context);
            }
            s.removeChildTag("stored_tile");
            BlockState placementState =
                    state.getBlock().getStateForPlacement(new BlockItemUseContext(context));
            if(placementState != null) {
                //no free cake
                if(state.hasProperty(CakeBlock.BITES)) {
                    placementState = placementState.with(CakeBlock.BITES, state.get(CakeBlock.BITES));
                }
                //don't destroy records
                if(state.hasProperty(JukeboxBlock.HAS_RECORD)) {
                    placementState = placementState.with(JukeboxBlock.HAS_RECORD, state.get(JukeboxBlock.HAS_RECORD));
                }
                state = placementState;
            }
            world.setBlockState(pos, state);
            //allow doors and tall flowers to set the other block
            state.getBlock().onBlockPlacedBy(world, pos, state, context.getPlayer(), new ItemStack(state.getBlock()));
            if(stored.getBoolean("has_te")) {
                TileEntity te = TileEntity.readTileEntity(state, stored.getCompound("te"));
                world.setTileEntity(pos, te);
            }
            if(context.getPlayer() == null || !context.getPlayer().isCreative()) {
                s.setDamage(s.getMaxDamage());
            }
        }
        return ActionResultType.SUCCESS;
    }
    
    @Override
    public boolean isEnchantable(@Nonnull ItemStack stack) {
        return false;
    }
    
    @Nullable
    public static BlockState getBlockState(ItemStack stack) {
        CompoundNBT stored = stack.getChildTag("stored_tile");
        if(stored == null) {
            return null;
        }
        return NBTUtil.readBlockState(stored.getCompound("state"));
    }
    
    public static int getHarvestLevel(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if(tag == null || !tag.contains("harvest_level")) {
            return DEFAULT_TIER;
        }
        return tag.getInt("harvest_level");
    }
    
    public static ItemStack setHarvestLevel(ItemStack stack, int level) {
        ItemStack res = stack.copy();
        res.getOrCreateTag().putInt("harvest_level", level);
        return res;
    }
    
    private static Object tierMessage(ItemStack stack) {
        int level = getHarvestLevel(stack);
        if(level < TIER_NAMES.length) {
            return TextUtils.withColor(
                    new TranslationTextComponent("message.tiletools.tier_" + TIER_NAMES[level]),
                    TIER_COLORS[level]
            );
        } else {
            return level;
        }
    }
}
