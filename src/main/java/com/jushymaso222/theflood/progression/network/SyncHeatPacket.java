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
    private final long floodXp;
    private final long floodXpRequired;

    public SyncHeatPacket(
        int soloHeat,
        int teamHeat,
        int baseHeat,
        int proximityBonus,
        int effectiveHeat,
        long floodXp,
        long floodXpRequired
    ) {
        this.soloHeat = soloHeat;
        this.teamHeat = teamHeat;
        this.baseHeat = baseHeat;
        this.proximityBonus = proximityBonus;
        this.effectiveHeat = effectiveHeat;
        this.floodXp = floodXp;
        this.floodXpRequired = floodXpRequired;
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
        buffer.writeLong(message.floodXp);
        buffer.writeLong(message.floodXpRequired);
    }
 
    public static SyncHeatPacket decode(
        FriendlyByteBuf buffer
    ) {
        return new SyncHeatPacket(
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readLong(),
                buffer.readLong()
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
                        message.effectiveHeat,
                        message.floodXp,
                        message.floodXpRequired
                )
        );

        context.setPacketHandled(true);
    }
}