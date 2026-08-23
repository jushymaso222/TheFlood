package com.jushymaso222.theflood.elite.behavior.mutations;

import java.util.UUID;

import com.jushymaso222.theflood.elite.EliteStateSync;
import com.jushymaso222.theflood.elite.behavior.EliteMutation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

import com.jushymaso222.theflood.elite.behavior.EliteMobCompatibility;
import com.jushymaso222.theflood.elite.presentation.EliteSounds;
import com.jushymaso222.theflood.elite.presentation.EliteVisuals;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.particles.ParticleTypes;

public final class ShiftingMutation
        implements EliteMutation {

    private static final String TARGET_UUID_KEY =
            "theflood_shifting_target";

    private static final String BACKSTAB_END_KEY =
            "theflood_shifting_backstab_end";

    private static final String NEXT_BACKSTAB_KEY =
            "theflood_shifting_next_backstab";

    private static final String BACKSTAB_ACTIVE_KEY =
            "theflood_shifting_backstab_active";

    private static final String BACKSTAB_ORIGIN_X_KEY =
            "theflood_shifting_origin_x";

    private static final String BACKSTAB_ORIGIN_Y_KEY =
            "theflood_shifting_origin_y";

    private static final String BACKSTAB_ORIGIN_Z_KEY =
            "theflood_shifting_origin_z";

    private static final String BACKSTAB_LOOK_X_KEY =
            "theflood_shifting_backstab_look_x";

    private static final String BACKSTAB_LOOK_Z_KEY =
            "theflood_shifting_backstab_look_z";

    private static final String HEALTH_PENALTY_APPLIED_KEY =
        "theflood_shifting_health_penalty_applied";

    private static final float HEALTH_MULTIPLIER =
            0.75F;

    private static final UUID HEALTH_MODIFIER_ID =
            UUID.fromString(
                    "0db44591-c330-41de-91a1-2d7ee737fa28"
            );

    /*
     * Time between ordinary backstab attempts.
     *
     * 160 ticks = 8 seconds.
     */
    private static final int BACKSTAB_COOLDOWN_TICKS =
            160;

    /*
     * Player gets half a second to react after
     * Shifting appears behind them.
     */
    private static final int BACKSTAB_WINDUP_TICKS =
            25;

    /*
     * Interrupted attacks get a longer cooldown.
     *
     * 240 ticks = 12 seconds.
     */
    private static final int INTERRUPTED_COOLDOWN_TICKS =
            240;

    /*
     * How far behind the player Shifting appears.
     */
    private static final double BACKSTAB_DISTANCE =
            1.0D;

    /*
     * Starting damage multiplier for a successful
     * backstab.
     */
    private static final float BACKSTAB_DAMAGE_MULTIPLIER =
            1.50F;

    /*
     * Ranged dodge displacement.
     */
    private static final double SHIFT_DODGE_DISTANCE =
            3.0D;

    /*
     * Small cooldown prevents automatic weapons from
     * causing dozens of teleports every tick.
     */
    private static final String NEXT_PROJECTILE_SHIFT_KEY =
            "theflood_shifting_next_projectile_shift";

    private static final int PROJECTILE_SHIFT_COOLDOWN_TICKS =
            10;

    /*
     * Status bar shows time until the next backstab.
     */
    private static final int STATUS_COLOR =
            0xFF9B6CFF;

    @Override
    public String id() {
        return "shifting";
    }

    @Override
    public String displayName() {
        return "Shifting";
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
                )
                || EliteMobCompatibility.isEnderman(
                        mob
                )
                || EliteMobCompatibility.isWarden(
                        mob
                )
                || EliteMobCompatibility.isSpider(
                        mob
                );
        }

    @Override
    public void onDeactivated(
            Mob elite
    ) {
        if (isBackstabActive(elite)) {
            returnToOrigin(
                    elite
            );
        }

        clearCombatState(
                elite
        );
    }

    private static void playShiftEffect(
        Mob elite,
        boolean playSound
) {
    EliteVisuals.burst(
            elite,
            ParticleTypes.PORTAL,
            24,
            0.45D,
            0.12D
    );

    if (playSound) {
        EliteSounds.playRandomPitch(
                elite,
                SoundEvents.ENDERMAN_TELEPORT,
                1.0F,
                1.0F,
                0.05F
        );
    }
}

    private static final double PROJECTILE_DETECTION_RADIUS =
        4.0D;

    private static void ensureHealthPenalty(
            Mob elite
    ) {
        AttributeInstance maxHealth =
                elite.getAttribute(
                        Attributes.MAX_HEALTH
                );

        if (
                maxHealth == null
                || maxHealth.getModifier(
                        HEALTH_MODIFIER_ID
                ) != null
        ) {
            return;
        }

        float healthPercent =
                elite.getHealth()
                        / elite.getMaxHealth();

        maxHealth.addPermanentModifier(
                new AttributeModifier(
                        HEALTH_MODIFIER_ID,
                        "The Flood Shifting health penalty",
                        -0.25D,
                        AttributeModifier.Operation.MULTIPLY_TOTAL
                )
        );

        elite.setHealth(
                Math.max(
                        1.0F,
                        elite.getMaxHealth()
                                * healthPercent
                )
        );
    }

    private static void checkIncomingProjectiles(
            ServerLevel level,
            Mob elite
    ) {
        if (
                elite.getPersistentData()
                        .getBoolean(
                                BACKSTAB_ACTIVE_KEY
                        )
        ) {
            return;
        }

        long gameTime =
                level.getGameTime();

        long nextShift =
                elite.getPersistentData()
                        .getLong(
                                NEXT_PROJECTILE_SHIFT_KEY
                        );

        if (gameTime < nextShift) {
            return;
        }

        for (
                Projectile projectile :
                level.getEntitiesOfClass(
                        Projectile.class,
                        elite.getBoundingBox()
                                .inflate(
                                        PROJECTILE_DETECTION_RADIUS
                                )
                )
        ) {
            if (!isProjectileThreateningElite(
                    projectile,
                    elite
            )) {
                continue;
            }

            if (tryProjectileShift(elite)) {
                return;
            }
        }
    }

    private static boolean isProjectileThreateningElite(
            Projectile projectile,
            Mob elite
    ) {
        /*
        * Don't dodge our own projectiles.
        */
        if (projectile.getOwner() == elite) {
            return false;
        }

        Vec3 velocity =
                projectile.getDeltaMovement();

        if (velocity.lengthSqr() <= 0.0001D) {
            return false;
        }

        Vec3 toElite =
                elite.position()
                        .add(
                                0.0D,
                                elite.getBbHeight()
                                        * 0.5D,
                                0.0D
                        )
                        .subtract(
                                projectile.position()
                        );

        /*
        * Positive dot product means the projectile
        * is traveling generally toward the Elite.
        */
        return velocity.normalize()
                .dot(
                        toElite.normalize()
                )
                > 0.75D;
    }

    @Override
    public void tick(
            Mob elite
    ) {
        if (
                !(elite.level() instanceof ServerLevel level)
        ) {
            return;
        }

        ServerPlayer target =
                getTargetPlayer(
                        level,
                        elite
                );
        
        ensureHealthPenalty(
                elite
        );

        checkIncomingProjectiles(
                level,
                elite
        );

        /*
         * Acquire a player from normal mob aggro.
         */
        if (target == null) {
            if (
                    elite.getTarget() instanceof ServerPlayer player
                    && player.isAlive()
                    && !player.isSpectator()
            ) {
                setTargetPlayer(
                        elite,
                        player
                );

                target =
                        player;
            } else {
                clearCombatState(
                        elite
                );

                return;
            }
        }

        if (
                !target.isAlive()
                || target.isSpectator()
                || target.serverLevel() != level
        ) {
            clearCombatState(
                    elite
            );

            return;
        }

        /*
         * If a backstab is currently in progress,
         * finish that state before doing anything else.
         */
        if (isBackstabActive(elite)) {
            tickBackstab(
                    level,
                    elite,
                    target
            );

            return;
        }

        /*
         * Keep normal aggro focused on the chosen player.
         */
        if (
                elite.getTarget()
                        != target
        ) {
            elite.setTarget(
                    target
            );
        }

        long gameTime =
                level.getGameTime();

        long nextBackstab =
                elite.getPersistentData()
                        .getLong(
                                NEXT_BACKSTAB_KEY
                        );

        /*
         * First initialization.
         */
        if (nextBackstab <= 0L) {
            nextBackstab =
                    gameTime
                            + BACKSTAB_COOLDOWN_TICKS;

            elite.getPersistentData()
                    .putLong(
                            NEXT_BACKSTAB_KEY,
                            nextBackstab
                    );
        }

        if (gameTime >= nextBackstab) {
            startBackstab(
                    elite,
                    target
            );

            return;
        }

        syncBackstabBar(
                elite,
                gameTime,
                nextBackstab
        );
    }

    @Override
    public float modifyIncomingDamage(
            Mob elite,
            DamageSource source,
            float damage
    ) {
        /*
         * During the backstab windup, a melee hit
         * interrupts the maneuver.
         */
        if (isBackstabActive(elite)) {
            if (!isRangedAttack(source)) {
                Entity attacker =
                        source.getEntity();

                if (
                        attacker instanceof ServerPlayer
                ) {
                    interruptBackstab(
                            elite
                    );

                    /*
                     * The interrupting melee attack still
                     * damages the Elite normally.
                     */
                    return damage;
                }
            }
        }

        /*
         * Projectile attack:
         * attempt to shift away and negate the hit.
         */
        if (isRangedAttack(source)) {
            if (tryProjectileShift(elite)) {
                return 0.0F;
            }
        }

        return damage;
    }

    private static void startBackstab(
            Mob elite,
            ServerPlayer target
    ) {
        Vec3 look =
                target.getLookAngle();

        Vec3 horizontal =
                new Vec3(
                        look.x,
                        0.0D,
                        look.z
                );

        if (horizontal.lengthSqr() > 0.0001D) {
            horizontal =
                    horizontal.normalize();

            elite.getPersistentData()
                    .putDouble(
                            BACKSTAB_LOOK_X_KEY,
                            horizontal.x
                    );

            elite.getPersistentData()
                    .putDouble(
                            BACKSTAB_LOOK_Z_KEY,
                            horizontal.z
                    );
        }
        /*
         * Save origin so Shifting can return after
         * the maneuver.
         */
        elite.getPersistentData()
                .putDouble(
                        BACKSTAB_ORIGIN_X_KEY,
                        elite.getX()
                );

        elite.getPersistentData()
                .putDouble(
                        BACKSTAB_ORIGIN_Y_KEY,
                        elite.getY()
                );

        elite.getPersistentData()
                .putDouble(
                        BACKSTAB_ORIGIN_Z_KEY,
                        elite.getZ()
                );

        elite.getPersistentData()
                .putBoolean(
                        BACKSTAB_ACTIVE_KEY,
                        true
                );

        elite.getPersistentData()
                .putLong(
                        BACKSTAB_END_KEY,
                        elite.level()
                                .getGameTime()
                                + BACKSTAB_WINDUP_TICKS
                );

        /*
         * Stop vanilla AI from immediately walking away
         * from the backstab position.
         */
        elite.getNavigation()
                .stop();

        elite.setDeltaMovement(
                0.0D,
                elite.getDeltaMovement().y,
                0.0D
        );

        elite.setNoAi(
                true
        );

        teleportBehindPlayer(
            elite,
            target
        );
    }

    private static void tickBackstab(
            ServerLevel level,
            Mob elite,
            ServerPlayer target
    ) {
        long endTime =
                elite.getPersistentData()
                        .getLong(
                                BACKSTAB_END_KEY
                        );

        /*
        * Stay tethered one block behind the player
        * throughout the reaction window.
        */
        Vec3 destination =
                getBackstabPosition(
                        elite,
                        target
                );

        elite.teleportTo(
                destination.x,
                target.getY(),
                destination.z
        );

        facePlayer(
                elite,
                target
        );

        if (
                level.getGameTime()
                        < endTime
        ) {
            return;
        }

        performBackstab(
                elite,
                target
        );
    }

    private static void performBackstab(
            Mob elite,
            ServerPlayer target
    ) {
        /*
         * Only hit if we're still close enough.
         *
         * This gives movement/dodging some counterplay.
         */
        if (
                elite.distanceToSqr(
                        target
                )
                        <= 9.0D
        ) {
            float damage =
                    (float) elite.getAttributeValue(
                            Attributes.ATTACK_DAMAGE
                    );

            target.hurt(
                    elite.damageSources()
                            .mobAttack(
                                    elite
                            ),
                    damage
                            * BACKSTAB_DAMAGE_MULTIPLIER
            );
        }

        returnToOrigin(
                elite
        );

        finishBackstab(
                elite,
                BACKSTAB_COOLDOWN_TICKS
        );
    }

    private static void interruptBackstab(
            Mob elite
    ) {
        EliteSounds.playRandomPitch(
                elite,
                SoundEvents.AMETHYST_BLOCK_BREAK,
                1.0F,
                0.8F,
                0.05F
        );

        returnToOrigin(
                elite
        );

        finishBackstab(
                elite,
                INTERRUPTED_COOLDOWN_TICKS
        );
    }

    private static void finishBackstab(
            Mob elite,
            int cooldownTicks
    ) {
        elite.getPersistentData()
                .putBoolean(
                        BACKSTAB_ACTIVE_KEY,
                        false
                );

        elite.getPersistentData()
                .putLong(
                        BACKSTAB_END_KEY,
                        0L
                );

        elite.getPersistentData()
                .putLong(
                        NEXT_BACKSTAB_KEY,
                        elite.level()
                                .getGameTime()
                                + cooldownTicks
                );

        /*
        * Re-enable normal AI after shifting back.
        */
        elite.setNoAi(
                false
        );

        EliteStateSync.sync(
                elite,
                cooldownTicks,
                cooldownTicks,
                true,
                STATUS_COLOR
        );
    }

    private static void teleportBehindPlayer(
        Mob elite,
        ServerPlayer target
) {
    Vec3 destination =
            getBackstabPosition(
                    elite,
                    target
            );

    /*
     * Departure effect.
     *
     * Sound plays here once.
     */
    playShiftEffect(
            elite,
            true
    );

    elite.teleportTo(
            destination.x,
            target.getY(),
            destination.z
    );

    /*
     * Arrival particles.
     *
     * No second sound.
     */
    playShiftEffect(
            elite,
            false
    );

    facePlayer(
            elite,
            target
    );
}

    private static Vec3 getBackstabPosition(
            Mob elite,
            ServerPlayer target
    ) {
        double lookX =
                elite.getPersistentData()
                        .getDouble(
                                BACKSTAB_LOOK_X_KEY
                        );

        double lookZ =
                elite.getPersistentData()
                        .getDouble(
                                BACKSTAB_LOOK_Z_KEY
                        );

        Vec3 behind =
                new Vec3(
                        -lookX,
                        0.0D,
                        -lookZ
                ).normalize()
                        .scale(
                                BACKSTAB_DISTANCE
                        );

        return target.position()
                .add(
                        behind
                );
    }

    private static void returnToOrigin(
            Mob elite
    ) {
        double x =
                elite.getPersistentData()
                        .getDouble(
                                BACKSTAB_ORIGIN_X_KEY
                        );

        double y =
                elite.getPersistentData()
                        .getDouble(
                                BACKSTAB_ORIGIN_Y_KEY
                        );

        double z =
                elite.getPersistentData()
                        .getDouble(
                                BACKSTAB_ORIGIN_Z_KEY
                        );

        playShiftEffect(
                elite,
                true
        );

        elite.teleportTo(
                x,
                y,
                z
        );

        playShiftEffect(
                elite,
                false
        );
    }

    private static boolean tryProjectileShift(
            Mob elite
    ) {
        if (
                !(elite.level() instanceof ServerLevel level)
        ) {
            return false;
        }

        long gameTime =
                level.getGameTime();

        long nextShift =
                elite.getPersistentData()
                        .getLong(
                                NEXT_PROJECTILE_SHIFT_KEY
                        );

        if (gameTime < nextShift) {
            return false;
        }

        elite.getPersistentData()
                .putLong(
                        NEXT_PROJECTILE_SHIFT_KEY,
                        gameTime
                                + PROJECTILE_SHIFT_COOLDOWN_TICKS
                );

        /*
         * Shift sideways relative to current facing.
         */
        Vec3 facing =
                elite.getViewVector(
                        1.0F
                );

        Vec3 sideways =
                new Vec3(
                        -facing.z,
                        0.0D,
                        facing.x
                );

        if (sideways.lengthSqr() <= 0.0001D) {
            return false;
        }

        sideways =
                sideways.normalize();

        /*
         * Randomly choose left or right.
         */
        if (level.random.nextBoolean()) {
            sideways =
                    sideways.scale(
                            -1.0D
                    );
        }

        Vec3 destination =
                elite.position()
                        .add(
                                sideways.scale(
                                        SHIFT_DODGE_DISTANCE
                                )
                        );

        playShiftEffect(
                elite,
                true
        );

        elite.teleportTo(
                destination.x,
                elite.getY(),
                destination.z
        );

        playShiftEffect(
                elite,
                false
        );

        return true;
    }

    private static boolean isRangedAttack(
            DamageSource source
    ) {
        return source.getDirectEntity()
                instanceof Projectile;
    }

    private static boolean isBackstabActive(
            Mob elite
    ) {
        return elite.getPersistentData()
                .getBoolean(
                        BACKSTAB_ACTIVE_KEY
                );
    }

    private static void facePlayer(
            Mob elite,
            ServerPlayer target
    ) {
        Vec3 direction =
                target.position()
                        .subtract(
                                elite.position()
                        );

        double yaw =
                Math.toDegrees(
                        Math.atan2(
                                -direction.x,
                                direction.z
                        )
                );

        elite.setYRot(
                (float) yaw
        );

        elite.setYHeadRot(
                (float) yaw
        );

        elite.setYBodyRot(
                (float) yaw
        );
    }

    private static void setTargetPlayer(
            Mob elite,
            ServerPlayer player
    ) {
        elite.getPersistentData()
                .putUUID(
                        TARGET_UUID_KEY,
                        player.getUUID()
                );
    }

    private static ServerPlayer getTargetPlayer(
            ServerLevel level,
            Mob elite
    ) {
        if (
                !elite.getPersistentData()
                        .hasUUID(
                                TARGET_UUID_KEY
                        )
        ) {
            return null;
        }

        UUID targetId =
                elite.getPersistentData()
                        .getUUID(
                                TARGET_UUID_KEY
                        );

        return level.getServer()
                .getPlayerList()
                .getPlayer(
                        targetId
                );
    }

    private static void clearCombatState(
            Mob elite
    ) {
        elite.getPersistentData()
                .remove(
                        TARGET_UUID_KEY
                );

        elite.getPersistentData()
                .putBoolean(
                        BACKSTAB_ACTIVE_KEY,
                        false
                );

        elite.getPersistentData()
                .putLong(
                        BACKSTAB_END_KEY,
                        0L
                );

        elite.getPersistentData()
                .putLong(
                        NEXT_BACKSTAB_KEY,
                        0L
                );

        elite.setNoAi(
                false
        );

        EliteStateSync.syncBasic(
                elite
        );
    }

    private static void syncBackstabBar(
            Mob elite,
            long gameTime,
            long nextBackstab
    ) {
        float remaining =
                Math.max(
                        0.0F,
                        nextBackstab
                                - gameTime
                );

        EliteStateSync.sync(
                elite,
                remaining,
                BACKSTAB_COOLDOWN_TICKS,
                true,
                STATUS_COLOR
        );
    }
}