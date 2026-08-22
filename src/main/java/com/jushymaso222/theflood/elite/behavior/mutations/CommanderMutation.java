package com.jushymaso222.theflood.elite.behavior.mutations;

import com.jushymaso222.theflood.elite.EliteData;
import com.jushymaso222.theflood.elite.EliteStateSync;
import com.jushymaso222.theflood.elite.behavior.EliteMutation;
import com.jushymaso222.theflood.spawning.SpawnDirector;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.MoverType;

import com.jushymaso222.theflood.elite.presentation.EliteVisuals;
import net.minecraft.core.particles.DustParticleOptions;

import com.jushymaso222.theflood.elite.presentation.ElitePose;
import com.jushymaso222.theflood.elite.presentation.ElitePresentation;
import com.jushymaso222.theflood.elite.presentation.EliteSounds;
import com.jushymaso222.theflood.sound.FloodSounds;

import net.minecraft.world.entity.Entity;

import net.minecraft.world.damagesource.DamageSource;

import java.util.List;
import java.util.UUID;

public final class CommanderMutation
        implements EliteMutation {

    public static final String COMMANDED_TAG =
            "theflood_commander_commanded";

    public static final String COMMANDER_UUID_TAG =
            "theflood_commander_uuid";

    private static final String NEXT_COMMAND_KEY =
            "theflood_commander_next_command";

    private static final String COMMANDED_PLAYER_UUID_KEY =
            "theflood_commander_target_player";

    private static final String RETREATING_KEY =
            "theflood_commander_retreating";

    private static final String RETREAT_END_TIME_KEY =
            "theflood_commander_retreat_end";

    private static final String COMMAND_SEQUENCE_START_KEY =
        "theflood_commander_sequence_start";

        private static final String COMMAND_SEQUENCE_TARGET_KEY =
                "theflood_commander_sequence_target";

        private static final int WHISTLE_DELAY_TICKS =
                5;

        private static final int COMMAND_DELAY_TICKS =
                8;

        private static final int COMMAND_POSE_TICKS =
                14;

    private static final int RETREAT_MAX_TICKS =
            100; // 5 seconds

    private static final int COMMAND_COOLDOWN_TICKS =
            240; // 12 seconds

    private static final double COMMAND_RADIUS =
            32.0D;

    private static final int MIN_COMMAND_COUNT =
            2;

    private static final int MAX_COMMAND_COUNT =
            4;

    private static final double RETREAT_DISTANCE =
            12.0D;

    private static final double RETREAT_SPEED =
            1.4D;

    private static final float FLEE_HEALTH_THRESHOLD =
            0.35F;

    private static final String RETREAT_PLAYER_UUID_KEY =
            "theflood_commander_retreat_player";

    private static final double COMMANDED_SPEED_BONUS =
            0.20D;

    private static final double COMMANDED_DAMAGE_BONUS =
            0.20D;

    private static final float DEFENSE_PER_MINION =
            0.15F;

    private static final float MAX_DEFENSE_REDUCTION =
            0.60F;

    private static final int STATUS_COLOR =
            0xFF4FA3FF;

    private static final UUID SPEED_MODIFIER_ID =
            UUID.fromString(
                    "ad128317-c7bb-46a0-9b16-44973d705116"
            );

    private static final UUID DAMAGE_MODIFIER_ID =
            UUID.fromString(
                    "38fd3119-a952-4926-b970-a8bf52d54054"
            );

    @Override
    public String id() {
        return "commander";
    }

    @Override
    public String displayName() {
        return "Commander";
    }

    private static void startRetreat(
            Mob commander,
            ServerPlayer player
    ) {
        commander.getPersistentData()
                .putBoolean(
                        RETREATING_KEY,
                        true
                );

        commander.getPersistentData()
                .putUUID(
                        RETREAT_PLAYER_UUID_KEY,
                        player.getUUID()
                );

        commander.getPersistentData()
                .putLong(
                        RETREAT_END_TIME_KEY,
                        commander.level()
                                .getGameTime()
                                + RETREAT_MAX_TICKS
                );

        commander.setTarget(
                null
        );

        commander.getNavigation()
                .stop();
    }

    private static void startCommandSequence(
        Mob commander,
        ServerPlayer target
) {
    long now =
            commander.level()
                    .getGameTime();

    commander.getPersistentData()
            .putLong(
                    COMMAND_SEQUENCE_START_KEY,
                    now
            );

    commander.getPersistentData()
            .putUUID(
                    COMMAND_SEQUENCE_TARGET_KEY,
                    target.getUUID()
            );

    ElitePresentation.setPose(
            commander,
            ElitePose.COMMANDING
    );

    EliteStateSync.syncBasic(
            commander
    );

    commander.getNavigation()
            .stop();
}

private static ServerPlayer getCommandSequenceTarget(
        ServerLevel level,
        Mob commander
) {
    if (
            !commander.getPersistentData()
                    .hasUUID(
                            COMMAND_SEQUENCE_TARGET_KEY
                    )
    ) {
        return null;
    }

    UUID targetId =
            commander.getPersistentData()
                    .getUUID(
                            COMMAND_SEQUENCE_TARGET_KEY
                    );

    return level.getServer()
            .getPlayerList()
            .getPlayer(
                    targetId
            );
}

private static boolean tickCommandSequence(
        ServerLevel level,
        Mob commander
) {
    if (
            !commander.getPersistentData()
                    .contains(
                            COMMAND_SEQUENCE_START_KEY
                    )
    ) {
        return false;
    }

    long start =
            commander.getPersistentData()
                    .getLong(
                            COMMAND_SEQUENCE_START_KEY
                    );

    long elapsed =
            level.getGameTime()
                    - start;

    ServerPlayer target =
            getCommandSequenceTarget(
                    level,
                    commander
            );

    /*
     * Target disappeared/died during the signal.
     */
    if (
            target == null
            || !target.isAlive()
            || target.isSpectator()
    ) {
        finishCommandSequence(
                commander
        );

        return false;
    }

    /*
     * Commander stays put during the signal.
     */
    commander.setTarget(
            null
    );

    commander.getNavigation()
            .stop();

    if (
            elapsed
                    == WHISTLE_DELAY_TICKS
    ) {
        EliteSounds.playRandomPitch(
                commander,
                FloodSounds.COMMANDER_WHISTLE.get(),
                1.0F,
                1.0F,
                0.02F
        );
    }

    if (
            elapsed
                    == COMMAND_DELAY_TICKS
    ) {
        int commanded =
                issueCommand(
                        level,
                        commander,
                        target
                );

        if (commanded > 0) {
            commander.getPersistentData()
                    .putUUID(
                            COMMANDED_PLAYER_UUID_KEY,
                            target.getUUID()
                    );

            commander.getPersistentData()
                    .putLong(
                            NEXT_COMMAND_KEY,
                            level.getGameTime()
                                    + COMMAND_COOLDOWN_TICKS
                    );
        }
        else {
            commander.getPersistentData()
                    .putLong(
                            NEXT_COMMAND_KEY,
                            level.getGameTime()
                                    + 40
                    );
        }
    }

    if (
            elapsed
                    >= COMMAND_POSE_TICKS
    ) {
        boolean commandedSomething =
                commander.getPersistentData()
                        .hasUUID(
                                COMMANDED_PLAYER_UUID_KEY
                        );

        finishCommandSequence(
                commander
        );

        if (commandedSomething) {
            startRetreat(
                    commander,
                    target
            );
        }
        else {
            float healthPercent =
                    commander.getHealth()
                            / commander.getMaxHealth();

            if (
                    healthPercent
                            <= FLEE_HEALTH_THRESHOLD
            ) {
                startRetreat(
                        commander,
                        target
                );
            }
        }

        return false;
    }

    return true;
}

private static void finishCommandSequence(
        Mob commander
) {
    commander.getPersistentData()
            .remove(
                    COMMAND_SEQUENCE_START_KEY
            );

    commander.getPersistentData()
            .remove(
                    COMMAND_SEQUENCE_TARGET_KEY
            );

    ElitePresentation.resetPose(
            commander
    );

    EliteStateSync.syncBasic(
            commander
    );
}

    private static ServerPlayer getRetreatPlayer(
            ServerLevel level,
            Mob commander
    ) {
        if (
                !commander.getPersistentData()
                        .hasUUID(
                                RETREAT_PLAYER_UUID_KEY
                        )
        ) {
            return null;
        }

        UUID playerId =
                commander.getPersistentData()
                        .getUUID(
                                RETREAT_PLAYER_UUID_KEY
                        );

        return level.getServer()
                .getPlayerList()
                .getPlayer(
                        playerId
                );
    }

    private static void resetCommandedMob(
            Mob mob
    ) {
        mob.getPersistentData()
                .remove(
                        COMMANDED_TAG
                );

        mob.getPersistentData()
                .remove(
                        COMMANDER_UUID_TAG
                );

        mob.setTarget(
                null
        );

        AttributeInstance speed =
                mob.getAttribute(
                        Attributes.MOVEMENT_SPEED
                );

        if (speed != null) {
            AttributeModifier modifier =
                    speed.getModifier(
                            SPEED_MODIFIER_ID
                    );

            if (modifier != null) {
                speed.removeModifier(
                        modifier
                );
            }
        }

        AttributeInstance damage =
                mob.getAttribute(
                        Attributes.ATTACK_DAMAGE
                );

        if (damage != null) {
            AttributeModifier modifier =
                    damage.getModifier(
                            DAMAGE_MODIFIER_ID
                    );

            if (modifier != null) {
                damage.removeModifier(
                        modifier
                );
            }
        }
    }

    private static void resetCommandGroup(
            ServerLevel level,
            Mob commander
    ) {
        for (Mob mob :
                level.getEntitiesOfClass(
                        Mob.class,
                        commander.getBoundingBox()
                                .inflate(
                                        96.0D
                                )
                )
        ) {
            if (
                    !mob.getPersistentData()
                            .getBoolean(
                                    COMMANDED_TAG
                            )
            ) {
                continue;
            }

            if (
                    !mob.getPersistentData()
                            .hasUUID(
                                    COMMANDER_UUID_TAG
                            )
            ) {
                continue;
            }

            if (
                    !mob.getPersistentData()
                            .getUUID(
                                    COMMANDER_UUID_TAG
                            )
                            .equals(
                                    commander.getUUID()
                            )
            ) {
                continue;
            }

            resetCommandedMob(
                    mob
            );
        }

        commander.getPersistentData()
                .remove(
                        COMMANDED_PLAYER_UUID_KEY
                );

        if (
                commander.getPersistentData()
                        .getBoolean(
                                RETREATING_KEY
                        )
        ) {
            endRetreat(
                    commander
            );
        }

        EliteStateSync.syncBasic(
                commander
        );
    }

    private static ServerPlayer getCommandedPlayer(
            ServerLevel level,
            Mob commander
    ) {
        if (
                !commander.getPersistentData()
                        .hasUUID(
                                COMMANDED_PLAYER_UUID_KEY
                        )
        ) {
            return null;
        }

        UUID playerId =
                commander.getPersistentData()
                        .getUUID(
                                COMMANDED_PLAYER_UUID_KEY
                        );

        return level.getServer()
                .getPlayerList()
                .getPlayer(
                        playerId
                );
    }

    private static void endRetreat(
            Mob commander
    ) {
        commander.getPersistentData()
                .putBoolean(
                        RETREATING_KEY,
                        false
                );

        commander.getPersistentData()
                .remove(
                        RETREAT_PLAYER_UUID_KEY
                );

        commander.getNavigation()
                .stop();
    }

    @Override
        public void onDeactivated(
                Mob elite
        ) {
        if (
                elite.level() instanceof ServerLevel level
        ) {
                resetCommandGroup(
                        level,
                        elite
                );
        }

        elite.getPersistentData()
                .remove(
                        COMMAND_SEQUENCE_START_KEY
                );

        elite.getPersistentData()
                .remove(
                        COMMAND_SEQUENCE_TARGET_KEY
                );

        ElitePresentation.resetPose(
                elite
        );

        elite.getPersistentData()
                .remove(
                        NEXT_COMMAND_KEY
                );

        elite.getPersistentData()
                .remove(
                        COMMANDED_PLAYER_UUID_KEY
                );

        elite.getPersistentData()
                .remove(
                        RETREAT_PLAYER_UUID_KEY
                );

        elite.getPersistentData()
                .remove(
                        RETREAT_END_TIME_KEY
                );

        elite.getPersistentData()
                .remove(
                        RETREATING_KEY
                );
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
                tickCommandSequence(
                        level,
                        elite
                )
        ) {
        return;
        }

        ServerPlayer commandedPlayer =
                getCommandedPlayer(
                        level,
                        elite
                );

        if (
                commandedPlayer != null
                && !commandedPlayer.isAlive()
        ) {
            resetCommandGroup(
                    level,
                    elite
            );

            return;
        }

        updateCommandedState(
                level,
                elite
        );

        if (
                elite.getPersistentData()
                        .getBoolean(
                                RETREATING_KEY
                        )
        ) {
            tickRetreat(
                    level,
                    elite
            );

            return;
        }

        ServerPlayer target =
                getPlayerTarget(
                        elite
                );

        if (target == null) {
            return;
        }

        long gameTime =
                level.getGameTime();

        long nextCommand =
                elite.getPersistentData()
                        .getLong(
                                NEXT_COMMAND_KEY
                        );

        if (gameTime < nextCommand) {
                return;
                }

                /*
                * Do not perform the whistle/command animation
                * unless there is actually somebody available
                * to command.
                */
                if (
                        !hasCommandableMobs(
                                level,
                                elite
                        )
                ) {
                /*
                * Check again soon, but don't whistle.
                */
                elite.getPersistentData()
                        .putLong(
                                NEXT_COMMAND_KEY,
                                gameTime + 40
                        );

                /*
                * If he's wounded and has nobody around,
                * preserve the existing coward behavior.
                */
                float healthPercent =
                        elite.getHealth()
                                / elite.getMaxHealth();

                if (
                        healthPercent
                                <= FLEE_HEALTH_THRESHOLD
                ) {
                        startRetreat(
                                elite,
                                target
                        );
                }

                return;
                }

                startCommandSequence(
                        elite,
                        target
                );

        elite.getPersistentData()
                .putLong(
                        NEXT_COMMAND_KEY,
                        gameTime
                                + COMMAND_COOLDOWN_TICKS
                );

        elite.getPersistentData()
            .putUUID(
                    COMMANDED_PLAYER_UUID_KEY,
                    target.getUUID()
            );

        startRetreat(
                elite,
                target
        );
    }

    private static boolean hasCommandableMobs(
        ServerLevel level,
        Mob commander
) {
    AABB searchArea =
            commander.getBoundingBox()
                    .inflate(
                            COMMAND_RADIUS
                    );

    return !level.getEntitiesOfClass(
            Mob.class,
            searchArea,
            mob ->
                    mob != commander
                    && mob.isAlive()
                    && mob.getPersistentData()
                            .getBoolean(
                                    SpawnDirector.FLOOD_CONTROLLED_TAG
                            )
                    && !EliteData.isElite(
                            mob
                    )
                    && !mob.getPersistentData()
                            .getBoolean(
                                    COMMANDED_TAG
                            )
    ).isEmpty();
}

    private static ServerPlayer getPlayerTarget(
            Mob elite
    ) {
        if (
                elite.getTarget() instanceof ServerPlayer player
                && player.isAlive()
                && !player.isSpectator()
        ) {
            return player;
        }

        return null;
    }

    private static int issueCommand(
            ServerLevel level,
            Mob commander,
            ServerPlayer target
    ) {
        AABB searchArea =
                commander.getBoundingBox()
                        .inflate(
                                COMMAND_RADIUS
                        );

        List<Mob> candidates =
                level.getEntitiesOfClass(
                        Mob.class,
                        searchArea,
                        mob ->
                                mob != commander
                                && mob.isAlive()
                                && mob.getPersistentData()
                                        .getBoolean(
                                                SpawnDirector.FLOOD_CONTROLLED_TAG
                                        )
                                && !EliteData.isElite(
                                        mob
                                )
                                && !mob.getPersistentData()
                                        .getBoolean(
                                                COMMANDED_TAG
                                        )
                );

        if (candidates.isEmpty()) {
            return 0;
        }

        /*
        * Prefer nearby troops.
        */
        candidates.sort(
                (a, b) ->
                        Double.compare(
                                a.distanceToSqr(
                                        commander
                                ),
                                b.distanceToSqr(
                                        commander
                                )
                        )
        );

        int count =
                Math.min(
                        MIN_COMMAND_COUNT
                                + level.random.nextInt(
                                        MAX_COMMAND_COUNT
                                                - MIN_COMMAND_COUNT
                                                + 1
                                ),
                        candidates.size()
                );

        for (
                int i = 0;
                i < count;
                i++
        ) {
            commandMob(
                    candidates.get(i),
                    commander,
                    target
            );
        }

        syncDefenseBar(
                commander,
                count
        );

        return count;
    }

    private static void commandMob(
            Mob mob,
            Mob commander,
            ServerPlayer target
    ) {
        mob.getPersistentData()
                .putBoolean(
                        COMMANDED_TAG,
                        true
                );

        mob.getPersistentData()
                .putUUID(
                        COMMANDER_UUID_TAG,
                        commander.getUUID()
                );

        mob.setTarget(
                target
        );

        mob.setPersistenceRequired();

        applyCommandBuffs(
                mob
        );
    }

    private static void applyCommandBuffs(
            Mob mob
    ) {
        AttributeInstance speed =
                mob.getAttribute(
                        Attributes.MOVEMENT_SPEED
                );

        if (
                speed != null
                && speed.getModifier(
                        SPEED_MODIFIER_ID
                ) == null
        ) {
            speed.addTransientModifier(
                    new AttributeModifier(
                            SPEED_MODIFIER_ID,
                            "The Flood Commander speed",
                            COMMANDED_SPEED_BONUS,
                            AttributeModifier.Operation.MULTIPLY_TOTAL
                    )
            );
        }

        AttributeInstance damage =
                mob.getAttribute(
                        Attributes.ATTACK_DAMAGE
                );

        if (
                damage != null
                && damage.getModifier(
                        DAMAGE_MODIFIER_ID
                ) == null
        ) {
            damage.addTransientModifier(
                    new AttributeModifier(
                            DAMAGE_MODIFIER_ID,
                            "The Flood Commander damage",
                            COMMANDED_DAMAGE_BONUS,
                            AttributeModifier.Operation.MULTIPLY_TOTAL
                    )
            );
        }
    }

    @Override
    public float modifyIncomingDamage(
            Mob elite,
            DamageSource source,
            float damage
    ) {
        if (
                !(elite.level() instanceof ServerLevel level)
        ) {
            return damage;
        }

        int livingCommanded =
                countLivingCommandedMobs(
                        level,
                        elite
                );

        float reduction =
                Math.min(
                        MAX_DEFENSE_REDUCTION,
                        livingCommanded
                                * DEFENSE_PER_MINION
                );

        return damage
                * (
                1.0F
                        - reduction
        );
    }

    private static int countLivingCommandedMobs(
            ServerLevel level,
            Mob commander
    ) {
        int count =
                0;

        for (Mob mob :
                level.getEntitiesOfClass(
                        Mob.class,
                        commander.getBoundingBox()
                                .inflate(
                                        64.0D
                                )
                )
        ) {
            if (
                    !mob.isAlive()
                    || !mob.getPersistentData()
                            .getBoolean(
                                    COMMANDED_TAG
                            )
            ) {
                continue;
            }

            if (
                    !mob.getPersistentData()
                            .hasUUID(
                                    COMMANDER_UUID_TAG
                            )
            ) {
                continue;
            }

            if (
                    mob.getPersistentData()
                            .getUUID(
                                    COMMANDER_UUID_TAG
                            )
                            .equals(
                                    commander.getUUID()
                            )
            ) {
                count++;
            }
        }

        return count;
    }

    private static void tickRetreat(
            ServerLevel level,
            Mob commander
    ) {
        ServerPlayer player =
                getRetreatPlayer(
                        level,
                        commander
                );

        if (
                player == null
                || !player.isAlive()
        ) {
            endRetreat(
                    commander
            );

            return;
        }

        double distanceSquared =
                commander.distanceToSqr(
                        player
                );

        if (
                distanceSquared
                        >= RETREAT_DISTANCE
                                * RETREAT_DISTANCE
        ) {
            endRetreat(
                    commander
            );

            return;
        }

        long retreatEnd =
                commander.getPersistentData()
                        .getLong(
                                RETREAT_END_TIME_KEY
                        );

        if (
                level.getGameTime()
                        >= retreatEnd
        ) {
            endRetreat(
                    commander
            );

            return;
        }

        Vec3 away =
                commander.position()
                        .subtract(
                                player.position()
                        );

        away =
                new Vec3(
                        away.x,
                        0.0D,
                        away.z
                );

        if (away.lengthSqr() <= 0.0001D) {
            return;
        }

        away =
                away.normalize()
                        .scale(
                                RETREAT_DISTANCE
                        );

        Vec3 destination =
                commander.position()
                        .add(
                                away
                        );

        /*
        * Keep the Commander disengaged while retreating.
        */
        commander.setTarget(
                null
        );

        commander.getNavigation()
                .moveTo(
                        destination.x,
                        destination.y,
                        destination.z,
                        RETREAT_SPEED
                );
    }

    private static void syncDefenseBar(
            Mob commander,
            int livingCommanded
    ) {
        if (livingCommanded <= 0) {
            EliteStateSync.syncBasic(
                    commander
            );

            return;
        }

        EliteStateSync.sync(
                commander,
                livingCommanded,
                MAX_COMMAND_COUNT,
                true,
                STATUS_COLOR
        );
    }

    private static void updateCommandedState(
            ServerLevel level,
            Mob commander
    ) {
        int livingCommanded =
                0;

        for (Mob mob :
                level.getEntitiesOfClass(
                        Mob.class,
                        commander.getBoundingBox()
                                .inflate(
                                        64.0D
                                )
                )
        ) {
            if (
                    !mob.getPersistentData()
                            .getBoolean(
                                    COMMANDED_TAG
                            )
            ) {
                continue;
            }

            if (
                    !mob.getPersistentData()
                            .hasUUID(
                                    COMMANDER_UUID_TAG
                            )
            ) {
                continue;
            }

            if (
                    !mob.getPersistentData()
                            .getUUID(
                                    COMMANDER_UUID_TAG
                            )
                            .equals(
                                    commander.getUUID()
                            )
            ) {
                continue;
            }

            if (
                        mob.isAlive()
                        && !mob.isRemoved()
                ) {
                livingCommanded++;

                /*
                * Visual indicator that this mob is currently
                * being buffed/commanded.
                */
                if (
                        mob.tickCount
                                % 5
                                == 0
                ) {
                        EliteVisuals.headOrbit(
                                mob,
                                DustParticleOptions.REDSTONE,
                                0.35D,
                                3,
                                0.18D
                        );
                }
                }
        }

        syncDefenseBar(
                commander,
                livingCommanded
        );
    }
}