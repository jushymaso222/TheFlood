package com.jushymaso222.theflood.elite.presentation;

import net.minecraft.client.model.geom.ModelPart;

public final class EliteHumanoidPoses {

    private EliteHumanoidPoses() {
    }

    public static void apply(
            ElitePose pose,
            float progress,
            float ageInTicks,
            ModelPart head,
            ModelPart body,
            ModelPart leftArm,
            ModelPart rightArm,
            ModelPart leftLeg,
            ModelPart rightLeg
    ) {
        switch (pose) {

            case STUNNED ->
                    applyStunned(
                            progress,
                            ageInTicks,
                            head,
                            body,
                            leftArm,
                            rightArm,
                            leftLeg,
                            rightLeg
                    );

            case COMMANDING ->
                        applyCommanding(
                                progress,
                                ageInTicks,
                                head,
                                body,
                                leftArm,
                                rightArm,
                                leftLeg,
                                rightLeg
                        );

            default -> {
            }
        }
    }

    private static void applyCommanding(
                float progress,
                float ageInTicks,
                ModelPart head,
                ModelPart body,
                ModelPart leftArm,
                ModelPart rightArm,
                ModelPart leftLeg,
                ModelPart rightLeg
        ) {
        /*
        * Slight commanding lean.
        */
        body.xRot =
                lerp(
                        body.xRot,
                        radians(
                                -4.0F
                        ),
                        progress
                );

        /*
        * Look slightly upward while signaling.
        */
        head.xRot =
                lerp(
                        head.xRot,
                        radians(
                                -8.0F
                        ),
                        progress
                );

        /*
        * Raise the right arm as the signal.
        */
        rightArm.xRot =
                lerp(
                        rightArm.xRot,
                        radians(
                                -125.0F
                        ),
                        progress
                );

        rightArm.yRot =
                lerp(
                        rightArm.yRot,
                        radians(
                                -10.0F
                        ),
                        progress
                );

        rightArm.zRot =
                lerp(
                        rightArm.zRot,
                        radians(
                                8.0F
                        ),
                        progress
                );

        /*
        * Left arm stays mostly natural.
        */
        leftArm.xRot =
                lerp(
                        leftArm.xRot,
                        radians(
                                -8.0F
                        ),
                        progress
                );
        }

    private static void applyStunned(
            float progress,
            float ageInTicks,
            ModelPart head,
            ModelPart body,
            ModelPart leftArm,
            ModelPart rightArm,
            ModelPart leftLeg,
            ModelPart rightLeg
    ) {
        /*
        * Subtle breathing / swaying while stunned.
        */
        float breathe =
                (float) Math.sin(
                        ageInTicks * 0.08F
                );

        float sway =
                (float) Math.sin(
                        ageInTicks * 0.05F
                );

        /*
        * HEAD
        */
        head.xRot =
                lerp(
                        head.xRot,
                        radians(
                                42.0F
                                + breathe * 1.5F
                        ),
                        progress
                );

        head.zRot =
                lerp(
                        head.zRot,
                        radians(
                                4.0F
                                + sway * 1.0F
                        ),
                        progress
                );

        head.y =
                lerp(
                        head.y,
                        1.0F
                                + breathe * 0.08F,
                        progress
                );


        /*
        * BODY
        */
        body.xRot =
                lerp(
                        body.xRot,
                        radians(
                                14.0F
                                + breathe * 0.8F
                        ),
                        progress
                );

        body.y =
                lerp(
                        body.y,
                        1.0F,
                        progress
                );

        body.z =
                lerp(
                        body.z,
                        0.0F,
                        progress
                );


        /*
        * LEFT ARM
        */
        leftArm.xRot =
                lerp(
                        leftArm.xRot,
                        radians(
                                -18.0F
                                + breathe * 1.0F
                        ),
                        progress
                );

        leftArm.yRot =
                lerp(
                        leftArm.yRot,
                        radians(
                                -3.0F
                        ),
                        progress
                );

        leftArm.zRot =
                lerp(
                        leftArm.zRot,
                        radians(
                                -4.0F
                                + sway * 0.7F
                        ),
                        progress
                );

        leftArm.y =
                lerp(
                        leftArm.y,
                        1.0F,
                        progress
                );


        /*
        * RIGHT ARM
        */
        rightArm.xRot =
                lerp(
                        rightArm.xRot,
                        radians(
                                -15.0F
                                + breathe * 1.0F
                        ),
                        progress
                );

        rightArm.yRot =
                lerp(
                        rightArm.yRot,
                        radians(
                                3.0F
                        ),
                        progress
                );

        rightArm.zRot =
                lerp(
                        rightArm.zRot,
                        radians(
                                4.0F
                                - sway * 0.7F
                        ),
                        progress
                );

        rightArm.y =
                lerp(
                        rightArm.y,
                        1.0F,
                        progress
                );


        /*
        * LEGS
        */
        leftLeg.xRot =
                lerp(
                        leftLeg.xRot,
                        radians(
                                0.0F
                        ),
                        progress
                );

        leftLeg.z =
                lerp(
                        leftLeg.z,
                        3.0F,
                        progress
                );

        rightLeg.xRot =
                lerp(
                        rightLeg.xRot,
                        radians(
                                0.0F
                        ),
                        progress
                );

        rightLeg.z =
                lerp(
                        rightLeg.z,
                        3.0F,
                        progress
                );
    }

    private static float radians(
            float degrees
    ) {
        return (float) Math.toRadians(
                degrees
        );
    }

    private static float lerp(
            float start,
            float end,
            float progress
    ) {
        return start
                + (end - start)
                * progress;
    }
}