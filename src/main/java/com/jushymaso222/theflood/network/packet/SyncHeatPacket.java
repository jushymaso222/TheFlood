package com.jushymaso222.theflood.network.packet;

import com.jushymaso222.theflood.client.ClientHeatData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncHeatPacket {

    private final int effectiveHeat;

    public SyncHeatPacket(int effectiveHeat) {
        this.effectiveHeat = effectiveHeat;
    }

    public static void encode(
            SyncHeatPacket message,
            FriendlyByteBuf buffer
    ) {
        buffer.writeInt(message.effectiveHeat);
    }

    public static SyncHeatPacket decode(
            FriendlyByteBuf buffer
    ) {
        return new SyncHeatPacket(
                buffer.readInt()
        );
    }

    public static void handle(
            SyncHeatPacket message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context =
                contextSupplier.get();

        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(
                        Dist.CLIENT,
                        () -> () ->
                                ClientHeatData.setEffectiveHeat(
                                        message.effectiveHeat
                                )
                )
        );

        context.setPacketHandled(true);
    }
}