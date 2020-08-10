package com.github.natanbc.tiletools;

import com.github.natanbc.tiletools.init.Registration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(TileTools.MOD_ID)
public class TileTools {
    public static final String MOD_ID = "tiletools";
    
    private static final Logger LOGGER = LogManager.getLogger();
    
    public TileTools() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener((FMLCommonSetupEvent e) -> {
            logger().info("Starting!");
        });
        
        Config.init();
        //register items, blocks, etc
        Registration.init();
    }
    
    public static Logger logger() {
        return LOGGER;
    }
}
