package com.github.natanbc.tiletools.util;

import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;

public class TextUtils {
    public static ITextComponent withFormatting(TextComponent component, TextFormatting formatting) {
        return component.func_230530_a_(Style.field_240709_b_.func_240712_a_(formatting));
    }
    
    public static ITextComponent withColor(TextComponent component, Color color) {
        return component.func_230530_a_(Style.field_240709_b_.func_240718_a_(color));
    }
}
