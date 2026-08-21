package com.jushymaso222.theflood.elite.debug.client;

import java.util.List;

public final class ClientMobStatsData {

    private static MobStats current;

    private ClientMobStatsData() {
    }

    public static void set(
            MobStats stats
    ) {
        current =
                stats;
    }

    public static MobStats get() {
        return current;
    }

    public static void clear() {
        current =
                null;
    }

    public record MobStats(
            int entityId,

            String entityName,
            String entityType,

            boolean elite,

            String mutation,
            int heat,
            List<String> attributes,

            float health,
            float maxHealth,

            double baseAttackDamage,
            double attackDamage,

            double baseArmor,
            double armor,

            double baseArmorToughness,
            double armorToughness,

            double baseAttackKnockback,
            double attackKnockback,

            double baseKnockbackResistance,
            double knockbackResistance,

            double baseMovementSpeed,
            double movementSpeed,

            double baseFollowRange,
            double followRange,

            String targetName,

            boolean noAi,
            boolean invulnerable,
            boolean onGround,
            boolean onFire,

            double x,
            double y,
            double z
    ) {
    }
}