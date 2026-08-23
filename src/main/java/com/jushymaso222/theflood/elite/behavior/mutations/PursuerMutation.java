package com.jushymaso222.theflood.elite.behavior.mutations;

import com.jushymaso222.theflood.elite.EliteStateSync;
import com.jushymaso222.theflood.elite.behavior.EliteMutation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import com.jushymaso222.theflood.elite.behavior.EliteMobCompatibility;
import com.jushymaso222.theflood.elite.presentation.EliteSounds;

import net.minecraft.sounds.SoundEvents;

import java.util.UUID;

public final class PursuerMutation
        implements EliteMutation {

    private static final String QUARRY_UUID_KEY =
            "theflood_pursuer_quarry";

    private static final String PURSUIT_KEY =
            "theflood_pursuer_progress";

    private static final String BURST_END_KEY =
            "theflood_pursuer_burst_end";

    private static final float MAX_PURSUIT =
            100.0F;

    /*
     * Minimum distance before pursuit momentum
     * begins building.
     */
    private static final double BUILD_DISTANCE =
            6.0D;

    /*
     * If the quarry gets farther than this,
     * Pursuer gives up and resets.
     */
    private static final double MAX_PURSUIT_DISTANCE =
            96.0D;

    private static final float BASE_PURSUIT_PER_TICK =
            0.35F;

    /*
     * 60 ticks = 3 seconds.
     */
    private static final int BURST_DURATION_TICKS =
            60;

    /*
     * +125% movement speed during chase burst.
     */
    private static final double BURST_SPEED_BONUS =
            1.25D;

    /*
     * Strong knockback resistance while bursting.
     */
    private static final double BURST_KNOCKBACK_BONUS =
            0.75D;

    private static final int STATUS_COLOR =
            0xFFB52A38;

    private static final UUID SPEED_MODIFIER_ID =
            UUID.fromString(
                    "ef1dcf32-4ec4-49e7-88fe-789d33be665f"
            );

    private static final UUID KNOCKBACK_MODIFIER_ID =
            UUID.fromString(
                    "5efdb63c-5643-4913-91db-07cb154f61f3"
            );

    @Override
    public String id() {
        return "pursuer";
    }

    @Override
    public String displayName() {
        return "Pursuer";
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
                )
                || EliteMobCompatibility.isEnderman(
                        mob
                )
                || EliteMobCompatibility.isWarden(
                        mob
                )
                || EliteMobCompatibility.isSpider(
                        mob
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

        ServerPlayer quarry =
                getQuarry(
                        level,
                        elite
                );

        /*
         * No stored quarry yet.
         *
         * If vanilla AI currently has a valid player target,
         * lock onto that player.
         */
        if (quarry == null) {
            if (
                    elite.getTarget()
                            instanceof ServerPlayer target
                    && target.isAlive()
                    && !target.isSpectator()
            ) {
                setQuarry(
                        elite,
                        target
                );

                quarry =
                        target;
            } else {
                resetPursuit(
                        elite
                );

                return;
            }
        }

        /*
         * Quarry is no longer valid.
         */
        if (
                !quarry.isAlive()
                || quarry.isSpectator()
                || quarry.serverLevel()
                        != level
        ) {
            resetPursuit(
                    elite
            );

            return;
        }

        double distanceSquared =
                elite.distanceToSqr(
                        quarry
                );

        if (
                distanceSquared
                        > MAX_PURSUIT_DISTANCE
                                * MAX_PURSUIT_DISTANCE
        ) {
            resetPursuit(
                    elite
            );

            return;
        }

        /*
         * Keep reacquiring the quarry even if vanilla AI
         * briefly loses line of sight.
         */
        if (
                elite.getTarget()
                        != quarry
        ) {
            elite.setTarget(
                    quarry
            );
        }

        /*
         * Burst behavior overrides normal buildup.
         */
        long burstEnd =
                elite.getPersistentData()
                        .getLong(
                                BURST_END_KEY
                        );

        if (burstEnd > 0L) {
            tickBurst(
                    elite,
                    quarry
            );

            return;
        }

        tickPursuitBuild(
                elite,
                quarry
        );
    }

    private static void tickPursuitBuild(
            Mob elite,
            ServerPlayer quarry
    ) {
        double distance =
                elite.distanceTo(
                        quarry
                );

        float pursuit =
                getPursuit(
                        elite
                );

        /*
         * If the player is close enough to actually fight,
         * pursuit momentum slowly drains instead.
         */
        if (distance <= BUILD_DISTANCE) {
            pursuit =
                    Math.max(
                            0.0F,
                            pursuit - 0.50F
                    );

            setPursuit(
                    elite,
                    pursuit
            );

            syncState(
                    elite
            );

            return;
        }

        float buildup =
                BASE_PURSUIT_PER_TICK;

        /*
         * Greater distance causes faster buildup.
         */
        if (distance > 12.0D) {
            buildup *=
                    1.5F;
        }

        if (distance > 20.0D) {
            buildup *=
                    2.0F;
        }

        pursuit =
                Math.min(
                        MAX_PURSUIT,
                        pursuit + buildup
                );

        setPursuit(
                elite,
                pursuit
        );

        if (
                pursuit
                        >= MAX_PURSUIT
        ) {
            startBurst(
                    elite
            );

            return;
        }

        syncState(
                elite
        );
    }

    private static void startBurst(
                Mob elite
        ) {
        elite.getPersistentData()
                .putLong(
                        BURST_END_KEY,
                        elite.level()
                                .getGameTime()
                                + BURST_DURATION_TICKS
                );

        /*
        * Pursuer announces the beginning of its charge.
        *
        * This method only runs once when the burst begins,
        * so the roar cannot repeat every tick.
        */
        EliteSounds.playRandomPitch(
                elite,
                SoundEvents.ZOGLIN_ANGRY,
                0.75F,
                1.15F,
                0.05F
        );

        applyBurstAttributes(
                elite
        );

        EliteStateSync.sync(
                elite,
                MAX_PURSUIT,
                MAX_PURSUIT,
                true,
                STATUS_COLOR
        );
        }

    private static void tickBurst(
            Mob elite,
            ServerPlayer quarry
    ) {
        long burstEnd =
                elite.getPersistentData()
                        .getLong(
                                BURST_END_KEY
                        );

        if (
                elite.level()
                        .getGameTime()
                        >= burstEnd
        ) {
            endBurst(
                    elite
            );

            return;
        }

        /*
        * Keep the target locked during the active burst.
        */
        elite.setTarget(
                quarry
        );
    }

    private static void endBurst(
            Mob elite
    ) {
        removeBurstAttributes(
                elite
        );

        elite.getPersistentData()
                .putLong(
                        BURST_END_KEY,
                        0L
                );

        setPursuit(
                elite,
                0.0F
        );

        syncState(
                elite
        );
    }

    @Override
        public void onDeactivated(
                Mob elite
        ) {
        resetPursuit(
                elite
        );
        }

    private static void applyBurstAttributes(
            Mob elite
    ) {
        AttributeInstance speed =
                elite.getAttribute(
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
                            "The Flood Pursuer burst speed",
                            BURST_SPEED_BONUS,
                            AttributeModifier.Operation.MULTIPLY_TOTAL
                    )
            );
        }

        AttributeInstance knockback =
                elite.getAttribute(
                        Attributes.KNOCKBACK_RESISTANCE
                );

        if (
                knockback != null
                && knockback.getModifier(
                        KNOCKBACK_MODIFIER_ID
                ) == null
        ) {
            knockback.addTransientModifier(
                    new AttributeModifier(
                            KNOCKBACK_MODIFIER_ID,
                            "The Flood Pursuer burst knockback resistance",
                            BURST_KNOCKBACK_BONUS,
                            AttributeModifier.Operation.ADDITION
                    )
            );
        }
    }

    private static void removeBurstAttributes(
            Mob elite
    ) {
        AttributeInstance speed =
                elite.getAttribute(
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

        AttributeInstance knockback =
                elite.getAttribute(
                        Attributes.KNOCKBACK_RESISTANCE
                );

        if (knockback != null) {
            AttributeModifier modifier =
                    knockback.getModifier(
                            KNOCKBACK_MODIFIER_ID
                    );

            if (modifier != null) {
                knockback.removeModifier(
                        modifier
                );
            }
        }
    }

    private static void setQuarry(
            Mob elite,
            ServerPlayer player
    ) {
        elite.getPersistentData()
                .putUUID(
                        QUARRY_UUID_KEY,
                        player.getUUID()
                );
    }

    private static ServerPlayer getQuarry(
            ServerLevel level,
            Mob elite
    ) {
        if (
                !elite.getPersistentData()
                        .hasUUID(
                                QUARRY_UUID_KEY
                        )
        ) {
            return null;
        }

        UUID quarryId =
                elite.getPersistentData()
                        .getUUID(
                                QUARRY_UUID_KEY
                        );

        return level.getServer()
                .getPlayerList()
                .getPlayer(
                        quarryId
                );
    }

    private static float getPursuit(
            Mob elite
    ) {
        return elite.getPersistentData()
                .getFloat(
                        PURSUIT_KEY
                );
    }

    private static void setPursuit(
            Mob elite,
            float pursuit
    ) {
        elite.getPersistentData()
                .putFloat(
                        PURSUIT_KEY,
                        Math.max(
                                0.0F,
                                Math.min(
                                        MAX_PURSUIT,
                                        pursuit
                                )
                        )
                );
    }

    private static void resetPursuit(
            Mob elite
    ) {
        removeBurstAttributes(
                elite
        );

        elite.getPersistentData()
                .remove(
                        QUARRY_UUID_KEY
                );

        elite.getPersistentData()
                .putLong(
                        BURST_END_KEY,
                        0L
                );

        setPursuit(
                elite,
                0.0F
        );

        EliteStateSync.syncBasic(
                elite
        );
    }

    private static void syncState(
            Mob elite
    ) {
        EliteStateSync.sync(
                elite,
                getPursuit(
                        elite
                ),
                MAX_PURSUIT,
                true,
                STATUS_COLOR
        );
    }
}