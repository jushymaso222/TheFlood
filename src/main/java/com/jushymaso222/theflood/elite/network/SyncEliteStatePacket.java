package com.jushymaso222.theflood.elite.network;

import com.jushymaso222.theflood.elite.client.ClientEliteStateData;

import net.minecraft.network.FriendlyByteBuf;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncEliteStatePacket {

    private final int entityId;
    private final String mutationId;
    private final float statusProgress;
    private final float statusMax;
    private final boolean statusActive;
    private final int statusColor;

    public SyncEliteStatePacket(
            int entityId,
            String mutationId,
            float statusProgress,
            float statusMax,
            boolean statusActive,
            int statusColor
    ) {
        this.entityId = entityId;
        this.mutationId = mutationId;
        this.statusProgress = statusProgress;
        this.statusMax = statusMax;
        this.statusActive = statusActive;
        this.statusColor = statusColor;
    }

    public static void encode(
            SyncEliteStatePacket packet,
            FriendlyByteBuf buffer
    ) {
        buffer.writeVarInt(
                packet.entityId
        );

        buffer.writeUtf(
                packet.mutationId
        );

        buffer.writeFloat(
                packet.statusProgress
        );

        buffer.writeFloat(
                packet.statusMax
        );

        buffer.writeBoolean(
                packet.statusActive
        );

        buffer.writeInt(
                packet.statusColor
        );
    }

    public static SyncEliteStatePacket decode(
            FriendlyByteBuf buffer
    ) {
        return new SyncEliteStatePacket(
                buffer.readVarInt(),
                buffer.readUtf(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readBoolean(),
                buffer.readInt()
        );
    }

    public static void handle(
            SyncEliteStatePacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context =
                contextSupplier.get();

        context.enqueueWork(
                () -> ClientEliteStateData.setState(
                        packet.entityId,
                        packet.mutationId,
                        packet.statusProgress,
                        packet.statusMax,
                        packet.statusActive,
                        packet.statusColor
                )
        );

        context.setPacketHandled(
                true
        );
    }
}