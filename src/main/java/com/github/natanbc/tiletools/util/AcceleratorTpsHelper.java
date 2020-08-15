package com.github.natanbc.tiletools.util;

import java.util.function.BiFunction;

@SuppressWarnings("unused")
public enum AcceleratorTpsHelper {
    DISABLED((factor, tps) -> factor),
    LINEAR((factor, tps) -> (int)Math.round(factor * (tps / 20))),
    EXPONENTIAL((factor, tps) -> (int)Math.round(factor * Math.pow(0.9, 20 - tps))),
    AGGRESSIVE((factor, tps) -> {
        if(tps > 19.5) {
            return factor;
        }
        return 0;
    });
    
    private final BiFunction<Integer, Double, Integer> function;
    
    AcceleratorTpsHelper(BiFunction<Integer, Double, Integer> function) {
        this.function = function;
    }
    
    public int recomputeFactor(int factor, double tps) {
        return Math.min(function.apply(factor, tps), factor);
    }
}
