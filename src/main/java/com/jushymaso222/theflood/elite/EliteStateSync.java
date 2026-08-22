package com.jushymaso222.theflood.elite;

import com.jushymaso222.theflood.elite.network.SyncEliteStatePacket;
import com.jushymaso222.theflood.network.FloodNetwork;

import net.minecraft.world.entity.Mob;

import net.minecraftforge.network.PacketDistributor;

import com.jushymaso222.theflood.elite.behavior.mutations.MimicMutation;
import com.jushymaso222.theflood.elite.presentation.ElitePoseController;

import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class EliteStateSync {

    private EliteStateSync() {
    }

    public static void sync(
                Mob elite,
                float progress,
                float maxProgress,
                boolean active,
                int color
        ) {
        String mutationId =
                EliteData.getMutation(
                        elite
                );

        boolean isMimic =
                "mimic".equals(
                        mutationId
                );

        String copiedMutationId =
                isMimic
                        ? MimicMutation.getCopiedMutationId(
                                elite
                        )
                        : "";

        boolean mimicRevealed =
                isMimic
                        && MimicMutation.isRevealed(
                                elite
                        );
        
        List<String> attributes =
                EliteData.getAttributes(
                        elite
                );

        String poseId =
                ElitePoseController.getPose(
                        elite
                ).name();

        FloodNetwork.CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(
                        () -> elite
                ),
                new SyncEliteStatePacket(
                        elite.getId(),
                        mutationId,
                        copiedMutationId,
                        mimicRevealed,
                        attributes,
                        progress,
                        maxProgress,
                        active,
                        color,
                        poseId
                )
        );
        }

    public static void syncBasic(
            Mob elite
    ) {
        sync(
                elite,
                0.0F,
                0.0F,
                false,
                0xFFFFFFFF
        );
    }

    public static void syncBasicTo(
                Mob elite,
                ServerPlayer player
        ) {
        String mutationId =
                EliteData.getMutation(
                        elite
                );

        boolean isMimic =
                "mimic".equals(
                        mutationId
                );

        String copiedMutationId =
                isMimic
                        ? MimicMutation.getCopiedMutationId(
                                elite
                        )
                        : "";

        boolean mimicRevealed =
                isMimic
                        && MimicMutation.isRevealed(
                                elite
                        );

        List<String> attributes =
                EliteData.getAttributes(
                        elite
                );

        String poseId =
                ElitePoseController.getPose(
                        elite
                ).name();

        FloodNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(
                        () -> player
                ),
                new SyncEliteStatePacket(
                        elite.getId(),
                        mutationId,
                        copiedMutationId,
                        mimicRevealed,
                        attributes,
                        0.0F,
                        0.0F,
                        false,
                        0xFFFFFFFF,
                        poseId
                )
        );
        }
}