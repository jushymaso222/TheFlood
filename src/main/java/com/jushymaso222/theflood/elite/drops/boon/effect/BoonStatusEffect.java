package com.jushymaso222.theflood.elite.drops.boon.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public final class BoonStatusEffect
        extends MobEffect {

    public BoonStatusEffect(
            int color
    ) {
        super(
                MobEffectCategory.BENEFICIAL,
                color
        );
    }
}