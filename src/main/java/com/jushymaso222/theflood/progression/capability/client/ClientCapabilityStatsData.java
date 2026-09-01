package com.jushymaso222.theflood.progression.capability.client;


public final class ClientCapabilityStatsData {

    private static CapabilityStats current;
    private static boolean inspectorOpen =
        false;


    private ClientCapabilityStatsData() {
    }


    public static void set(
            CapabilityStats stats
    ) {
        current =
                stats;
    }


    public static CapabilityStats get() {
        return current;
    }

    public static boolean isInspectorOpen() {
        return inspectorOpen;
    }


    public static void setInspectorOpen(
            boolean open
    ) {
        inspectorOpen =
                open;
    }


    public static void clear() {
        current =
                null;

        inspectorOpen =
                false;
    }


    public record CapabilityStats(

            String playerName,

            /*
            * OFFENSE
            */
            double offenseScore,
            double effectiveOffense,
            double peakOffense,
            double offenseConfidence,
            double offenseResponse,

            long offenseSamples,

            double lastOffensiveDamage,
            double lastOffenseObservation,
            double averageOffensiveDamage,
            double peakObservedOffensiveDamage,


            /*
            * DEFENSE
            */
            double defenseScore,
            double effectiveDefense,
            double peakDefense,
            double defenseConfidence,
            double defenseResponse,

            long defenseSamples,
            double lastDefenseObservation,


            /*
            * SURVIVAL
            */
            double survivalScore,
            double effectiveSurvival,
            double peakSurvival,
            double survivalConfidence,
            double survivalResponse,

            long survivalSamples,
            double lastSurvivalObservation,


            /*
            * MOBILITY
            */
            double mobilityScore,
            double effectiveMobility,
            double peakMobility,
            double mobilityConfidence,
            double mobilityResponse,

            long mobilitySamples,
            double lastMobilityObservation,


            /*
            * KNOWN PROGRESSION
            */
            int milestoneProgression
    ) {
    }
}