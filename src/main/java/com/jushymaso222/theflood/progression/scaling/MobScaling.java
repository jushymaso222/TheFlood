package com.jushymaso222.theflood.progression.scaling;

import com.jushymaso222.theflood.config.TheFloodConfig;

import net.minecraft.world.entity.EntityType;

public final class MobScaling {

    private static final int MAX_VANILLA_HEAT = 100;

    private enum ProgressionRegion {
        OVERWORLD,
        NETHER,
        END
    }

    private MobScaling() {
    }

    public static TheFloodConfig.StandardMob getConfig(
            EntityType<?> entityType
    ) {
        if (entityType == EntityType.ZOMBIE) {
            return TheFloodConfig.MOBS.zombie;
        }

        if (entityType == EntityType.SKELETON) {
            return TheFloodConfig.MOBS.skeleton;
        }

        if (entityType == EntityType.SPIDER) {
            return TheFloodConfig.MOBS.spider;
        }

        if (entityType == EntityType.CREEPER) {
            return TheFloodConfig.MOBS.creeper;
        }

        if (entityType == EntityType.ENDERMAN) {
            return TheFloodConfig.MOBS.enderman;
        }

        if (entityType == EntityType.WARDEN) {
            return TheFloodConfig.MOBS.warden;
        }

        return null;
    }

    public static double getBaseHealth(
            EntityType<?> entityType
    ) {
        TheFloodConfig.StandardMob config = getConfig(entityType);

        if (config == null) {
            return 0.0D;
        }

        return config.baseHealth.get();
    }

    public static double getBaseDamage(
            EntityType<?> entityType
    ) {
        TheFloodConfig.StandardMob config = getConfig(entityType);

        if (config == null) {
            return 0.0D;
        }

        return config.baseDamage.get();
    }

    /*
     * Native Minecraft max health is deliberately separate from the
     * configured Flood effective-health target.
     *
     * For most current mobs these values happen to match baseHealth.
     * Wardens are the important exception: vanilla Wardens have 500 HP.
     * Using the configured Flood baseHealth as their native health caused
     * the damage multiplier to leave the full vanilla 500 HP pool intact.
     */
    public static double getNativeHealth(
            EntityType<?> entityType
    ) {
        if (entityType == EntityType.ZOMBIE) {
            return 20.0D;
        }

        if (entityType == EntityType.SKELETON) {
            return 20.0D;
        }

        if (entityType == EntityType.SPIDER) {
            return 16.0D;
        }

        if (entityType == EntityType.CREEPER) {
            return 20.0D;
        }

        if (entityType == EntityType.ENDERMAN) {
            return 40.0D;
        }

        if (entityType == EntityType.WARDEN) {
            return 500.0D;
        }

        return 0.0D;
    }

    /*
     * Compatibility/debug helper: normalized progress from unlock to Heat 100.
     * Effective stats no longer use this as their only curve.
     */
    public static double getVanillaHeatProgress(
            EntityType<?> entityType,
            int heat
    ) {
        TheFloodConfig.StandardMob config = getConfig(entityType);

        if (config == null) {
            return 0.0D;
        }

        int unlockHeat = config.unlockHeat.get();

        if (heat <= unlockHeat || unlockHeat >= MAX_VANILLA_HEAT) {
            return 0.0D;
        }

        return clamp01(
                (double) (heat - unlockHeat)
                        / (double) (MAX_VANILLA_HEAT - unlockHeat)
        );
    }

    /*
     * Compatibility/debug helper retaining the original single-curve view.
     * Piecewise stat scaling below uses primary and residual curves instead.
     */
    public static double getVanillaHeatCurve(
            EntityType<?> entityType,
            int heat
    ) {
        return applyCurve(
                getVanillaHeatProgress(entityType, heat),
                getPrimaryCurveExponent()
        );
    }

    public static double getEffectiveHealth(
            EntityType<?> entityType,
            int heat
    ) {
        TheFloodConfig.StandardMob config = getConfig(entityType);

        if (config == null) {
            return 0.0D;
        }

        return getPiecewiseStat(
                entityType,
                heat,
                config.baseHealth.get(),
                config.primaryVanillaHealth.get(),
                config.maximumVanillaHealth.get()
        );
    }

    public static double getEffectiveDamage(
            EntityType<?> entityType,
            int heat
    ) {
        TheFloodConfig.StandardMob config = getConfig(entityType);

        if (config == null) {
            return 0.0D;
        }

        return getPiecewiseStat(
                entityType,
                heat,
                config.baseDamage.get(),
                config.primaryVanillaDamage.get(),
                config.maximumVanillaDamage.get()
        );
    }

