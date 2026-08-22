package com.jushymaso222.theflood.elite.network;

import com.jushymaso222.theflood.elite.client.ClientEliteStateData;

import net.minecraft.network.FriendlyByteBuf;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;
import java.util.List;

public class SyncEliteStatePacket {

    private final int entityId;
        private final String mutationId;
        private final String copiedMutationId;
        private final boolean mimicRevealed;
        private final float statusProgress;
        private final float statusMax;
        private final boolean statusActive;
        private final int statusColor;
        private final List<String> attributes;
        private final String poseId;

    public SyncEliteStatePacket(
        int entityId,
        String mutationId,
        String copiedMutationId,
        boolean mimicRevealed,
        List<String> attributes,
        float statusProgress,
        float statusMax,
        boolean statusActive,
        int statusColor,
        String poseId
) {
    this.entityId =
            entityId;

    this.mutationId =
            mutationId;

    this.copiedMutationId =
            copiedMutationId;

    this.mimicRevealed =
            mimicRevealed;

    this.attributes =
            attributes;

    this.statusProgress =
            statusProgress;

    this.statusMax =
            statusMax;

    this.statusActive =
            statusActive;

    this.statusColor =
            statusColor;

    this.poseId =
            poseId;
}

    public static void encode(
        SyncEliteStatePacket message,
        FriendlyByteBuf buffer
) {
    buffer.writeInt(
            message.entityId
    );

    buffer.writeUtf(
            message.mutationId
    );

    buffer.writeUtf(
            message.copiedMutationId
    );

    buffer.writeBoolean(
            message.mimicRevealed
    );

    buffer.writeCollection(
        message.attributes,
        FriendlyByteBuf::writeUtf
    );

    buffer.writeFloat(
            message.statusProgress
    );

    buffer.writeFloat(
            message.statusMax
    );

    buffer.writeBoolean(
            message.statusActive
    );

    buffer.writeInt(
            message.statusColor
    );

    buffer.writeUtf(
            message.poseId
    );
}

    public static SyncEliteStatePacket decode(
        FriendlyByteBuf buffer
) {
    return new SyncEliteStatePacket(
        buffer.readInt(),
        buffer.readUtf(),
        buffer.readUtf(),
        buffer.readBoolean(),
        buffer.readList(
                FriendlyByteBuf::readUtf
        ),
        buffer.readFloat(),
        buffer.readFloat(),
        buffer.readBoolean(),
        buffer.readInt(),
        buffer.readUtf()
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
                        packet.copiedMutationId,
                        packet.mimicRevealed,
                        packet.attributes,
                        packet.statusProgress,
                        packet.statusMax,
                        packet.statusActive,
                        packet.statusColor,
                        packet.poseId
                )
        );

        context.setPacketHandled(
                true
        );
        }
}