package com.jushymaso222.theflood.progression.milestone;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.jushymaso222.theflood.network.FloodNetwork;
import net.minecraftforge.network.PacketDistributor;
import com.jushymaso222.theflood.milestone.network.MilestoneUnlockedPacket;

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
                /*
                * Mark FIRST.
                *
                * This prevents any possibility of the
                * reward/notification path causing this
                * milestone to trigger twice.
                */
                MilestoneData.markAchieved(
                        player,
                        milestone.id()
                );


                /*
                * Reward.
                */
                MilestoneRewards.award(
                        player,
                        milestone
                );


                /*
                * Client notification.
                */
                FloodNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(
                                () -> player
                        ),
                        new MilestoneUnlockedPacket(
                                milestone.title(),
                                milestone.description(),
                                milestone.progressionValue(),
                                milestone.floodXpReward()
                        )
                );
            }
        }
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