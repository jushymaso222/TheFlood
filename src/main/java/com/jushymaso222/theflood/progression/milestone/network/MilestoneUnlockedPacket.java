package com.jushymaso222.theflood.milestone.network;

import com.jushymaso222.theflood.progression.milestone.client.ClientMilestoneData;
import com.jushymaso222.theflood.progression.milestone.client.ClientMilestoneNotifications;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MilestoneUnlockedPacket {

    private final ResourceLocation id;

    private final Component title;
    private final Component description;

    private final int progressionValue;
    private final double floodXpReward;


    public MilestoneUnlockedPacket(
            ResourceLocation id,
            Component title,
            Component description,
            int progressionValue,
            double floodXpReward
    ) {
        this.id =
                id;

        this.title =
                title;

        this.description =
                description;

        this.progressionValue =
                progressionValue;

        this.floodXpReward =
                floodXpReward;
    }


    public static void encode(
            MilestoneUnlockedPacket packet,
            FriendlyByteBuf buffer
    ) {
        buffer.writeResourceLocation(
                packet.id
        );

        buffer.writeComponent(
                packet.title
        );

        buffer.writeComponent(
                packet.description
        );

        buffer.writeVarInt(
                packet.progressionValue
        );

        buffer.writeDouble(
                packet.floodXpReward
        );
    }


    public static MilestoneUnlockedPacket decode(
            FriendlyByteBuf buffer
    ) {
        return new MilestoneUnlockedPacket(
                buffer.readResourceLocation(),
                buffer.readComponent(),
                buffer.readComponent(),
                buffer.readVarInt(),
                buffer.readDouble()
        );
    }


    public static void handle(
            MilestoneUnlockedPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context =
                contextSupplier.get();

        context.enqueueWork(
                () -> {

                    /*
                     * Existing popup notification.
                     */
                    ClientMilestoneNotifications.add(
                            packet.title,
                            packet.description,
                            packet.progressionValue,
                            packet.floodXpReward
                    );


                    /*
                     * Update milestone UI data immediately.
                     *
                     * This marks the milestone completed and
                     * puts it at the top of the recent list.
                     */
                    ClientMilestoneData.addRecent(
                                packet.id
                        );
                }
        );

        context.setPacketHandled(
                true
        );
    }
}