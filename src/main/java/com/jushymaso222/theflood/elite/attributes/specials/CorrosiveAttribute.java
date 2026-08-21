package com.jushymaso222.theflood.elite.attributes.specials;

import com.jushymaso222.theflood.elite.attributes.SpecialAttribute;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class CorrosiveAttribute
        implements SpecialAttribute {

    /*
     * One shared modifier UUID means repeated hits
     * refresh/replace the effect instead of stacking
     * multiple Corrosive penalties.
     */
    private static final UUID ARMOR_MODIFIER_ID =
            UUID.fromString(
                    "83377e81-63b2-4764-8ba5-73795e207b67"
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

    private static final double LEVEL_ONE_ARMOR_REDUCTION =
            -0.15D;

    private static final double LEVEL_TWO_ARMOR_REDUCTION =
            -0.25D;

    private static final double LEVEL_THREE_ARMOR_REDUCTION =
            -0.35D;


    private static final int LEVEL_ONE_DURATION =
            80; // 4 sec

    private static final int LEVEL_TWO_DURATION =
            100; // 5 sec

    private static final int LEVEL_THREE_DURATION =
            120; // 6 sec;


    @Override
    public String id() {
        return "corrosive";
    }


    @Override
    public String displayName() {
        return "Corrosive";
    }


    /*
     * Called after the Elite successfully damages
     * a player.
     */
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

        AttributeInstance armor =
                target.getAttribute(
                        Attributes.ARMOR
                );

        if (armor == null) {
            return;
        }

        /*
         * Remove the old version so a stronger level
         * can replace it and repeated hits don't stack.
         */
        armor.removeModifier(
                ARMOR_MODIFIER_ID
        );

        armor.addTransientModifier(
                new AttributeModifier(
                        ARMOR_MODIFIER_ID,
                        "The Flood Corrosive",
                        getArmorReduction(
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


    /*
     * This gets called from our general player-tick
     * cleanup hook.
     */
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

        removeCorrosion(
                player
        );
    }


    public static void removeCorrosion(
            Player player
    ) {
        AttributeInstance armor =
                player.getAttribute(
                        Attributes.ARMOR
                );

        if (armor != null) {
            armor.removeModifier(
                    ARMOR_MODIFIER_ID
            );
        }

        EXPIRATIONS.remove(
                player.getUUID()
        );
    }


    private static double getArmorReduction(
            int level
    ) {
        return switch (level) {

            case 2 ->
                    LEVEL_TWO_ARMOR_REDUCTION;

            case 3 ->
                    LEVEL_THREE_ARMOR_REDUCTION;

            default ->
                    LEVEL_ONE_ARMOR_REDUCTION;
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