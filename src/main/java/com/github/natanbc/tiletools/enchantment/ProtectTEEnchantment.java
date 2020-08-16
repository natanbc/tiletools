package com.github.natanbc.tiletools.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;

public class ProtectTEEnchantment extends Enchantment {
    public ProtectTEEnchantment() {
        super(Rarity.COMMON, EnchantmentType.DIGGER, new EquipmentSlotType[] {
                EquipmentSlotType.MAINHAND
        });
    }
}
