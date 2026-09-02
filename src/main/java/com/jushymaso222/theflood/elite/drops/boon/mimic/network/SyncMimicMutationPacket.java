package com.jushymaso222.theflood.elite.drops.boon.mimic.network;

import com.jushymaso222.theflood.elite.drops.boon.mimic.PlayerMimicMutation;
import com.jushymaso222.theflood.elite.drops.boon.mimic.client.ClientMimicMutationData;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class SyncMimicMutationPacket {

    private final String mutationId;
    private final long remainingTicks;


    public SyncMimicMutationPacket(
            PlayerMimicMutation mutation,
            long remainingTicks
    ) {
        this.mutationId =
                mutation != null
                        ? mutation.id()
                        : "";

        this.remainingTicks =
                remainingTicks;
    }


    private SyncMimicMutationPacket(
            String mutationId,
            long remainingTicks
    ) {
        this.mutationId =
                mutationId;

        this.remainingTicks =
                remainingTicks;
    }


    public static void encode(
            SyncMimicMutationPacket packet,
            FriendlyByteBuf buffer
    ) {
        buffer.writeUtf(
                packet.mutationId
        );

        buffer.writeLong(
                packet.remainingTicks
        );
    }


    public static SyncMimicMutationPacket decode(
            FriendlyByteBuf buffer
    ) {
        return new SyncMimicMutationPacket(
                buffer.readUtf(),
                buffer.readLong()
        );
    }


    public static void handle(
            SyncMimicMutationPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context =
                contextSupplier.get();

        context.enqueueWork(
                () -> {
                    PlayerMimicMutation mutation =
                            PlayerMimicMutation.fromId(
                                    packet.mutationId
                            );

                    if (
                            mutation == null
                            || packet.remainingTicks <= 0L
                    ) {
                        ClientMimicMutationData.clear();

                        return;
                    }

                    ClientMimicMutationData.set(
                            mutation,
                            packet.remainingTicks
                    );
                }
        );

        context.setPacketHandled(
                true
        );
    }
}