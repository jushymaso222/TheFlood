package com.jushymaso222.theflood.progression.capability.client;


public final class ClientCapabilityStatsData {

    private static CapabilityStats current;


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


    public static void clear() {
        current =
                null;
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

            long defenseSamples,
            double lastDefenseObservation,


            /*
             * SURVIVAL
             */
            double survivalScore,
            double effectiveSurvival,
            double peakSurvival,
            double survivalConfidence,

            long survivalSamples,
            double lastSurvivalObservation,


            /*
             * MOBILITY
             */
            double mobilityScore,
            double effectiveMobility,
            double peakMobility,
            double mobilityConfidence,

            long mobilitySamples,
            double lastMobilityObservation,


            /*
             * KNOWN PROGRESSION
             */
            int milestoneProgression
    ) {
    }
}