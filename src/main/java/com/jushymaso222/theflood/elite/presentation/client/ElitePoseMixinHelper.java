package com.jushymaso222.theflood.elite.presentation.client;

import com.jushymaso222.theflood.elite.client.ClientEliteStateData;
import com.jushymaso222.theflood.elite.presentation.EliteHumanoidPoses;
import com.jushymaso222.theflood.elite.presentation.ElitePose;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import net.minecraft.client.model.geom.ModelPart;

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

        /*
        * Reset positional changes left behind by custom
        * poses while preserving vanilla animation rotations.
        */
        restoreDefaultPivots(
                model
        );

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


        /*
        * =====================================================
        * ACTUAL POSES
        * =====================================================
        *
        * Only apply this section when a real pose is active.
        *
        * DEFAULT should NOT return from the whole helper,
        * because additive modifiers like Frenzied still
        * need to run.
        */
        if (
                renderedPose != ElitePose.DEFAULT
                && progress > 0.001F
        ) {
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


        /*
        * =====================================================
        * FRENZIED SHAKE
        * =====================================================
        *
        * Independent of ElitePose.
        */
        if (
                state != null
                && "frenzied".equals(
                        state.mutationId()
                )
                && state.statusMax() > 0.0F
        ) {
        float angerPercent =
                state.statusProgress()
                        / state.statusMax();

        angerPercent =
                Math.max(
                        0.0F,
                        Math.min(
                                angerPercent,
                                1.0F
                        )
                );

        EliteHumanoidPoses.applyFrenzyShake(
                angerPercent,
                ageInTicks,
                model.head,
                model.body,
                model.leftArm,
                model.rightArm
        );
        }

        /*
        * =====================================================
        * PURSUER RUN
        * =====================================================
        *
        * Pursuer reaches max status when its chase burst
        * begins and returns to zero when the burst ends.
        */
        if (
                state != null
                && "pursuer".equals(
                        state.mutationId()
                )
                && state.statusMax() > 0.0F
                && state.statusProgress()
                        >= state.statusMax()
        ) {
        EliteHumanoidPoses.applyPursuerRun(
                ageInTicks,
                model.body,
                model.leftArm,
                model.rightArm,
                model.leftLeg,
                model.rightLeg
        );
        }
    }

    private static void restoreDefaultPivots(
        HumanoidModel<?> model
) {
    restorePivot(
            model.head
    );

    restorePivot(
            model.body
    );

    restorePivot(
            model.leftArm
    );

    restorePivot(
            model.rightArm
    );

    restorePivot(
            model.leftLeg
    );

    restorePivot(
            model.rightLeg
    );

    /*
     * Head/body roll can be left behind by custom poses.
     *
     * Vanilla normally controls head pitch/yaw, but does
     * not reliably overwrite Z rotation every frame.
     */
    model.head.zRot =
            0.0F;

    model.body.zRot =
            0.0F;
}

        private static void restorePivot(
                ModelPart part
        ) {
        /*
        * Save vanilla's current animation rotations.
        */
        float xRot =
                part.xRot;

        float yRot =
                part.yRot;

        float zRot =
                part.zRot;

        /*
        * Reset the part back to the model's original pose.
        *
        * This restores x/y/z pivots that our custom
        * Undying pose may have moved.
        */
        part.resetPose();

        /*
        * Put vanilla's current animation rotations back.
        */
        part.xRot =
                xRot;

        part.yRot =
                yRot;

        part.zRot =
                zRot;
        }
}