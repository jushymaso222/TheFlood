package com.jushymaso222.theflood.progression.milestone;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class MilestoneData {

    private static final String ROOT_KEY =
            "theflood_milestones";

    private MilestoneData() {
    }

    private static CompoundTag getData(
            ServerPlayer player
    ) {
        CompoundTag persistent =
                player.getPersistentData();

        if (
                !persistent.contains(
                        ROOT_KEY
                )
        ) {
            persistent.put(
                    ROOT_KEY,
                    new CompoundTag()
            );
        }

        return persistent.getCompound(
                ROOT_KEY
        );
    }

    public static boolean hasAchieved(
            ServerPlayer player,
            ResourceLocation id
    ) {
        return getData(
                player
        ).getBoolean(
                id.toString()
        );
    }

    public static void copy(
            ServerPlayer oldPlayer,
            ServerPlayer newPlayer
    ) {
        CompoundTag oldPersistent =
                oldPlayer.getPersistentData();

        if (!oldPersistent.contains(ROOT_KEY)) {
            return;
        }

        CompoundTag oldMilestones =
                oldPersistent.getCompound(
                        ROOT_KEY
                );

        newPlayer.getPersistentData()
                .put(
                        ROOT_KEY,
                        oldMilestones.copy()
                );
    }

    public static void markAchieved(
            ServerPlayer player,
            ResourceLocation id
    ) {
        getData(
                player
        ).putBoolean(
                id.toString(),
                true
        );
    }
}