package com.jushymaso222.theflood.progression.client;

import net.minecraft.Util;

public final class ClientFloodXpGain {

    private static final long DISPLAY_DURATION_MS =
            3000L;

    private static final long FADE_DURATION_MS =
            750L;

    private static long accumulatedXp =
            0L;

    private static long lastGainTime =
            0L;

    private ClientFloodXpGain() {
    }

    public static void addXp(
            long amount
    ) {
        if (amount <= 0L) {
            return;
        }

        accumulatedXp +=
                amount;

        /*
         * Every new gain restarts the 3-second window.
         */
        lastGainTime =
                Util.getMillis();
    }

    public static long getAccumulatedXp() {
        if (!isVisible()) {
            return 0L;
        }

        return accumulatedXp;
    }

    public static boolean isVisible() {
        if (accumulatedXp <= 0L) {
            return false;
        }

        long elapsed =
                Util.getMillis()
                        - lastGainTime;

        if (elapsed >= DISPLAY_DURATION_MS) {
            accumulatedXp =
                    0L;

            return false;
        }

        return true;
    }

    public static float getAlpha() {
        if (!isVisible()) {
            return 0.0F;
        }

        long elapsed =
                Util.getMillis()
                        - lastGainTime;

        long fadeStart =
                DISPLAY_DURATION_MS
                        - FADE_DURATION_MS;

        if (elapsed <= fadeStart) {
            return 1.0F;
        }

        float fadeProgress =
                (
                        elapsed - fadeStart
                )
                        / (float) FADE_DURATION_MS;

        return Math.max(
                0.0F,
                1.0F - fadeProgress
        );
    }

    public static void clear() {
        accumulatedXp =
                0L;

        lastGainTime =
                0L;
    }
}