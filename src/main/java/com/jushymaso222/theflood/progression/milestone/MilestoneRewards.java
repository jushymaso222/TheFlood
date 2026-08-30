package com.jushymaso222.theflood.progression.milestone;

import com.jushymaso222.theflood.progression.HeatManager;

import net.minecraft.server.level.ServerPlayer;

public final class MilestoneRewards {

    private MilestoneRewards() {
    }


    public static long awardPersonal(
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
         * Personal milestone rewards are based on the
         * player's Solo Heat, even if they are currently
         * part of a team.
         */
        long requiredXp =
                HeatManager.getBaseFloodXpRequired(
                        HeatManager.getSoloHeat(
                                player
                        )
                );

        long rewardXp =
                calculateReward(
                        requiredXp,
                        rewardFraction
                );

        /*
         * Always writes directly into the player's
         * personal Solo progression.
         */
        HeatManager.addSoloFloodXp(
                player,
                rewardXp
        );

        return rewardXp;
    }


    public static long awardTeam(
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
         * Team milestone rewards are based on the
         * currently active/team Heat requirement.
         */
        long requiredXp =
                HeatManager.getFloodXpRequired(
                        player
                );

        long rewardXp =
                calculateReward(
                        requiredXp,
                        rewardFraction
                );

        /*
         * Normal Flood XP path.
         *
         * Since the player is on a team here,
         * this goes into shared Team Flood XP.
         */
        HeatManager.addFloodXp(
                player,
                rewardXp
        );

        return rewardXp;
    }


    private static long calculateReward(
            long requiredXp,
            double rewardFraction
    ) {
        return Math.max(
                1L,
                Math.round(
                        requiredXp
                                * rewardFraction
                )
        );
    }
}