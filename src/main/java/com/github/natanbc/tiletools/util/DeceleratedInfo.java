package com.github.natanbc.tiletools.util;

import net.minecraft.tileentity.TileEntity;

import java.lang.reflect.Constructor;

public class DeceleratedInfo {
    private final Constructor<? extends TileEntity> original;
    private final Constructor<? extends TileEntity> decelerated;
    
    public DeceleratedInfo(Constructor<? extends TileEntity> original, Constructor<? extends TileEntity> decelerated) {
        this.original = original;
        this.decelerated = decelerated;
    }
    
    public TileEntity decelerate(TileEntity owner, TileEntity te) {
        return createAndCopy(decelerated, te, owner);
    }
    
    public TileEntity disable(TileEntity te) {
        return createAndCopy(original, te);
    }
    
    private static TileEntity createAndCopy(Constructor<? extends TileEntity> ctor, TileEntity te, Object... args) {
        TileEntity res;
        try {
            res = ctor.newInstance(args);
        } catch(ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
        res.deserializeNBT(te.getBlockState(), te.serializeNBT());
        return res;
    }
}
