package com.jushymaso222.theflood.elite.behavior.mutations;

import com.jushymaso222.theflood.elite.behavior.EliteMutation;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import com.jushymaso222.theflood.elite.EliteStateSync;

public final class BulwarkMutation
        implements EliteMutation {

    private static final String STAGGER_KEY =
            "theflood_bulwark_stagger";

    private static final String STUN_END_KEY =
            "theflood_bulwark_stun_end";

    private static final String WAS_STUNNED_KEY =
            "theflood_bulwark_was_stunned";

    private static final String STUN_TARGET_KEY =
            "theflood_bulwark_stun_target";

    private static final float STAGGER_REQUIRED =
            30.0F;

    private static final int STUN_DURATION_TICKS =
            80; // 4 seconds

    private static final int STATUS_COLOR =
            0xFFFFC72C;

    private static final float STUNNED_REAR_DAMAGE_MULTIPLIER =
            2.0F;

    private static final String PREVIOUS_NO_AI_KEY =
            "theflood_bulwark_previous_no_ai";

    /*
     * Damage multiplier for attacks that hit
     * the protected frontal area.
     *
     * 0.25 = 75% damage reduction.
     */
    private static final float FRONT_DAMAGE_MULTIPLIER =
            0.25F;

    /*
     * Dot-product threshold determining how wide
     * the protected frontal area is.
     *
     * 0.0 roughly represents the entire front half
     * of the mob.
     */
    private static final double FRONT_DOT_THRESHOLD =
            0.0D;

    private static void syncState(
            Mob elite
    ) {
        float stagger =
                elite.getPersistentData()
                        .getFloat(
                                STAGGER_KEY
                        );

        boolean stunned =
                isStunned(
                        elite
                );

        float displayedStagger =
                stunned
                        ? STAGGER_REQUIRED
                        : stagger;

        EliteStateSync.sync(
                elite,
                displayedStagger,
                STAGGER_REQUIRED,
                true,
                STATUS_COLOR
        );
    }

    private static void stun(
            Mob elite
    ) {
        long stunEnd =
                elite.level()
                        .getGameTime()
                        + STUN_DURATION_TICKS;

        elite.getPersistentData()
                .putLong(
                        STUN_END_KEY,
                        stunEnd
                );

        elite.getPersistentData()
                .putFloat(
                        STAGGER_KEY,
                        0.0F
                );

        if (elite.getTarget() != null) {
            elite.getPersistentData()
                    .putUUID(
                            STUN_TARGET_KEY,
                            elite.getTarget().getUUID()
                    );
        }

        /*
        * Remember the mob's previous AI state so we
        * don't accidentally enable AI on something
        * that intentionally had it disabled.
        */
        elite.getPersistentData()
                .putBoolean(
                        PREVIOUS_NO_AI_KEY,
                        elite.isNoAi()
                );

        /*
        * Completely suspend vanilla AI while stunned.
        *
        * This prevents pathfinding, attacking and,
        * importantly, constantly turning toward the player.
        */
        elite.setNoAi(
                true
        );

        elite.getNavigation()
                .stop();

        elite.setDeltaMovement(
                0.0D,
                elite.getDeltaMovement().y,
                0.0D
        );

        syncState(
                elite
        );
    }

    private static boolean isAttackerInFront(
            Mob elite,
            Entity attacker
    ) {
        Vec3 facing =
                elite.getViewVector(
                        1.0F
                );

        facing =
                new Vec3(
                        facing.x,
                        0.0D,
                        facing.z
                );

        if (facing.lengthSqr() <= 0.0001D) {
            return false;
        }

        Vec3 toAttacker =
                attacker.position()
                        .subtract(
                                elite.position()
                        );

        toAttacker =
                new Vec3(
                        toAttacker.x,
                        0.0D,
                        toAttacker.z
                );

        if (toAttacker.lengthSqr() <= 0.0001D) {
            return false;
        }

        return facing.normalize()
                .dot(
                        toAttacker.normalize()
                )
                >= FRONT_DOT_THRESHOLD;
    }

    private static boolean isStunned(
            Mob elite
    ) {
        long stunEnd =
                elite.getPersistentData()
                        .getLong(
                                STUN_END_KEY
                        );

        return elite.level()
                .getGameTime()
                < stunEnd;
    }

    @Override
    public String id() {
        return "bulwark";
    }

    @Override
    public String displayName() {
        return "Bulwark";
    }

    @Override
    public float modifyIncomingDamage(
            Mob elite,
            DamageSource source,
            float damage
    ) {
        Entity attacker =
                source.getEntity();

        if (attacker == null) {
            return damage;
        }

        boolean attackerInFront =
                isAttackerInFront(
                        elite,
                        attacker
                );

        /*
        * During stun, the shield is effectively broken.
        *
        * Rear attacks receive a large punish bonus.
        */
        if (isStunned(elite)) {

            if (!attackerInFront) {
                return damage
                        * STUNNED_REAR_DAMAGE_MULTIPLIER;
            }

            return damage;
        }

        /*
        * Rear attacks already bypass Bulwark protection.
        */
        if (!attackerInFront) {
            return damage;
        }

        /*
        * Frontal attacks build stagger based on the
        * ORIGINAL incoming damage.
        *
        * This means stronger weapons break its guard faster.
        */
        float stagger =
                elite.getPersistentData()
                        .getFloat(
                                STAGGER_KEY
                        );

        stagger +=
                damage;

        if (
                stagger
                        >= STAGGER_REQUIRED
        ) {
            stun(
                    elite
            );
        } else {
            elite.getPersistentData()
                    .putFloat(
                            STAGGER_KEY,
                            stagger
                    );

            syncState(
                    elite
            );
        }

        /*
        * Until the guard breaks, frontal damage is
        * heavily reduced.
        */
        return damage
                * FRONT_DAMAGE_MULTIPLIER;
    }

    @Override
    public void tick(
            Mob elite
    ) {
        boolean stunned =
                isStunned(
                        elite
                );

        boolean wasStunned =
                elite.getPersistentData()
                        .getBoolean(
                                WAS_STUNNED_KEY
                        );

        /*
        * Stun has just ended.
        */
        if (
                wasStunned
                && !stunned
        ) {
            elite.getPersistentData()
                    .putBoolean(
                            WAS_STUNNED_KEY,
                            false
                    );

            elite.getPersistentData()
                    .putFloat(
                            STAGGER_KEY,
                            0.0F
                    );

            /*
            * Restore whatever AI state this mob had
            * before Bulwark stunned it.
            */
            boolean previousNoAi =
                    elite.getPersistentData()
                            .getBoolean(
                                    PREVIOUS_NO_AI_KEY
                            );

            elite.setNoAi(
                    previousNoAi
            );

            if (
                    !previousNoAi
                    && elite.getPersistentData()
                            .hasUUID(
                                    STUN_TARGET_KEY
                            )
                    && elite.level() instanceof ServerLevel serverLevel
            ) {
                UUID targetId =
                        elite.getPersistentData()
                                .getUUID(
                                        STUN_TARGET_KEY
                                );

                Entity target =
                        serverLevel.getEntity(
                                targetId
                        );

                if (
                        target instanceof LivingEntity livingTarget
                        && livingTarget.isAlive()
                ) {
                    elite.setTarget(
                            livingTarget
                    );
                }

                elite.getPersistentData()
                        .remove(
                                STUN_TARGET_KEY
                        );
            }

            syncState(
                    elite
            );

            return;
        }

        if (!stunned) {
            return;
        }

        elite.getPersistentData()
                .putBoolean(
                        WAS_STUNNED_KEY,
                        true
                );

        /*
        * AI should already be disabled by stun(),
        * but reinforce it in case something external
        * tries to re-enable it.
        */
        elite.setNoAi(
                true
        );
    }
}