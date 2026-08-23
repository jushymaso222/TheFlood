package com.jushymaso222.theflood.elite.behavior.mutations;

import com.jushymaso222.theflood.elite.EliteStateSync;
import com.jushymaso222.theflood.elite.behavior.EliteMutation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.ai.attributes.Attributes;

import com.jushymaso222.theflood.elite.behavior.EliteMobCompatibility;
import com.jushymaso222.theflood.elite.presentation.ElitePose;
import com.jushymaso222.theflood.elite.presentation.ElitePresentation;
import com.jushymaso222.theflood.elite.presentation.EliteSounds;
import com.jushymaso222.theflood.elite.presentation.EliteVisuals;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;

import java.util.Random;

public final class InfestedMutation
        implements EliteMutation {

    public static final String INFESTED_MINION_TAG =
            "theflood_infested_minion";

    private static final String PROGRESS_KEY =
            "theflood_infested_progress";

    public static final String INFESTED_EXPIRE_TAG =
            "theflood_infested_expire";

    private static final String CONJURE_START_KEY =
        "theflood_infested_conjure_start";

        private static final String CONJURE_TARGET_KEY =
                "theflood_infested_conjure_target";

        private static final int SOUND_DELAY_TICKS =
                4;

        private static final int SPAWN_DELAY_TICKS =
                8;

        private static final int CONJURE_DURATION_TICKS =
                14;

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
        public boolean canApplyTo(
                Mob mob
        ) {
        return EliteMobCompatibility.isZombie(
                mob
        )
                || EliteMobCompatibility.isSkeleton(
                        mob
                );
        }

    @Override
        public void onDeactivated(
                Mob elite
        ) {
        elite.getPersistentData()
                .remove(
                        PROGRESS_KEY
                );

        elite.getPersistentData()
                .remove(
                        CONJURE_START_KEY
                );

        elite.getPersistentData()
                .remove(
                        CONJURE_TARGET_KEY
                );

        ElitePresentation.resetPose(
                elite
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

        if (
                tickConjure(
                        level,
                        elite
                )
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
        startConjure(
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

    private static void startConjure(
        Mob elite,
        ServerPlayer target
) {
    long now =
            elite.level()
                    .getGameTime();

    elite.getPersistentData()
            .putLong(
                    CONJURE_START_KEY,
                    now
            );

    elite.getPersistentData()
            .putUUID(
                    CONJURE_TARGET_KEY,
                    target.getUUID()
            );

    ElitePresentation.setPose(
            elite,
            ElitePose.CONJURING
    );

    EliteStateSync.syncBasic(
            elite
    );

    elite.getNavigation()
            .stop();
}

        private static boolean tickConjure(
        ServerLevel level,
        Mob elite
) {
    if (
            !elite.getPersistentData()
                    .contains(
                            CONJURE_START_KEY
                    )
    ) {
        return false;
    }

    long start =
            elite.getPersistentData()
                    .getLong(
                            CONJURE_START_KEY
                    );

    long elapsed =
            level.getGameTime()
                    - start;

    ServerPlayer target =
            null;

    if (
            elite.getPersistentData()
                    .hasUUID(
                            CONJURE_TARGET_KEY
                    )
    ) {
        target =
                level.getServer()
                        .getPlayerList()
                        .getPlayer(
                                elite.getPersistentData()
                                        .getUUID(
                                                CONJURE_TARGET_KEY
                                        )
                        );
    }

    if (
            target == null
            || !target.isAlive()
            || target.isSpectator()
    ) {
        finishConjure(
                elite
        );

        return false;
    }

    elite.getNavigation()
            .stop();

    if (
            elapsed
                    == SOUND_DELAY_TICKS
    ) {
        EliteSounds.playRandomPitch(
                elite,
                SoundEvents.EVOKER_PREPARE_SUMMON,
                1.0F,
                1.15F,
                0.05F
        );
    }

    if (
            elapsed
                    == SPAWN_DELAY_TICKS
    ) {
        EliteSounds.playRandomPitch(
                elite,
                SoundEvents.SILVERFISH_AMBIENT,
                1.0F,
                0.85F,
                0.08F
        );

        spawnInfestation(
                level,
                elite,
                target
        );
    }

    if (
            elapsed
                    >= CONJURE_DURATION_TICKS
    ) {
        finishConjure(
                elite
        );

        return false;
    }

    return true;
}

        private static void finishConjure(
        Mob elite
) {
    elite.getPersistentData()
            .remove(
                    CONJURE_START_KEY
            );

    elite.getPersistentData()
            .remove(
                    CONJURE_TARGET_KEY
            );

    ElitePresentation.resetPose(
            elite
    );

    EliteStateSync.syncBasic(
            elite
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

            EliteVisuals.burst(
                        level,
                        ParticleTypes.POOF,
                        elite.getX() + offsetX,
                        elite.getY() + 0.1D,
                        elite.getZ() + offsetZ,
                        8,
                        0.18D,
                        0.05D,
                        0.18D,
                        0.02D
                );

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