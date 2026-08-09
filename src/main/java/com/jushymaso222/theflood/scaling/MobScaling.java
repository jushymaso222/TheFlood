package com.jushymaso222.theflood.scaling;

import com.jushymaso222.theflood.config.TheFloodConfig;
import net.minecraft.world.entity.EntityType;

public final class MobScaling {

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
        TheFloodConfig.StandardMob config =
                getConfig(entityType);

        if (config == null) {
            return 0.0;
        }

        return config.baseHealth.get();
    }

    public static double getBaseDamage(
            EntityType<?> entityType
    ) {
        TheFloodConfig.StandardMob config =
                getConfig(entityType);

        if (config == null) {
            return 0.0;
        }

        return config.baseDamage.get();
    }

    public static double getEffectiveHealth(
            EntityType<?> entityType,
            int heat
    ) {
        TheFloodConfig.StandardMob config =
                getConfig(entityType);

        if (config == null) {
            return 0.0;
        }

        int heatSinceUnlock = Math.max(
                0,
                heat - config.unlockHeat.get()
        );

        return Math.min(
                config.maximumHealth.get(),
                config.baseHealth.get()
                        + heatSinceUnlock
                        * config.healthPerDay.get()
        );
    }

    public static double getEffectiveDamage(
            EntityType<?> entityType,
            int heat
    ) {
        TheFloodConfig.StandardMob config =
                getConfig(entityType);

        if (config == null) {
            return 0.0;
        }

        int heatSinceUnlock = Math.max(
                0,
                heat - config.unlockHeat.get()
        );

        return Math.min(
                config.maximumDamage.get(),
                config.baseDamage.get()
                        + heatSinceUnlock
                        * config.damagePerDay.get()
        );
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
                actualHealth <= 0.0
                || effectiveHealth <= 0.0
        ) {
            return 1.0;
        }

        /*
         * Example:
         *
         * Actual HP    = 20
         * Effective HP = 40
         *
         * 20 / 40 = 0.5
         *
         * Player therefore deals half normal damage.
         */
        return actualHealth / effectiveHealth;
    }

    public static double getMobDamageMultiplier(
            EntityType<?> entityType,
            int heat
    ) {
        double baseDamage =
                getBaseDamage(entityType);

        double effectiveDamage =
                getEffectiveDamage(
                        entityType,
                        heat
                );

        if (
                baseDamage <= 0.0
                || effectiveDamage <= 0.0
        ) {
            return 1.0;
        }

        /*
         * Example:
         *
         * Base damage      = 3
         * Effective damage = 6
         *
         * 6 / 3 = 2
         *
         * Mob therefore deals twice normal damage
         * to this particular player.
         */
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
}