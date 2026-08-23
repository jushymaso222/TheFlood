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

            case CONJURING ->
                        applyConjuring(
                                progress,
                                ageInTicks,
                                head,
                                body,
                                leftArm,
                                rightArm,
                                leftLeg,
                                rightLeg
                        );

            case UNDYING_DOWNED ->
                        applyUndyingDowned(
                                progress,
                                ageInTicks,
                                head,
                                body,
                                leftArm,
                                rightArm,
                                leftLeg,
                                rightLeg
                        );

            case UNDYING_SINKING ->
                        applyUndyingSinking(
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

    private static void applyUndyingSinking(
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
     * Slow-starting downward pull.
     */
    float eased =
            progress
                    * progress
                    * progress;

    /*
     * Keep this relatively small.
     *
     * ModelPart positions are model pixels, so even
     * seemingly small values move quite a lot.
     */
    float sink =
            eased
                    * 8.0F;

    /*
     * Shake fades away as the body is pulled under.
     */
    float shakeA =
            (float) Math.sin(
                    ageInTicks * 3.2F
            )
                    * 0.15F
                    * (1.0F - eased);

    float shakeB =
            (float) Math.cos(
                    ageInTicks * 3.7F
            )
                    * 0.12F
                    * (1.0F - eased);


    /*
     * BODY
     *
     * IMPORTANT:
     * These are the SAME base coordinates as the
     * finished DOWNED pose. No lerping from vanilla.
     */
    body.xRot =
            radians(
                    90.0F
            );

    body.y =
            21.0F
                    + sink
                    + shakeA;

    body.z =
            0.0F;


    /*
     * HEAD
     */
    head.xRot =
            radians(
                    90.0F
            );

    head.zRot =
            radians(
                    18.0F
            );

    head.x =
            10.0F
                    + shakeB;

    head.y =
            21.0F
                    + sink;

    head.z =
            -10.0F;


    /*
     * LEFT ARM
     */
    leftArm.xRot =
            radians(
                    90.0F
            );

    leftArm.zRot =
            radians(
                    -20.0F
            );

    leftArm.x =
            -14.0F
                    + shakeA;

    leftArm.y =
            21.5F
                    + sink;

    leftArm.z =
            -3.0F;


    /*
     * RIGHT ARM
     */
    rightArm.xRot =
            radians(
                    90.0F
            );

    rightArm.zRot =
            radians(
                    25.0F
            );

    rightArm.x =
            15.0F
                    + shakeB;

    rightArm.y =
            21.5F
                    + sink;

    rightArm.z =
            4.0F;


    /*
     * LEFT LEG
     */
    leftLeg.xRot =
            radians(
                    90.0F
            );

    leftLeg.zRot =
            radians(
                    -8.0F
            );

    leftLeg.x =
            -5.0F;

    leftLeg.y =
            22.0F
                    + sink
                    + shakeB;

    leftLeg.z =
            16.0F;


    /*
     * RIGHT LEG
     */
    rightLeg.xRot =
            radians(
                    90.0F
            );

    rightLeg.zRot =
            radians(
                    10.0F
            );

    rightLeg.x =
            6.0F;

    rightLeg.y =
            22.0F
                    + sink
                    + shakeA;

    rightLeg.z =
            17.0F;
}

    private static void applyConjuring(
        float progress,
        float ageInTicks,
        ModelPart head,
        ModelPart body,
        ModelPart leftArm,
        ModelPart rightArm,
        ModelPart leftLeg,
        ModelPart rightLeg
) {
    body.xRot =
            lerp(
                    body.xRot,
                    radians(
                            -3.0F
                    ),
                    progress
            );

    head.xRot =
            lerp(
                    head.xRot,
                    radians(
                            -12.0F
                    ),
                    progress
            );

    /*
     * Raise both arms much higher.
     */
    leftArm.xRot =
            lerp(
                    leftArm.xRot,
                    radians(
                            -155.0F
                    ),
                    progress
            );

    rightArm.xRot =
            lerp(
                    rightArm.xRot,
                    radians(
                            -155.0F
                    ),
                    progress
            );

    /*
     * Spread them outward into a Y shape.
     */
    leftArm.zRot =
            lerp(
                    leftArm.zRot,
                    radians(
                            35.0F
                    ),
                    progress
            );

    rightArm.zRot =
            lerp(
                    rightArm.zRot,
                    radians(
                            -35.0F
                    ),
                    progress
            );

    /*
     * Small outward yaw helps prevent the arms
     * looking like they're stacked directly over
     * the shoulders.
     */
    leftArm.yRot =
            lerp(
                    leftArm.yRot,
                    radians(
                            -8.0F
                    ),
                    progress
            );

    rightArm.yRot =
            lerp(
                    rightArm.yRot,
                    radians(
                            8.0F
                    ),
                    progress
            );
}

        public static void applyPursuerRun(
        float ageInTicks,
        ModelPart body,
        ModelPart leftArm,
        ModelPart rightArm,
        ModelPart leftLeg,
        ModelPart rightLeg
) {
    /*
     * Small forward lean only.
     *
     * Too much body rotation makes the torso look
     * disconnected from the legs.
     */
    body.xRot +=
            radians(
                    4.0F
            );

    /*
     * Faster arm pumping sells the sprint without
     * forcing the entire model into extreme poses.
     */
    float swing =
            (float) Math.sin(
                    ageInTicks
                            * 0.85F
            );

    float armSwing =
            radians(
                    24.0F
            )
                    * swing;

    leftArm.xRot +=
            armSwing;

    rightArm.xRot -=
            armSwing;

    /*
     * Only slightly reinforce the vanilla leg swing.
     *
     * Vanilla is already animating the legs based on
     * actual movement, so we don't want to replace it.
     */
    float legSwing =
            radians(
                    8.0F
            )
                    * swing;

    leftLeg.xRot +=
            legSwing;

    rightLeg.xRot -=
            legSwing;
}

        private static void applyUndyingDowned(
        float progress,
        float ageInTicks,
        ModelPart head,
        ModelPart body,
        ModelPart leftArm,
        ModelPart rightArm,
        ModelPart leftLeg,
        ModelPart rightLeg
) {
    float shakeA =
            (float) Math.sin(
                    ageInTicks * 2.8F
            )
                    * 0.15F;

    float shakeB =
            (float) Math.cos(
                    ageInTicks * 3.4F
            )
                    * 0.12F;


    /*
     * =====================================================
     * TORSO
     * =====================================================
     *
     * Main body stays near the center of the ritual.
     */
    body.xRot =
            lerp(
                    body.xRot,
                    radians(
                            90.0F
                    ),
                    progress
            );

    body.y =
            lerp(
                    body.y,
                    21.0F + shakeA,
                    progress
            );

    body.z =
            lerp(
                    body.z,
                    0.0F,
                    progress
            );


    /*
     * =====================================================
     * HEAD
     * =====================================================
     *
     * Toss it well beyond one shoulder.
     */
    head.xRot =
            lerp(
                    head.xRot,
                    radians(
                            90.0F
                    ),
                    progress
            );

    head.zRot =
            lerp(
                    head.zRot,
                    radians(
                            18.0F
                    ),
                    progress
            );

    head.x =
            lerp(
                    head.x,
                    10.0F + shakeB,
                    progress
            );

    head.y =
            lerp(
                    head.y,
                    21.0F,
                    progress
            );

    head.z =
            lerp(
                    head.z,
                    -10.0F,
                    progress
            );


    /*
     * =====================================================
     * LEFT ARM
     * =====================================================
     */
    leftArm.xRot =
            lerp(
                    leftArm.xRot,
                    radians(
                            90.0F
                    ),
                    progress
            );

    leftArm.zRot =
            lerp(
                    leftArm.zRot,
                    radians(
                            -20.0F
                    ),
                    progress
            );

    leftArm.x =
            lerp(
                    leftArm.x,
                    -14.0F + shakeA,
                    progress
            );

    leftArm.y =
            lerp(
                    leftArm.y,
                    21.5F,
                    progress
            );

    leftArm.z =
            lerp(
                    leftArm.z,
                    -3.0F,
                    progress
            );


    /*
     * =====================================================
     * RIGHT ARM
     * =====================================================
     */
    rightArm.xRot =
            lerp(
                    rightArm.xRot,
                    radians(
                            90.0F
                    ),
                    progress
            );

    rightArm.zRot =
            lerp(
                    rightArm.zRot,
                    radians(
                            25.0F
                    ),
                    progress
            );

    rightArm.x =
            lerp(
                    rightArm.x,
                    15.0F + shakeB,
                    progress
            );

    rightArm.y =
            lerp(
                    rightArm.y,
                    21.5F,
                    progress
            );

    rightArm.z =
            lerp(
                    rightArm.z,
                    4.0F,
                    progress
            );


    /*
     * =====================================================
     * LEFT LEG
     * =====================================================
     *
     * Push the legs much farther away from the torso.
     * This is what will make the corpse read as full-length.
     */
    leftLeg.xRot =
            lerp(
                    leftLeg.xRot,
                    radians(
                            90.0F
                    ),
                    progress
            );

    leftLeg.zRot =
            lerp(
                    leftLeg.zRot,
                    radians(
                            -8.0F
                    ),
                    progress
            );

    leftLeg.x =
            lerp(
                    leftLeg.x,
                    -5.0F,
                    progress
            );

    leftLeg.y =
            lerp(
                    leftLeg.y,
                    22.0F + shakeB,
                    progress
            );

    leftLeg.z =
            lerp(
                    leftLeg.z,
                    16.0F,
                    progress
            );


    /*
     * =====================================================
     * RIGHT LEG
     * =====================================================
     */
    rightLeg.xRot =
            lerp(
                    rightLeg.xRot,
                    radians(
                            90.0F
                    ),
                    progress
            );

    rightLeg.zRot =
            lerp(
                    rightLeg.zRot,
                    radians(
                            10.0F
                    ),
                    progress
            );

    rightLeg.x =
            lerp(
                    rightLeg.x,
                    6.0F,
                    progress
            );

    rightLeg.y =
            lerp(
                    rightLeg.y,
                    22.0F + shakeA,
                    progress
            );

    rightLeg.z =
            lerp(
                    rightLeg.z,
                    17.0F,
                    progress
            );
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

    public static void applyFrenzyShake(
        float anger,
        float ageInTicks,
        ModelPart head,
        ModelPart body,
        ModelPart leftArm,
        ModelPart rightArm
) {
    float clamped =
            Math.max(
                    0.0F,
                    Math.min(
                            anger,
                            1.0F
                    )
            );

    if (clamped <= 0.0F) {
        return;
    }

    /*
     * IMPORTANT:
     *
     * Don't multiply ageInTicks by an anger-dependent
     * speed. Changing that speed changes the phase of
     * the entire oscillator and causes huge visual
     * jumps while anger decays.
     *
     * Keep time progression constant and let anger
     * control the STRENGTH of the shake instead.
     */
    float phase =
            ageInTicks
                    * 2.5F;

    float strength =
            radians(
                    clamped
                            * 1.25F
            );

    float shakeA =
            (float) Math.sin(
                    phase
            )
                    * strength;

    float shakeB =
            (float) Math.cos(
                    phase
                            * 1.37F
            )
                    * strength
                    * 0.65F;


    /*
     * Head carries most of the visible tremble.
     */
    head.zRot +=
            shakeA;

    head.yRot +=
            shakeB;


    /*
     * Much subtler movement through the torso.
     */
    body.zRot +=
            shakeA
                    * 0.20F;


    /*
     * Arms inherit just enough movement to make
     * the entire body feel tense.
     */
    leftArm.zRot +=
            shakeB
                    * 0.25F;

    rightArm.zRot -=
            shakeB
                    * 0.25F;
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