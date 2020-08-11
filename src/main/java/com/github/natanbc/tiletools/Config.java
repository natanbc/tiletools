package com.github.natanbc.tiletools;

import com.github.natanbc.tiletools.util.AcceleratorTpsHelper;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Config {
    public static final ForgeConfigSpec CLIENT;
    public static final ForgeConfigSpec COMMON;
    public static final ForgeConfigSpec SERVER;
    
    public static final ForgeConfigSpec.IntValue ACCELERATOR_RADIUS;
    public static final ForgeConfigSpec.IntValue ACCELERATOR_FACTOR;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ACCELERATOR_BLACKLIST;
    public static final ForgeConfigSpec.EnumValue<AcceleratorTpsHelper> ACCELERATOR_TPS_HELPER;
    
    public static final ForgeConfigSpec.IntValue DECELERATOR_RADIUS;
    public static final ForgeConfigSpec.IntValue DECELERATOR_FACTOR;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> DECELERATOR_BLACKLIST;
    
    public static final ForgeConfigSpec.IntValue FREEZER_RADIUS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> FREEZER_BLACKLIST;
    
    public static final ForgeConfigSpec.IntValue GROWTH_FACTOR;
    
    public static final ForgeConfigSpec.IntValue BOTTLE_RECHARGE_TIME;
    public static final ForgeConfigSpec.IntValue BOTTLE_RECHARGE_TIER_SCALE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BOTTLE_BLACKLIST;
    
    static {
        ForgeConfigSpec.Builder client = new ForgeConfigSpec.Builder();
        ForgeConfigSpec.Builder common = new ForgeConfigSpec.Builder();
        ForgeConfigSpec.Builder server = new ForgeConfigSpec.Builder();
        
        try(Section s = section(common, "accelerator")) {
            ACCELERATOR_RADIUS = common
                     .comment("Radius used for finding blocks to accelerate.",
                              "The search happens on a cube, centered at the accelerator",
                              "from X - r, Y - r, Z - r to X + r, Y + r, Z + r.",
                              "Setting to 0 disables the accelerator")
                     .defineInRange("radius", 2, 0, 3);
            ACCELERATOR_FACTOR = common
                     .comment("How many ticks each block affected by an accelerator",
                              "should receive per world tick. Setting to 0 disables the",
                              "accelerator")
                     .defineInRange("factor", 10, 0, 100);
            ACCELERATOR_BLACKLIST = common
                     .comment("List of tile entities that should not be accelerated")
                     .defineList("blacklist",
                             Collections.singletonList("tiletools:decelerator"), _1 -> true);
            ACCELERATOR_TPS_HELPER = common
                     .comment("Determines how the accelerator slows down it's additional",
                              "ticks when TPS is low. Valid methods are:",
                              "- disabled: additional ticks are not affected by tps",
                              "- linear: additional ticks are linearly proportional to tps (factor *= (tps/20))",
                              "- exponential: additional ticks are proportional to 0.9^(20 - tps)",
                              "- aggressive: additional ticks are disabled if tps is lower than 19.5")
                     .defineEnum("tps_helper", AcceleratorTpsHelper.LINEAR);
        }
        
        try(Section s = section(common, "decelerator")) {
            DECELERATOR_RADIUS = common
                     .comment("Radius used for finding blocks to decelerate.",
                             "The search happens on a cube, centered at the decelerator",
                             "from X - r, Y - r, Z - r to X + r, Y + r, Z + r.",
                             "Setting to 0 disables the decelerator")
                     .defineInRange("radius", 2, 0, 3);
            DECELERATOR_FACTOR = common
                     .comment("How many world ticks each block affected by a decelerator",
                             "should receive to actually tick. Setting to 1 or less disables",
                             "the decelerator")
                     .defineInRange("factor", 10, 0, 100);
            DECELERATOR_BLACKLIST = common
                     .comment("List of tile entities that should not be decelerated")
                     .defineList("blacklist",
                            Collections.singletonList("tiletools:accelerator"), _1 -> true);
        }
        
        try(Section s = section(common, "freezer")) {
            FREEZER_RADIUS = common
                    .comment("Radius used for finding blocks to freeze.",
                            "The search happens on a cube, centered at the freezer",
                            "from X - r, Y - r, Z - r to X + r, Y + r, Z + r.",
                            "Setting to 0 disables the freezer")
                    .defineInRange("radius", 2, 0, 3);
            FREEZER_BLACKLIST = common
                    .comment("List of tile entities that should not be frozen")
                    .defineList("blacklist", Arrays.asList("tiletools:accelerator", "tiletools:freezer"), _1 -> true);
        }
        
        try(Section s = section(common, "growth_accelerator")) {
            GROWTH_FACTOR = common
                     .comment("How many block ticks and growth ticks should each growth",
                              "accelerator send to the plant on it per world tick",
                              "Note that block ticks are NOT tile entity ticks.",
                              "Setting to 0 disables the growth accelerator")
                     .defineInRange("factor", 2, 0, 100);
        }
        
        try(Section s = section(common, "tile_in_a_bottle",
                "How long recharging the tile in a bottle takes.",
                "Every 10 ticks, it gets recharged by (tier+1)*<scale>",
                "points.",
                "Wood is tier 0, stone is 1, iron is 2, diamond is 3, netherite is 4")) {
            BOTTLE_RECHARGE_TIME = common
                    .comment("How long recharging the tile in a bottle takes.",
                             "It can be used again after regaining this many points",
                             "Setting this to 0 disables the need to recharge.")
                    .defineInRange("recharge_time", 1000, 0, 100000);
            BOTTLE_RECHARGE_TIER_SCALE = common
                    .comment("Determines how much the recharge time depends on tier",
                             "The higher it is, the faster higher tiers recharge compared",
                             "to lower tiers.",
                             "Setting this to 0 makes each bottle single-use")
                    .defineInRange("tier_scale", 5, 0, 50);
            BOTTLE_BLACKLIST = common
                    .comment("Blocks that cannot be picked up by the bottle")
                    .defineList("blacklist", Collections.emptyList(), _1 -> true);
        }
        
        CLIENT = client.build();
        COMMON = common.build();
        SERVER = server.build();
    }
    
    public static void init() {
        register(ModConfig.Type.CLIENT, CLIENT);
        register(ModConfig.Type.COMMON, COMMON);
        register(ModConfig.Type.SERVER, SERVER);
    }
    
    public static boolean isAcceleratorBlacklisted(TileEntity te) {
        return isInList(ACCELERATOR_BLACKLIST.get(), te);
    }
    
    public static boolean isDeceleratorBlacklisted(TileEntity te) {
        return isInList(DECELERATOR_BLACKLIST.get(), te);
    }
    
    public static boolean isFreezerBlacklisted(TileEntity te) {
        return isInList(FREEZER_BLACKLIST.get(), te);
    }
    
    public static boolean isBottleBlacklisted(BlockState state) {
        ResourceLocation res = state.getBlock().getRegistryName();
        return res != null && BOTTLE_BLACKLIST.get().contains(res.toString());
    }
    
    private static boolean isInList(List<? extends String> list, TileEntity te) {
        ResourceLocation res = te.getType().getRegistryName();
        return res == null ? list.contains(te.getClass().getName()) : list.contains(res.toString());
    }
    
    private static void register(ModConfig.Type type, ForgeConfigSpec spec) {
        if(!spec.isEmpty()) {
            ModLoadingContext.get().registerConfig(type, spec);
        }
    }
    
    private static Section section(ForgeConfigSpec.Builder builder, String name, String... comment) {
        builder.comment(comment).push(name);
        return builder::pop;
    }
    
    private interface Section extends AutoCloseable {
        @Override
        void close();
    }
}
