package com.jushymaso222.theflood.elite.behavior.mutations;

import com.jushymaso222.theflood.elite.behavior.EliteMutation;
import com.jushymaso222.theflood.elite.EliteStateSync;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Mob;

import com.jushymaso222.theflood.elite.presentation.ElitePose;
import com.jushymaso222.theflood.elite.presentation.ElitePresentation;
import com.jushymaso222.theflood.elite.presentation.EliteVisuals;

import com.jushymaso222.theflood.elite.behavior.EliteMobCompatibility;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

public final class UndyingMutation
        implements EliteMutation {

    private static final String REVIVAL_ACTIVE_KEY =
            "theflood_undying_revival_active";

    private static final String REVIVAL_PROGRESS_KEY =
            "theflood_undying_revival_progress";

    private static final String REVIVAL_END_TIME_KEY =
            "theflood_undying_revival_end_time";

    private static final String REVIVAL_COUNT_KEY =
            "theflood_undying_revival_count";

    private static final String FINAL_DEATH_KEY =
            "theflood_undying_final_death";

    private static final String LAST_HURT_TIME_KEY =
            "theflood_undying_last_hurt_time";

    private static final String REVIVAL_MAX_PROGRESS_KEY =
            "theflood_undying_revival_max_progress";

    private static final String REASSEMBLE_END_KEY =
        "theflood_undying_reassemble_end";

    private static final String FINAL_DEATH_END_KEY =
        "theflood_undying_final_death_end";

        private static final int FINAL_DEATH_ANIMATION_TICKS =
                24;

        private static final int REASSEMBLE_TICKS =
                16;

    private static final int REGEN_DELAY_TICKS =
            100; // 5 sec

    private static final float REGEN_PER_TICK =
            0.10F;

    private static final int DPS_CHECK_DURATION_TICKS =
            100; // 5 sec

    private static final float BASE_DPS_CHECK_DAMAGE =
            40.0F;

    private static final float CHECK_EASING_PER_FAILURE =
            0.20F;

    private static final float REVIVE_HEALTH_PERCENT =
            0.35F;

    private static final int STATUS_COLOR =
            0xFF9C5CFF;

    @Override
    public String id() {
        return "undying";
    }

    @Override
    public String displayName() {
        return "Undying";
    }

    @Override
        public boolean canApplyTo(
                Mob mob
        ) {
        return EliteMobCompatibility.isZombie(
                mob
        )
                || EliteMobCompatibility.isSkeleton(
                        mob
                );
        }

    @Override
    public boolean handleLethalDamage(
            Mob elite,
            float incomingDamage
    ) {
        if (
                elite.getPersistentData()
                        .getBoolean(
                                FINAL_DEATH_KEY
                        )
        ) {
            return false;
        }

        if (isRevivalActive(elite)) {
            return true;
        }

        startRevivalCheck(
                elite
        );

        return true;
    }

    private static void startRevivalCheck(
            Mob elite
    ) {
        ElitePresentation.setPose(
                elite,
                ElitePose.UNDYING_DOWNED
        );

        EliteStateSync.syncBasic(
                elite
        );

        int failures =
                elite.getPersistentData()
                        .getInt(
                                REVIVAL_COUNT_KEY
                        );

        float requiredDamage =
                BASE_DPS_CHECK_DAMAGE
                        * Math.max(
                                0.25F,
                                1.0F
                                        - (
                                        failures
                                                * CHECK_EASING_PER_FAILURE
                                )
                        );

        elite.getPersistentData()
                .putBoolean(
                        REVIVAL_ACTIVE_KEY,
                        true
                );

        elite.getPersistentData()
                .putFloat(
                        REVIVAL_PROGRESS_KEY,
                        requiredDamage
                );

        elite.getPersistentData()
                .putFloat(
                        REVIVAL_MAX_PROGRESS_KEY,
                        requiredDamage
                );

        elite.getPersistentData()
                .putLong(
                        REVIVAL_END_TIME_KEY,
                        elite.level()
                                .getGameTime()
                                + DPS_CHECK_DURATION_TICKS
                );

        elite.setHealth(
                1.0F
        );

        elite.setNoAi(
                true
        );

        syncRevivalState(
                elite,
                requiredDamage
        );
    }

    public static boolean isFinalDeath(
        Mob elite
) {
    return elite != null
            && elite.getPersistentData()
                    .getBoolean(
                            FINAL_DEATH_KEY
                    );
}

    private static boolean isRevivalActive(
            Mob elite
    ) {
        return elite.getPersistentData()
                .getBoolean(
                        REVIVAL_ACTIVE_KEY
                );
    }

    private static void syncRevivalState(
            Mob elite,
            float remaining
    ) {
        float maximum =
                elite.getPersistentData()
                        .getFloat(
                                REVIVAL_MAX_PROGRESS_KEY
                        );

        EliteStateSync.sync(
                elite,
                remaining,
                maximum,
                true,
                STATUS_COLOR
        );
    }

    @Override
    public float modifyIncomingDamage(
            Mob elite,
            DamageSource source,
            float damage
    ) {
        if (
                elite.getPersistentData()
                        .getBoolean(
                                FINAL_DEATH_KEY
                        )
                && !source.is(
                        DamageTypes.GENERIC_KILL
                )
        ) {
        return 0.0F;
        }

        elite.getPersistentData()
                .putLong(
                        LAST_HURT_TIME_KEY,
                        elite.level()
                                .getGameTime()
                );

        if (!isRevivalActive(elite)) {
            return damage;
        }

        float remaining =
                elite.getPersistentData()
                        .getFloat(
                                REVIVAL_PROGRESS_KEY
                        );

        remaining =
                Math.max(
                        0.0F,
                        remaining - damage
                );

        elite.getPersistentData()
                .putFloat(
                        REVIVAL_PROGRESS_KEY,
                        remaining
                );

        if (remaining <= 0.0F) {
            finishDeath(
                    elite
            );
        } else {
            syncRevivalState(
                    elite,
                    remaining
            );
        }

        /*
        * Don't let revival-phase attacks damage HP.
        */
        return 0.0F;
    }

    private static boolean tickReassembly(
        Mob elite
) {
    long end =
            elite.getPersistentData()
                    .getLong(
                            REASSEMBLE_END_KEY
                    );

    if (end <= 0L) {
        return false;
    }

    if (
            elite.level()
                    .getGameTime()
                    < end
    ) {
        return true;
    }

    elite.getPersistentData()
            .putLong(
                    REASSEMBLE_END_KEY,
                    0L
            );

    elite.setNoAi(
            false
    );

    EliteStateSync.syncBasic(
            elite
    );

    return false;
}

        private static boolean tickFinalDeath(
        Mob elite
) {
    long end =
            elite.getPersistentData()
                    .getLong(
                            FINAL_DEATH_END_KEY
                    );

    if (end <= 0L) {
        return false;
    }

    long now =
            elite.level()
                    .getGameTime();

    if (
            elite.level() instanceof ServerLevel level
    ) {
        /*
         * Keep the ritual alive while the pieces
         * are being pulled downward.
         */
        if (
                elite.tickCount
                        % 2
                        == 0
        ) {
            EliteVisuals.ritualSigil(
                    level,
                    elite,
                    ParticleTypes.ENCHANT,
                    20,
                    1.15D,
                    0.16D
            );
        }

        /*
         * Heavier smoke during the final pull.
         */
        if (
                elite.tickCount
                        % 3
                        == 0
        ) {
            EliteVisuals.undyingSmoke(
                    level,
                    elite
            );
        }
    }

    if (now < end) {
        return true;
    }

    /*
     * Final portal-collapse burst.
     */
    if (
            elite.level() instanceof ServerLevel level
    ) {
        EliteVisuals.burst(
                elite,
                ParticleTypes.PORTAL,
                30,
                0.55D,
                0.08D
        );

        EliteVisuals.burst(
                elite,
                ParticleTypes.WITCH,
                18,
                0.45D,
                0.04D
        );
    }

    elite.getPersistentData()
            .putLong(
                    FINAL_DEATH_END_KEY,
                    0L
            );

    /*
     * FINAL_DEATH_KEY is already true, so
     * handleLethalDamage() will finally allow this
     * lethal hit through.
     */
    elite.setNoAi(
            false
    );

    elite.hurt(
            elite.damageSources()
                    .genericKill(),
            Float.MAX_VALUE
    );

    return true;
}

    @Override
        public void tick(
                Mob elite
        ) {
        if (
                tickFinalDeath(
                        elite
                )
        ) {
        return;
        }
        /*
        * Downed ritual owns the entity completely.
        */
        if (
                isRevivalActive(
                        elite
                )
        ) {
                tickRevivalCheck(
                        elite
                );

                return;
        }

        /*
        * Reassembly also owns the entity until finished.
        */
        if (
                tickReassembly(
                        elite
                )
        ) {
                return;
        }

        /*
        * Only regenerate while behaving normally.
        */
        tickRegeneration(
                elite
        );
        }

    private static void tickRegeneration(
            Mob elite
    ) {
        if (
                elite.getHealth()
                        >= elite.getMaxHealth()
        ) {
            return;
        }

        long lastHurt =
                elite.getPersistentData()
                        .getLong(
                                LAST_HURT_TIME_KEY
                        );

        if (
                elite.level()
                        .getGameTime()
                        - lastHurt
                        < REGEN_DELAY_TICKS
        ) {
            return;
        }

        if (
                elite.tickCount
                        % 6
                        == 0
                && elite.level() instanceof ServerLevel level
        ) {
        EliteVisuals.burst(
                elite,
                ParticleTypes.WITCH,
                2,
                0.25D,
                0.01D
        );
        }

        elite.heal(
                REGEN_PER_TICK
        );
    }

    private static void tickRevivalCheck(
            Mob elite
    ) {
        long endTime =
                elite.getPersistentData()
                        .getLong(
                                REVIVAL_END_TIME_KEY
                        );

        if (
                elite.level() instanceof ServerLevel level
        ) {
        if (
                elite.tickCount
                        % 3
                        == 0
        ) {
                EliteVisuals.ritualSigil(
                        level,
                        elite,
                        ParticleTypes.ENCHANT,
                        16,
                        1.1D,
                        0.10D
                );
        }

        if (
                elite.tickCount
                        % 5
                        == 0
        ) {
                EliteVisuals.undyingSmoke(
                        level,
                        elite
                );
        }
        }

        if (
                elite.level()
                        .getGameTime()
                        < endTime
        ) {
            return;
        }

        failRevivalCheck(
                elite
        );
    }

    private static void failRevivalCheck(
            Mob elite
    ) {
        ElitePresentation.resetPose(
                elite
        );

        EliteStateSync.syncBasic(
                elite
        );

        int failures =
                elite.getPersistentData()
                        .getInt(
                                REVIVAL_COUNT_KEY
                        )
                        + 1;

        elite.getPersistentData()
                .putInt(
                        REVIVAL_COUNT_KEY,
                        failures
                );

        elite.getPersistentData()
                .putBoolean(
                        REVIVAL_ACTIVE_KEY,
                        false
                );

        elite.getPersistentData()
        .putLong(
                REASSEMBLE_END_KEY,
                elite.level()
                        .getGameTime()
                        + REASSEMBLE_TICKS
        );

        float revivedHealth =
                elite.getMaxHealth()
                        * REVIVE_HEALTH_PERCENT;

        elite.setHealth(
                Math.max(
                        1.0F,
                        revivedHealth
                )
        );
    }

    private static boolean isReassembling(
                Mob elite
        ) {
        return elite.getPersistentData()
                .getLong(
                        REASSEMBLE_END_KEY
                ) > 0L;
        }

    @Override
        public boolean canDeactivate(
                Mob elite
        ) {
        return !isRevivalActive(
                elite
        )
                && !isReassembling(
                        elite
                );
        }

    private static void finishDeath(
                Mob elite
        ) {
        elite.getPersistentData()
                .putBoolean(
                        REVIVAL_ACTIVE_KEY,
                        false
                );

        elite.getPersistentData()
                .putBoolean(
                        FINAL_DEATH_KEY,
                        true
                );

        elite.getPersistentData()
                .putLong(
                        FINAL_DEATH_END_KEY,
                        elite.level()
                                .getGameTime()
                                + FINAL_DEATH_ANIMATION_TICKS
                );

        elite.setNoAi(
                true
        );

        ElitePresentation.setPose(
                elite,
                ElitePose.UNDYING_SINKING
        );

        EliteStateSync.syncBasic(
                elite
        );
        }
}