package com.jushymaso222.theflood.elite.attributes.specials;

import com.jushymaso222.theflood.elite.attributes.SpecialAttribute;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class VolatileAttribute
        implements SpecialAttribute {

    private static final String INSTABILITY_KEY =
            "theflood_volatile_instability";

    /*
     * Amount of the Elite's max health that must be
     * dealt before it releases a shockwave.
     *
     * Higher levels trigger more often.
     */
    private static final float LEVEL_ONE_THRESHOLD =
            0.60F;

    private static final float LEVEL_TWO_THRESHOLD =
            0.50F;

    private static final float LEVEL_THREE_THRESHOLD =
            0.40F;

    private static final double LEVEL_ONE_RADIUS =
            4.0D;

    private static final double LEVEL_TWO_RADIUS =
            5.0D;

    private static final double LEVEL_THREE_RADIUS =
            6.0D;

    private static final float LEVEL_ONE_DAMAGE =
            4.0F;

    private static final float LEVEL_TWO_DAMAGE =
            6.0F;

    private static final float LEVEL_THREE_DAMAGE =
            8.0F;

    private static final double KNOCKBACK_STRENGTH =
            0.75D;

    private static final String COOLDOWN_END_KEY =
            "theflood_volatile_cooldown_end";

    private static final int DETONATION_COOLDOWN_TICKS =
            60; // 3 seconds

    @Override
    public String id() {
        return "volatile";
    }

    @Override
    public String displayName() {
        return "Volatile";
    }

    @Override
    public void onDamaged(
            Mob elite,
            DamageSource source,
            float damageTaken,
            int level
    ) {
        if (
                damageTaken <= 0.0F
                || !elite.isAlive()
        ) {
            return;
        }

        if (
                !(elite.level() instanceof ServerLevel serverLevel)
        ) {
            return;
        }

        long gameTime =
                elite.level()
                        .getGameTime();

        long cooldownEnd =
                elite.getPersistentData()
                        .getLong(
                                COOLDOWN_END_KEY
                        );

        if (
                cooldownEnd > 0L
                && gameTime < cooldownEnd
        ) {
            return;
        }

        float instability =
                elite.getPersistentData()
                        .getFloat(
                                INSTABILITY_KEY
                        );

        instability +=
                damageTaken;

        float threshold =
                elite.getMaxHealth()
                        * getThresholdPercent(
                                level
                        );

        if (
                instability
                        < threshold
        ) {
            elite.getPersistentData()
                    .putFloat(
                            INSTABILITY_KEY,
                            instability
                    );

            return;
        }

        /*
         * Reset BEFORE detonating so damage caused by
         * the shockwave can't accidentally recurse.
         */
        elite.getPersistentData()
                .putFloat(
                        INSTABILITY_KEY,
                        0.0F
                );

        elite.getPersistentData()
                .putLong(
                        COOLDOWN_END_KEY,
                        gameTime
                                + DETONATION_COOLDOWN_TICKS
                );

        detonate(
                serverLevel,
                elite,
                level
        );
    }

    private static void detonate(
            ServerLevel level,
            Mob elite,
            int attributeLevel
    ) {
        double radius =
                getRadius(
                        attributeLevel
                );

        float damage =
                getDamage(
                        attributeLevel
                );

        List<ServerPlayer> players =
                level.getEntitiesOfClass(
                        ServerPlayer.class,
                        elite.getBoundingBox()
                                .inflate(
                                        radius
                                ),
                        player ->
                                player.isAlive()
                                && !player.isSpectator()
                                && player.distanceToSqr(
                                        elite
                                )
                                <= radius * radius
                );

        for (ServerPlayer player : players) {
            player.hurt(
                    elite.damageSources()
                            .mobAttack(
                                    elite
                            ),
                    damage
            );

            applyKnockback(
                    elite,
                    player
            );
        }

        /*
         * Temporary visual feedback.
         *
         * We'll make this prettier during the polish pass.
         */
        level.sendParticles(
                ParticleTypes.EXPLOSION,
                elite.getX(),
                elite.getY()
                        + elite.getBbHeight()
                        * 0.5D,
                elite.getZ(),
                8,
                radius * 0.25D,
                0.5D,
                radius * 0.25D,
                0.05D
        );

        /*
         * Explosion sound/visual without destroying blocks.
         */
        level.explode(
                elite,
                elite.getX(),
                elite.getY(),
                elite.getZ(),
                0.0F,
                ServerLevel.ExplosionInteraction.NONE
        );
    }

    private static void applyKnockback(
            Mob elite,
            ServerPlayer player
    ) {
        Vec3 direction =
                player.position()
                        .subtract(
                                elite.position()
                        );

        if (
                direction.lengthSqr()
                        <= 0.0001D
        ) {
            return;
        }

        direction =
                direction.normalize();

        player.push(
                direction.x
                        * KNOCKBACK_STRENGTH,
                0.25D,
                direction.z
                        * KNOCKBACK_STRENGTH
        );

        player.hurtMarked =
                true;
    }

    private static float getThresholdPercent(
            int level
    ) {
        return switch (level) {

            case 2 ->
                    LEVEL_TWO_THRESHOLD;

            case 3 ->
                    LEVEL_THREE_THRESHOLD;

            default ->
                    LEVEL_ONE_THRESHOLD;
        };
    }

    private static double getRadius(
            int level
    ) {
        return switch (level) {

            case 2 ->
                    LEVEL_TWO_RADIUS;

            case 3 ->
                    LEVEL_THREE_RADIUS;

            default ->
                    LEVEL_ONE_RADIUS;
        };
    }

    private static float getDamage(
            int level
    ) {
        return switch (level) {

            case 2 ->
                    LEVEL_TWO_DAMAGE;

            case 3 ->
                    LEVEL_THREE_DAMAGE;

            default ->
                    LEVEL_ONE_DAMAGE;
        };
    }
}