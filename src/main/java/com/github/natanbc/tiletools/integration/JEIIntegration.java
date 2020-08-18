package com.github.natanbc.tiletools.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;

import javax.annotation.Nonnull;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@JeiPlugin
public class JEIIntegration implements IModPlugin {
    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation("tiletools");
    }
    
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        JsonArray array = (JsonArray)new JsonStreamParser(
                new InputStreamReader(JEIIntegration.class.getResourceAsStream(
                        "/assets/tiletools/jei_info.json"
                ), StandardCharsets.UTF_8)
        ).next();
        for(JsonElement element : array) {
            JsonObject obj = element.getAsJsonObject();
            registration.addIngredientInfo(
                    CraftingHelper.getItemStack(obj.get("target").getAsJsonObject(), true),
                    VanillaTypes.ITEM,
                    toMessageNames(obj.get("text"))
            );
        }
    }
    
    private static String[] toMessageNames(JsonElement element) {
        JsonArray array = element.getAsJsonArray();
        String[] ret = new String[array.size()];
        for(int i = 0; i < ret.length; i++) {
            ret[i] = "message.tiletools." + array.get(i).getAsString();
        }
        return ret;
    }
}
