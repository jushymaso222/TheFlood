package com.jushymaso222.theflood.elite.attributes.specials;

import com.jushymaso222.theflood.elite.attributes.SpecialAttribute;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class RadioactiveAttribute
        implements SpecialAttribute {

    /*
     * =====================================================
     * BALANCE
     * =====================================================
     */

    private static final double LEVEL_ONE_RADIUS =
            5.0D;

    private static final double LEVEL_TWO_RADIUS =
            6.0D;

    private static final double LEVEL_THREE_RADIUS =
            7.0D;


    /*
     * Exposure is measured from 0 -> 100.
     *
     * Damage begins once the player reaches
     * DAMAGE_THRESHOLD.
     */

    private static final float MAX_EXPOSURE =
            100.0F;

    private static final float DAMAGE_THRESHOLD =
            50.0F;


    /*
     * Exposure gained PER TICK.
     *
     * At 20 ticks/sec:
     *
     * Radioactive    -> ~5 sec before damage
     * Radioactive+   -> ~4 sec
     * Radioactive++  -> ~3.3 sec
     */

    private static final float LEVEL_ONE_EXPOSURE_RATE =
            0.50F;

    private static final float LEVEL_TWO_EXPOSURE_RATE =
            0.625F;

    private static final float LEVEL_THREE_EXPOSURE_RATE =
            0.75F;


    /*
     * Exposure disappears fairly quickly once the
     * player escapes.
     */

    private static final float EXPOSURE_DECAY_RATE =
            1.0F;


    /*
     * Once dangerous exposure is reached, damage
     * occurs once every second.
     */

    private static final int DAMAGE_INTERVAL =
            20;


    /*
     * Particle ring refresh rate.
     */

    private static final int PARTICLE_INTERVAL =
            5;

    private static final int PARTICLE_COUNT =
            32;


    /*
     * =====================================================
     * PLAYER EXPOSURE
     * =====================================================
     *
     * Mob UUID
     *      ↓
     * Player UUID -> exposure
     *
     * Keeping the Elite UUID in here prevents two
     * Radioactive Elites from accidentally sharing
     * the same exposure meter.
     */

    private static final Map<UUID, Map<UUID, Float>> EXPOSURE =
            new HashMap<>();


    @Override
    public String id() {
        return "radioactive";
    }


    @Override
    public String displayName() {
        return "Radioactive";
    }


    @Override
    public void tick(
            Mob elite,
            int level
    ) {
        if (
                elite.level()
                        .isClientSide()
                || !elite.isAlive()
        ) {
            return;
        }

        if (
                !(elite.level() instanceof ServerLevel serverLevel)
        ) {
            return;
        }

        double radius =
                getRadius(
                        level
                );

        /*
         * Draw the simple debug/gameplay ring.
         */
        if (
                elite.tickCount
                        % PARTICLE_INTERVAL
                        == 0
        ) {
            spawnRadiusRing(
                    serverLevel,
                    elite,
                    radius
            );
        }


        /*
         * Get this Elite's exposure table.
         */
        Map<UUID, Float> playerExposure =
                EXPOSURE.computeIfAbsent(
                        elite.getUUID(),
                        ignored ->
                                new HashMap<>()
                );


        /*
         * Search slightly beyond the actual radius.
         *
         * This lets us continue decaying players who
         * have just stepped outside the radioactive
         * zone.
         */
        AABB searchArea =
                elite.getBoundingBox()
                        .inflate(
                                radius + 8.0D
                        );

        List<ServerPlayer> nearbyPlayers =
                serverLevel.getEntitiesOfClass(
                        ServerPlayer.class,
                        searchArea,
                        player ->
                                player.isAlive()
                                && !player.isSpectator()
                );


        /*
         * Process nearby players.
         */
        for (ServerPlayer player : nearbyPlayers) {

            double distanceSquared =
                    player.distanceToSqr(
                            elite
                    );

            boolean inside =
                    distanceSquared
                            <= radius * radius;

            if (inside) {
                increaseExposure(
                        elite,
                        player,
                        playerExposure,
                        level
                );
            } else {
                decreaseExposure(
                        player,
                        playerExposure
                );
            }
        }


        /*
         * Players can move completely outside our
         * search box while still having exposure.
         *
         * Those entries must continue decaying too.
         */
        playerExposure.replaceAll(
                (playerId, exposure) -> {

                    ServerPlayer player =
                            serverLevel.getServer()
                                    .getPlayerList()
                                    .getPlayer(
                                            playerId
                                    );

                    if (
                            player == null
                            || player.level()
                                    != serverLevel
                    ) {
                        return Math.max(
                                0.0F,
                                exposure
                                        - EXPOSURE_DECAY_RATE
                        );
                    }

                    /*
                     * Already processed nearby players.
                     */
                    if (
                            nearbyPlayers.contains(
                                    player
                            )
                    ) {
                        return exposure;
                    }

                    return Math.max(
                            0.0F,
                            exposure
                                    - EXPOSURE_DECAY_RATE
                    );
                }
        );


        /*
         * Remove completely cleared exposure entries.
         */
        playerExposure.entrySet()
                .removeIf(
                        entry ->
                                entry.getValue()
                                        <= 0.0F
                );

        if (playerExposure.isEmpty()) {
            EXPOSURE.remove(
                    elite.getUUID()
            );
        }
    }


    /*
     * =====================================================
     * EXPOSURE
     * =====================================================
     */

    private static void increaseExposure(
            Mob elite,
            ServerPlayer player,
            Map<UUID, Float> exposureMap,
            int level
    ) {
        float current =
                exposureMap.getOrDefault(
                        player.getUUID(),
                        0.0F
                );

        float exposureRate =
                getExposureRate(
                        level
                );

        float next =
                Math.min(
                        MAX_EXPOSURE,
                        current
                                + exposureRate
                );

        exposureMap.put(
                player.getUUID(),
                next
        );


        /*
         * Radiation isn't immediately harmful.
         *
         * The player gets time to realize:
         *
         * "I probably shouldn't be standing here."
         */
        if (
                next
                        < DAMAGE_THRESHOLD
        ) {
            return;
        }


        /*
         * Damage once per second.
         */
        if (
                elite.tickCount
                        % DAMAGE_INTERVAL
                        != 0
        ) {
            return;
        }


        float damage =
                getRadiationDamage(
                        next,
                        level
                );

        player.hurt(
                elite.damageSources()
                        .magic(),
                damage
        );
    }


    private static void decreaseExposure(
            ServerPlayer player,
            Map<UUID, Float> exposureMap
    ) {
        float current =
                exposureMap.getOrDefault(
                        player.getUUID(),
                        0.0F
                );

        if (current <= 0.0F) {
            return;
        }

        float next =
                Math.max(
                        0.0F,
                        current
                                - EXPOSURE_DECAY_RATE
                );

        if (next <= 0.0F) {
            exposureMap.remove(
                    player.getUUID()
            );
        } else {
            exposureMap.put(
                    player.getUUID(),
                    next
            );
        }
    }


    /*
     * =====================================================
     * DAMAGE
     * =====================================================
     */

    private static float getRadiationDamage(
            float exposure,
            int level
    ) {
        /*
         * 0 at threshold
         * 1 at maximum exposure.
         */
        float danger =
                (
                        exposure
                                - DAMAGE_THRESHOLD
                )
                        /
                        (
                                MAX_EXPOSURE
                                        - DAMAGE_THRESHOLD
                        );

        danger =
                Math.max(
                        0.0F,
                        Math.min(
                                1.0F,
                                danger
                        )
                );


        /*
         * Damage grows the longer the player refuses
         * to leave.
         *
         * At maximum exposure:
         *
         * Radioactive    -> 2 HP/sec
         * Radioactive+   -> 3 HP/sec
         * Radioactive++  -> 4 HP/sec
         */

        float maxDamage =
                switch (level) {

                    case 2 ->
                            3.0F;

                    case 3 ->
                            4.0F;

                    default ->
                            2.0F;
                };

        /*
         * Minimum tick damage once the dangerous
         * threshold has been crossed.
         */
        return 0.5F
                + danger
                * (
                        maxDamage
                                - 0.5F
                );
    }


    /*
     * =====================================================
     * PARTICLE RING
     * =====================================================
     */

    private static void spawnRadiusRing(
            ServerLevel level,
            Mob elite,
            double radius
    ) {
        /*
         * Bright radioactive green.
         */
        DustParticleOptions particle =
                new DustParticleOptions(
                        new Vector3f(
                                0.2F,
                                1.0F,
                                0.2F
                        ),
                        1.0F
                );

        double centerX =
                elite.getX();

        double centerY =
                elite.getY()
                        + 0.05D;

        double centerZ =
                elite.getZ();


        for (
                int i = 0;
                i < PARTICLE_COUNT;
                i++
        ) {
            double angle =
                    (
                            Math.PI
                                    * 2.0D
                    )
                            * i
                            / PARTICLE_COUNT;

            double x =
                    centerX
                            + Math.cos(
                            angle
                    )
                            * radius;

            double z =
                    centerZ
                            + Math.sin(
                            angle
                    )
                            * radius;

            level.sendParticles(
                    particle,
                    x,
                    centerY,
                    z,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }


    /*
     * =====================================================
     * LEVEL SCALING
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


    private static float getExposureRate(
            int level
    ) {
        return switch (level) {

            case 2 ->
                    LEVEL_TWO_EXPOSURE_RATE;

            case 3 ->
                    LEVEL_THREE_EXPOSURE_RATE;

            default ->
                    LEVEL_ONE_EXPOSURE_RATE;
        };
    }
}