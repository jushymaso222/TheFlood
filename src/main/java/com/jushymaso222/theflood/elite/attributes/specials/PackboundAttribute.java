package com.jushymaso222.theflood.elite.attributes.specials;

import com.jushymaso222.theflood.elite.EliteData;
import com.jushymaso222.theflood.elite.attributes.SpecialAttribute;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.List;
import java.util.UUID;

public final class PackboundAttribute
        implements SpecialAttribute {

    /*
     * =====================================================
     * MODIFIER IDS
     * =====================================================
     */

    private static final UUID DAMAGE_MODIFIER_ID =
            UUID.fromString(
                    "6bb91684-5046-47df-a264-12d267e394d1"
            );

    private static final UUID SPEED_MODIFIER_ID =
            UUID.fromString(
                    "21650c98-c21a-43d7-bbf5-d409e4bd3c12"
            );

    private static final UUID ARMOR_MODIFIER_ID =
            UUID.fromString(
                    "ab755a5a-7a4d-42fb-b574-8e92c4cbdbb5"
            );


    /*
     * =====================================================
     * BALANCE
     * =====================================================
     */

    private static final double SEARCH_RADIUS =
            10.0D;


    /*
     * Bonus PER nearby Flood mob.
     */

    private static final double LEVEL_ONE_DAMAGE =
            0.04D;

    private static final double LEVEL_TWO_DAMAGE =
            0.05D;

    private static final double LEVEL_THREE_DAMAGE =
            0.06D;


    private static final double LEVEL_ONE_SPEED =
            0.03D;

    private static final double LEVEL_TWO_SPEED =
            0.04D;

    private static final double LEVEL_THREE_SPEED =
            0.05D;


    private static final double LEVEL_ONE_ARMOR =
            0.04D;

    private static final double LEVEL_TWO_ARMOR =
            0.05D;

    private static final double LEVEL_THREE_ARMOR =
            0.06D;


    private static final int LEVEL_ONE_MAX_ALLIES =
            5;

    private static final int LEVEL_TWO_MAX_ALLIES =
            6;

    private static final int LEVEL_THREE_MAX_ALLIES =
            7;


    /*
     * We don't need to scan every single tick.
     *
     * Four updates per second is plenty responsive
     * for this mechanic.
     */
    private static final int UPDATE_INTERVAL =
            5;


    @Override
    public String id() {
        return "packbound";
    }


    @Override
    public String displayName() {
        return "Packbound";
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
                elite.tickCount
                        % UPDATE_INTERVAL
                        != 0
        ) {
            return;
        }

        if (
                !(elite.level() instanceof ServerLevel serverLevel)
        ) {
            return;
        }

        int nearbyAllies =
                countNearbyAllies(
                        serverLevel,
                        elite,
                        level
                );

        applyBonuses(
                elite,
                level,
                nearbyAllies
        );
    }


    /*
     * =====================================================
     * ALLY COUNT
     * =====================================================
     */

    private static int countNearbyAllies(
            ServerLevel level,
            Mob elite,
            int attributeLevel
    ) {
        int maximum =
                getMaxAllies(
                        attributeLevel
                );

        List<Mob> nearby =
                level.getEntitiesOfClass(
                        Mob.class,
                        elite.getBoundingBox()
                                .inflate(
                                        SEARCH_RADIUS
                                ),
                        mob ->
                                isValidAlly(
                                        elite,
                                        mob
                                )
                );

        return Math.min(
                nearby.size(),
                maximum
        );
    }


    private static boolean isValidAlly(
            Mob elite,
            Mob mob
    ) {
        if (
                mob == elite
                || !mob.isAlive()
        ) {
            return false;
        }

        /*
         * Don't count other Elites.
         *
         * Packbound measures the pack surrounding
         * the Elite, not other Elite encounters.
         */
        if (
                EliteData.isElite(
                        mob
                )
        ) {
            return false;
        }

        return isFloodControlled(
                mob
        );
    }


    /*
     * =====================================================
     * BONUSES
     * =====================================================
     */

    private static void applyBonuses(
            Mob elite,
            int level,
            int allies
    ) {
        /*
         * Remove the previous Packbound values first.
         *
         * This lets the strength update immediately as
         * members of the pack die or leave the radius.
         */
        removeModifier(
                elite,
                Attributes.ATTACK_DAMAGE,
                DAMAGE_MODIFIER_ID
        );

        removeModifier(
                elite,
                Attributes.MOVEMENT_SPEED,
                SPEED_MODIFIER_ID
        );

        removeModifier(
                elite,
                Attributes.ARMOR,
                ARMOR_MODIFIER_ID
        );


        /*
         * Isolated Packbound = no bonus.
         */
        if (allies <= 0) {
            return;
        }


        double damageBonus =
                getDamagePerAlly(
                        level
                )
                        * allies;

        double speedBonus =
                getSpeedPerAlly(
                        level
                )
                        * allies;

        double armorBonus =
                getArmorPerAlly(
                        level
                )
                        * allies;


        addModifier(
                elite,
                Attributes.ATTACK_DAMAGE,
                DAMAGE_MODIFIER_ID,
                "The Flood Packbound Damage",
                damageBonus
        );

        addModifier(
                elite,
                Attributes.MOVEMENT_SPEED,
                SPEED_MODIFIER_ID,
                "The Flood Packbound Speed",
                speedBonus
        );

        addModifier(
                elite,
                Attributes.ARMOR,
                ARMOR_MODIFIER_ID,
                "The Flood Packbound Armor",
                armorBonus
        );
    }


    /*
     * =====================================================
     * ATTRIBUTE HELPERS
     * =====================================================
     */

    private static void addModifier(
            Mob elite,
            net.minecraft.world.entity.ai.attributes.Attribute attribute,
            UUID id,
            String name,
            double amount
    ) {
        AttributeInstance instance =
                elite.getAttribute(
                        attribute
                );

        if (instance == null) {
            return;
        }

        instance.addTransientModifier(
                new AttributeModifier(
                        id,
                        name,
                        amount,
                        AttributeModifier.Operation.MULTIPLY_TOTAL
                )
        );
    }


    private static void removeModifier(
            Mob elite,
            net.minecraft.world.entity.ai.attributes.Attribute attribute,
            UUID id
    ) {
        AttributeInstance instance =
                elite.getAttribute(
                        attribute
                );

        if (instance == null) {
            return;
        }

        instance.removeModifier(
                id
        );
    }


    /*
     * =====================================================
     * LEVEL VALUES
     * =====================================================
     */

    private static int getMaxAllies(
            int level
    ) {
        return switch (level) {

            case 2 ->
                    LEVEL_TWO_MAX_ALLIES;

            case 3 ->
                    LEVEL_THREE_MAX_ALLIES;

            default ->
                    LEVEL_ONE_MAX_ALLIES;
        };
    }


    private static double getDamagePerAlly(
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


    private static double getSpeedPerAlly(
            int level
    ) {
        return switch (level) {

            case 2 ->
                    LEVEL_TWO_SPEED;

            case 3 ->
                    LEVEL_THREE_SPEED;

            default ->
                    LEVEL_ONE_SPEED;
        };
    }


    private static double getArmorPerAlly(
            int level
    ) {
        return switch (level) {

            case 2 ->
                    LEVEL_TWO_ARMOR;

            case 3 ->
                    LEVEL_THREE_ARMOR;

            default ->
                    LEVEL_ONE_ARMOR;
        };
    }


    /*
     * =====================================================
     * FLOOD CHECK
     * =====================================================
     *
     * Use the same implementation you used for
     * SiphoningAttribute.
     */

    private static boolean isFloodControlled(
            Mob mob
    ) {
        return mob.getPersistentData()
                .getBoolean(
                        "theflood_controlled"
                );
    }
}