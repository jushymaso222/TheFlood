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
    private double mobilityScore;


    /*
     * ============================================
     * OBSERVATION DIAGNOSTICS
     * ============================================
     *
     * Every capability dimension tracks:
     *
     * - number of useful observations
     * - most recent 0-100 observation
     *
     * Offense additionally retains raw damage
     * diagnostics because that sensor already exists.
     */

    private long offenseSamples;
    private long defenseSamples;
    private long survivalSamples;
    private long mobilitySamples;

    private double lastOffenseObservation;
    private double lastDefenseObservation;
    private double lastSurvivalObservation;
    private double lastMobilityObservation;


    /*
     * ============================================
     * OFFENSE-SPECIFIC DIAGNOSTICS
     * ============================================
     */

    private double lastOffensiveDamage;
    private double averageOffensiveDamage;
    private double peakObservedOffensiveDamage;


    /*
     * ============================================
     * CONFIDENCE
     * ============================================
     *
     * A score based on one observation should not
     * be trusted as much as a score based on many.
     *
     * Confidence is independent for each dimension.
     */

    private double offenseConfidence;
    private double defenseConfidence;
    private double survivalConfidence;
    private double mobilityConfidence;


    /*
     * ============================================
     * PEAK MEMORY
     * ============================================
     *
     * The Flood remembers demonstrated capability.
     *
     * Temporarily becoming weaker should not
     * immediately convince it that the player is
     * harmless.
     */

    private double peakOffense;
    private double peakDefense;
    private double peakSurvival;
    private double peakMobility;


    public CapabilityProfile() {
    }


    /*
     * ============================================
     * SAMPLE COUNTS
     * ============================================
     */

    public long getOffenseSamples() {
        return offenseSamples;
    }

    public long getDefenseSamples() {
        return defenseSamples;
    }

    public long getSurvivalSamples() {
        return survivalSamples;
    }

    public long getMobilitySamples() {
        return mobilitySamples;
    }


    /*
     * ============================================
     * LAST OBSERVATIONS
     * ============================================
     */

    public double getLastOffenseObservation() {
        return lastOffenseObservation;
    }

    public double getLastDefenseObservation() {
        return lastDefenseObservation;
    }

    public double getLastSurvivalObservation() {
        return lastSurvivalObservation;
    }

    public double getLastMobilityObservation() {
        return lastMobilityObservation;
    }


    /*
     * ============================================
     * OFFENSE DIAGNOSTICS
     * ============================================
     */

    public double getLastOffensiveDamage() {
        return lastOffensiveDamage;
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


    public double getMobilityScore() {
        return mobilityScore;
    }

    public void setMobilityScore(
            double mobilityScore
    ) {
        this.mobilityScore =
                clampScore(
                        mobilityScore
                );

        peakMobility =
                Math.max(
                        peakMobility,
                        this.mobilityScore
                );
    }


    /*
     * ============================================
     * RECORD OBSERVATIONS
     * ============================================
     */

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

        double totalDamage =
                averageOffensiveDamage
                        * offenseSamples;

        offenseSamples++;

        averageOffensiveDamage =
                (totalDamage + rawDamage)
                        / offenseSamples;

        lastOffensiveDamage =
                rawDamage;

        lastOffenseObservation =
                clampScore(
                        observation
                );

        peakObservedOffensiveDamage =
                Math.max(
                        peakObservedOffensiveDamage,
                        rawDamage
                );
    }


    public void recordDefenseObservation(
            double observation
    ) {
        if (!Double.isFinite(observation)) {
            return;
        }

        defenseSamples++;

        lastDefenseObservation =
                clampScore(
                        observation
                );
    }


    public void recordSurvivalObservation(
            double observation
    ) {
        if (!Double.isFinite(observation)) {
            return;
        }

        survivalSamples++;

        lastSurvivalObservation =
                clampScore(
                        observation
                );
    }


    public void recordMobilityObservation(
            double observation
    ) {
        if (!Double.isFinite(observation)) {
            return;
        }

        mobilitySamples++;

        lastMobilityObservation =
                clampScore(
                        observation
                );
    }


    /*
     * ============================================
     * RESTORE DIAGNOSTICS
     * ============================================
     */

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


    public void restoreDefenseDiagnostics(
            long samples,
            double lastObservation
    ) {
        defenseSamples =
                Math.max(
                        0L,
                        samples
                );

        lastDefenseObservation =
                clampScore(
                        lastObservation
                );
    }


    public void restoreSurvivalDiagnostics(
            long samples,
            double lastObservation
    ) {
        survivalSamples =
                Math.max(
                        0L,
                        samples
                );

        lastSurvivalObservation =
                clampScore(
                        lastObservation
                );
    }


    public void restoreMobilityDiagnostics(
            long samples,
            double lastObservation
    ) {
        mobilitySamples =
                Math.max(
                        0L,
                        samples
                );

        lastMobilityObservation =
                clampScore(
                        lastObservation
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


    public double getMobilityConfidence() {
        return mobilityConfidence;
    }

    public void setMobilityConfidence(
            double confidence
    ) {
        mobilityConfidence =
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

    public double getPeakMobility() {
        return peakMobility;
    }


    public void restorePeaks(
            double offense,
            double defense,
            double survival,
            double mobility
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

        peakMobility =
                clampScore(
                        mobility
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
     * effective >= 65% of historical peak.
     */

    public double getEffectiveOffense() {
        return getEffectiveScore(
                offenseScore,
                peakOffense
        );
    }

    public double getEffectiveDefense() {
        return defenseScore;
        }

    public double getEffectiveSurvival() {
        return survivalScore;
        }

    public double getEffectiveMobility() {
        return getEffectiveScore(
                mobilityScore,
                peakMobility
        );
    }


    /*
     * ============================================
     * UTILITIES
     * ============================================
     */

    private static double getEffectiveScore(
            double current,
            double peak
    ) {
        return Math.max(
                current,
                peak * 0.65D
        );
    }


    private static double clampScore(
            double value
    ) {
        if (!Double.isFinite(value)) {
            return 0.0D;
        }

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
        if (!Double.isFinite(value)) {
            return 0.0D;
        }

        return Math.max(
                0.0D,
                Math.min(
                        1.0D,
                        value
                )
        );
    }
}