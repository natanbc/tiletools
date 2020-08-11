package com.github.natanbc.tiletools.init;

import com.github.natanbc.tiletools.TileTools;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

@Mod.EventBusSubscriber(modid = TileTools.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandRegistration {
    @SubscribeEvent
    public static void serverStart(FMLServerStartingEvent event) {
        CommandDispatcher<CommandSource> dispatcher =
                event.getServer().getCommandManager().getDispatcher();
        //dispatcher.register(Commands.literal(TileTools.MOD_ID));
    }
}
