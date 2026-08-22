package com.jushymaso222.theflood.elite.presentation.client;

import com.jushymaso222.theflood.elite.client.ClientEliteStateData;
import com.jushymaso222.theflood.elite.presentation.EliteHumanoidPoses;
import com.jushymaso222.theflood.elite.presentation.ElitePose;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public final class ElitePoseMixinHelper {

    private ElitePoseMixinHelper() {
    }

    public static void apply(
            LivingEntity entity,
            HumanoidModel<?> model,
            float ageInTicks
    ) {
        if (
                !(entity instanceof Mob mob)
        ) {
            return;
        }

        ClientEliteStateData.EliteState state =
                ClientEliteStateData.getState(
                        mob.getId()
                );

        ElitePose requestedPose =
                ElitePose.DEFAULT;

        if (state != null) {
            try {
                requestedPose =
                        ElitePose.valueOf(
                                state.poseId()
                        );
            }
            catch (
                    IllegalArgumentException
                    | NullPointerException ignored
            ) {
                requestedPose =
                        ElitePose.DEFAULT;
            }
        }

        ElitePoseTransitions.TransitionState transition =
                ElitePoseTransitions.update(
                        mob.getId(),
                        requestedPose,
                        ageInTicks
                );

        ElitePose renderedPose =
                transition.pose();

        float progress =
                transition.progress();

        if (
                renderedPose == ElitePose.DEFAULT
                || progress <= 0.001F
        ) {
            return;
        }

        EliteHumanoidPoses.apply(
                renderedPose,
                progress,
                ageInTicks,
                model.head,
                model.body,
                model.leftArm,
                model.rightArm,
                model.leftLeg,
                model.rightLeg
        );
    }
}