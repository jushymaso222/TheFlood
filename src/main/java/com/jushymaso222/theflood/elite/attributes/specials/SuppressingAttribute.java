package com.jushymaso222.theflood.elite.attributes.specials;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.jushymaso222.theflood.elite.attributes.SpecialAttribute;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class SuppressingAttribute
        implements SpecialAttribute {

    private static final UUID SPEED_MODIFIER_ID =
            UUID.fromString(
                    "6f6d7778-fec8-4ed3-bf04-14f092e55f18"
            );

    /*
     * Player UUID -> expiration game time.
     */
    private static final Map<UUID, Long> EXPIRATIONS =
            new HashMap<>();


    /*
     * =====================================================
     * BALANCE
     * =====================================================
     */

    private static final double LEVEL_ONE_SLOW =
            -0.15D;

    private static final double LEVEL_TWO_SLOW =
            -0.25D;

    private static final double LEVEL_THREE_SLOW =
            -0.35D;


    private static final int LEVEL_ONE_DURATION =
            50; // 2.5 sec

    private static final int LEVEL_TWO_DURATION =
            60; // 3 sec

    private static final int LEVEL_THREE_DURATION =
            70; // 3.5 sec


    @Override
    public String id() {
        return "suppressing";
    }


    @Override
    public String displayName() {
        return "Suppressing";
    }


    @Override
    public void onDamageDealt(
            Mob elite,
            Player target,
            float damageDealt,
            int level
    ) {
        if (
                damageDealt <= 0.0F
                || !target.isAlive()
        ) {
            return;
        }

        AttributeInstance movementSpeed =
                target.getAttribute(
                        Attributes.MOVEMENT_SPEED
                );

        if (movementSpeed == null) {
            return;
        }

        /*
         * Refresh instead of stacking.
         */
        movementSpeed.removeModifier(
                SPEED_MODIFIER_ID
        );

        movementSpeed.addTransientModifier(
                new AttributeModifier(
                        SPEED_MODIFIER_ID,
                        "The Flood Suppressing",
                        getSlowAmount(
                                level
                        ),
                        AttributeModifier.Operation.MULTIPLY_TOTAL
                )
        );

        EXPIRATIONS.put(
                target.getUUID(),
                target.level()
                        .getGameTime()
                        + getDuration(
                                level
                        )
        );
    }


    public static void tickPlayer(
            Player player
    ) {
        Long expiration =
                EXPIRATIONS.get(
                        player.getUUID()
                );

        if (expiration == null) {
            return;
        }

        if (
                player.level()
                        .getGameTime()
                        < expiration
        ) {
            return;
        }

        removeSuppression(
                player
        );
    }


    public static void removeSuppression(
            Player player
    ) {
        AttributeInstance movementSpeed =
                player.getAttribute(
                        Attributes.MOVEMENT_SPEED
                );

        if (movementSpeed != null) {
            movementSpeed.removeModifier(
                    SPEED_MODIFIER_ID
            );
        }

        EXPIRATIONS.remove(
                player.getUUID()
        );
    }


    private static double getSlowAmount(
            int level
    ) {
        return switch (level) {

            case 2 ->
                    LEVEL_TWO_SLOW;

            case 3 ->
                    LEVEL_THREE_SLOW;

            default ->
                    LEVEL_ONE_SLOW;
        };
    }


    private static int getDuration(
            int level
    ) {
        return switch (level) {

            case 2 ->
                    LEVEL_TWO_DURATION;

            case 3 ->
                    LEVEL_THREE_DURATION;

            default ->
                    LEVEL_ONE_DURATION;
        };
    }
}