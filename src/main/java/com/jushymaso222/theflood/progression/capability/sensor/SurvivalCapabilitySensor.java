package com.jushymaso222.theflood.progression.capability.sensor;

import com.jushymaso222.theflood.progression.capability.CapabilityManager;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public final class SurvivalCapabilitySensor {

    /*
     * =====================================================
     * CONFIGURATION
     * =====================================================
     */

    /*
     * If the player goes this long without taking another
     * meaningful hit, we consider the pressure encounter
     * survived.
     *
     * 10 seconds.
     */
    private static final int ENCOUNTER_TIMEOUT_TICKS =
            200;


    /*
     * Damage below this fraction of max health does not
     * begin a Survival encounter by itself.
     *
     * This stops tiny environmental scratches from
     * generating endless Survival samples.
     *
     * 5% max health.
     */
    private static final double MIN_RELATIVE_DAMAGE =
            0.05D;


    /*
     * Runtime encounter state.
     *
     * This does NOT need persistence. The learned result
     * is persisted by CapabilityProfile/CapabilityData.
     */
    private static final Map<UUID, SurvivalEncounter> ENCOUNTERS =
            new HashMap<>();


    private SurvivalCapabilitySensor() {
    }


    /*
     * =====================================================
     * DAMAGE OBSERVATION
     * =====================================================
     *
     * Called when actual post-mitigation damage reaches
     * the player.
     *
     * Damage itself is NOT a positive Survival sample.
     *
     * It merely tells us:
     *
     * "The player is currently under pressure."
     */

    public static void observeDamage(
            ServerPlayer player,
            double finalDamage
    ) {
        if (
                player == null
                || !Double.isFinite(finalDamage)
                || finalDamage <= 0.0D
        ) {
            return;
        }


        double maxHealth =
                player.getMaxHealth();

        if (
                !Double.isFinite(maxHealth)
                || maxHealth <= 0.0D
        ) {
            return;
        }


        double healthBefore =
                player.getHealth();

        double healthAfter =
                Math.max(
                        0.0D,
                        healthBefore - finalDamage
                );

        double relativeDamage =
                finalDamage / maxHealth;


        /*
         * If there is no active encounter and this was
         * only trivial damage, ignore it.
         */

        SurvivalEncounter existing =
                ENCOUNTERS.get(
                        player.getUUID()
                );

        if (
                existing == null
                && relativeDamage < MIN_RELATIVE_DAMAGE
        ) {
            return;
        }


        SurvivalEncounter encounter =
                ENCOUNTERS.computeIfAbsent(
                        player.getUUID(),
                        ignored ->
                                new SurvivalEncounter(
                                        player.tickCount,
                                        healthBefore / maxHealth
                                )
                );


        encounter.lastDamageTick =
                player.tickCount;

        encounter.totalDamageTaken +=
                finalDamage;

        encounter.hitCount++;


        double healthRatioAfter =
                clamp01(
                        healthAfter / maxHealth
                );

        encounter.lowestHealthRatio =
                Math.min(
                        encounter.lowestHealthRatio,
                        healthRatioAfter
                );


        /*
         * Keep track of the single most dangerous hit.
         */

        encounter.largestRelativeHit =
                Math.max(
                        encounter.largestRelativeHit,
                        relativeDamage
                );


        /*
         * A hit that mathematically reduces the player
         * to zero health is already a failed Survival
         * outcome.
         *
         * We don't wait for the death event to understand
         * that being one-shot is not impressive survival.
         *
         * The death hook will clean the encounter up.
         */

        if (healthAfter <= 0.0D) {

            recordDeathObservation(
                    player,
                    encounter
            );

            encounter.failureRecorded =
                    true;
        }
    }


    /*
     * =====================================================
     * TICK
     * =====================================================
     *
     * Determines when the player has successfully survived
     * an encounter.
     */

    public static void tick(
            ServerPlayer player
    ) {
        if (player == null) {
            return;
        }


        SurvivalEncounter encounter =
                ENCOUNTERS.get(
                        player.getUUID()
                );

        if (encounter == null) {
            return;
        }


        /*
         * If lethal damage was already observed, don't
         * accidentally convert it into a successful
         * encounter while waiting for the death event.
         */

        if (encounter.failureRecorded) {
            return;
        }


        int ticksSinceDamage =
                player.tickCount
                        - encounter.lastDamageTick;

        if (
                ticksSinceDamage
                        < ENCOUNTER_TIMEOUT_TICKS
        ) {
            return;
        }


        /*
         * Player went 10 seconds without taking more
         * meaningful damage.
         *
         * They survived the encounter.
         */

        recordSurvivalObservation(
                player,
                encounter
        );


        ENCOUNTERS.remove(
                player.getUUID()
        );
    }


    /*
     * =====================================================
     * DEATH
     * =====================================================
     */

    public static void observeDeath(
            ServerPlayer player
    ) {
        if (player == null) {
            return;
        }


        SurvivalEncounter encounter =
                ENCOUNTERS.remove(
                        player.getUUID()
                );


        /*
         * Even if no tracked encounter exists, an actual
         * death is still useful Survival evidence.
         *
         * This also catches weird/modded damage paths that
         * bypass our normal damage observation.
         */

        if (encounter == null) {

            CapabilityManager.recordSurvivalObservation(
                    player,
                    0.0D
            );

            return;
        }


        /*
         * Lethal damage may already have recorded the
         * failure before the actual death event fired.
         *
         * Don't count the same death twice.
         */

        if (encounter.failureRecorded) {
            return;
        }


        recordDeathObservation(
                player,
                encounter
        );
    }


    /*
     * =====================================================
     * SUCCESSFUL ENCOUNTER
     * =====================================================
     */

    private static void recordSurvivalObservation(
            ServerPlayer player,
            SurvivalEncounter encounter
    ) {
        double maxHealth =
                player.getMaxHealth();

        if (maxHealth <= 0.0D) {
            return;
        }


        double endingHealthRatio =
                clamp01(
                        player.getHealth()
                                / maxHealth
                );


        /*
         * How dangerous did the encounter become?
         *
         * Reaching lower health means surviving the
         * encounter was more meaningful.
         */

        double danger =
                1.0D
                        - encounter.lowestHealthRatio;


        /*
         * How much did the player recover by the time the
         * encounter ended?
         */

        double recovery =
                Math.max(
                        0.0D,
                        endingHealthRatio
                                - encounter.lowestHealthRatio
                );


        /*
         * Successful survival baseline.
         *
         * Merely surviving a mild encounter shouldn't
         * imply elite survivability.
         *
         * Surviving genuinely dangerous situations,
         * especially after reaching low health, should.
         */

        double observation =
                20.0D
                        + danger * 60.0D
                        + recovery * 20.0D;


        /*
         * Clamp because modded health/damage behavior can
         * produce unusual values.
         */

        observation =
                clampScore(
                        observation
                );


        CapabilityManager.recordSurvivalObservation(
                player,
                observation
        );
    }


    /*
     * =====================================================
     * FAILED ENCOUNTER
     * =====================================================
     */

    private static void recordDeathObservation(
            ServerPlayer player,
            SurvivalEncounter encounter
    ) {
        /*
         * Death is failure.
         *
         * A one-shot is especially strong evidence because
         * the player demonstrated essentially no ability
         * to survive the delivered threat.
         *
         * For now all deaths produce a zero observation.
         *
         * Repeated deaths therefore continuously pull the
         * learned current Survival score downward.
         */

        CapabilityManager.recordSurvivalObservation(
                player,
                0.0D
        );
    }


    /*
     * =====================================================
     * CLEANUP
     * =====================================================
     */

    public static void remove(
            ServerPlayer player
    ) {
        if (player == null) {
            return;
        }

        ENCOUNTERS.remove(
                player.getUUID()
        );
    }


    /*
     * =====================================================
     * UTILITIES
     * =====================================================
     */

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


    /*
     * =====================================================
     * RUNTIME ENCOUNTER STATE
     * =====================================================
     */

    private static final class SurvivalEncounter {

        private final int startTick;

        private int lastDamageTick;

        private double lowestHealthRatio;

        private double totalDamageTaken;

        private double largestRelativeHit;

        private int hitCount;

        private boolean failureRecorded;


        private SurvivalEncounter(
                int startTick,
                double startingHealthRatio
        ) {
            this.startTick =
                    startTick;

            this.lastDamageTick =
                    startTick;

            this.lowestHealthRatio =
                    clamp01(
                            startingHealthRatio
                    );
        }
    }
}