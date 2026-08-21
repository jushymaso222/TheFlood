package com.jushymaso222.theflood.elite.attributes.specials;

import com.jushymaso222.theflood.elite.attributes.SpecialAttribute;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class StaticAttribute
        implements SpecialAttribute {

    private static final String TARGET_UUID_KEY =
            "theflood_static_target_uuid";

    private static final String STRIKE_TIME_KEY =
            "theflood_static_strike_time";

    private static final String NEXT_TRIGGER_TIME_KEY =
            "theflood_static_next_trigger_time";


    /*
     * =====================================================
     * BALANCE
     * =====================================================
     */

    private static final double LEVEL_ONE_RANGE =
            6.0D;

    private static final double LEVEL_TWO_RANGE =
            7.0D;

    private static final double LEVEL_THREE_RANGE =
            8.0D;


    private static final int LEVEL_ONE_WARNING_TICKS =
            40; // 2 sec

    private static final int LEVEL_TWO_WARNING_TICKS =
            35; // 1.75 sec

    private static final int LEVEL_THREE_WARNING_TICKS =
            30; // 1.5 sec


    private static final int LEVEL_ONE_COOLDOWN_TICKS =
            160; // 8 sec

    private static final int LEVEL_TWO_COOLDOWN_TICKS =
            140; // 7 sec

    private static final int LEVEL_THREE_COOLDOWN_TICKS =
            120; // 6 sec


    private static final float LEVEL_ONE_DAMAGE =
            4.0F;

    private static final float LEVEL_TWO_DAMAGE =
            6.0F;

    private static final float LEVEL_THREE_DAMAGE =
            8.0F;

    @Override
    public String id() {
        return "static";
    }


    @Override
    public String displayName() {
        return "Static";
    }


    @Override
    public void tick(
            Mob elite,
            int level
    ) {
        if (
                elite.level().isClientSide()
                || !elite.isAlive()
        ) {
            return;
        }

        if (
                !(elite.level() instanceof ServerLevel serverLevel)
        ) {
            return;
        }

        long gameTime =
                serverLevel.getGameTime();

        /*
         * If a strike is already charging,
         * process that first.
         */
        if (
                elite.getPersistentData()
                        .hasUUID(
                                TARGET_UUID_KEY
                        )
        ) {
            tickPendingStrike(
                    serverLevel,
                    elite,
                    level,
                    gameTime
            );

            return;
        }

        /*
         * Wait for cooldown.
         */
        long nextTrigger =
                elite.getPersistentData()
                        .getLong(
                                NEXT_TRIGGER_TIME_KEY
                        );

        if (
                nextTrigger > 0L
                && gameTime < nextTrigger
        ) {
            return;
        }

        ServerPlayer target =
                findTarget(
                        serverLevel,
                        elite,
                        level
                );

        if (target == null) {
            return;
        }

        beginStrike(
                elite,
                target,
                level,
                gameTime
        );
    }


    /*
     * =====================================================
     * STRIKE START
     * =====================================================
     */

    private static void beginStrike(
            Mob elite,
            ServerPlayer target,
            int level,
            long gameTime
    ) {
        elite.getPersistentData()
                .putUUID(
                        TARGET_UUID_KEY,
                        target.getUUID()
                );

        elite.getPersistentData()
                .putLong(
                        STRIKE_TIME_KEY,
                        gameTime
                                + getWarningTicks(
                                        level
                                )
                );
    }


    /*
     * =====================================================
     * PENDING STRIKE
     * =====================================================
     */

    private static void tickPendingStrike(
            ServerLevel level,
            Mob elite,
            int attributeLevel,
            long gameTime
    ) {
        UUID targetId =
                elite.getPersistentData()
                        .getUUID(
                                TARGET_UUID_KEY
                        );

        ServerPlayer target =
                level.getServer()
                        .getPlayerList()
                        .getPlayer(
                                targetId
                        );

        /*
         * Target disappeared, died, changed dimension,
         * etc.
         */
        if (
                target == null
                || !target.isAlive()
                || target.isSpectator()
                || target.serverLevel() != level
        ) {
            cancelStrike(
                    elite,
                    attributeLevel,
                    gameTime
            );

            return;
        }

        double range =
                getRange(
                        attributeLevel
                );

        /*
         * Getting out of range successfully avoids
         * the strike.
         */
        if (
                elite.distanceToSqr(
                        target
                )
                        > range * range
        ) {
            cancelStrike(
                    elite,
                    attributeLevel,
                    gameTime
            );

            return;
        }

        /*
         * Temporary warning particles.
         *
         * We'll give this much better visual/audio
         * treatment during polish.
         */
        if (
                elite.tickCount
                        % 4
                        == 0
        ) {
            spawnWarningParticles(
                    level,
                    target
            );
        }

        long strikeTime =
                elite.getPersistentData()
                        .getLong(
                                STRIKE_TIME_KEY
                        );

        if (
                gameTime
                        < strikeTime
        ) {
            return;
        }

        performStrike(
                level,
                elite,
                target,
                attributeLevel
        );

        finishStrike(
                elite,
                attributeLevel,
                gameTime
        );
    }


    /*
     * =====================================================
     * ACTUAL STRIKE
     * =====================================================
     */

    private static void performStrike(
            ServerLevel level,
            Mob elite,
            ServerPlayer target,
            int attributeLevel
    ) {
        float damage =
                getDamage(
                        attributeLevel
                );

        /*
         * Magic damage for now.
         *
         * We can give Static its own custom damage
         * type later if desired.
         */
        target.hurt(
                elite.damageSources()
                        .magic(),
                damage
        );

        /*
         * Temporary visual burst.
         */
        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                target.getX(),
                target.getY()
                        + target.getBbHeight()
                        * 0.5D,
                target.getZ(),
                30,
                0.5D,
                1.0D,
                0.5D,
                0.15D
        );

        level.sendParticles(
                ParticleTypes.FLASH,
                target.getX(),
                target.getY()
                        + 1.0D,
                target.getZ(),
                1,
                0.0D,
                0.0D,
                0.0D,
                0.0D
        );
    }


    /*
     * =====================================================
     * TARGETING
     * =====================================================
     */

    private static ServerPlayer findTarget(
            ServerLevel level,
            Mob elite,
            int attributeLevel
    ) {
        double range =
                getRange(
                        attributeLevel
                );

        AABB searchBox =
                elite.getBoundingBox()
                        .inflate(
                                range
                        );

        List<ServerPlayer> players =
                level.getEntitiesOfClass(
                        ServerPlayer.class,
                        searchBox,
                        player ->
                                player.isAlive()
                                && !player.isSpectator()
                                && elite.distanceToSqr(
                                        player
                                )
                                <= range * range
                );

        if (players.isEmpty()) {
            return null;
        }

        /*
         * Prefer the closest valid player.
         */
        return players.stream()
                .min(
                        Comparator.comparingDouble(
                                elite::distanceToSqr
                        )
                )
                .orElse(
                        null
                );
    }


    /*
     * =====================================================
     * STATE CLEANUP
     * =====================================================
     */

    private static void cancelStrike(
            Mob elite,
            int level,
            long gameTime
    ) {
        clearPendingStrike(
                elite
        );

        /*
         * Escaping still consumes the ability.
         *
         * Otherwise the Elite could instantly select
         * the player again the moment they step back in.
         */
        elite.getPersistentData()
                .putLong(
                        NEXT_TRIGGER_TIME_KEY,
                        gameTime
                                + getCooldownTicks(
                                        level
                                )
                );
    }


    private static void finishStrike(
            Mob elite,
            int level,
            long gameTime
    ) {
        clearPendingStrike(
                elite
        );

        elite.getPersistentData()
                .putLong(
                        NEXT_TRIGGER_TIME_KEY,
                        gameTime
                                + getCooldownTicks(
                                        level
                                )
                );
    }


    private static void clearPendingStrike(
            Mob elite
    ) {
        elite.getPersistentData()
                .remove(
                        TARGET_UUID_KEY
                );

        elite.getPersistentData()
                .remove(
                        STRIKE_TIME_KEY
                );
    }


    /*
     * =====================================================
     * TEMPORARY WARNING VISUAL
     * =====================================================
     */

    private static void spawnWarningParticles(
            ServerLevel level,
            ServerPlayer target
    ) {
        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                target.getX(),
                target.getY()
                        + target.getBbHeight()
                        * 0.5D,
                target.getZ(),
                4,
                0.35D,
                0.75D,
                0.35D,
                0.02D
        );
    }


    /*
     * =====================================================
     * LEVEL SCALING
     * =====================================================
     */

    private static double getRange(
            int level
    ) {
        return switch (level) {

            case 2 ->
                    LEVEL_TWO_RANGE;

            case 3 ->
                    LEVEL_THREE_RANGE;

            default ->
                    LEVEL_ONE_RANGE;
        };
    }


    private static int getWarningTicks(
            int level
    ) {
        return switch (level) {

            case 2 ->
                    LEVEL_TWO_WARNING_TICKS;

            case 3 ->
                    LEVEL_THREE_WARNING_TICKS;

            default ->
                    LEVEL_ONE_WARNING_TICKS;
        };
    }


    private static int getCooldownTicks(
            int level
    ) {
        return switch (level) {

            case 2 ->
                    LEVEL_TWO_COOLDOWN_TICKS;

            case 3 ->
                    LEVEL_THREE_COOLDOWN_TICKS;

            default ->
                    LEVEL_ONE_COOLDOWN_TICKS;
        };
    }


    private static float getDamage(
            int level
    ) {
        return switch (level) {

            case 2 ->
                    LEVEL_TWO_DAMAGE;

            case 3 ->
                    LEVEL_THREE_DAMAGE;

            default ->
                    LEVEL_ONE_DAMAGE;
        };
    }
}