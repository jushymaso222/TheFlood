package com.jushymaso222.theflood.elite.presentation;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import net.minecraft.core.particles.ParticleTypes;

public final class EliteVisuals {

    private EliteVisuals() {
    }


    /*
     * =====================================================
     * BASIC BURST
     * =====================================================
     */

    public static void burst(
            Mob elite,
            ParticleOptions particle,
            int count,
            double spread,
            double speed
    ) {
        if (
                !(elite.level() instanceof ServerLevel level)
        ) {
            return;
        }

        level.sendParticles(
                particle,
                elite.getX(),
                elite.getY()
                        + elite.getBbHeight()
                        * 0.5D,
                elite.getZ(),
                count,
                spread,
                spread,
                spread,
                speed
        );
    }


    public static void headBurst(
            Mob elite,
            ParticleOptions particle,
            int count,
            double spread,
            double speed
    ) {
        if (
                !(elite.level() instanceof ServerLevel level)
        ) {
            return;
        }

        level.sendParticles(
                particle,
                elite.getX(),
                elite.getY()
                        + elite.getBbHeight()
                        * 0.85D,
                elite.getZ(),
                count,
                spread,
                spread,
                spread,
                speed
        );
    }


    /*
     * =====================================================
     * RINGS
     * =====================================================
     */

