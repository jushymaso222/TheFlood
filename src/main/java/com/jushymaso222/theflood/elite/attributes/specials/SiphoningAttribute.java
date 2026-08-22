package com.jushymaso222.theflood.elite.attributes.specials;

import com.jushymaso222.theflood.elite.EliteData;
import com.jushymaso222.theflood.elite.attributes.SpecialAttribute;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;

import java.util.Comparator;
import java.util.List;

public final class SiphoningAttribute
        implements SpecialAttribute {

    private static final String NEXT_SIPHON_TIME_KEY =
            "theflood_siphoning_next_time";

    /*
     * =====================================================
     * BALANCE
     * =====================================================
     */

    private static final double LEVEL_ONE_RADIUS =
            8.0D;

    private static final double LEVEL_TWO_RADIUS =
            10.0D;

    private static final double LEVEL_THREE_RADIUS =
            12.0D;


    private static final int LEVEL_ONE_INTERVAL =
            80; // 4 sec

    private static final int LEVEL_TWO_INTERVAL =
            70; // 3.5 sec

    private static final int LEVEL_THREE_INTERVAL =
            60; // 3 sec


    private static final float LEVEL_ONE_DRAIN_DAMAGE =
            6.0F;

    private static final float LEVEL_TWO_DRAIN_DAMAGE =
            8.0F;

    private static final float LEVEL_THREE_DRAIN_DAMAGE =
            10.0F;


    private static final float LEVEL_ONE_HEAL_PERCENT =
            0.50F;

    private static final float LEVEL_TWO_HEAL_PERCENT =
            0.60F;

    private static final float LEVEL_THREE_HEAL_PERCENT =
            0.70F;


    @Override
    public String id() {
        return "siphoning";
    }


    @Override
    public String displayName() {
        return "Siphoning";
    }


    @Override
    public void tick(
            Mob elite,
            int level
    ) {
        if (
                elite.level().isClientSide()
                || !elite.isAlive()
        ) {
            return;
        }

        /*
         * Don't consume allies if we don't actually
         * need healing.
         */
        if (
                elite.getHealth()
                        >= elite.getMaxHealth()
        ) {
            return;
        }

        if (
                !(elite.level() instanceof ServerLevel serverLevel)
        ) {
            return;
        }

        long gameTime =
                serverLevel.getGameTime();

        long nextSiphon =
                elite.getPersistentData()
                        .getLong(
                                NEXT_SIPHON_TIME_KEY
                        );

        if (
                nextSiphon > 0L
                && gameTime < nextSiphon
        ) {
            return;
        }

        Mob donor =
                findDonor(
                        serverLevel,
                        elite,
                        level
                );

        /*
         * No food nearby.
         *
         * Don't consume the cooldown so the Elite can
         * immediately siphon if an ally enters range.
         */
        if (donor == null) {
            return;
        }

        siphon(
                serverLevel,
                elite,
                donor,
                level
        );

        elite.getPersistentData()
                .putLong(
                        NEXT_SIPHON_TIME_KEY,
                        gameTime
                                + getInterval(
                                        level
                                )
                );
    }


    /*
     * =====================================================
     * TARGET SELECTION
     * =====================================================
     */

    private static Mob findDonor(
            ServerLevel level,
            Mob elite,
            int attributeLevel
    ) {
        double radius =
                getRadius(
                        attributeLevel
                );

        List<Mob> candidates =
                level.getEntitiesOfClass(
                        Mob.class,
                        elite.getBoundingBox()
                                .inflate(
                                        radius
                                ),
                        mob ->
                                isValidDonor(
                                        elite,
                                        mob,
                                        radius
                                )
                );

        if (candidates.isEmpty()) {
            return null;
        }

        /*
         * Consume the closest available Flood mob.
         */
        return candidates.stream()
                .min(
                        Comparator.comparingDouble(
                                elite::distanceToSqr
                        )
                )
                .orElse(
                        null
                );
    }


    private static boolean isValidDonor(
            Mob elite,
            Mob donor,
            double radius
    ) {
        if (
                donor == elite
                || !donor.isAlive()
        ) {
            return false;
        }

        if (
                elite.distanceToSqr(
                        donor
                )
                        > radius * radius
        ) {
            return false;
        }

        /*
         * Don't siphon other Elites.
         *
         * This avoids destroying valuable encounters
         * and prevents weird Elite-on-Elite chains.
         */
        if (
                EliteData.isElite(
                        donor
                )
        ) {
            return false;
        }

        /*
         * IMPORTANT:
         *
         * Replace this with the SAME check your mod
         * already uses to identify Flood-controlled mobs.
         *
         * Example if you use persistent data:
         *
         * return donor.getPersistentData()
         *         .getBoolean("theflood_controlled");
         *
         * Or call your existing helper instead.
         */

        return isFloodControlled(
                donor
        );
    }


    /*
     * =====================================================
     * SIPHON
     * =====================================================
     */

    private static void siphon(
            ServerLevel level,
            Mob elite,
            Mob donor,
            int attributeLevel
    ) {
        float drainDamage =
                getDrainDamage(
                        attributeLevel
                );

        /*
         * Don't heal from damage that exceeds the
         * donor's remaining health.
         *
         * If donor has 3 HP left and our drain is 10,
         * we only actually consumed 3 HP worth of life.
         */
        float actualDrain =
                Math.min(
                        donor.getHealth(),
                        drainDamage
                );

        if (actualDrain <= 0.0F) {
            return;
        }

        donor.hurt(
                elite.damageSources()
                        .magic(),
                drainDamage
        );

        float healing =
                actualDrain
                        * getHealPercent(
                                attributeLevel
                        );

        elite.heal(
                healing
        );

        spawnTemporaryParticles(
                level,
                elite,
                donor
        );
    }


    /*
     * =====================================================
     * TEMP VISUAL
     * =====================================================
     *
     * Just enough feedback to understand what's happening.
     * We can make an actual drain stream during polish.
     */

    private static void spawnTemporaryParticles(
            ServerLevel level,
            Mob elite,
            Mob donor
    ) {
        level.sendParticles(
                ParticleTypes.SOUL,
                donor.getX(),
                donor.getY()
                        + donor.getBbHeight()
                        * 0.5D,
                donor.getZ(),
                12,
                0.3D,
                0.5D,
                0.3D,
                0.02D
        );

        level.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                elite.getX(),
                elite.getY()
                        + elite.getBbHeight()
                        * 0.5D,
                elite.getZ(),
                6,
                0.25D,
                0.4D,
                0.25D,
                0.02D
        );
    }


    /*
     * =====================================================
     * LEVEL VALUES
     * =====================================================
     */

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


    private static int getInterval(
            int level
    ) {
        return switch (level) {

            case 2 ->
                    LEVEL_TWO_INTERVAL;

            case 3 ->
                    LEVEL_THREE_INTERVAL;

            default ->
                    LEVEL_ONE_INTERVAL;
        };
    }


    private static float getDrainDamage(
            int level
    ) {
        return switch (level) {

            case 2 ->
                    LEVEL_TWO_DRAIN_DAMAGE;

            case 3 ->
                    LEVEL_THREE_DRAIN_DAMAGE;

            default ->
                    LEVEL_ONE_DRAIN_DAMAGE;
        };
    }


    private static float getHealPercent(
            int level
    ) {
        return switch (level) {

            case 2 ->
                    LEVEL_TWO_HEAL_PERCENT;

            case 3 ->
                    LEVEL_THREE_HEAL_PERCENT;

            default ->
                    LEVEL_ONE_HEAL_PERCENT;
        };
    }


    /*
     * =====================================================
     * FLOOD CHECK
     * =====================================================
     *
     * REPLACE THIS BODY with your existing Flood-controlled
     * helper/tag check.
     */

    private static boolean isFloodControlled(
            Mob mob
    ) {
        return mob.getPersistentData()
                .getBoolean(
                        "theflood_controlled"
                );
    }
}