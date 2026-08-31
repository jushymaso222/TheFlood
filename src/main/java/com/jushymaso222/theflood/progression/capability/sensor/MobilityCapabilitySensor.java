package com.jushymaso222.theflood.progression.capability.sensor;

import com.jushymaso222.theflood.progression.capability.CapabilityManager;
import com.jushymaso222.theflood.progression.capability.CapabilityProfile;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public final class MobilityCapabilitySensor {

    /*
     * Sample twice per second.
     */

    private static final int SAMPLE_INTERVAL_TICKS =
            10;


    /*
     * Runtime-only state.
     *
     * Nothing here needs to survive a restart.
     */

    private static final Map<UUID, SampleState> STATES =
            new HashMap<>();


    private MobilityCapabilitySensor() {
    }


    public static void tick(
            ServerPlayer player
    ) {
        if (player == null) {
            return;
        }

        UUID playerId =
                player.getUUID();

        SampleState state =
                STATES.computeIfAbsent(
                        playerId,
                        ignored ->
                                new SampleState(
                                        player.getX(),
                                        player.getY(),
                                        player.getZ(),
                                        player.tickCount
                                )
                );

        int elapsedTicks =
                player.tickCount
                        - state.lastSampleTick;

        if (elapsedTicks < SAMPLE_INTERVAL_TICKS) {
            return;
        }


        /*
         * Teleports should not teach the Flood that
         * the player can physically move hundreds of
         * blocks per second.
         */

        double dx =
                player.getX()
                        - state.x;

        double dy =
                player.getY()
                        - state.y;

        double dz =
                player.getZ()
                        - state.z;

        double horizontalDistance =
                Math.sqrt(
                        dx * dx
                                + dz * dz
                );

        double totalDistance =
                Math.sqrt(
                        dx * dx
                                + dy * dy
                                + dz * dz
                );


        /*
         * Update the baseline before returning from
         * any filtering below.
         */

        state.x =
                player.getX();

        state.y =
                player.getY();

        state.z =
                player.getZ();

        state.lastSampleTick =
                player.tickCount;


        /*
         * Extremely large displacement in half a
         * second is almost certainly teleportation.
         */

        if (totalDistance > 40.0D) {
            return;
        }


        double seconds =
                elapsedTicks
                        / 20.0D;

        if (seconds <= 0.0D) {
            return;
        }

        double horizontalSpeed =
                horizontalDistance
                        / seconds;

        double verticalSpeed =
                Math.abs(dy)
                        / seconds;


        /*
         * Ignore standing still / tiny movement.
         */

        if (
                horizontalSpeed < 0.5D
                && verticalSpeed < 0.5D
        ) {
            return;
        }


        /*
         * Vanilla walking is roughly a low-level
         * mobility observation.
         *
         * Faster sprinting, flight, grapples,
         * vehicles, movement gear, etc. naturally
         * move farther up the scale.
         *
         * Log scaling keeps absurd modded movement
         * from destroying the range.
         */

        double horizontalObservation =
                speedToScore(
                        horizontalSpeed
                );

        double verticalObservation =
                speedToScore(
                        verticalSpeed
                                * 1.5D
                );


        /*
         * Use whichever demonstrated movement axis
         * is more impressive.
         */

        double observation =
                Math.max(
                        horizontalObservation,
                        verticalObservation
                );

        CapabilityProfile profile =
                CapabilityManager.get(
                        player
                );

        if (profile == null) {
            return;
        }

        CapabilityManager.recordMobilityObservation(
                player,
                observation
        );
    }


    public static void remove(
            ServerPlayer player
    ) {
        if (player == null) {
            return;
        }

        STATES.remove(
                player.getUUID()
        );
    }


    private static double speedToScore(
            double blocksPerSecond
    ) {
        if (
                !Double.isFinite(blocksPerSecond)
                || blocksPerSecond <= 0.0D
        ) {
            return 0.0D;
        }

        double score =
                35.0D
                        * (
                        Math.log1p(
                                blocksPerSecond
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


    private static final class SampleState {

        private double x;
        private double y;
        private double z;

        private int lastSampleTick;


        private SampleState(
                double x,
                double y,
                double z,
                int lastSampleTick
        ) {
            this.x =
                    x;

            this.y =
                    y;

            this.z =
                    z;

            this.lastSampleTick =
                    lastSampleTick;
        }
    }
}