package com.jushymaso222.theflood.progression.capability.sensor;

import com.jushymaso222.theflood.progression.capability.CapabilityManager;

import net.minecraft.server.level.ServerPlayer;


public final class DefenseCapabilitySensor {

    private DefenseCapabilitySensor() {
    }


    /*
     * ============================================
     * POST-MITIGATION DAMAGE
     * ============================================
     *
     * Defense is based on how much of the player's
     * health pool actually gets removed by a hit
     * AFTER their defensive systems have done
     * everything they can.
     *
     * We deliberately do NOT care about raw incoming
     * damage here.
     *
     * Armor, enchantments, resistance, modded armor,
     * shields, etc. are all naturally accounted for
     * by the final damage that gets through.
     */

    public static void observeDamage(
            ServerPlayer player,
            double finalDamage
    ) {
        if (
                player == null
                || !Double.isFinite(finalDamage)
                || finalDamage < 0.0D
        ) {
            return;
        }


        double maxHealth =
                player.getMaxHealth();

        if (
                !Double.isFinite(maxHealth)
                || maxHealth <= 0.0D
        ) {
            return;
        }


        /*
         * Fraction of the player's entire health bar
         * removed by this hit.
         *
         * Examples with 20 max health:
         *
         *  1 damage  ->  5%
         *  5 damage  -> 25%
         * 10 damage  -> 50%
         * 20 damage  ->100%
         * 60 damage  ->100% after clamping
         */

        double healthChunk =
                finalDamage
                        / maxHealth;

        healthChunk =
                clamp01(
                        healthChunk
                );


        /*
         * Convert damage received into defensive
         * capability.
         *
         * Smaller chunks = stronger Defense.
         *
         *  0% health lost -> 100 Defense
         * 10% health lost ->  90 Defense
         * 25% health lost ->  75 Defense
         * 50% health lost ->  50 Defense
         * 75% health lost ->  25 Defense
         *100% health lost ->   0 Defense
         */

        double observation =
                (
                        1.0D
                                - healthChunk
                )
                        * 100.0D;


        CapabilityManager.recordDefenseObservation(
                player,
                observation
        );
    }


    private static double clamp01(
            double value
    ) {
        if (!Double.isFinite(value)) {
            return 0.0D;
        }

        return Math.max(
                0.0D,
                Math.min(
                        1.0D,
                        value
                )
        );
    }
}