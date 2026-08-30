package com.jushymaso222.theflood.progression.milestone;

import com.jushymaso222.theflood.progression.HeatManager;

import net.minecraft.server.level.ServerPlayer;

public final class MilestoneRewards {

    private MilestoneRewards() {
    }


    public static long award(
            ServerPlayer player,
            MilestoneDefinition milestone
    ) {
        if (
                player == null
                || milestone == null
        ) {
            return 0L;
        }


        double rewardFraction =
                milestone.floodXpReward();

        if (rewardFraction <= 0.0D) {
            return 0L;
        }


        /*
         * Use the player's CURRENT Flood XP
         * requirement as the basis for the reward.
         *
         * Example:
         *
         * requirement = 10,000 XP
         * reward      = 0.04
         *
         * awarded XP  = 400
         */
        long requiredXp =
                HeatManager.getFloodXpRequired(
                        player
                );


        long rewardXp =
                Math.max(
                        1L,
                        Math.round(
                                requiredXp
                                        * rewardFraction
                        )
                );


        /*
         * Silent addition.
         *
         * The milestone notification itself
         * communicates the reward, so we don't
         * also want the normal +XP popup.
         */
        HeatManager.addFloodXp(
                player,
                rewardXp
        );


        return rewardXp;
    }
}