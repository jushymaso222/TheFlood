package com.jushymaso222.theflood.elite.drops.boon.mimic;

import com.jushymaso222.theflood.elite.drops.boon.BoonData;
import com.jushymaso222.theflood.elite.drops.boon.BoonManager;
import com.jushymaso222.theflood.elite.drops.boon.BoonType;

import com.jushymaso222.theflood.elite.drops.boon.mimic.network.SyncMimicMutationPacket;
import com.jushymaso222.theflood.network.FloodNetwork;

import net.minecraftforge.network.PacketDistributor;

import net.minecraft.server.level.ServerPlayer;

public final class PlayerMimicMutationManager {

    private PlayerMimicMutationManager() {
    }


    public static boolean activate(
            ServerPlayer player,
            PlayerMimicMutation mutation
    ) {
        if (
                player == null
                || mutation == null
        ) {
            return false;
        }

        /*
         * For now, do not allow activation while another
         * generic Boon is active.
         *
         * Later, if the active Boon is already MIMIC,
         * we can decide whether a new Mimic Boon should
         * replace the existing mutation.
         */
        if (
                BoonData.hasActiveBoon(
                        player
                )
        ) {
            return false;
        }

        boolean activated =
                BoonManager.tryActivate(
                        player,
                        BoonType.MIMIC
                );

        if (!activated) {
            return false;
        }

        PlayerMimicMutationData.setActiveMutation(
                player,
                mutation
        );

        sync(
                player
        );

        return true;
    }

    private static void sync(
            ServerPlayer player
    ) {
        PlayerMimicMutation mutation =
                PlayerMimicMutationData.getActiveMutation(
                        player
                );

        long remainingTicks =
                BoonData.getRemainingTicks(
                        player
                );

        FloodNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(
                        () -> player
                ),
                new SyncMimicMutationPacket(
                        mutation,
                        remainingTicks
                )
        );
    }


    public static void deactivate(
            ServerPlayer player
    ) {
        if (player == null) {
            return;
        }

        PlayerMimicMutation mutation =
                PlayerMimicMutationData.getActiveMutation(
                        player
                );

        /*
         * Later:
         *
         * switch (mutation) {
         *     case SPIKED -> cleanup Spiked state
         *     case SHIFTING -> cleanup Shifting state
         *     ...
         * }
         */

        PlayerMimicMutationData.clear(
                player
        );

        sync(
                player
        );
    }


    public static void tick(
            ServerPlayer player
    ) {
        if (
                player == null
                || !BoonData.hasBoon(
                        player,
                        BoonType.MIMIC
                )
        ) {
            return;
        }

        PlayerMimicMutation mutation =
                PlayerMimicMutationData.getActiveMutation(
                        player
                );

        if (mutation == null) {
            return;
        }

        /*
         * Later:
         *
         * switch (mutation) {
         *     case SPIKED -> tick Spiked
         *     case SHIFTING -> tick Shifting
         *     case UNDYING -> tick Undying
         *     case FRENZIED -> tick Frenzied
         *     case COMMANDER -> tick Commander
         * }
         */
    }
}