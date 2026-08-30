package com.jushymaso222.theflood.milestone.network;

import com.jushymaso222.theflood.network.FloodNetwork;
import com.jushymaso222.theflood.progression.milestone.MilestoneManager;
import com.jushymaso222.theflood.progression.milestone.MilestoneSnapshot;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class RequestMilestoneSnapshotPacket {

    /*
     * =================================================
     * ENCODE
     * =================================================
     *
     * There is nothing to send.
     *
     * The server already knows which player sent
     * the request.
     */

    public static void encode(
            RequestMilestoneSnapshotPacket packet,
            FriendlyByteBuf buffer
    ) {
    }


    /*
     * =================================================
     * DECODE
     * =================================================
     */

    public static RequestMilestoneSnapshotPacket decode(
            FriendlyByteBuf buffer
    ) {
        return new RequestMilestoneSnapshotPacket();
    }


    /*
     * =================================================
     * HANDLE
     * =================================================
     */

    public static void handle(
            RequestMilestoneSnapshotPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context =
                contextSupplier.get();


        context.enqueueWork(
                () -> {

                    ServerPlayer player =
                            context.getSender();

                    if (player == null) {
                        return;
                    }


                    /*
                     * Build the authoritative milestone
                     * state for this player.
                     */
                    MilestoneSnapshot snapshot =
                            MilestoneManager.getSnapshot(
                                    player
                            );


                    /*
                     * Send the completed milestone state
                     * and persistent completion history
                     * back to this client.
                     */
                    FloodNetwork.CHANNEL.send(
                            PacketDistributor.PLAYER.with(
                                    () -> player
                            ),
                            new SyncMilestoneSnapshotPacket(
                                    snapshot.progressionValue(),
                                    snapshot.achieved(),
                                    snapshot.completionHistory()
                            )
                    );
                }
        );


        context.setPacketHandled(
                true
        );
    }
}