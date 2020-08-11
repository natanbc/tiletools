package com.github.natanbc.tiletools.util;

import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;

public class TextUtils {
    public static ITextComponent withFormatting(TextComponent component, TextFormatting formatting) {
        return component.mergeStyle(Style.EMPTY.applyFormatting(formatting));
    }
    
    public static ITextComponent withColor(TextComponent component, Color color) {
        return component.mergeStyle(Style.EMPTY.setColor(color));
    }
}
