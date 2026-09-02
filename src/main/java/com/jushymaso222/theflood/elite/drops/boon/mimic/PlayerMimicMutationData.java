package com.jushymaso222.theflood.elite.drops.boon.mimic;

import net.minecraft.server.level.ServerPlayer;

public final class PlayerMimicMutationData {

    private static final String ACTIVE_MUTATION_KEY =
            "theflood_active_mimic_mutation";


    private PlayerMimicMutationData() {
    }


    public static PlayerMimicMutation getActiveMutation(
            ServerPlayer player
    ) {
        if (player == null) {
            return null;
        }

        String id =
                player.getPersistentData()
                        .getString(
                                ACTIVE_MUTATION_KEY
                        );

        return PlayerMimicMutation.fromId(
                id
        );
    }


    public static boolean hasActiveMutation(
            ServerPlayer player
    ) {
        return getActiveMutation(
                player
        ) != null;
    }


    public static void setActiveMutation(
            ServerPlayer player,
            PlayerMimicMutation mutation
    ) {
        if (
                player == null
                || mutation == null
        ) {
            return;
        }

        player.getPersistentData()
                .putString(
                        ACTIVE_MUTATION_KEY,
                        mutation.id()
                );
    }


    public static void clear(
            ServerPlayer player
    ) {
        if (player == null) {
            return;
        }

        player.getPersistentData()
                .remove(
                        ACTIVE_MUTATION_KEY
                );
    }
}