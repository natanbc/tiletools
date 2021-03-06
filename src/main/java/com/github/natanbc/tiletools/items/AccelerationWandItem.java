package com.github.natanbc.tiletools.items;

import com.github.natanbc.tiletools.Config;
import com.github.natanbc.tiletools.init.Registration;
import com.github.natanbc.tiletools.util.TextUtils;
import com.github.natanbc.tiletools.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.profiler.IProfiler;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class AccelerationWandItem extends Item {
    //higher values nerf mending more
    //update the recipe file if this value is changed
    private static final int DURABILITY = 5000;
    
    public AccelerationWandItem() {
        super(new Item.Properties()
                      .maxDamage(DURABILITY)
                      .setNoRepair()
                      .group(Registration.ITEM_GROUP)
        );
    }
    
    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        if(isInGroup(group)) {
            ItemStack stack = new ItemStack(this);
            Map<Enchantment, Integer> map = new HashMap<>();
            map.put(Enchantments.MENDING, 1);
            EnchantmentHelper.setEnchantments(map, stack);
            items.add(stack);
        }
    }
    
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.MENDING;
    }
    
    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(book);
        return map.size() == 1 && map.containsKey(Enchantments.MENDING);
    }
    
    @Override
    public boolean isRepairable(@Nonnull ItemStack stack) {
        return false;
    }
    
    @Override
    public boolean hasEffect(@Nonnull ItemStack stack) {
        return true;
    }
    
    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        if(context.getWorld().isRemote) return super.onItemUse(context);
        PlayerEntity player = context.getPlayer();
        if(player instanceof FakePlayer) {
            return ActionResultType.FAIL;
        }
        if(player != null && player.isSneaking()) {
            return doUse(player, context.getItem(), context.getWorld(), context.getPos());
        }
        return super.onItemUse(context);
    }
    
    private static ActionResultType doUse(PlayerEntity player, ItemStack stack, World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof ITickableTileEntity) {
            stack.setAnimationsToGo(5);
            return tickTileEntity(player, stack, (ITickableTileEntity)te);
        } else {
            BlockState state = world.getBlockState(pos);
            if(state.ticksRandomly() && world instanceof ServerWorld) {
                stack.setAnimationsToGo(5);
                return tickCrop(player, stack, (ServerWorld)world, pos);
            }
        }
        return ActionResultType.PASS;
    }
    
    private static ActionResultType tickTileEntity(PlayerEntity player, ItemStack stack, ITickableTileEntity te) {
        if(Config.isTEWandBlacklisted((TileEntity)te)) {
            return ActionResultType.FAIL;
        }
        int configSpeedup = Config.WAND_TE_SPEEDUP.get();
        int speedup = Config.WAND_TPS_HELPER.get().recomputeFactor(
                configSpeedup, WorldUtils.tps(player.world));
        double scaledUsage = Config.WAND_TE_USES.get() * (speedup / (double) configSpeedup);
        int damage = (int)Math.max(Math.ceil(DURABILITY / scaledUsage), 1);
        if(allowEffect(stack, player, damage)) {
            IProfiler profiler = player.world.getProfiler();
            profiler.startSection("tiletools_acceleration_wand");
            try {
                for(int i = 0; i < speedup; i++) {
                    te.tick();
                }
            } finally {
                profiler.endSection();
            }
        }
        return ActionResultType.SUCCESS;
    }
    
    private static ActionResultType tickCrop(PlayerEntity player, ItemStack stack, ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if(Config.isCropWandBlacklisted(state)) {
            return ActionResultType.FAIL;
        }
        int speedup = Config.WAND_CROP_SPEEDUP.get();
        int damage = Math.max(DURABILITY / Config.WAND_CROP_USES.get(), 1);
        if(allowEffect(stack, player, damage)) {
            for(int i = 0; i < speedup; i++) {
                state.randomTick(world, pos, world.rand);
                state = world.getBlockState(pos);
                if(!state.ticksRandomly()) break;
            }
        }
        return ActionResultType.SUCCESS;
    }
    
    private static boolean allowEffect(ItemStack stack, PlayerEntity player, int damage) {
        stack.setAnimationsToGo(5);
        if(damageItem(stack, player, damage)) {
            player.sendStatusMessage(
                    TextUtils.withFormatting(
                            new TranslationTextComponent("message.tiletools.wand_damage"),
                            TextFormatting.DARK_RED
                    ), false);
            if(Config.WAND_BAD_EFFECTS_ENABLED.get()) {
                player.attackEntityFrom(DamageSource.MAGIC, 4);
                int duration = Config.WAND_BAD_EFFECTS_DURATION.get();
                player.addPotionEffect(new EffectInstance(Effects.BLINDNESS, duration, 2));
                player.addPotionEffect(new EffectInstance(Effects.MINING_FATIGUE, duration, 150));
                player.addPotionEffect(new EffectInstance(Effects.SLOWNESS, duration, 150));
                player.addPotionEffect(new EffectInstance(Effects.JUMP_BOOST, duration, 150));
            }
            return false;
        } else {
            return true;
        }
    }
    
    private static boolean damageItem(ItemStack stack, PlayerEntity player, int damage) {
        if(!player.world.isRemote && !player.abilities.isCreativeMode) {
            int amt = stack.getItem().damageItem(stack, damage, player, __ -> {});
            if (stack.attemptDamageItem(amt, player.getRNG(), player instanceof ServerPlayerEntity ? (ServerPlayerEntity)player : null)) {
                stack.setDamage(stack.getMaxDamage() - 1);
                return true;
            }
        }
        return false;
    }
}
