package com.jushymaso222.theflood.progression;

public final class HeatTier {

    private HeatTier() {
    }

    /*
     * =================================================
     * TIER NAMES
     * =================================================
     */

    public static String getName(
            int heat
    ) {
        if (heat >= 80) {
            return "APOCALYPTIC";
        }

        if (heat >= 60) {
            return "CHAOTIC";
        }

        if (heat >= 40) {
            return "HELLISH";
        }

        if (heat >= 20) {
            return "DANGEROUS";
        }

        if (heat >= 1) {
            return "CALM";
        }

        return "DORMANT";
    }


    /*
     * =================================================
     * TIER COLORS
     * =================================================
     *
     * These match the existing neon Heat HUD colors.
     *
     * Dormant intentionally shares Calm's green.
     */

    public static int getColor(
            int heat
    ) {
        if (heat >= 80) {
            return 0xFFD94CFF;
        }

        if (heat >= 60) {
            return 0xFFFF4040;
        }

        if (heat >= 40) {
            return 0xFFFF9A24;
        }

        if (heat >= 20) {
            return 0xFF28D7FF;
        }

        return 0xFF35FF8A;
    }


    /*
     * =================================================
     * TIER BOUNDARIES
     * =================================================
     */

    public static int getMinimumHeat(
            int heat
    ) {
        if (heat >= 80) {
            return 80;
        }

        if (heat >= 60) {
            return 60;
        }

        if (heat >= 40) {
            return 40;
        }

        if (heat >= 20) {
            return 20;
        }

        if (heat >= 1) {
            return 1;
        }

        return 0;
    }


    public static int getMaximumHeat(
            int heat
    ) {
        if (heat >= 80) {
            return 100;
        }

        if (heat >= 60) {
            return 79;
        }

        if (heat >= 40) {
            return 59;
        }

        if (heat >= 20) {
            return 39;
        }

        if (heat >= 1) {
            return 19;
        }

        return 0;
    }


    /*
     * =================================================
     * NEXT TIER
     * =================================================
     */

    public static int getNextTierHeat(
            int heat
    ) {
        if (heat < 1) {
            return 1;
        }

        if (heat < 20) {
            return 20;
        }

        if (heat < 40) {
            return 40;
        }

        if (heat < 60) {
            return 60;
        }

        if (heat < 80) {
            return 80;
        }

        return 100;
    }


    public static String getNextTierName(
            int heat
    ) {
        if (heat < 1) {
            return "CALM";
        }

        if (heat < 20) {
            return "DANGEROUS";
        }

        if (heat < 40) {
            return "HELLISH";
        }

        if (heat < 60) {
            return "CHAOTIC";
        }

        if (heat < 80) {
            return "APOCALYPTIC";
        }

        return "APOCALYPTIC";
    }
}