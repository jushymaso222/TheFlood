package com.jushymaso222.theflood.progression.network;

import com.jushymaso222.theflood.progression.client.ClientFloodXpGain;

import net.minecraft.network.FriendlyByteBuf;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class FloodXpGainPacket {

    private final long amount;

    public FloodXpGainPacket(
            long amount
    ) {
        this.amount =
                amount;
    }

    public static void encode(
            FloodXpGainPacket message,
            FriendlyByteBuf buffer
    ) {
        buffer.writeLong(
                message.amount
        );
    }

    public static FloodXpGainPacket decode(
            FriendlyByteBuf buffer
    ) {
        return new FloodXpGainPacket(
                buffer.readLong()
        );
    }

    public static void handle(
            FloodXpGainPacket message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context =
                contextSupplier.get();

        context.enqueueWork(() -> {
            ClientFloodXpGain.addXp(
                    message.amount
            );
        });

        context.setPacketHandled(
                true
        );
    }
}