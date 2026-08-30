package com.jushymaso222.theflood.progression.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public final class CapabilityData {

    private static final String ROOT_KEY =
            "theflood_capability";

    private static final String OFFENSE =
            "offense";

    private static final String DEFENSE =
            "defense";

    private static final String SURVIVAL =
            "survival";

    private static final String OFFENSE_CONFIDENCE =
            "offense_confidence";

    private static final String DEFENSE_CONFIDENCE =
            "defense_confidence";

    private static final String SURVIVAL_CONFIDENCE =
            "survival_confidence";

    private static final String PEAK_OFFENSE =
            "peak_offense";

    private static final String PEAK_DEFENSE =
            "peak_defense";

    private static final String PEAK_SURVIVAL =
            "peak_survival";

    private static final String OFFENSE_SAMPLES =
            "offense_samples";

    private static final String LAST_OFFENSIVE_DAMAGE =
            "last_offensive_damage";

    private static final String LAST_OFFENSE_OBSERVATION =
            "last_offense_observation";

    private static final String AVERAGE_OFFENSIVE_DAMAGE =
            "average_offensive_damage";

    private static final String PEAK_OBSERVED_OFFENSIVE_DAMAGE =
            "peak_observed_offensive_damage";


    private CapabilityData() {
    }


    /*
     * ============================================
     * LOAD
     * ============================================
     */

    public static CapabilityProfile get(
            ServerPlayer player
    ) {
        CapabilityProfile profile =
                new CapabilityProfile();

        CompoundTag persistent =
                player.getPersistentData();

        if (!persistent.contains(ROOT_KEY)) {
            return profile;
        }

        CompoundTag tag =
                persistent.getCompound(
                        ROOT_KEY
                );


        profile.setOffenseScore(
                tag.getDouble(OFFENSE)
        );

        profile.setDefenseScore(
                tag.getDouble(DEFENSE)
        );

        profile.setSurvivalScore(
                tag.getDouble(SURVIVAL)
        );


        profile.setOffenseConfidence(
                tag.getDouble(
                        OFFENSE_CONFIDENCE
                )
        );

        profile.setDefenseConfidence(
                tag.getDouble(
                        DEFENSE_CONFIDENCE
                )
        );

        profile.setSurvivalConfidence(
                tag.getDouble(
                        SURVIVAL_CONFIDENCE
                )
        );


        /*
         * Peaks need to be restored separately.
         *
         * We'll add direct restoration methods to
         * CapabilityProfile below.
         */
        profile.restorePeaks(
                tag.getDouble(PEAK_OFFENSE),
                tag.getDouble(PEAK_DEFENSE),
                tag.getDouble(PEAK_SURVIVAL)
        );

        profile.restoreOffenseDiagnostics(
                tag.getLong(
                        OFFENSE_SAMPLES
                ),
                tag.getDouble(
                        LAST_OFFENSIVE_DAMAGE
                ),
                tag.getDouble(
                        LAST_OFFENSE_OBSERVATION
                ),
                tag.getDouble(
                        AVERAGE_OFFENSIVE_DAMAGE
                ),
                tag.getDouble(
                        PEAK_OBSERVED_OFFENSIVE_DAMAGE
                )
        );

        return profile;
    }


    /*
     * ============================================
     * SAVE
     * ============================================
     */

    public static void save(
            ServerPlayer player,
            CapabilityProfile profile
    ) {
        if (
                player == null
                || profile == null
        ) {
            return;
        }

        CompoundTag tag =
                new CompoundTag();


        tag.putDouble(
                OFFENSE,
                profile.getOffenseScore()
        );

        tag.putDouble(
                DEFENSE,
                profile.getDefenseScore()
        );

        tag.putDouble(
                SURVIVAL,
                profile.getSurvivalScore()
        );


        tag.putDouble(
                OFFENSE_CONFIDENCE,
                profile.getOffenseConfidence()
        );

        tag.putDouble(
                DEFENSE_CONFIDENCE,
                profile.getDefenseConfidence()
        );

        tag.putDouble(
                SURVIVAL_CONFIDENCE,
                profile.getSurvivalConfidence()
        );


        tag.putDouble(
                PEAK_OFFENSE,
                profile.getPeakOffense()
        );

        tag.putDouble(
                PEAK_DEFENSE,
                profile.getPeakDefense()
        );

        tag.putDouble(
                PEAK_SURVIVAL,
                profile.getPeakSurvival()
        );

        tag.putLong(
                OFFENSE_SAMPLES,
                profile.getOffenseSamples()
        );

        tag.putDouble(
                LAST_OFFENSIVE_DAMAGE,
                profile.getLastOffensiveDamage()
        );

        tag.putDouble(
                LAST_OFFENSE_OBSERVATION,
                profile.getLastOffenseObservation()
        );

        tag.putDouble(
                AVERAGE_OFFENSIVE_DAMAGE,
                profile.getAverageOffensiveDamage()
        );

        tag.putDouble(
                PEAK_OBSERVED_OFFENSIVE_DAMAGE,
                profile.getPeakObservedOffensiveDamage()
        );


        player.getPersistentData().put(
                ROOT_KEY,
                tag
        );
    }


    /*
     * ============================================
     * COPY
     * ============================================
     *
     * Used when Forge creates a new Player entity,
     * such as after death.
     */

    public static void copy(
            ServerPlayer oldPlayer,
            ServerPlayer newPlayer
    ) {
        if (
                oldPlayer == null
                || newPlayer == null
        ) {
            return;
        }

        CompoundTag oldPersistent =
                oldPlayer.getPersistentData();

        if (!oldPersistent.contains(ROOT_KEY)) {
            return;
        }

        CompoundTag capability =
                oldPersistent
                        .getCompound(ROOT_KEY)
                        .copy();

        newPlayer
                .getPersistentData()
                .put(
                        ROOT_KEY,
                        capability
                );
    }


    /*
     * ============================================
     * RESET
     * ============================================
     *
     * Mostly useful for development/testing.
     */

    public static void reset(
            ServerPlayer player
    ) {
        if (player == null) {
            return;
        }

        player
                .getPersistentData()
                .remove(
                        ROOT_KEY
                );
    }
}