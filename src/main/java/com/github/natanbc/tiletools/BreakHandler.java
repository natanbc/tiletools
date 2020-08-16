package com.github.natanbc.tiletools;

import com.github.natanbc.tiletools.init.Registration;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TileTools.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BreakHandler {
    @SubscribeEvent
    public static void breakStart(PlayerInteractEvent.LeftClickBlock event) {
        PlayerEntity player = event.getPlayer();
        if(player == null || player.isSneaking()) return;
        int level = EnchantmentHelper.getEnchantmentLevel(
                Registration.PROTECT_TE_ENCHANTMENT.get(),
                player.getHeldItemMainhand());
        if(level == 0) return;
        if(event.getWorld().getTileEntity(event.getPos()) == null) return;
        event.setUseItem(Event.Result.DENY);
    }
}
