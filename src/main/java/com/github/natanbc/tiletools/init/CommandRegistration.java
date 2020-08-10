package com.github.natanbc.tiletools.init;

import com.github.natanbc.tiletools.TileTools;
import com.github.natanbc.tiletools.util.TextUtils;
import com.github.natanbc.tiletools.util.TileEntityDecelerator;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import java.util.Map;

@Mod.EventBusSubscriber(modid = TileTools.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandRegistration {
    @SubscribeEvent
    public static void serverStart(FMLServerStartingEvent event) {
        CommandDispatcher<CommandSource> dispatcher =
                event.getServer().getCommandManager().getDispatcher();
        dispatcher.register(Commands.literal(TileTools.MOD_ID)
            .then(deceleratorUnsupported())
        );
    }
    
    private static ArgumentBuilder<CommandSource, ?> deceleratorUnsupported() {
        return Commands.literal("unsupported")
                .requires(cs -> cs.hasPermissionLevel(0))
                .executes(ctx -> {
                    Map<Class<? extends TileEntity>, String> map =
                            TileEntityDecelerator.unsupportedClasses();
                    map.forEach((c, r) -> {
                        ITextComponent component = new StringTextComponent("")
                                .func_230529_a_(TextUtils.withFormatting(
                                        new StringTextComponent(c.getName()),
                                        TextFormatting.YELLOW
                                ))
                                .func_230529_a_(new StringTextComponent(" -> "))
                                .func_230529_a_(TextUtils.withFormatting(
                                        new StringTextComponent(r),
                                        TextFormatting.RED
                                ));
                        ctx.getSource().sendFeedback(
                                component,
                                true
                        );
                    });
                    return 0;
                });
    }
}
