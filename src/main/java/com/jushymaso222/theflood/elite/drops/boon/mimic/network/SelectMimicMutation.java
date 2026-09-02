package com.jushymaso222.theflood.elite.drops.boon.mimic.network;

import com.jushymaso222.theflood.elite.drops.boon.BoonItems;
import com.jushymaso222.theflood.elite.drops.boon.mimic.PlayerMimicMutation;
import com.jushymaso222.theflood.elite.drops.boon.mimic.PlayerMimicMutationManager;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class SelectMimicMutation {

    private final String mutationId;


    public SelectMimicMutation(
            PlayerMimicMutation mutation
    ) {
        this.mutationId =
                mutation.id();
    }


    private SelectMimicMutation(
            String mutationId
    ) {
        this.mutationId =
                mutationId;
    }


    public static void encode(
            SelectMimicMutation packet,
            FriendlyByteBuf buffer
    ) {
        buffer.writeUtf(
                packet.mutationId
        );
    }


    public static SelectMimicMutation decode(
            FriendlyByteBuf buffer
    ) {
        return new SelectMimicMutation(
                buffer.readUtf()
        );
    }


    public static void handle(
            SelectMimicMutation packet,
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

                    PlayerMimicMutation mutation =
                            PlayerMimicMutation.fromId(
                                    packet.mutationId
                            );

                    if (mutation == null) {
                        return;
                    }

                    InteractionHand hand =
                            findMimicBoonHand(
                                    player
                            );

                    if (hand == null) {
                        return;
                    }

                    boolean activated =
                            PlayerMimicMutationManager.activate(
                                    player,
                                    mutation
                            );

                    if (!activated) {
                        return;
                    }

                    ItemStack stack =
                            player.getItemInHand(
                                    hand
                            );

                    stack.shrink(
                            1
                    );
                }
        );

        context.setPacketHandled(
                true
        );
    }


    private static InteractionHand findMimicBoonHand(
            ServerPlayer player
    ) {
        if (
                player.getMainHandItem()
                        .is(
                                BoonItems.BOON_MIMIC.get()
                        )
        ) {
            return InteractionHand.MAIN_HAND;
        }

        if (
                player.getOffhandItem()
                        .is(
                                BoonItems.BOON_MIMIC.get()
                        )
        ) {
            return InteractionHand.OFF_HAND;
        }

        return null;
    }
}