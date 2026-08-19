package com.jushymaso222.theflood.elite.behavior.mutations;

import com.jushymaso222.theflood.elite.EliteStateSync;
import com.jushymaso222.theflood.elite.behavior.EliteMutation;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public final class FrenziedMutation
        implements EliteMutation {

    private static final String ANGER_KEY =
            "theflood_frenzied_anger";

    private static final String LAST_HURT_TIME_KEY =
            "theflood_frenzied_last_hurt_time";

    private static final float MAX_ANGER =
            100.0F;

    private static final UUID SPEED_MODIFIER_ID =
            UUID.fromString(
                    "16d61a88-46c5-4c64-933e-b80be84d76dd"
            );

    private static final UUID KNOCKBACK_RESISTANCE_MODIFIER_ID =
            UUID.fromString(
                    "8f36b3a2-6e2d-4e26-9f85-76f4bb1db4a2"
            );

    /*
    * Maximum +50% movement speed at full anger.
    */
    private static final double MAX_SPEED_BONUS =
            1.00D;

    private static final double MAX_KNOCKBACK_RESISTANCE_BONUS =
            0.60D;

    /*
     * How much anger is generated per point
     * of incoming damage.
     *
     * 4.0 means 5 incoming damage adds 20 anger.
     */
    private static final float ANGER_PER_DAMAGE =
            4.0F;

    /*
     * Anger does not begin decaying until the
     * Elite has gone this long without being hit.
     */
    private static final int ANGER_DECAY_DELAY_TICKS =
            60; // 3 seconds

    /*
     * Amount of anger lost every server tick
     * after the decay delay has passed.
     *
     * 0.20/tick = 4 anger per second.
     */
    private static final float ANGER_DECAY_PER_TICK =
            0.20F;

    /*
    * At maximum anger, outgoing damage is increased
    * by 75%.
    */
    private static final float MAX_DAMAGE_BONUS =
            0.75F;

    /*
     * Generic Elite status-bar color.
     *
     * Frenzied uses a strong red/orange.
     */
    private static final int STATUS_COLOR =
            0xFFFF4A1C;

    private static void updateKnockbackResistance(
            Mob elite
    ) {
        AttributeInstance knockbackResistance =
                elite.getAttribute(
                        Attributes.KNOCKBACK_RESISTANCE
                );

        if (knockbackResistance == null) {
            return;
        }

        AttributeModifier existing =
                knockbackResistance.getModifier(
                        KNOCKBACK_RESISTANCE_MODIFIER_ID
                );

        if (existing != null) {
            knockbackResistance.removeModifier(
                    existing
            );
        }

        float angerPercent =
                getAngerPercent(
                        elite
                );

        if (angerPercent <= 0.0F) {
            return;
        }

        double resistanceBonus =
                MAX_KNOCKBACK_RESISTANCE_BONUS
                        * angerPercent;

        knockbackResistance.addTransientModifier(
                new AttributeModifier(
                        KNOCKBACK_RESISTANCE_MODIFIER_ID,
                        "The Flood Frenzied knockback resistance",
                        resistanceBonus,
                        AttributeModifier.Operation.ADDITION
                )
        );
    }

    @Override
    public String id() {
        return "frenzied";
    }

    @Override
    public String displayName() {
        return "Frenzied";
    }

    @Override
    public float modifyIncomingDamage(
            Mob elite,
            DamageSource source,
            float damage
    ) {
        /*
         * Being hurt enrages the Elite.
         *
         * Use the original incoming damage so powerful
         * attacks generate more anger than weak attacks.
         */
        float currentAnger =
                getAnger(
                        elite
                );

        float angerAdded =
                damage
                        * ANGER_PER_DAMAGE;

        float newAnger =
                Math.min(
                        MAX_ANGER,
                        currentAnger
                                + angerAdded
                );

        setAnger(
                elite,
                newAnger
        );

        updateMovementSpeed(
                elite
        );

        updateKnockbackResistance(
                elite
        );

        elite.getPersistentData()
                .putLong(
                        LAST_HURT_TIME_KEY,
                        elite.level()
                                .getGameTime()
                );

        syncState(
                elite
        );

        /*
         * Frenzied does not modify incoming damage.
         */
        return damage;
    }

    @Override
    public float modifyOutgoingDamage(
            Mob elite,
            float damage
    ) {
        float angerPercent =
                getAngerPercent(
                        elite
                );

        float damageMultiplier =
                1.0F
                        + (
                        angerPercent
                                * MAX_DAMAGE_BONUS
                );

        return damage
                * damageMultiplier;
    }

    private static void updateMovementSpeed(
            Mob elite
    ) {
        AttributeInstance movementSpeed =
                elite.getAttribute(
                        Attributes.MOVEMENT_SPEED
                );

        if (movementSpeed == null) {
            return;
        }

        /*
        * Remove the previous dynamic Frenzied modifier
        * before applying the new value.
        */
        AttributeModifier existing =
                movementSpeed.getModifier(
                        SPEED_MODIFIER_ID
                );

        if (existing != null) {
            movementSpeed.removeModifier(
                    existing
            );
        }

        float angerPercent =
                getAngerPercent(
                        elite
                );

        if (angerPercent <= 0.0F) {
            return;
        }

        double speedBonus =
                MAX_SPEED_BONUS
                        * angerPercent;

        movementSpeed.addTransientModifier(
                new AttributeModifier(
                        SPEED_MODIFIER_ID,
                        "The Flood Frenzied speed",
                        speedBonus,
                        AttributeModifier.Operation.MULTIPLY_TOTAL
                )
        );
    }

    @Override
    public void tick(
            Mob elite
    ) {
        float anger =
                getAnger(
                        elite
                );

        if (anger <= 0.0F) {
            return;
        }

        long lastHurtTime =
                elite.getPersistentData()
                        .getLong(
                                LAST_HURT_TIME_KEY
                        );

        long gameTime =
                elite.level()
                        .getGameTime();

        /*
         * Stay enraged for a short time after being hit.
         */
        if (
                gameTime - lastHurtTime
                        < ANGER_DECAY_DELAY_TICKS
        ) {
            return;
        }

        float newAnger =
                Math.max(
                        0.0F,
                        anger
                                - ANGER_DECAY_PER_TICK
                );

        /*
         * Avoid unnecessary NBT writes and packets
         * when the value effectively hasn't changed.
         */
        if (newAnger == anger) {
            return;
        }

        setAnger(
                elite,
                newAnger
        );

        updateMovementSpeed(
                elite
        );

        updateKnockbackResistance(
                elite
        );

        syncState(
                elite
        );
    }

    private static float getAnger(
            Mob elite
    ) {
        return elite.getPersistentData()
                .getFloat(
                        ANGER_KEY
                );
    }

    private static void setAnger(
            Mob elite,
            float anger
    ) {
        elite.getPersistentData()
                .putFloat(
                        ANGER_KEY,
                        Math.max(
                                0.0F,
                                Math.min(
                                        MAX_ANGER,
                                        anger
                                )
                        )
                );
    }

    private static void syncState(
            Mob elite
    ) {
        EliteStateSync.sync(
                elite,
                getAnger(
                        elite
                ),
                MAX_ANGER,
                true,
                STATUS_COLOR
        );
    }

    public static float getAngerPercent(
            Mob elite
    ) {
        return getAnger(
                elite
        ) / MAX_ANGER;
    }
}