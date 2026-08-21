package com.jushymaso222.theflood.elite.behavior.mutations;

import com.jushymaso222.theflood.elite.EliteStateSync;
import com.jushymaso222.theflood.elite.behavior.EliteMutation;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;

public final class SpikedMutation
        implements EliteMutation {

    private static final String PHASE_KEY =
            "theflood_spiked_phase";

    private static final String PHASE_PROGRESS_KEY =
            "theflood_spiked_phase_progress";

    private static final String REFLECTING_KEY =
            "theflood_spiked_reflecting";

    private static final String PHASE_BLUE =
            "blue";

    private static final String PHASE_ORANGE =
            "orange";

    /*
     * How long a phase lasts if the player does not
     * significantly accelerate the swap.
     *
     * 200 ticks = 10 seconds.
     */
    private static final float PHASE_DURATION_TICKS =
            200.0F;

    /*
     * Safe damage accelerates the phase transition.
     *
     * Example:
     * 10 damage -> removes 40 additional progress.
     */
    private static final float DAMAGE_PROGRESS_MULTIPLIER =
            4.0F;

    /*
     * Percentage of protected attack damage reflected
     * back to the attacker.
     */
    private static final float REFLECT_PERCENT =
            0.35F;

    /*
     * BLUE:
     * melee is reflected
     * ranged is safe
     */
    private static final int BLUE_COLOR =
            0xFF4AA8FF;

    /*
     * ORANGE:
     * ranged is reflected
     * melee is safe
     */
    private static final int ORANGE_COLOR =
            0xFFFF8A32;

    @Override
    public String id() {
        return "spiked";
    }

    @Override
    public String displayName() {
        return "Spiked";
    }

    @Override
    public void tick(
            Mob elite
    ) {
        ensureInitialized(
                elite
        );

        float progress =
                getPhaseProgress(
                        elite
                );

        progress -=
                1.0F;

        if (progress <= 0.0F) {
            swapPhase(
                    elite
            );

            return;
        }

        setPhaseProgress(
                elite,
                progress
        );

        syncState(
                elite
        );
    }

    @Override
    public float modifyIncomingDamage(
            Mob elite,
            DamageSource source,
            float damage
    ) {
        ensureInitialized(
                elite
        );

        /*
         * Avoid reflection loops.
         */
        if (
                elite.getPersistentData()
                        .getBoolean(
                                REFLECTING_KEY
                        )
        ) {
            return damage;
        }

        Entity attacker =
                source.getEntity();

        if (attacker == null) {
            return damage;
        }

        boolean ranged =
                isRangedAttack(
                        source
                );

        boolean bluePhase =
                isBluePhase(
                        elite
                );

        /*
         * BLUE:
         * melee = protected/reflected
         * ranged = safe
         *
         * ORANGE:
         * ranged = protected/reflected
         * melee = safe
         */
        boolean protectedAttack =
                bluePhase
                        ? !ranged
                        : ranged;

        if (protectedAttack) {
            reflectDamage(
                    elite,
                    attacker,
                    damage
            );

            /*
             * The original hit still damages the Elite.
             *
             * Spiked punishes the wrong attack type,
             * but does not make the Elite immune.
             */
            return damage;
        }

        /*
         * Correct attack type accelerates the phase swap.
         */
        reducePhaseProgressFromDamage(
                elite,
                damage
        );

        return damage;
    }

    private static void ensureInitialized(
            Mob elite
    ) {
        if (
                !elite.getPersistentData()
                        .contains(
                                PHASE_KEY
                        )
        ) {
            elite.getPersistentData()
                    .putString(
                            PHASE_KEY,
                            PHASE_BLUE
                    );
        }

        if (
                !elite.getPersistentData()
                        .contains(
                                PHASE_PROGRESS_KEY
                        )
        ) {
            elite.getPersistentData()
                    .putFloat(
                            PHASE_PROGRESS_KEY,
                            PHASE_DURATION_TICKS
                    );

            syncState(
                    elite
            );
        }
    }

    private static boolean isBluePhase(
            Mob elite
    ) {
        return PHASE_BLUE.equals(
                elite.getPersistentData()
                        .getString(
                                PHASE_KEY
                        )
        );
    }

    private static float getPhaseProgress(
            Mob elite
    ) {
        return elite.getPersistentData()
                .getFloat(
                        PHASE_PROGRESS_KEY
                );
    }

    private static void setPhaseProgress(
            Mob elite,
            float progress
    ) {
        elite.getPersistentData()
                .putFloat(
                        PHASE_PROGRESS_KEY,
                        Math.max(
                                0.0F,
                                Math.min(
                                        PHASE_DURATION_TICKS,
                                        progress
                                )
                        )
                );
    }

    private static void reducePhaseProgressFromDamage(
            Mob elite,
            float damage
    ) {
        float progress =
                getPhaseProgress(
                        elite
                );

        progress -=
                damage
                        * DAMAGE_PROGRESS_MULTIPLIER;

        if (progress <= 0.0F) {
            swapPhase(
                    elite
            );

            return;
        }

        setPhaseProgress(
                elite,
                progress
        );

        syncState(
                elite
        );
    }

    private static void swapPhase(
            Mob elite
    ) {
        String newPhase =
                isBluePhase(
                        elite
                )
                        ? PHASE_ORANGE
                        : PHASE_BLUE;

        elite.getPersistentData()
                .putString(
                        PHASE_KEY,
                        newPhase
                );

        elite.getPersistentData()
                .putFloat(
                        PHASE_PROGRESS_KEY,
                        PHASE_DURATION_TICKS
                );

        syncState(
                elite
        );
    }

    private static boolean isRangedAttack(
            DamageSource source
    ) {
        Entity directEntity =
                source.getDirectEntity();

        /*
         * Vanilla arrows, tridents and many modded
         * projectile weapons.
         */
        if (directEntity instanceof Projectile) {
            return true;
        }

        /*
         * Everything else is temporarily considered
         * melee/non-ranged.
         *
         * We can add TACZ-specific detection here if
         * testing shows its bullets do not appear as
         * Projectile entities.
         */
        return false;
    }

    private static void reflectDamage(
            Mob elite,
            Entity attacker,
            float incomingDamage
    ) {
        /*
         * Only living player attackers should receive
         * the reflection for now.
         */
        if (!(attacker instanceof ServerPlayer player)) {
            return;
        }

        float reflectedDamage =
                incomingDamage
                        * REFLECT_PERCENT;

        if (reflectedDamage <= 0.0F) {
            return;
        }

        elite.getPersistentData()
                .putBoolean(
                        REFLECTING_KEY,
                        true
                );

        try {
            player.hurt(
                    elite.damageSources()
                            .thorns(
                                    elite
                            ),
                    reflectedDamage
            );
        } finally {
            elite.getPersistentData()
                    .putBoolean(
                            REFLECTING_KEY,
                            false
                    );
        }
    }

    @Override
        public void onDeactivated(
                Mob elite
        ) {
        elite.getPersistentData()
                .remove(
                        PHASE_KEY
                );

        elite.getPersistentData()
                .remove(
                        PHASE_PROGRESS_KEY
                );

        elite.getPersistentData()
                .remove(
                        REFLECTING_KEY
                );
        }

    private static void syncState(
            Mob elite
    ) {
        boolean bluePhase =
                isBluePhase(
                        elite
                );

        EliteStateSync.sync(
                elite,
                getPhaseProgress(
                        elite
                ),
                PHASE_DURATION_TICKS,
                true,
                bluePhase
                        ? BLUE_COLOR
                        : ORANGE_COLOR
        );
    }
}