package com.jushymaso222.theflood.elite.behavior.mutations;

import com.jushymaso222.theflood.elite.EliteStateSync;
import com.jushymaso222.theflood.elite.behavior.EliteMutation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Random;

public final class InfestedMutation
        implements EliteMutation {

    public static final String INFESTED_MINION_TAG =
            "theflood_infested_minion";

    private static final String PROGRESS_KEY =
            "theflood_infested_progress";

    public static final String INFESTED_EXPIRE_TAG =
            "theflood_infested_expire";

    /*
     * 200 ticks = 10 seconds.
     *
     * TEMPORARY BALANCE VALUE.
     */
    private static final int SPAWN_INTERVAL_TICKS =
            280;

    private static final int MIN_SPAWN_COUNT =
            3;

    private static final int MAX_SPAWN_COUNT =
            5;

    private static final int STATUS_COLOR =
            0xFFB8B8B8;

    private static final Random RANDOM =
            new Random();

    @Override
    public String id() {
        return "infested";
    }

    @Override
        public void onDeactivated(
                Mob elite
        ) {
        elite.getPersistentData()
                .remove(
                        PROGRESS_KEY
                );
        }

    @Override
    public String displayName() {
        return "Infested";
    }

    @Override
    public void tick(
            Mob elite
    ) {
        if (
                !(elite.level() instanceof ServerLevel level)
        ) {
            return;
        }

        /*
         * The infestation only progresses while this
         * Elite is actively engaged with a player.
         */
        if (
                !(elite.getTarget() instanceof ServerPlayer target)
                || !target.isAlive()
                || target.isSpectator()
        ) {
            /*
             * Do NOT reset progress.
             *
             * Losing aggro pauses the infestation timer
             * where it currently is.
             */
            return;
        }

        int progress =
                elite.getPersistentData()
                        .getInt(
                                PROGRESS_KEY
                        );

        progress++;

        if (
                progress
                        >= SPAWN_INTERVAL_TICKS
        ) {
            spawnInfestation(
                    level,
                    elite,
                    target
            );

            progress =
                    0;
        }

        elite.getPersistentData()
                .putInt(
                        PROGRESS_KEY,
                        progress
                );

        syncState(
                elite,
                progress
        );
    }

    private static void spawnInfestation(
            ServerLevel level,
            Mob elite,
            ServerPlayer target
    ) {
        int count =
                MIN_SPAWN_COUNT
                        + RANDOM.nextInt(
                                MAX_SPAWN_COUNT
                                        - MIN_SPAWN_COUNT
                                        + 1
                        );

        for (
                int i = 0;
                i < count;
                i++
        ) {
            Silverfish silverfish =
                    EntityType.SILVERFISH.create(
                            level
                    );

            if (silverfish == null) {
                continue;
            }

            if (
            silverfish.getAttribute(
                            Attributes.MOVEMENT_SPEED
                    ) != null
            ) {
                silverfish.getAttribute(
                        Attributes.MOVEMENT_SPEED
                ).setBaseValue(
                        0.45D
                );
            
            }
            if (
                    silverfish.getAttribute(
                            Attributes.FOLLOW_RANGE
                    ) != null
            ) {
                silverfish.getAttribute(
                        Attributes.FOLLOW_RANGE
                ).setBaseValue(
                        64.0D
                );
            }

            double offsetX =
                    (
                            RANDOM.nextDouble()
                                    - 0.5D
                    ) * 1.5D;

            double offsetZ =
                    (
                            RANDOM.nextDouble()
                                    - 0.5D
                    ) * 1.5D;

            silverfish.moveTo(
                    elite.getX()
                            + offsetX,
                    elite.getY(),
                    elite.getZ()
                            + offsetZ,
                    RANDOM.nextFloat()
                            * 360.0F,
                    0.0F
            );

            silverfish.getPersistentData()
                .putLong(
                        "theflood_infested_expire",
                        level.getGameTime()
                                + 20L * 30L
                );

            silverfish.getPersistentData()
                    .putBoolean(
                            INFESTED_MINION_TAG,
                            true
                    );

            silverfish.setTarget(
                    target
            );

            /*
             * Keep them focused on the encounter.
             */
            silverfish.setPersistenceRequired();

            level.addFreshEntity(
                    silverfish
            );
        }
    }

    private static void syncState(
            Mob elite,
            int progress
    ) {
        EliteStateSync.sync(
                elite,
                progress,
                SPAWN_INTERVAL_TICKS,
                true,
                STATUS_COLOR
        );
    }
}