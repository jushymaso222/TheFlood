package com.jushymaso222.theflood.scaling;

import com.jushymaso222.theflood.config.TheFloodConfig;
import net.minecraft.world.entity.EntityType;

public final class MobScaling {

    private MobScaling() {
    }

    public static TheFloodConfig.StandardMob getConfig(EntityType<?> entityType) {
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

    public static double getBaseHealth(EntityType<?> entityType) {
        TheFloodConfig.StandardMob config = getConfig(entityType);

        if (config == null) {
            return 0.0;
        }

        return config.baseHealth.get();
    }

    public static double getBaseDamage(EntityType<?> entityType) {
        TheFloodConfig.StandardMob config = getConfig(entityType);

        if (config == null) {
            return 0.0;
        }

        return config.baseDamage.get();
    }

    public static double getEffectiveHealth(
            EntityType<?> entityType,
            int progressionDay
    ) {
        TheFloodConfig.StandardMob config = getConfig(entityType);

        if (config == null) {
            return 0.0;
        }

        int daysSinceUnlock = Math.max(
                0,
                progressionDay - config.unlockDay.get()
        );

        return Math.min(
                config.maximumHealth.get(),
                config.baseHealth.get()
                        + daysSinceUnlock * config.healthPerDay.get()
        );
    }

    public static double getEffectiveDamage(
            EntityType<?> entityType,
            int progressionDay
    ) {
        TheFloodConfig.StandardMob config = getConfig(entityType);

        if (config == null) {
            return 0.0;
        }

        int daysSinceUnlock = Math.max(
                0,
                progressionDay - config.unlockDay.get()
        );

        return Math.min(
                config.maximumDamage.get(),
                config.baseDamage.get()
                        + daysSinceUnlock * config.damagePerDay.get()
        );
    }

    public static double getPlayerDamageMultiplier(
            EntityType<?> entityType,
            int progressionDay
    ) {
        double actualHealth = getBaseHealth(entityType);
        double effectiveHealth = getEffectiveHealth(
                entityType,
                progressionDay
        );

        if (actualHealth <= 0.0 || effectiveHealth <= 0.0) {
            return 1.0;
        }

        return actualHealth / effectiveHealth;
    }

    public static double getMobDamageMultiplier(
            EntityType<?> entityType,
            int progressionDay
    ) {
        double baseDamage = getBaseDamage(entityType);
        double effectiveDamage = getEffectiveDamage(
                entityType,
                progressionDay
        );

        if (baseDamage <= 0.0 || effectiveDamage <= 0.0) {
            return 1.0;
        }

        return effectiveDamage / baseDamage;
    }
}