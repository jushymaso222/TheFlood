package com.jushymaso222.theflood.elite.presentation.client;

import com.jushymaso222.theflood.elite.presentation.ElitePose;

import java.util.HashMap;
import java.util.Map;

public final class ElitePoseTransitions {

    /*
     * Per-entity transition state.
     */
    private static final Map<Integer, TransitionData> TRANSITIONS =
            new HashMap<>();


    /*
     * =====================================================
     * TUNING
     * =====================================================
     */

    /*
     * How quickly poses blend in/out.
     *
     * Higher = faster.
     */
    private static final float TRANSITION_SPEED =
            0.12F;


    private ElitePoseTransitions() {
    }


    /*
     * =====================================================
     * PUBLIC API
     * =====================================================
     */

    public static TransitionState update(
            int entityId,
            ElitePose requestedPose,
            float ageInTicks
    ) {
        TransitionData data =
                TRANSITIONS.computeIfAbsent(
                        entityId,
                        ignored ->
                                new TransitionData()
                );

        /*
        * The same entity can pass through multiple model
        * mixins during one render frame.
        *
        * Only advance the transition once for that frame.
        */
        boolean newFrame =
                Float.compare(
                        data.lastAgeInTicks,
                        ageInTicks
                ) != 0;

        if (newFrame) {
            data.lastAgeInTicks =
                    ageInTicks;

            if (
                    requestedPose != ElitePose.DEFAULT
            ) {
                if (
                        data.renderedPose
                                != requestedPose
                ) {
                    data.renderedPose =
                            requestedPose;

                    data.progress =
                            0.0F;
                }

                data.progress =
                        approach(
                                data.progress,
                                1.0F,
                                TRANSITION_SPEED
                        );
            }
            else {
                data.progress =
                        approach(
                                data.progress,
                                0.0F,
                                TRANSITION_SPEED
                        );

                if (
                        data.progress
                                <= 0.001F
                ) {
                    data.progress =
                            0.0F;

                    data.renderedPose =
                            ElitePose.DEFAULT;
                }
            }
        }

        return new TransitionState(
                data.renderedPose,
                data.progress
        );
    }


    public static void remove(
            int entityId
    ) {
        TRANSITIONS.remove(
                entityId
        );
    }


    public static void clear() {
        TRANSITIONS.clear();
    }


    /*
     * =====================================================
     * INTERNAL
     * =====================================================
     */

    private static float approach(
            float current,
            float target,
            float speed
    ) {
        return current
                + (target - current)
                * speed;
    }


    private static final class TransitionData {

        private ElitePose renderedPose =
                ElitePose.DEFAULT;

        private float progress =
                0.0F;

        private float lastAgeInTicks =
                Float.NaN;
    }


    /*
     * What the renderer actually needs each frame.
     */
    public record TransitionState(
            ElitePose pose,
            float progress
    ) {
    }
}