    public static void ring(
            Mob elite,
            ParticleOptions particle,
            double radius,
            int count,
            double yOffset
    ) {
        if (
                !(elite.level() instanceof ServerLevel level)
        ) {
            return;
        }

        if (count <= 0) {
            return;
        }

        for (
                int i = 0;
                i < count;
                i++
        ) {
            double angle =
                    Math.PI
                            * 2.0D
                            * i
                            / count;

            double x =
                    elite.getX()
                            + Math.cos(
                            angle
                    )
                            * radius;

            double z =
                    elite.getZ()
                            + Math.sin(
                            angle
                    )
                            * radius;

            level.sendParticles(
                    particle,
                    x,
                    elite.getY()
                            + yOffset,
                    z,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }


    public static void headRing(
            Mob elite,
            ParticleOptions particle,
            double radius,
            int count
    ) {
        ring(
                elite,
                particle,
                radius,
                count,
                elite.getBbHeight()
                        * 0.85D
        );
    }


    /*
     * =====================================================
     * ORBIT
     * =====================================================
     *
     * Spawns one or more particles at rotating positions
     * around the mob.
     *
     * Call every few ticks for a continuous orbit.
     */

    public static void orbit(
            Mob elite,
            ParticleOptions particle,
            double radius,
            double yOffset,
            int points,
            double speed
    ) {
        if (
                !(elite.level() instanceof ServerLevel level)
        ) {
            return;
        }

        if (points <= 0) {
            return;
        }

        double baseAngle =
                elite.tickCount
                        * speed;

        for (
                int i = 0;
                i < points;
                i++
        ) {
            double angle =
                    baseAngle
                            + Math.PI
                            * 2.0D
                            * i
                            / points;

            double x =
                    elite.getX()
                            + Math.cos(
                            angle
                    )
                            * radius;

            double z =
                    elite.getZ()
                            + Math.sin(
                            angle
                    )
                            * radius;

            level.sendParticles(
                    particle,
                    x,
                    elite.getY()
                            + yOffset,
                    z,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }


    public static void headOrbit(
            Mob elite,
            ParticleOptions particle,
            double radius,
            int points,
            double speed
    ) {
        orbit(
                elite,
                particle,
                radius,
                elite.getBbHeight()
                        * 0.85D,
                points,
                speed
        );
    }


    /*
     * =====================================================
     * SPIRAL
     * =====================================================
     *
     * Creates a vertical spiral around the mob.
     */

    public static void spiral(
            Mob elite,
            ParticleOptions particle,
            double radius,
            double height,
            int points,
            double rotations
    ) {
        if (
                !(elite.level() instanceof ServerLevel level)
        ) {
            return;
        }

        if (points <= 0) {
            return;
        }

        for (
                int i = 0;
                i < points;
                i++
        ) {
            double progress =
                    (double) i
                            / Math.max(
                            1,
                            points - 1
                    );

            double angle =
                    progress
                            * Math.PI
                            * 2.0D
                            * rotations;

            double x =
                    elite.getX()
                            + Math.cos(
                            angle
                    )
                            * radius;

            double y =
                    elite.getY()
                            + progress
                            * height;

            double z =
                    elite.getZ()
                            + Math.sin(
                            angle
                    )
                            * radius;

            level.sendParticles(
                    particle,
                    x,
                    y,
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
     * GROUND BURST
     * =====================================================
     */

    public static void groundBurst(
            Mob elite,
            ParticleOptions particle,
            int count,
            double radius,
            double speed
    ) {
        if (
                !(elite.level() instanceof ServerLevel level)
        ) {
            return;
        }

        level.sendParticles(
                particle,
                elite.getX(),
                elite.getY()
                        + 0.05D,
                elite.getZ(),
                count,
                radius,
                0.05D,
                radius,
                speed
        );
    }


    /*
     * =====================================================
     * BEAM / LINK
     * =====================================================
     *
     * Useful for:
     * - Siphoning
     * - Commander
     * - Packbound
     * - Vampiric
     */

    public static void link(
            Entity from,
            Entity to,
            ParticleOptions particle,
            int points
    ) {
        if (
                !(from.level() instanceof ServerLevel level)
        ) {
            return;
        }

        if (
                to.level()
                        != from.level()
                || points <= 0
        ) {
            return;
        }

        double startX =
                from.getX();

        double startY =
                from.getY()
                        + from.getBbHeight()
                        * 0.5D;

        double startZ =
                from.getZ();

        double endX =
                to.getX();

        double endY =
                to.getY()
                        + to.getBbHeight()
                        * 0.5D;

        double endZ =
                to.getZ();

        for (
                int i = 0;
                i <= points;
                i++
        ) {
            double progress =
                    (double) i
                            / points;

            double x =
                    lerp(
                            startX,
                            endX,
                            progress
                    );

            double y =
                    lerp(
                            startY,
                            endY,
                            progress
                    );

            double z =
                    lerp(
                            startZ,
                            endZ,
                            progress
                    );

            level.sendParticles(
                    particle,
                    x,
                    y,
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
     * RISING COLUMN
     * =====================================================
     *
     * Good for Undying / portals / revival effects.
     */

    public static void risingColumn(
            Mob elite,
            ParticleOptions particle,
            double radius,
            double height,
            int points,
            double rotationSpeed
    ) {
        if (
                !(elite.level() instanceof ServerLevel level)
        ) {
            return;
        }

        if (points <= 0) {
            return;
        }

        double time =
                elite.tickCount
                        * rotationSpeed;

        for (
                int i = 0;
                i < points;
                i++
        ) {
            double progress =
                    (double) i
                            / Math.max(
                            1,
                            points - 1
                    );

            double angle =
                    time
                            + progress
                            * Math.PI
                            * 4.0D;

            double currentRadius =
                    radius
                            * (
                            1.0D
                            - progress
                    );

            double x =
                    elite.getX()
                            + Math.cos(
                            angle
                    )
                            * currentRadius;

            double y =
                    elite.getY()
                            + progress
                            * height;

            double z =
                    elite.getZ()
                            + Math.sin(
                            angle
                    )
                            * currentRadius;

            level.sendParticles(
                    particle,
                    x,
                    y,
                    z,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

    public static void burst(
        ServerLevel level,
        ParticleOptions particle,
        double x,
        double y,
        double z,
        int count,
        double spreadX,
        double spreadY,
        double spreadZ,
        double speed
) {
    level.sendParticles(
            particle,
            x,
            y,
            z,
            count,
            spreadX,
            spreadY,
            spreadZ,
            speed
    );
}

        public static void ritualSigil(
        ServerLevel level,
        Mob elite,
        ParticleOptions particle,
        int points,
        double radius,
        double rotationSpeed
) {
    double time =
            level.getGameTime()
                    * rotationSpeed;

    for (
            int i = 0;
            i < points;
            i++
    ) {
        double angle =
                time
                        + (
                        Math.PI * 2.0D
                                * i
                                / points
                );

        double x =
                elite.getX()
                        + Math.cos(angle)
                        * radius;

        double z =
                elite.getZ()
                        + Math.sin(angle)
                        * radius;

        level.sendParticles(
                particle,
                x,
                elite.getY() + 0.03D,
                z,
                1,
                0.0D,
                0.0D,
                0.0D,
                0.0D
        );
    }
}

        public static void undyingSmoke(
        ServerLevel level,
        Mob elite
) {
    burst(
            level,
            ParticleTypes.WITCH,
            elite.getX(),
            elite.getY() + 0.1D,
            elite.getZ(),
            4,
            0.35D,
            0.08D,
            0.35D,
            0.02D
    );

    burst(
            level,
            ParticleTypes.SMOKE,
            elite.getX(),
            elite.getY() + 0.1D,
            elite.getZ(),
            3,
            0.30D,
            0.08D,
            0.30D,
            0.01D
    );
}


    /*
     * =====================================================
     * HELPERS
     * =====================================================
     */

    private static double lerp(
            double start,
            double end,
            double progress
    ) {
        return start
                + (
                end - start
        )
                * progress;
    }
}