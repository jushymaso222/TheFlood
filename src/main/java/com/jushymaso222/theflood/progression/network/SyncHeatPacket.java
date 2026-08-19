package com.jushymaso222.theflood.progression.network;

import com.jushymaso222.theflood.progression.client.ClientHeatData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncHeatPacket {

    private final int soloHeat;
    private final int teamHeat;
    private final int baseHeat;
    private final int proximityBonus;
    private final int effectiveHeat;

    public SyncHeatPacket(
        int soloHeat,
        int teamHeat,
        int baseHeat,
        int proximityBonus,
        int effectiveHeat
    ) {
        this.soloHeat = soloHeat;
        this.teamHeat = teamHeat;
        this.baseHeat = baseHeat;
        this.proximityBonus = proximityBonus;
        this.effectiveHeat = effectiveHeat;
    }

    public static void encode(
        SyncHeatPacket message,
        FriendlyByteBuf buffer
    ) {
        buffer.writeInt(message.soloHeat);
        buffer.writeInt(message.teamHeat);
        buffer.writeInt(message.baseHeat);
        buffer.writeInt(message.proximityBonus);
        buffer.writeInt(message.effectiveHeat);
    }
 
    public static SyncHeatPacket decode(
        FriendlyByteBuf buffer
    ) {
        return new SyncHeatPacket(
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
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
                ClientHeatData.update(
                        message.soloHeat,
                        message.teamHeat,
                        message.baseHeat,
                        message.proximityBonus,
                        message.effectiveHeat
                )
        );

        context.setPacketHandled(true);
    }
}