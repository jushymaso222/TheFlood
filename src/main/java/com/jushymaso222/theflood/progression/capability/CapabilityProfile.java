package com.jushymaso222.theflood.progression.capability;

public final class CapabilityProfile {

    /*
     * ============================================
     * OBSERVED SCORES
     * ============================================
     *
     * These represent what The Flood has actually
     * observed the player doing.
     *
     * 0   = essentially no demonstrated capability
     * 100 = extreme demonstrated capability
     */

    private double offenseScore;
    private double defenseScore;
    private double survivalScore;

    /*
    * ============================================
    * OFFENSE DIAGNOSTICS
    * ============================================
    */

    private long offenseSamples;

    private double lastOffensiveDamage;
    private double lastOffenseObservation;

    private double averageOffensiveDamage;
    private double peakObservedOffensiveDamage;


    /*
     * ============================================
     * CONFIDENCE
     * ============================================
     *
     * A score based on one observation should not
     * be trusted as much as a score based on 100.
     *
     * Confidence is stored independently for each
     * dimension.
     */

    private double offenseConfidence;
    private double defenseConfidence;
    private double survivalConfidence;


    /*
     * ============================================
     * PEAK MEMORY
     * ============================================
     *
     * The Flood remembers what it has seen.
     *
     * Taking off powerful armor should not
     * instantly convince it that the player is
     * harmless.
     */

    private double peakOffense;
    private double peakDefense;
    private double peakSurvival;


    public CapabilityProfile() {
    }

    public long getOffenseSamples() {
        return offenseSamples;
    }


    public double getLastOffensiveDamage() {
        return lastOffensiveDamage;
    }


    public double getLastOffenseObservation() {
        return lastOffenseObservation;
    }


    public double getAverageOffensiveDamage() {
        return averageOffensiveDamage;
    }


    public double getPeakObservedOffensiveDamage() {
        return peakObservedOffensiveDamage;
    }


    /*
     * ============================================
     * SCORES
     * ============================================
     */

    public double getOffenseScore() {
        return offenseScore;
    }

    public void setOffenseScore(
            double offenseScore
    ) {
        this.offenseScore =
                clampScore(
                        offenseScore
                );

        peakOffense =
                Math.max(
                        peakOffense,
                        this.offenseScore
                );
    }


    public double getDefenseScore() {
        return defenseScore;
    }

    public void setDefenseScore(
            double defenseScore
    ) {
        this.defenseScore =
                clampScore(
                        defenseScore
                );

        peakDefense =
                Math.max(
                        peakDefense,
                        this.defenseScore
                );
    }


    public double getSurvivalScore() {
        return survivalScore;
    }

    public void setSurvivalScore(
            double survivalScore
    ) {
        this.survivalScore =
                clampScore(
                        survivalScore
                );

        peakSurvival =
                Math.max(
                        peakSurvival,
                        this.survivalScore
                );
    }

    public void recordOffenseObservation(
            double rawDamage,
            double observation
    ) {
        if (
                !Double.isFinite(rawDamage)
                || !Double.isFinite(observation)
                || rawDamage <= 0.0D
        ) {
            return;
        }


        /*
        * Update running average without needing to
        * retain every individual damage sample.
        */
        double totalDamage =
                averageOffensiveDamage
                        * offenseSamples;

        offenseSamples++;

        averageOffensiveDamage =
                (totalDamage + rawDamage)
                        / offenseSamples;


        /*
        * Most recent observation.
        */
        lastOffensiveDamage =
                rawDamage;

        lastOffenseObservation =
                clampScore(
                        observation
                );


        /*
        * Historical raw damage peak.
        */
        peakObservedOffensiveDamage =
                Math.max(
                        peakObservedOffensiveDamage,
                        rawDamage
                );
    }

    public void restoreOffenseDiagnostics(
            long samples,
            double lastDamage,
            double lastObservation,
            double averageDamage,
            double peakDamage
    ) {
        offenseSamples =
                Math.max(
                        0L,
                        samples
                );

        lastOffensiveDamage =
                sanitizeNonNegative(
                        lastDamage
                );

        lastOffenseObservation =
                clampScore(
                        lastObservation
                );

        averageOffensiveDamage =
                sanitizeNonNegative(
                        averageDamage
                );

        peakObservedOffensiveDamage =
                Math.max(
                        sanitizeNonNegative(
                                peakDamage
                        ),
                        Math.max(
                                lastOffensiveDamage,
                                averageOffensiveDamage
                        )
                );
    }


    /*
     * ============================================
     * CONFIDENCE
     * ============================================
     */

    public double getOffenseConfidence() {
        return offenseConfidence;
    }

    public void setOffenseConfidence(
            double confidence
    ) {
        offenseConfidence =
                clamp01(
                        confidence
                );
    }


    public double getDefenseConfidence() {
        return defenseConfidence;
    }

    public void setDefenseConfidence(
            double confidence
    ) {
        defenseConfidence =
                clamp01(
                        confidence
                );
    }


    public double getSurvivalConfidence() {
        return survivalConfidence;
    }

    public void setSurvivalConfidence(
            double confidence
    ) {
        survivalConfidence =
                clamp01(
                        confidence
                );
    }


    /*
     * ============================================
     * PEAKS
     * ============================================
     */

    public double getPeakOffense() {
        return peakOffense;
    }

    public double getPeakDefense() {
        return peakDefense;
    }

    public double getPeakSurvival() {
        return peakSurvival;
    }

    public void restorePeaks(
            double offense,
            double defense,
            double survival
    ) {
        peakOffense =
                clampScore(
                        offense
                );

        peakDefense =
                clampScore(
                        defense
                );

        peakSurvival =
                clampScore(
                        survival
                );
    }


    /*
     * ============================================
     * EFFECTIVE SCORES
     * ============================================
     *
     * Current observations matter most, but The
     * Flood retains some memory of demonstrated
     * capability.
     *
     * For now:
     *
     * effective >= 65% of historical peak.
     *
     * We can tune this once we're gathering real
     * combat data.
     */

    public double getEffectiveOffense() {
        return Math.max(
                offenseScore,
                peakOffense * 0.65D
        );
    }

    public double getEffectiveDefense() {
        return Math.max(
                defenseScore,
                peakDefense * 0.65D
        );
    }

    public double getEffectiveSurvival() {
        return Math.max(
                survivalScore,
                peakSurvival * 0.65D
        );
    }



    /*
     * ============================================
     * UTILITIES
     * ============================================
     */

    private static double clampScore(
            double value
    ) {
        return Math.max(
                0.0D,
                Math.min(
                        100.0D,
                        value
                )
        );
    }

    private static double sanitizeNonNegative(
            double value
    ) {
        if (
                !Double.isFinite(value)
                || value < 0.0D
        ) {
            return 0.0D;
        }

        return value;
    }

    private static double clamp01(
            double value
    ) {
        return Math.max(
                0.0D,
                Math.min(
                        1.0D,
                        value
                )
        );
    }
}