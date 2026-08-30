package com.jushymaso222.theflood.milestone.network;

import com.jushymaso222.theflood.progression.milestone.client.ClientMilestoneData;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncMilestoneSnapshotPacket {

    private final int progressionValue;

    private final List<ResourceLocation> achieved;

    private final List<ResourceLocation> completionHistory;


    public SyncMilestoneSnapshotPacket(
            int progressionValue,
            List<ResourceLocation> achieved,
            List<ResourceLocation> completionHistory
    ) {
        this.progressionValue =
                progressionValue;

        this.achieved =
                List.copyOf(
                        achieved
                );

        this.completionHistory =
                List.copyOf(
                        completionHistory
                );
    }


    /*
     * =================================================
     * ENCODE
     * =================================================
     */

    public static void encode(
            SyncMilestoneSnapshotPacket packet,
            FriendlyByteBuf buffer
    ) {
        buffer.writeVarInt(
                packet.progressionValue
        );


        /*
         * Completed milestone IDs.
         */
        buffer.writeVarInt(
                packet.achieved.size()
        );

        for (
                ResourceLocation id :
                packet.achieved
        ) {
            buffer.writeResourceLocation(
                    id
            );
        }


        /*
         * Completion order.
         *
         * Oldest -> newest.
         */
        buffer.writeVarInt(
                packet.completionHistory.size()
        );

        for (
                ResourceLocation id :
                packet.completionHistory
        ) {
            buffer.writeResourceLocation(
                    id
            );
        }
    }


    /*
     * =================================================
     * DECODE
     * =================================================
     */

    public static SyncMilestoneSnapshotPacket decode(
            FriendlyByteBuf buffer
    ) {
        int progressionValue =
                buffer.readVarInt();


        int achievedCount =
                buffer.readVarInt();

        List<ResourceLocation> achieved =
                new ArrayList<>(
                        achievedCount
                );

        for (
                int i = 0;
                i < achievedCount;
                i++
        ) {
            achieved.add(
                    buffer.readResourceLocation()
            );
        }


        int historyCount =
                buffer.readVarInt();

        List<ResourceLocation> completionHistory =
                new ArrayList<>(
                        historyCount
                );

        for (
                int i = 0;
                i < historyCount;
                i++
        ) {
            completionHistory.add(
                    buffer.readResourceLocation()
            );
        }


        return new SyncMilestoneSnapshotPacket(
                progressionValue,
                achieved,
                completionHistory
        );
    }


    /*
     * =================================================
     * HANDLE
     * =================================================
     */

    public static void handle(
            SyncMilestoneSnapshotPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context =
                contextSupplier.get();

        context.enqueueWork(
                () ->
                        ClientMilestoneData.setSnapshot(
                                packet.progressionValue,
                                packet.achieved,
                                packet.completionHistory
                        )
        );

        context.setPacketHandled(
                true
        );
    }
}