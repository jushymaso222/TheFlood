package com.jushymaso222.theflood.elite;

import com.jushymaso222.theflood.elite.network.SyncEliteStatePacket;
import com.jushymaso222.theflood.network.FloodNetwork;

import net.minecraft.world.entity.Mob;

import net.minecraftforge.network.PacketDistributor;

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
        FloodNetwork.CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(
                        () -> elite
                ),
                new SyncEliteStatePacket(
                        elite.getId(),
                        EliteData.getMutation(elite),
                        progress,
                        maxProgress,
                        active,
                        color
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
}