package com.jushymaso222.theflood.elite.behavior.mutations;

import com.jushymaso222.theflood.elite.behavior.EliteMutation;
import com.jushymaso222.theflood.elite.EliteData;
import com.jushymaso222.theflood.elite.EliteStateSync;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;

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

    @Override
    public void tick(
            Mob elite
    ) {
        if (isRevivalActive(elite)) {
            tickRevivalCheck(
                    elite
            );

            return;
        }

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

        elite.setNoAi(
                false
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

        EliteStateSync.syncBasic(
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

        elite.setNoAi(
                false
        );

        EliteStateSync.syncBasic(
                elite
        );

        elite.hurt(
                elite.damageSources()
                        .genericKill(),
                Float.MAX_VALUE
        );
    }
}