    /*
     * Home-region progression:
     *
     *   Overworld : Heat 0-35
     *   Nether    : Heat 35-70
     *   End       : Heat 70-100
     *
     * A mob gains most of its intended progression inside its home region.
     * After that region ends, it continues to improve through the residual
     * curve, but much more slowly. This lets old areas feel easier without
     * ever becoming completely static or harmless.
     *
     * A mob whose unlock Heat is at/after its region endpoint (currently the
     * Warden at Heat 35) skips the primary interpolation and begins residual
     * progression directly from its configured base value.
     */
    private static double getPiecewiseStat(
            EntityType<?> entityType,
            int heat,
            double baseValue,
            double primaryValue,
            double maximumValue
    ) {
        TheFloodConfig.StandardMob config = getConfig(entityType);

        if (config == null) {
            return baseValue;
        }

        int unlockHeat = config.unlockHeat.get();
        int primaryEndHeat = getPrimaryProgressionEndHeat(entityType);

        if (heat <= unlockHeat) {
            return baseValue;
        }

        if (primaryEndHeat <= unlockHeat) {
            double residualProgress = normalizedProgress(
                    unlockHeat,
                    MAX_VANILLA_HEAT,
                    heat
            );

            return interpolate(
                    baseValue,
                    maximumValue,
                    applyCurve(
                            residualProgress,
                            getResidualCurveExponent()
                    )
            );
        }

        if (heat <= primaryEndHeat) {
            double primaryProgress = normalizedProgress(
                    unlockHeat,
                    primaryEndHeat,
                    heat
            );

            return interpolate(
                    baseValue,
                    primaryValue,
                    applyCurve(
                            primaryProgress,
                            getPrimaryCurveExponent()
                    )
            );
        }

        double residualProgress = normalizedProgress(
                primaryEndHeat,
                MAX_VANILLA_HEAT,
                heat
        );

        return interpolate(
                primaryValue,
                maximumValue,
                applyCurve(
                        residualProgress,
                        getResidualCurveExponent()
                )
        );
    }

    private static ProgressionRegion getProgressionRegion(
            EntityType<?> entityType
    ) {
        /*
         * Every mob implemented in 1.9 is currently part of the Overworld
         * roster. Nether and End cases are already represented here so their
         * mobs can be added later without redesigning the scaling model.
         */
        return ProgressionRegion.OVERWORLD;
    }

    public static int getPrimaryProgressionEndHeat(
            EntityType<?> entityType
    ) {
        return switch (getProgressionRegion(entityType)) {
            case OVERWORLD -> TheFloodConfig.MOBS.overworldProgressionEndHeat.get();
            case NETHER -> TheFloodConfig.MOBS.netherProgressionEndHeat.get();
            case END -> TheFloodConfig.MOBS.endProgressionEndHeat.get();
        };
    }

    public static double getPlayerDamageMultiplier(
                EntityType<?> entityType,
                int heat
        ) {
        double actualHealth =
                getBaseHealth(entityType);

        double effectiveHealth =
                getEffectiveHealth(
                        entityType,
                        heat
                );

        if (
                actualHealth <= 0.0D
                        || effectiveHealth <= 0.0D
        ) {
                return 1.0D;
        }

        return actualHealth
                / effectiveHealth;
        }

    public static double getMobDamageMultiplier(
            EntityType<?> entityType,
            int heat
    ) {
        double baseDamage = getBaseDamage(entityType);
        double effectiveDamage = getEffectiveDamage(entityType, heat);

        if (
                baseDamage <= 0.0D
                        || effectiveDamage <= 0.0D
        ) {
            return 1.0D;
        }

        return effectiveDamage / baseDamage;
    }

    public static boolean isFloodMob(
            EntityType<?> type
    ) {
        return type == EntityType.ZOMBIE
                || type == EntityType.SKELETON
                || type == EntityType.SPIDER
                || type == EntityType.CREEPER
                || type == EntityType.ENDERMAN
                || type == EntityType.WARDEN;
    }

    private static double getPrimaryCurveExponent() {
        double exponent = TheFloodConfig.MOBS.vanillaHeatCurveExponent.get();

        if (!Double.isFinite(exponent) || exponent <= 0.0D) {
            return 2.0D;
        }

        return exponent;
    }

    private static double getResidualCurveExponent() {
        double exponent = TheFloodConfig.MOBS.residualHeatCurveExponent.get();

        if (!Double.isFinite(exponent) || exponent <= 0.0D) {
            return 2.0D;
        }

        return exponent;
    }

    private static double normalizedProgress(
            int startHeat,
            int endHeat,
            int heat
    ) {
        if (endHeat <= startHeat) {
            return heat > startHeat ? 1.0D : 0.0D;
        }

        return clamp01(
                (double) (heat - startHeat)
                        / (double) (endHeat - startHeat)
        );
    }

    private static double applyCurve(
            double progress,
            double exponent
    ) {
        return clamp01(
                Math.pow(
                        clamp01(progress),
                        exponent
                )
        );
    }

    private static double interpolate(
            double start,
            double end,
            double progress
    ) {
        if (
                !Double.isFinite(start)
                        || !Double.isFinite(end)
                        || !Double.isFinite(progress)
        ) {
            return start;
        }

        progress = clamp01(progress);

        return start + (end - start) * progress;
    }

    private static double clamp01(
            double value
    ) {
        if (!Double.isFinite(value)) {
            return 0.0D;
        }

        return Math.max(
                0.0D,
                Math.min(1.0D, value)
        );
    }
}
