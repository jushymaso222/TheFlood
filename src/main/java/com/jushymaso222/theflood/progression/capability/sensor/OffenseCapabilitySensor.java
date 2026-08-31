package com.jushymaso222.theflood.progression.capability.sensor;

import com.jushymaso222.theflood.progression.capability.CapabilityManager;

import net.minecraft.server.level.ServerPlayer;


public final class OffenseCapabilitySensor {

    private OffenseCapabilitySensor() {
    }


    /*
     * ============================================
     * DAMAGE OUTPUT
     * ============================================
     *
     * Measures the actual offensive output
     * demonstrated by the player.
     *
     * We intentionally observe real damage rather
     * than inspecting weapons, enchantments, mods,
     * attributes, etc.
     *
     * If an unknown mod gives the player an absurdly
     * powerful weapon, The Flood doesn't need to
     * understand the weapon.
     *
     * It only needs to understand what the player
     * actually did with it.
     */

    public static void observeDamage(
            ServerPlayer player,
            double damage
    ) {
        if (
                player == null
                || damage <= 0.0D
                || !Double.isFinite(damage)
        ) {
            return;
        }


        /*
         * Convert demonstrated damage into a
         * normalized 0-100 capability observation.
         *
         * Logarithmic scaling prevents extremely
         * large modded damage values from completely
         * destroying the scale.
         *
         * This curve is intentionally centralized
         * inside the sensor because it describes
         * what the raw observation MEANS.
         *
         * Learning from that observation belongs
         * to CapabilityManager.
         */

        double observation =
                damageToOffenseScore(
                        damage
                );


        CapabilityManager.recordOffenseObservation(
                player,
                damage,
                observation
        );
    }


    /*
     * ============================================
     * DAMAGE -> CAPABILITY
     * ============================================
     *
     * Initial tuning curve.
     *
     * We expect to adjust this once we have real
     * gameplay telemetry from vanilla and modded
     * combat.
     */

    private static double damageToOffenseScore(
            double damage
    ) {
        double score =
                25.0D
                        * (
                        Math.log1p(
                                damage
                        )
                                / Math.log(
                                11.0D
                        )
                );

        return Math.max(
                0.0D,
                Math.min(
                        100.0D,
                        score
                )
        );
    }
}