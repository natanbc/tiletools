package com.github.natanbc.tiletools.util;

import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public class ReflectionHelper {
    //yes, the <T> is needed, ask javac why
    public static <T> MethodHandle unreflectSetter(Class<T> owner, String name) {
        return unreflectSetter(ObfuscationReflectionHelper.findField(owner, name));
    }
    
    public static MethodHandle unreflectSetter(Field field) {
        try {
            return MethodHandles.lookup().unreflectSetter(field);
        } catch(IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }
}
