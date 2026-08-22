package com.jushymaso222.theflood.mixin.client;

import com.jushymaso222.theflood.elite.presentation.client.ElitePoseMixinHelper;

import net.minecraft.client.model.AbstractZombieModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.monster.Monster;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractZombieModel.class)
public abstract class AbstractZombieModelMixin {

    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/monster/Monster;FFFFF)V",
            at = @At("TAIL")
    )
    private void theFlood$applyElitePoseAfterZombieAnimation(
            Monster entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        ElitePoseMixinHelper.apply(
                entity,
                (HumanoidModel<?>)(Object)this,
                ageInTicks
        );
    }
}