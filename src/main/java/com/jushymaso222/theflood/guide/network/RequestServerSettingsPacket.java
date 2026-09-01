package com.jushymaso222.theflood.guide.network;

import com.jushymaso222.theflood.config.ServerSettingsSnapshot;
import com.jushymaso222.theflood.network.FloodNetwork;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class RequestServerSettingsPacket {

    public static void encode(
            RequestServerSettingsPacket message,
            FriendlyByteBuf buffer
    ) {
    }

    public static RequestServerSettingsPacket decode(
            FriendlyByteBuf buffer
    ) {
        return new RequestServerSettingsPacket();
    }

    public static void handle(
            RequestServerSettingsPacket message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context =
                contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player =
                    context.getSender();

            if (player == null) {
                return;
            }

            FloodNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(
                            () -> player
                    ),
                    new SyncServerSettingsPacket(
                            ServerSettingsSnapshot.create()
                    )
            );
        });

        context.setPacketHandled(true);
    }
}