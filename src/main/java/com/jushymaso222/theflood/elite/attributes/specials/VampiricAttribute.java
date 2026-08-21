package com.jushymaso222.theflood.elite.attributes.specials;

import com.jushymaso222.theflood.elite.attributes.SpecialAttribute;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public final class VampiricAttribute
        implements SpecialAttribute {

    /*
     * =====================================================
     * BALANCE
     * =====================================================
     *
     * Percentage of successful player damage
     * returned to the Elite as health.
     *
     * Vampiric    = 10%
     * Vampiric+   = 17.5%
     * Vampiric++  = 25%
     */

    private static final float LEVEL_ONE_LIFESTEAL =
            0.10F;

    private static final float LEVEL_TWO_LIFESTEAL =
            0.175F;

    private static final float LEVEL_THREE_LIFESTEAL =
            0.25F;


    @Override
    public String id() {
        return "vampiric";
    }


    @Override
    public String displayName() {
        return "Vampiric";
    }


    /*
     * Called after the Elite successfully damages
     * a player.
     *
     * The Elite heals for a percentage of the
     * damage that was actually dealt.
     */
    @Override
    public void onDamageDealt(
            Mob elite,
            Player target,
            float damageDealt,
            int level
    ) {
        /*
         * Nothing to steal if no damage was dealt.
         */
        if (damageDealt <= 0.0F) {
            return;
        }

        /*
         * No reason to process healing if the
         * Elite is already dead.
         */
        if (!elite.isAlive()) {
            return;
        }

        float lifestealPercent =
                getLifestealPercent(
                        level
                );

        float healing =
                damageDealt
                        * lifestealPercent;

        if (healing <= 0.0F) {
            return;
        }

        elite.heal(
                healing
        );
    }


    private static float getLifestealPercent(
            int level
    ) {
        return switch (level) {

            case 2 ->
                    LEVEL_TWO_LIFESTEAL;

            case 3 ->
                    LEVEL_THREE_LIFESTEAL;

            default ->
                    LEVEL_ONE_LIFESTEAL;
        };
    }
}