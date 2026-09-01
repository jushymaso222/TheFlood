package com.jushymaso222.theflood.progression.milestone;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.jushymaso222.theflood.network.FloodNetwork;
import net.minecraftforge.network.PacketDistributor;
import com.jushymaso222.theflood.milestone.network.MilestoneUnlockedPacket;

import com.jushymaso222.theflood.team.FloodTeam;
import com.jushymaso222.theflood.team.FloodTeamSavedData;
import com.jushymaso222.theflood.team.TeamManager;

import java.util.ArrayList;
import java.util.List;

public final class MilestoneManager {

    private MilestoneManager() {
    }

    public static void evaluate(
            ServerPlayer player
    ) {
        if (player == null) {
            return;
        }

        for (
                MilestoneDefinition milestone :
                MilestoneRegistry.all()
        ) {
            /*
             * Already permanently achieved.
             */
            if (
                    MilestoneData.hasAchieved(
                            player,
                            milestone.id()
                    )
            ) {
                continue;
            }

            /*
             * Player just satisfied it.
             */
            if (
        milestone.matches(
                        player
                )
        ) {
        complete(
                player,
                milestone.id()
        );
        }
        }
    }

    public static boolean complete(
        ServerPlayer player,
        ResourceLocation milestoneId
) {
    if (player == null || milestoneId == null) {
        return false;
    }

    MilestoneDefinition milestone =
            MilestoneRegistry.get(
                    milestoneId
            );

    if (milestone == null) {
        return false;
    }

    /*
     * Already permanently achieved.
     */
    if (
            MilestoneData.hasAchieved(
                    player,
                    milestone.id()
            )
    ) {
        return false;
    }


    /*
     * Permanently complete it.
     */
    MilestoneData.markAchieved(
            player,
            milestone.id()
    );


    FloodTeam team =
            TeamManager.getTeamForPlayer(
                    player
            );


    /*
     * Personal reward.
     */
    MilestoneRewards.awardPersonal(
            player,
            milestone
    );


    /*
     * Team reward.
     */
    if (team != null) {

        boolean firstTeamCompletion =
                team.completeMilestone(
                        milestone.id()
                );

        if (firstTeamCompletion) {

            FloodTeamSavedData
                    .get(player.server)
                    .setDirty();

            MilestoneRewards.awardTeam(
                    player,
                    milestone
            );
        }
    }


    /*
     * Client notification + immediate client
     * milestone state update.
     */
    FloodNetwork.CHANNEL.send(
            PacketDistributor.PLAYER.with(
                    () -> player
            ),
            new MilestoneUnlockedPacket(
                    milestone.id(),
                    milestone.title(),
                    milestone.description(),
                    milestone.progressionValue(),
                    milestone.floodXpReward()
            )
    );

    return true;
}

    public static MilestoneSnapshot getSnapshot(
            ServerPlayer player
    ) {
        List<ResourceLocation> achieved =
                new ArrayList<>();

        int progressionValue =
                0;

        for (
                MilestoneDefinition milestone :
                MilestoneRegistry.all()
        ) {
            if (
                    !MilestoneData.hasAchieved(
                            player,
                            milestone.id()
                    )
            ) {
                continue;
            }

            achieved.add(
                    milestone.id()
            );

            progressionValue =
                    Math.max(
                            progressionValue,
                            milestone.progressionValue()
                    );
        }

        return new MilestoneSnapshot(
                progressionValue,
                List.copyOf(
                        achieved
                ),
                MilestoneData.getCompletionHistory(
                        player
                )
        );

    }

    public static int getProgressionValue(
            ServerPlayer player
    ) {
        return getSnapshot(
                player
        ).progressionValue();
    }
}