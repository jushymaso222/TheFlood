package com.jushymaso222.theflood.progression.capability.sensor;

import com.jushymaso222.theflood.progression.capability.CapabilityManager;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public final class DefenseCapabilitySensor {

    private DefenseCapabilitySensor() {
    }


    /*
     * ============================================
     * PENDING DAMAGE OBSERVATIONS
     * ============================================
     *
     * Flood combat scaling occurs before Minecraft
     * finishes processing the player's defensive
     * systems.
     *
     * We remember:
     *
     * - the original incoming damage
     * - how much The Flood amplified that damage
     *
     * LivingDamageEvent later gives us the amount
     * that actually reached the player.
     */

    private static final Map<UUID, PendingHit> PENDING_HITS =
            new HashMap<>();


    /*
     * ============================================
     * BEGIN OBSERVATION
     * ============================================
     */

    public static void beginObservation(
            ServerPlayer player,
            double rawDamage,
            double floodMultiplier
    ) {
        if (
                player == null
                        || !Double.isFinite(rawDamage)
                        || rawDamage <= 0.0D
                        || !Double.isFinite(floodMultiplier)
                        || floodMultiplier <= 0.0D
        ) {
            return;
        }


        PENDING_HITS.put(
                player.getUUID(),
                new PendingHit(
                        rawDamage,
                        floodMultiplier,
                        player.tickCount
                )
        );
    }


    /*
     * ============================================
     * FINAL DAMAGE
     * ============================================
     *
     * Called from LivingDamageEvent after the
     * player's defensive systems have processed
     * the Flood-amplified attack.
     */

    public static void observeFinalDamage(
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


        PendingHit hit =
                PENDING_HITS.remove(
                        player.getUUID()
                );

        if (hit == null) {
            return;
        }


        /*
         * Prevent an unrelated later hit from being
         * paired with an old pending observation.
         */

        if (
                player.tickCount
                        - hit.tick()
                        > 1
        ) {
            return;
        }


        double rawDamage =
                hit.rawDamage();

        double floodMultiplier =
                hit.floodMultiplier();


        /*
         * ============================================
         * REMOVE FLOOD PRESSURE
         * ============================================
         *
         * The Flood's own Heat / swarm / capability
         * scaling must not make the player's armor
         * appear weaker than it actually is.
         *
         * This normalization affects the SENSOR ONLY.
         * Actual gameplay damage remains untouched.
         */

        double normalizedFinalDamage =
                finalDamage
                        / floodMultiplier;


        /*
         * ============================================
         * MITIGATION
         * ============================================
         */

        double mitigation =
                1.0D
                        - (
                                normalizedFinalDamage
                                        / rawDamage
                        );

        mitigation =
                clamp01(
                        mitigation
                );


        /*
         * Defense describes the percentage of the
         * original attack effectively prevented by
         * the player's defensive systems.
         */

        double observation =
                mitigation
                        * 100.0D;


        /*
         * ============================================
         * OBSERVATION WEIGHT
         * ============================================
         *
         * Weak attacks still tell us something about
         * the player's defenses, but they should not
         * carry the same authority as dangerous hits.
         *
         * IMPORTANT:
         *
         * Weight uses ORIGINAL damage, not the
         * Flood-amplified damage. Heat must not make
         * an otherwise weak attack into stronger
         * capability evidence.
         */

        double weight =
                clamp01(
                        rawDamage
                                / 20.0D
                );


        CapabilityManager.recordDefenseObservation(
                player,
                observation,
                weight
        );
    }


    /*
     * ============================================
     * CLEANUP
     * ============================================
     */

    public static void remove(
            ServerPlayer player
    ) {
        if (player == null) {
            return;
        }

        PENDING_HITS.remove(
                player.getUUID()
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


    /*
     * ============================================
     * PENDING HIT
     * ============================================
     */

    private record PendingHit(
            double rawDamage,
            double floodMultiplier,
            int tick
    ) {
    }
}