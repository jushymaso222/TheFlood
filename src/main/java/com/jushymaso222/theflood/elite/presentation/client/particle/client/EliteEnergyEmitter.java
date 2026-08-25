package com.jushymaso222.theflood.elite.presentation.particle.client;

import com.jushymaso222.theflood.TheFlood;
import com.jushymaso222.theflood.elite.client.ClientEliteStateData;
import com.jushymaso222.theflood.elite.presentation.client.EliteAuraColors;
import com.jushymaso222.theflood.elite.presentation.particle.EliteEnergyParticleOptions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(
        modid = TheFlood.MOD_ID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class EliteEnergyEmitter {

    private static long lastEmissionGameTime =
            Long.MIN_VALUE;

    private static final Random RANDOM =
            new Random();

    /*
     * Lower = more particles.
     *
     * 2 means roughly every other client tick.
     */
    private static final int SPAWN_INTERVAL =
            4;

    /*
     * Number of wisps per emission.
     */
    private static final int PARTICLES_PER_EMISSION =
            2;

    /*
     * How far outside the mob's body particles begin.
     */
    private static final double SURFACE_OFFSET =
            0.08D;

    /*
     * Outward drift speed.
     */
    private static final double OUTWARD_SPEED =
            0.006D;

    /*
     * Initial upward movement.
     */
    private static final double UPWARD_SPEED =
            0.004D;

    private EliteEnergyEmitter() {
    }

    @SubscribeEvent
    public static void onClientTick(
            TickEvent.ClientTickEvent event
    ) {
        if (
                event.phase
                        != TickEvent.Phase.END
        ) {
            return;
        }

        Minecraft minecraft =
                Minecraft.getInstance();

        if (
                minecraft.isPaused()
        ) {
        return;
        }

        ClientLevel level =
                minecraft.level;

        if (level == null) {
            return;
        }

        if (
                minecraft.player == null
        ) {
            return;
        }

        /*
         * Don't emit every tick.
         */
        long gameTime =
        level.getGameTime();

        /*
        * Client ticks can continue while the world itself
        * is not advancing.
        *
        * Never emit more than once for the same game tick.
        */
        if (
                gameTime
                        == lastEmissionGameTime
        ) {
        return;
        }

        /*
        * Don't emit every game tick.
        */
        if (
                gameTime
                        % SPAWN_INTERVAL
                        != 0
        ) {
        return;
        }

        lastEmissionGameTime =
                gameTime;

        /*
         * Scan loaded entities near the client.
         *
         * This is purely cosmetic, so we don't need
         * server packets for each particle.
         */
        for (
                Entity entity :
                level.entitiesForRendering()
        ) {
            if (
                    !(entity instanceof Mob mob)
            ) {
                continue;
            }

            ClientEliteStateData.EliteState state =
                    ClientEliteStateData.getState(
                            mob.getId()
                    );

            if (state == null) {
                continue;
            }

            /*
             * Hidden Mimics must look completely normal.
             */
            if (
                    "mimic".equals(
                            state.mutationId()
                    )
                    && !state.mimicRevealed()
            ) {
                continue;
            }

            /*
             * Don't waste particles on very distant mobs.
             */
            if (
                    mob.distanceToSqr(
                            minecraft.player
                    )
                            > 48.0D * 48.0D
            ) {
                continue;
            }

            emit(
                    level,
                    mob,
                    state
            );
        }
    }

    private static void emit(
            ClientLevel level,
            Mob mob,
            ClientEliteStateData.EliteState state
    ) {
        EliteAuraColors.AuraColor color =
                EliteAuraColors.get(
                        state
                );

        for (
                int i = 0;
                i < PARTICLES_PER_EMISSION;
                i++
        ) {
            /*
             * Pick a random point around the mob's body.
             */
            double angle =
                    RANDOM.nextDouble()
                            * Math.PI
                            * 2.0D;

            double radius =
                    mob.getBbWidth()
                            * 0.5D
                            + SURFACE_OFFSET;

            double height =
                    RANDOM.nextDouble()
                            * mob.getBbHeight();

            double normalX =
                    Math.cos(
                            angle
                    );

            double normalZ =
                    Math.sin(
                            angle
                    );

            double x =
                    mob.getX()
                            + normalX
                            * radius;

            double y =
                    mob.getY()
                            + height;

            double z =
                    mob.getZ()
                            + normalZ
                            * radius;

            /*
             * Mostly outward, with a little randomness.
             */
            double velocityX =
                    normalX
                            * OUTWARD_SPEED
                            + randomRange(
                            -0.004D,
                            0.004D
                    );

            double velocityY =
                    UPWARD_SPEED
                            + RANDOM.nextDouble()
                            * 0.006D;

            double velocityZ =
                    normalZ
                            * OUTWARD_SPEED
                            + randomRange(
                            -0.004D,
                            0.004D
                    );

            level.addParticle(
                    new EliteEnergyParticleOptions(
                            color.red(),
                            color.green(),
                            color.blue()
                    ),
                    x,
                    y,
                    z,
                    velocityX,
                    velocityY,
                    velocityZ
            );
        }
    }

    private static double randomRange(
            double min,
            double max
    ) {
        return min
                + RANDOM.nextDouble()
                * (
                max - min
        );
    }
}