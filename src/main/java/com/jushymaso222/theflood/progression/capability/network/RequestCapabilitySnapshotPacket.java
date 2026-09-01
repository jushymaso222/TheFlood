package com.jushymaso222.theflood.progression.capability.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class RequestCapabilitySnapshotPacket {

    public RequestCapabilitySnapshotPacket() {
    }

    public static void encode(
            RequestCapabilitySnapshotPacket message,
            FriendlyByteBuf buffer
    ) {
        /*
         * No data needed.
         */
    }

    public static RequestCapabilitySnapshotPacket decode(
            FriendlyByteBuf buffer
    ) {
        return new RequestCapabilitySnapshotPacket();
    }

    public static void handle(
            RequestCapabilitySnapshotPacket message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context =
                contextSupplier.get();

        context.enqueueWork(
                () -> {
                    ServerPlayer player =
                            context.getSender();

                    if (player == null) {
                        return;
                    }

                    SyncCapabilityStatsPacket.sendSnapshot(
                            player,
                            false
                    );
                }
        );

        context.setPacketHandled(
                true
        );
    }
}